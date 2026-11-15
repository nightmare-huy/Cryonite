package dev.cryonite.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Recipe with three input positions and recipe-specific special fuel. */
public record PrimordialSmeltingRecipe(Ingredient input, Ingredient catalyst1, Ingredient catalyst2,
                                      Ingredient fuel, ItemStack result, int cookingTime) implements Recipe<PrimordialRecipeInput> {
    public PrimordialSmeltingRecipe {
        if (cookingTime < 1) throw new IllegalArgumentException("cooking_time must be positive");
        result = result.copy();
    }

    @Override public boolean matches(PrimordialRecipeInput container, Level level) {
        if (!input.test(container.getItem(0)) || !fuel.test(container.getItem(3))) return false;
        if (!catalyst1.isEmpty() && !catalyst1.test(container.getItem(1))) return false;
        if (catalyst1.isEmpty() && !container.getItem(1).isEmpty()) return false;
        if (!catalyst2.isEmpty() && !catalyst2.test(container.getItem(2))) return false;
        return !catalyst2.isEmpty() || container.getItem(2).isEmpty();
    }
    @Override public ItemStack assemble(PrimordialRecipeInput container, HolderLookup.Provider registries) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(HolderLookup.Provider registries) { return result.copy(); }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.PRIMORDIAL_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.PRIMORDIAL_TYPE.get(); }

    public static final class Serializer implements RecipeSerializer<PrimordialSmeltingRecipe> {
        public static final MapCodec<PrimordialSmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PrimordialSmeltingRecipe::input),
                Ingredient.CODEC.optionalFieldOf("catalyst_1", Ingredient.EMPTY).forGetter(PrimordialSmeltingRecipe::catalyst1),
                Ingredient.CODEC.optionalFieldOf("catalyst_2", Ingredient.EMPTY).forGetter(PrimordialSmeltingRecipe::catalyst2),
                Ingredient.CODEC.fieldOf("fuel").forGetter(PrimordialSmeltingRecipe::fuel),
                ItemStack.CODEC.fieldOf("result").forGetter(PrimordialSmeltingRecipe::result),
                Codec.INT.optionalFieldOf("cooking_time", 200).forGetter(PrimordialSmeltingRecipe::cookingTime)
        ).apply(instance, PrimordialSmeltingRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT_CODEC = StreamCodec.of(
                (buffer, value) -> buffer.writeVarInt(value), RegistryFriendlyByteBuf::readVarInt);
        public static final StreamCodec<RegistryFriendlyByteBuf, PrimordialSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PrimordialSmeltingRecipe::input,
                Ingredient.CONTENTS_STREAM_CODEC, PrimordialSmeltingRecipe::catalyst1,
                Ingredient.CONTENTS_STREAM_CODEC, PrimordialSmeltingRecipe::catalyst2,
                Ingredient.CONTENTS_STREAM_CODEC, PrimordialSmeltingRecipe::fuel,
                ItemStack.STREAM_CODEC, PrimordialSmeltingRecipe::result,
                INT_CODEC, PrimordialSmeltingRecipe::cookingTime,
                PrimordialSmeltingRecipe::new);
        @Override public MapCodec<PrimordialSmeltingRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, PrimordialSmeltingRecipe> streamCodec() { return STREAM_CODEC; }
    }
}
