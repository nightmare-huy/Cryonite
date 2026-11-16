package dev.nightmarehuy.cryonite.compat.create.client;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.compat.create.KineticConverterMenu;
import dev.nightmarehuy.cryonite.compat.create.KineticEnergyConverterBlockEntity;
import dev.nightmarehuy.cryonite.network.UpdateKineticConverterPayload;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * Control panel of the Kinetic Energy Converter.
 *
 * <p>The speed can be typed (Enter or "Apply"), dragged with the slider, stepped with the +/- buttons or set
 * with a preset; two more buttons reverse the rotation and switch the converter on and off. The left edge
 * shows the energy buffer, the bottom the live readouts: speed, stress capacity and consumption.</p>
 *
 * <p>The screen edits a local copy of the settings and sends it with {@link UpdateKineticConverterPayload}:
 * at once for buttons, at most every {@value #SEND_INTERVAL} ticks while the slider is dragged or the text
 * changes. The readouts always show what the server reports.</p>
 */
public class KineticConverterScreen extends AbstractContainerScreen<KineticConverterMenu> {
    private static final ResourceLocation TEXTURE = Cryonite.id("textures/gui/container/kinetic_converter_gui.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final String KEY = "gui.cryonite.kinetic_converter.";

    private static final int MAX_RPM = KineticEnergyConverterBlockEntity.MAX_RPM;

    // Layout inside the 256x196 panel (tools/generate_hd_pixelart.py draws the same rectangles).
    private static final int BAR_X = 11;
    private static final int BAR_Y = 21;
    private static final int BAR_WIDTH = 16;
    private static final int BAR_HEIGHT = 164;
    private static final int BAR_U = 256;
    private static final int CONTENT_X = 34;
    private static final int CONTENT_WIDTH = 214;
    private static final int ROW_HEIGHT = 18;
    private static final int LABEL_Y = 21;
    private static final int BOX_Y = 32;
    private static final int SLIDER_Y = 54;
    private static final int STEPS_Y = 76;
    private static final int PRESETS_Y = 98;
    private static final int SWITCHES_Y = 120;
    private static final int READOUT_X = 39;
    private static final int READOUT_Y = 147;
    private static final int READOUT_RIGHT = 243;
    private static final int GAUGE_X = 39;
    private static final int GAUGE_Y = 179;
    private static final int GAUGE_WIDTH = 204;
    private static final int GAUGE_HEIGHT = 4;
    private static final int GAUGE_U = 280;

    private static final int[] STEPS = {-1000, -100, -10, -1, 1, 10, 100, 1000};
    private static final int[] STEP_WIDTHS = {34, 28, 22, 16, 16, 22, 28, 34};
    private static final int[] PRESETS = {0, 256, 1024, MAX_RPM};
    private static final int[] PRESET_WIDTHS = {46, 40, 46, 76};
    private static final int GAP = 2;

    /** Ticks between two packets while the slider is dragged or the text is edited. */
    private static final int SEND_INTERVAL = 3;
    /** Ticks after the last own change before settings changed by someone else are taken over. */
    private static final int ADOPT_DELAY = 20;

    // The panel is dark, so the labels use light colours instead of the vanilla dark grey.
    private static final int TITLE_COLOR = 0xEBD9A8;
    private static final int LABEL_COLOR = 0xA9A5B4;
    private static final int VALUE_COLOR = 0xF2E9D0;
    private static final int GOOD_COLOR = 0x7CE0A0;
    private static final int WARN_COLOR = 0xFF8A5C;
    private static final int IDLE_COLOR = 0x8C8894;

    private EditBox rpmBox;
    private RpmSlider slider;
    private Button directionButton;
    private Button powerButton;

    // Local copy of the settings; the server gets it with the next packet.
    private boolean loaded;
    private int rpm;
    private boolean reversed;
    private boolean enabled;

    /** True while the widgets are updated from code, so that they do not report it back as user input. */
    private boolean syncing;
    private boolean unsent;
    private int sendCooldown;
    private int quietTicks = ADOPT_DELAY;

    public KineticConverterScreen(KineticConverterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 256;
        imageHeight = 196;
    }

    // ------------------------------------------------------------------ widgets

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 7;
        if (!loaded) {
            loaded = true;
            rpm = Mth.clamp(menu.targetRpm(), 0, MAX_RPM);
            reversed = menu.reversed();
            enabled = menu.enabled();
        }

        int left = leftPos + CONTENT_X;

        rpmBox = new EditBox(font, left, topPos + BOX_Y, 96, ROW_HEIGHT, Component.translatable(KEY + "target"));
        rpmBox.setMaxLength(4);
        rpmBox.setFilter(text -> text.chars().allMatch(c -> c >= '0' && c <= '9'));
        rpmBox.setValue(Integer.toString(rpm));
        rpmBox.setResponder(this::onTextChanged);
        addRenderableWidget(rpmBox);

        addRenderableWidget(Button.builder(Component.translatable(KEY + "apply"), button -> applyText())
                .bounds(left + 98, topPos + BOX_Y, CONTENT_WIDTH - 98, ROW_HEIGHT).build());

        slider = new RpmSlider(left, topPos + SLIDER_Y, CONTENT_WIDTH, ROW_HEIGHT, rpm);
        addRenderableWidget(slider);

        int x = left;
        for (int i = 0; i < STEPS.length; i++) {
            int step = STEPS[i];
            addRenderableWidget(Button.builder(Component.literal((step > 0 ? "+" : "") + step), button -> changeRpm(rpm + step, null))
                    .bounds(x, topPos + STEPS_Y, STEP_WIDTHS[i], ROW_HEIGHT).build());
            x += STEP_WIDTHS[i] + GAP;
        }

        x = left;
        for (int i = 0; i < PRESETS.length; i++) {
            int preset = PRESETS[i];
            Component label = preset == 0 ? Component.translatable(KEY + "preset.min")
                    : preset == MAX_RPM ? Component.translatable(KEY + "preset.max")
                    : Component.literal(Integer.toString(preset));
            addRenderableWidget(Button.builder(label, button -> {
                changeRpm(preset, null);
                send();
            }).bounds(x, topPos + PRESETS_Y, PRESET_WIDTHS[i], ROW_HEIGHT).build());
            x += PRESET_WIDTHS[i] + GAP;
        }

        directionButton = addRenderableWidget(Button.builder(directionLabel(), button -> {
            reversed = !reversed;
            button.setMessage(directionLabel());
            unsent = true;
            send();
        }).bounds(left, topPos + SWITCHES_Y, 130, ROW_HEIGHT).build());

        powerButton = addRenderableWidget(Button.builder(powerLabel(), button -> {
            enabled = !enabled;
            button.setMessage(powerLabel());
            unsent = true;
            send();
        }).bounds(left + 130 + GAP, topPos + SWITCHES_Y, CONTENT_WIDTH - 130 - GAP, ROW_HEIGHT).build());
    }

    private Component directionLabel() {
        return Component.translatable(KEY + "direction", Component.translatable(KEY + (reversed ? "direction.ccw" : "direction.cw")));
    }

    private Component powerLabel() {
        return Component.translatable(KEY + (enabled ? "power.on" : "power.off"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    // ------------------------------------------------------------------ editing

    /**
     * Sets the local speed and brings the other widgets in line.
     *
     * @param source the widget the value comes from (it is left untouched), or null
     */
    private void changeRpm(int value, Object source) {
        value = Mth.clamp(value, 0, MAX_RPM);
        if (value != rpm) {
            rpm = value;
            unsent = true;
        }
        syncing = true;
        if (source != rpmBox) {
            rpmBox.setValue(Integer.toString(rpm));
        }
        if (source != slider) {
            slider.showRpm(rpm);
        }
        syncing = false;
    }

    /** Typing changes the speed right away; a number above the maximum counts as the maximum. */
    private void onTextChanged(String text) {
        if (syncing || text.isEmpty()) {
            return;
        }
        changeRpm(Integer.parseInt(text), rpmBox);
    }

    /** Enter or "Apply": the typed number is clamped, written back into the field and sent. */
    private void applyText() {
        String text = rpmBox.getValue();
        changeRpm(text.isEmpty() ? rpm : Integer.parseInt(text), null);
        unsent = true;
        send();
    }

    private void send() {
        if (!unsent) {
            return;
        }
        unsent = false;
        sendCooldown = SEND_INTERVAL;
        quietTicks = 0;
        PacketDistributor.sendToServer(new UpdateKineticConverterPayload(menu.pos(), rpm, reversed, enabled));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (sendCooldown > 0) {
            sendCooldown--;
        }
        if (unsent) {
            if (sendCooldown == 0) {
                send();
            }
            return;
        }
        // Someone else (or the server's clamp) changed the settings: take them over once our own edits have settled.
        if (quietTicks < ADOPT_DELAY) {
            quietTicks++;
        } else if (!rpmBox.isFocused() && !isDragging()
                && (menu.targetRpm() != rpm || menu.reversed() != reversed || menu.enabled() != enabled)) {
            reversed = menu.reversed();
            enabled = menu.enabled();
            rpm = Mth.clamp(menu.targetRpm(), 0, MAX_RPM);
            changeRpm(rpm, null);
            unsent = false;
            directionButton.setMessage(directionLabel());
            powerButton.setMessage(powerLabel());
        }
    }

    @Override
    public void removed() {
        send();
        super.removed();
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != GLFW.GLFW_KEY_ESCAPE && rpmBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                applyText();
            } else {
                rpmBox.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;    // the inventory key must not close the panel while a number is typed
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Container screens swallow drags for their slots; the slider needs them. */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && isDragging() && getFocused() != null && getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    // ------------------------------------------------------------------ rendering

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0.0F, 0.0F, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int energy = (int) ((long) menu.energy() * BAR_HEIGHT / KineticEnergyConverterBlockEntity.ENERGY_CAPACITY);
        if (energy == 0 && menu.energy() > 0) {
            energy = 1;
        }
        energy = Math.min(energy, BAR_HEIGHT);
        if (energy > 0) {
            int empty = BAR_HEIGHT - energy;
            graphics.blit(TEXTURE, leftPos + BAR_X, topPos + BAR_Y + empty, (float) BAR_U, (float) empty,
                    BAR_WIDTH, energy, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        int gauge = Math.min(GAUGE_WIDTH, menu.speed() * GAUGE_WIDTH / MAX_RPM);
        if (gauge == 0 && menu.speed() > 0) {
            gauge = 1;
        }
        if (gauge > 0) {
            graphics.blit(TEXTURE, leftPos + GAUGE_X, topPos + GAUGE_Y, (float) GAUGE_U, 0.0F,
                    gauge, GAUGE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TITLE_COLOR, false);
        graphics.drawString(font, Component.translatable(KEY + "target"), CONTENT_X, LABEL_Y, LABEL_COLOR, false);

        graphics.drawString(font, Component.translatable(KEY + "speed", menu.speed()), READOUT_X, READOUT_Y, VALUE_COLOR, false);
        graphics.drawString(font, Component.translatable(KEY + "output", KineticEnergyConverterBlockEntity.grouped(menu.stressOutput())),
                READOUT_X, READOUT_Y + 10, VALUE_COLOR, false);
        graphics.drawString(font, Component.translatable(KEY + "consumption", KineticEnergyConverterBlockEntity.grouped(menu.consumption())),
                READOUT_X, READOUT_Y + 20, VALUE_COLOR, false);

        Component status = status();
        graphics.drawString(font, status, READOUT_RIGHT - font.width(status), READOUT_Y, statusColor(), false);
    }

    private Component status() {
        String state;
        if (!menu.enabled()) {
            state = "off";
        } else if (menu.targetRpm() == 0) {
            state = "idle";
        } else if (!menu.running()) {
            state = "no_energy";
        } else if (menu.overstressed()) {
            state = "overstressed";
        } else {
            state = "running";
        }
        return Component.translatable(KEY + "status." + state);
    }

    private int statusColor() {
        if (!menu.enabled() || menu.targetRpm() == 0) {
            return IDLE_COLOR;
        }
        return menu.running() && !menu.overstressed() ? GOOD_COLOR : WARN_COLOR;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT, mouseX, mouseY)) {
            List<Component> lines = List.of(
                    Component.translatable(KEY + "energy", KineticEnergyConverterBlockEntity.grouped(menu.energy()),
                            KineticEnergyConverterBlockEntity.grouped(KineticEnergyConverterBlockEntity.ENERGY_CAPACITY)),
                    Component.translatable(KEY + "energy.input", KineticEnergyConverterBlockEntity.grouped(KineticEnergyConverterBlockEntity.MAX_TRANSFER))
                            .withStyle(ChatFormatting.GRAY));
            graphics.renderComponentTooltip(font, lines, mouseX, mouseY);
        }
    }

    // ------------------------------------------------------------------ slider

    /** Slider over 0..8048 RPM. */
    private final class RpmSlider extends AbstractSliderButton {
        RpmSlider(int x, int y, int width, int height, int initialRpm) {
            super(x, y, width, height, CommonComponents.EMPTY, initialRpm / (double) MAX_RPM);
            updateMessage();
        }

        /** Moves the handle without reporting it as user input. */
        void showRpm(int value) {
            this.value = Mth.clamp(value / (double) MAX_RPM, 0.0, 1.0);
            updateMessage();
        }

        private int sliderRpm() {
            return (int) Math.round(this.value * MAX_RPM);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(KEY + "slider", sliderRpm()));
        }

        @Override
        protected void applyValue() {
            if (!syncing) {
                changeRpm(sliderRpm(), this);
            }
        }

        /** The last position of a drag is sent at once. */
        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            send();
        }
    }
}
