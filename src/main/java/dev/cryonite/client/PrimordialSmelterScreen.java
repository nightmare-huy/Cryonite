package dev.cryonite.client;

import dev.cryonite.menu.PrimordialSmelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class PrimordialSmelterScreen extends AbstractContainerScreen<PrimordialSmelterMenu> {
    public PrimordialSmelterScreen(PrimordialSmelterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos, top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, 0xFF252934);
        graphics.fill(left + 4, top + 4, left + 172, top + 77, 0xFF3B414D);
        graphics.fill(left + 8, top + 80, left + 168, top + 162, 0xFF303640);
        graphics.fill(left + 130, top + 31, left + 138, top + 48, 0xFF171A20);
        graphics.fill(left + 151, top + 31, left + 168, top + 48, 0xFF171A20);
        int progress = Math.max(0, Math.min(16, menu.getProgress() * 16 / Math.max(1, menu.getTotalTime())));
        if (progress > 0) graphics.fill(left + 151, top + 32, left + 151 + progress, top + 47, 0xFF63D9EC);
        if (menu.getProgress() > 0) graphics.fill(left + 131, top + 33, left + 137, top + 47, 0xFFFF8B36);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xFFE6E6E6, false);
        graphics.drawString(font, playerInventoryTitle, 8, 71, 0xFFE6E6E6, false);
        graphics.drawString(font, Component.translatable("screen.cryonite.slots"), 8, 58, 0xFFBAC4D4, false);
    }
}
