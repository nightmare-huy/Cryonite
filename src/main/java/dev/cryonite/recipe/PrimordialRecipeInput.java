package dev.cryonite.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import java.util.List;

/** Five-slot recipe view: input, two catalysts, special fuel and output. */
public record PrimordialRecipeInput(List<ItemStack> stacks) implements RecipeInput {
    public PrimordialRecipeInput {
        if (stacks.size() != 5) throw new IllegalArgumentException("Primordial input requires exactly five slots");
        stacks = List.copyOf(stacks);
    }
    @Override public ItemStack getItem(int index) { return stacks.get(index); }
    @Override public int size() { return stacks.size(); }
}
