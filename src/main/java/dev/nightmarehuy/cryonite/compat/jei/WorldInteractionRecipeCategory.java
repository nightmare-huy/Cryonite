package dev.nightmarehuy.cryonite.compat.jei;

import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

/**
 * "World processing (quenching and stabilization)": item + block or medium -> result, with the way to do it.
 *
 * <pre>
 *   [item] + [medium]  ==>  [ result ]
 *   Right-click the Powder Snow Cauldron with ...
 * </pre>
 */
public class WorldInteractionRecipeCategory extends DescribedRecipeCategory<WorldInteractionJeiRecipe> {
    private static final int INPUT_X = 23;
    private static final int PLUS_X = 46;
    private static final int MEDIUM_X = 66;
    private static final int ARROW_X = 91;
    private static final int OUTPUT_X = 126;

    public WorldInteractionRecipeCategory(IGuiHelper guiHelper, List<WorldInteractionJeiRecipe> recipes) {
        super(
                CryoniteJeiPlugin.WORLD_INTERACTION,
                Component.translatable("jei.cryonite.category.world_interaction"),
                guiHelper.createDrawableItemLike(Items.CAULDRON),
                recipes);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorldInteractionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.input());
        // The block or medium is not an inventory ingredient: a catalyst, still found with "U".
        builder.addSlot(RecipeIngredientRole.CATALYST, MEDIUM_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStacks(recipe.medium());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X, SLOT_Y)
                .setOutputSlotBackground()
                .addItemStack(recipe.output());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, WorldInteractionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addRecipePlusSign().setPosition(PLUS_X, SLOT_Y + 1);
        builder.addAnimatedRecipeArrow(recipe.ticks()).setPosition(ARROW_X, SLOT_Y);
    }
}
