package dev.nightmarehuy.cryonite.client;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlockEntity;
import dev.nightmarehuy.cryonite.menu.PrimordialSmelterMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Screen of the Primordial Smelter: flame indicator between the main input and the special fuel,
 * progress arrow pointing at the output.
 */
public class PrimordialSmelterScreen extends AbstractContainerScreen<PrimordialSmelterMenu> {
    private static final ResourceLocation TEXTURE = Cryonite.id("textures/gui/container/primordial_smelter.png");

    // Positions inside the 176x166 panel.
    private static final int FLAME_X = 53;
    private static final int FLAME_Y = 36;
    private static final int FLAME_SIZE = 14;
    private static final int ARROW_X = 104;
    private static final int ARROW_Y = 35;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;
    // Sprites right of the panel in the 256x256 sheet.
    private static final int FLAME_U = 176;
    private static final int FLAME_V = 0;
    private static final int ARROW_U = 176;
    private static final int ARROW_V = 14;
    // The panel is dark, so the labels use light colours instead of the vanilla dark grey.
    private static final int TITLE_COLOR = 0xEBD9A8;
    private static final int LABEL_COLOR = 0xA9A5B4;

    public PrimordialSmelterScreen(PrimordialSmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        if (menu.isWorking()) {
            // The flame burns down while the single fuel item of this operation is consumed.
            int lit = FLAME_SIZE - menu.scaledProgress(FLAME_SIZE - 1);
            int offset = FLAME_SIZE - lit;
            graphics.blit(TEXTURE, leftPos + FLAME_X, topPos + FLAME_Y + offset, FLAME_U, FLAME_V + offset, FLAME_SIZE, lit);
        }

        int arrow = menu.scaledProgress(ARROW_WIDTH);
        if (arrow > 0) {
            graphics.blit(TEXTURE, leftPos + ARROW_X, topPos + ARROW_Y, ARROW_U, ARROW_V, arrow, ARROW_HEIGHT);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TITLE_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** Empty machine slots explain what belongs into them. */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && !hoveredSlot.hasItem() && menu.getCarried().isEmpty()) {
            String key = switch (hoveredSlot.index) {
                case PrimordialSmelterBlockEntity.SLOT_INPUT -> "gui.cryonite.slot.input";
                case PrimordialSmelterBlockEntity.SLOT_CATALYST_A, PrimordialSmelterBlockEntity.SLOT_CATALYST_B -> "gui.cryonite.slot.catalyst";
                case PrimordialSmelterBlockEntity.SLOT_FUEL -> "gui.cryonite.slot.fuel";
                default -> null;
            };
            if (key != null) {
                graphics.renderTooltip(font, Component.translatable(key).withStyle(ChatFormatting.GRAY), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }
}
