package dev.nightmarehuy.cryonite.client;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.menu.DimensionTerminalMenu;
import dev.nightmarehuy.cryonite.network.ListDimensionsRequestPayload;
import dev.nightmarehuy.cryonite.network.TeleportDimensionPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

/**
 * Пространственный Терминал — экран выбора измерений в стиле Cover Flow.
 * <p>
 * Карусель карточек с плавной инерцией скролла, настоящим 3D-поворотом центральной карточки
 * вокруг оси Y и зеркальным отражением на глянцевом полу.
 */
public class DimensionTerminalScreen extends AbstractContainerScreen<DimensionTerminalMenu> {

    private static final ResourceLocation BG_TEXTURE = Cryonite.id("textures/gui/container/dimension_terminal.png");

    /** Баннеры карт измерений для обложек карточек. */
    private static final ResourceLocation MAP_OVERWORLD = Cryonite.id("textures/gui/container/dimension_map_overworld.png");
    private static final ResourceLocation MAP_MINING = Cryonite.id("textures/gui/container/dimension_map_mining_dimension.png");
    private static final ResourceLocation MAP_NETHER = Cryonite.id("textures/gui/container/dimension_map_the_nether.png");
    private static final ResourceLocation MAP_END = Cryonite.id("textures/gui/container/dimension_map_the_end.png");

    /** Размер одной карточки измерения. */
    private static final int CARD_W = 110;
    private static final int CARD_H = 150;
    /** По умолчанию 4 главных измерения + модовые. */
    private static final int VIEW_W = 260;
    private static final int VIEW_H = 200;

    /** Скорость сглаживания скролла (кадры в секунду). */
    private static final float SCROLL_SPEED = 10.0f;

    /** Целевой индекс карточки и текущее дробное положение карусели. */
    private int selectedIndex = 0;
    private float currentScroll = 0.0f;

    /** Время последнего кадра для вычисления deltaTime. */
    private long lastTimeNanos = System.nanoTime();

    /** Карточки, которые сейчас показываются (заполняются после ответа сервера). */
    private final List<DimensionTerminalClient.DimensionEntry> cards = new ArrayList<>();

    /** Кнопка телепортации внизу экрана. */
    private Button teleportButton;
    private Button leftArrow;
    private Button rightArrow;

    public DimensionTerminalScreen(DimensionTerminalMenu menu, net.minecraft.world.entity.player.Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 200;
    }

    @Override
    protected void init() {
        super.init();
        requestDimensions();

        int cx = width / 2;
        int cy = height / 2;

        // Стрелки по краям экрана.
        leftArrow = addRenderableWidget(Button.builder(Component.literal("◀"), button -> previousCard())
                .bounds(cx - VIEW_W / 2 - 40, cy - 20, 30, 40).build());
        rightArrow = addRenderableWidget(Button.builder(Component.literal("▶"), button -> nextCard())
                .bounds(cx + VIEW_W / 2 + 10, cy - 20, 30, 40).build());

        teleportButton = addRenderableWidget(Button.builder(Component.translatable("message.cryonite.terminal.teleport"),
                button -> teleportSelected()).bounds(cx - 60, cy + VIEW_H / 2 + 10, 120, 20).build());
        updateTeleportButton();
    }

    /** Запросить с сервера список всех доступных измерений. */
    private void requestDimensions() {
        PacketDistributor.sendToServer(new ListDimensionsRequestPayload());
    }

    /** Установить карточки из ответа сервера (вызывается из обработчика пакета). */
    public void setCards(List<DimensionTerminalClient.DimensionEntry> entries) {
        cards.clear();
        cards.addAll(entries);
        if (cards.isEmpty()) {
            cards.add(new DimensionTerminalClient.DimensionEntry(Cryonite.id("mining_dimension"), "Mining Dimension"));
        }
        if (selectedIndex >= cards.size()) {
            selectedIndex = cards.size() - 1;
        }
        currentScroll = selectedIndex;
        updateTeleportButton();
    }

    private void nextCard() {
        if (cards.isEmpty()) {
            return;
        }
        selectedIndex = Math.min(selectedIndex + 1, cards.size() - 1);
        updateTeleportButton();
    }

    private void previousCard() {
        if (cards.isEmpty()) {
            return;
        }
        selectedIndex = Math.max(selectedIndex - 1, 0);
        updateTeleportButton();
    }

    private void selectCard(int index) {
        if (index >= 0 && index < cards.size()) {
            selectedIndex = index;
            updateTeleportButton();
        }
    }

    private void updateTeleportButton() {
        if (teleportButton != null) {
            boolean hasCards = !cards.isEmpty();
            teleportButton.visible = hasCards;
            teleportButton.active = hasCards && selectedIndex >= 0 && selectedIndex < cards.size();
        }
    }

