package dev.nightmarehuy.cryonite.compat.jei;

import java.util.List;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Shared layout of both documentation categories: one row of slots on top and a word-wrapped description
 * below it. The category is as tall as its longest description needs in the current language
 * (JEI rebuilds its categories on every resource reload, so a language switch is picked up).
 */
abstract class DescribedRecipeCategory<T extends DescribedJeiRecipe> extends AbstractRecipeCategory<T> {
    protected static final int WIDTH = 170;
    /** Height of the slot row: fits the 26 px frame of an output slot. */
    protected static final int SLOT_ROW_HEIGHT = 26;
    /** Y of the items in the slot row. */
    protected static final int SLOT_Y = 5;

    private static final int TEXT_X = 2;
    private static final int TEXT_Y = SLOT_ROW_HEIGHT + 4;
    private static final int TEXT_WIDTH = WIDTH - 2 * TEXT_X;
    private static final int TEXT_COLOR = 0xFF404040;

    protected DescribedRecipeCategory(RecipeType<T> recipeType, Component title, IDrawable icon, List<T> recipes) {
        super(recipeType, title, icon, WIDTH, TEXT_Y + tallestDescription(recipes));
    }

    private static int tallestDescription(List<? extends DescribedJeiRecipe> recipes) {
        Font font = Minecraft.getInstance().font;
        int tallest = font.lineHeight;
        for (DescribedJeiRecipe recipe : recipes) {
            tallest = Math.max(tallest, font.wordWrapHeight(recipe.description(), TEXT_WIDTH));
        }
        return tallest;
    }

    @Override
    public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, recipe.description(), TEXT_X, TEXT_Y, TEXT_WIDTH, TEXT_COLOR);
    }

    @Override
    public ResourceLocation getRegistryName(T recipe) {
        return recipe.id();
    }
}
