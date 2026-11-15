package dev.nightmarehuy.cryonite.compat.jei;

import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * "Ore mining and artifact hunting": what to bring, what to look for and what it yields.
 *
 * <pre>
 *   [pickaxe | chest] [surroundings]  ==>  [drop | template] [ore block]
 *   Overworld: deepslate (Y -64 to 0) ...
 * </pre>
 */
public class AcquisitionRecipeCategory extends DescribedRecipeCategory<AcquisitionJeiRecipe> {
    private static final int MEANS_X = 28;
    private static final int SURROUNDINGS_X = 50;
    private static final int ARROW_X = 76;
    private static final int RESULT_X = 107;
    private static final int SILK_TOUCH_X = RESULT_X + 18;

    public AcquisitionRecipeCategory(IGuiHelper guiHelper, List<AcquisitionJeiRecipe> recipes) {
        super(
                CryoniteJeiPlugin.ACQUISITION,
                Component.translatable("jei.cryonite.category.acquisition"),
                guiHelper.createDrawableItemLike(ModItems.CRYONITE_ORE),
                recipes);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AcquisitionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.CATALYST, MEANS_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStacks(recipe.means());
        builder.addSlot(RecipeIngredientRole.INPUT, SURROUNDINGS_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStacks(recipe.surroundings());
        builder.addSlot(RecipeIngredientRole.OUTPUT, RESULT_X, SLOT_Y)
                .setStandardSlotBackground()
                .addItemStack(recipe.result());
        if (!recipe.silkTouch().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, SILK_TOUCH_X, SLOT_Y)
                    .setStandardSlotBackground()
                    .addItemStack(recipe.silkTouch())
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.translatable("jei.cryonite.acquisition.silk_touch").withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, AcquisitionJeiRecipe recipe, IFocusGroup focuses) {
        builder.addRecipeArrow().setPosition(ARROW_X, SLOT_Y);
    }
}