    private void teleportSelected() {
        if (cards.isEmpty() || selectedIndex < 0 || selectedIndex >= cards.size()) {
            return;
        }
        DimensionTerminalClient.DimensionEntry entry = cards.get(selectedIndex);
        PacketDistributor.sendToServer(new TeleportDimensionPayload(entry.key()));
        this.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (cards.isEmpty()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (scrollY > 0) {
            previousCard();
        } else if (scrollY < 0) {
            nextCard();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (cards.isEmpty()) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_A) {
            previousCard();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_D) {
            nextCard();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_SPACE) {
            teleportSelected();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 || cards.isEmpty()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int cx = width / 2;
        int cy = height / 2 - 20;
        for (int i = 0; i < cards.size(); i++) {
            float offset = i - currentScroll;
            float xOffset = (float) (Math.signum(offset) * Math.pow(Math.abs(offset), 0.78) * 95.0);
            int cardX = cx + Math.round(xOffset) - CARD_W / 2;
            int cardY = cy - CARD_H / 2;
            if (mouseX >= cardX && mouseX < cardX + CARD_W && mouseY >= cardY && mouseY < cardY + CARD_H) {
                if (i == selectedIndex) {
                    teleportSelected();
                } else {
                    selectCard(i);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private float getDeltaTime() {
        long now = System.nanoTime();
        float delta = (now - lastTimeNanos) / 1_000_000_000.0f;
        lastTimeNanos = now;
        return Mth.clamp(delta, 0.0f, 0.1f);
    }

    /** Плавная инерция скролла — вызывается из render(), так как tick() final в AbstractContainerScreen. */
    private void updateScroll() {
        if (cards.isEmpty()) {
            return;
        }
        float delta = getDeltaTime();
        currentScroll += (selectedIndex - currentScroll) * Mth.clamp(delta * SCROLL_SPEED, 0.0f, 1.0f);
        if (Math.abs(selectedIndex - currentScroll) < 0.01f) {
            currentScroll = selectedIndex;
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(BG_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateScroll();
        renderBg(graphics, partialTick, mouseX, mouseY);
        int cx = width / 2;
        int cy = height / 2 - 20;

        if (cards.isEmpty() && !DimensionTerminalClient.dimensions.isEmpty()) {
            setCards(new ArrayList<>(DimensionTerminalClient.dimensions));
        }
        if (cards.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("message.cryonite.terminal.loading"), cx, cy - 10, 0xFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        // Рисуем карточки от краёв к центру, чтобы центральная была поверх боковых.
        int count = cards.size();
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, (a, b) -> Float.compare(Math.abs(b - currentScroll), Math.abs(a - currentScroll)));

        for (int i : order) {
            renderCard(graphics, i, cx, cy, mouseX, mouseY, partialTick);
        }

        // Заголовок и подсказки.
        graphics.drawCenteredString(font, title, cx, cy - VIEW_H / 2 - 30, 0xEBD9A8);
        if (cards.size() > 1) {
            graphics.drawString(font, Component.literal((selectedIndex + 1) + " / " + cards.size()),
                    cx - VIEW_W / 2 + 5, cy + VIEW_H / 2 + 40, 0xA9A5B4);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(GuiGraphics graphics, int index, int cx, int cy, int mouseX, int mouseY, float partialTick) {
        DimensionTerminalClient.DimensionEntry entry = cards.get(index);
        float offset = index - currentScroll;

        float xOffset = (float) (Math.signum(offset) * Math.pow(Math.abs(offset), 0.78) * 95.0);
        float zOffset = -Math.abs(offset) * 40.0f;
        float scale = Mth.clamp(1.0f - Math.abs(offset) * 0.22f, 0.55f, 1.0f);
        float yAngle = Mth.clamp(-offset * 38.0f, -58.0f, 58.0f);

        boolean selected = Math.abs(offset) < 0.01f;

        int cardX = cx + Math.round(xOffset) - CARD_W / 2;
        int cardY = cy - CARD_H / 2;

        // --- Отражение на полу (перевернутая затухающая копия) ---
        int reflectY = cardY + CARD_H + 2;
        graphics.pose().pushPose();
        graphics.pose().translate(cardX + CARD_W / 2.0f, reflectY + CARD_H / 2.0f, 100.0f + zOffset);
        graphics.pose().scale(scale, -scale, 1.0f);
        graphics.pose().mulPose(new Quaternionf().rotateY((float) Math.toRadians(yAngle)));
        drawCardContent(graphics, index, -CARD_W / 2, -CARD_H / 2, 0.15f, false);
        graphics.pose().popPose();

        // --- Сама карточка ---
        graphics.pose().pushPose();
        graphics.pose().translate(cardX + CARD_W / 2.0f, cardY + CARD_H / 2.0f, 150.0f + zOffset);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().mulPose(new Quaternionf().rotateY((float) Math.toRadians(yAngle)));
        drawCardContent(graphics, index, -CARD_W / 2, -CARD_H / 2, 1.0f, selected);
        graphics.pose().popPose();
    }

    /** Отрисовывает содержимое одной карточки относительно её центра. */
    private void drawCardContent(GuiGraphics graphics, int index, int x, int y, float alpha, boolean selected) {
        DimensionTerminalClient.DimensionEntry entry = cards.get(index);

        // Фон карточки.
        int bgColor = 0x222222;
        graphics.fill(x + 2, y + 2, x + CARD_W - 2, y + CARD_H - 2, bgColor);

        // Цвет рамки зависит от измерения.
        int borderColor = borderColor(entry.key());
        if (selected) {
            float pulse = 0.6f + 0.4f * Mth.sin((float) (System.nanoTime() / 1e9) * 3.0f);
            borderColor = blend(borderColor, 0xFFFFFF, pulse);
        }
        drawRectOutline(graphics, x, y, CARD_W, CARD_H, borderColor);

        // Баннер карты измерения.
        drawDimensionMap(graphics, entry.key(), x + CARD_W / 2 - 32, y + 20, 64, 64);

        // Название измерения.
        Component name = DimensionTerminalClient.formattedName(entry.name());
        int nameWidth = font.width(name.getString());
        graphics.drawString(font, name, x + CARD_W / 2 - nameWidth / 2, y + 96, 0xFFFFFF, false);

        // Статус.
        Component status = index == getCurrentDimensionIndex()
                ? Component.translatable("gui.cryonite.terminal.status.current")
                : Component.translatable("gui.cryonite.terminal.status.available");
        graphics.drawString(font, status, x + CARD_W / 2 - font.width(status.getString()) / 2, y + 112, 0xA9A5B4, false);

        // Парящие иконки руд под названием.
        renderOreIcons(graphics, entry.key(), x + CARD_W / 2 - 40, y + 128);
    }

    private int getCurrentDimensionIndex() {
        return DimensionTerminalClient.currentDimensionIndex;
    }

    private int borderColor(ResourceLocation key) {
        String path = key.getPath();
        if (path.equals("mining_dimension")) {
            return 0xFFD700;
        }
        if (path.equals("overworld")) {
            return 0x00BFFF;
        }
        if (path.equals("the_nether")) {
            return 0xFF4500;
        }
        if (path.equals("the_end")) {
            return 0x8B00FF;
        }
        return 0xAAAAAA;
    }

    private void drawRectOutline(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private int blend(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF, g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int r2 = (color2 >> 16) & 0xFF, g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);
        return (r << 16) | (g << 8) | b;
    }

    /** Баннер карты измерения: текстура в стиле Jappa. */
    private void drawDimensionMap(GuiGraphics graphics, ResourceLocation key, int x, int y, int w, int h) {
        String path = key.getPath();
        ResourceLocation map = switch (path) {
            case "overworld" -> MAP_OVERWORLD;
            case "mining_dimension" -> MAP_MINING;
            case "the_nether" -> MAP_NETHER;
            case "the_end" -> MAP_END;
            default -> MAP_OVERWORLD;
        };
        graphics.blit(map, x, y, 0, 0, w, h);
    }

    private void renderOreIcons(GuiGraphics graphics, ResourceLocation key, int x, int y) {
        String path = key.getPath();
        List<ItemStack> ores;
        if (path.equals("overworld")) {
            ores = List.of(new ItemStack(Blocks.COAL_ORE), new ItemStack(Blocks.IRON_ORE), new ItemStack(Blocks.DIAMOND_ORE));
        } else if (path.equals("mining_dimension")) {
            ores = List.of(new ItemStack(Blocks.COAL_ORE), new ItemStack(Blocks.IRON_ORE), new ItemStack(Blocks.GOLD_ORE));
        } else if (path.equals("the_nether")) {
            ores = List.of(new ItemStack(Blocks.NETHER_QUARTZ_ORE), new ItemStack(Blocks.ANCIENT_DEBRIS), new ItemStack(Blocks.GOLD_ORE));
        } else if (path.equals("the_end")) {
            ores = List.of(new ItemStack(Blocks.END_STONE), new ItemStack(Items.END_CRYSTAL), new ItemStack(Items.ELYTRA));
        } else {
            ores = List.of(new ItemStack(Blocks.STONE), new ItemStack(Blocks.DIRT), new ItemStack(Blocks.GRASS_BLOCK));
        }
        for (int i = 0; i < ores.size(); i++) {
            graphics.renderItem(ores.get(i), x + i * 22, y);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
