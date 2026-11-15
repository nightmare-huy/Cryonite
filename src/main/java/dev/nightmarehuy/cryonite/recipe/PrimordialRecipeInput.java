package dev.nightmarehuy.cryonite.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * The four ingredient slots of the Primordial Smelter
 * (1.21.1: {@link RecipeInput} replaces the old {@code Container} in {@code matches}/{@code assemble}).
 */
public record PrimordialRecipeInput(ItemStack input, ItemStack catalystA, ItemStack catalystB, ItemStack fuel) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> input;
            case 1 -> catalystA;
            case 2 -> catalystB;
            case 3 -> fuel;
            default -> throw new IllegalArgumentException("No item for index " + index);
        };
    }

    @Override
    public int size() {
        return 4;
    }
}
