package dev.nightmarehuy.cryonite.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.nightmarehuy.cryonite.registry.ModRecipes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Recipe of the Primordial Smelter.
 *
 * <pre>{@code
 * {
 *   "type": "cryonite:primordial_smelting",
 *   "input": { "item": "cryonite:cooled_infernite_blank" },            // slot 0, optional "count"
 *   "catalysts": [ { "item": "minecraft:gold_block" },                  // slots 1 and 2 in any order (0..2 entries)
 *                  { "item": "minecraft:ghast_tear" } ],
 *   "fuel": [ { "item": "minecraft:blaze_powder" }, ... ],              // slot 3: the ONLY fuel(s) this recipe accepts
 *   "cookingTime": 300,
 *   "result": { "id": "cryonite:infernite_ingot", "count": 1 }
 * }
 * }</pre>
 *
 * Catalyst slots a recipe does not use must stay empty, which keeps every recipe unambiguous.
 * One fuel item is burned per operation.
 */
public record PrimordialSmeltingRecipe(SizedIngredient input, List<SizedIngredient> catalysts, Ingredient fuel, ItemStack result, int cookingTime)
        implements Recipe<PrimordialRecipeInput> {

    public static final int MAX_CATALYSTS = 2;
    public static final int DEFAULT_COOKING_TIME = 200;

    public PrimordialSmeltingRecipe {
        catalysts = List.copyOf(catalysts);
    }

    @Override
    public boolean matches(PrimordialRecipeInput container, Level level) {
        return input.test(container.input())
                && fuel.test(container.fuel())
                && catalystCosts(container.catalystA(), container.catalystB()) != null;
    }

    /**
     * Resolves which catalyst slot pays for which catalyst ingredient.
     *
     * @return items to remove from catalyst slot A and B, or {@code null} when the slots do not satisfy the recipe
     */
    @Nullable
    public int[] catalystCosts(ItemStack slotA, ItemStack slotB) {
        return switch (catalysts.size()) {
            case 0 -> slotA.isEmpty() && slotB.isEmpty() ? new int[] {0, 0} : null;
            case 1 -> {
                SizedIngredient only = catalysts.get(0);
                if (only.test(slotA) && slotB.isEmpty()) {
                    yield new int[] {only.count(), 0};
                }
                if (only.test(slotB) && slotA.isEmpty()) {
                    yield new int[] {0, only.count()};
                }
                yield null;
            }
            default -> {
                SizedIngredient first = catalysts.get(0);
                SizedIngredient second = catalysts.get(1);
                if (first.test(slotA) && second.test(slotB)) {
                    yield new int[] {first.count(), second.count()};
                }
                if (first.test(slotB) && second.test(slotA)) {
                    yield new int[] {second.count(), first.count()};
                }
                yield null;
            }
        };
    }

    @Override
    public ItemStack assemble(PrimordialRecipeInput container, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        catalysts.forEach(catalyst -> ingredients.add(catalyst.ingredient()));
        ingredients.add(fuel);
        return ingredients;
    }

    /** Keeps the recipe out of the vanilla recipe book, which has no category for it. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.PRIMORDIAL_SMELTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.PRIMORDIAL_SMELTING.get();
    }

    /** {@code true} when at least one Primordial Smelter recipe burns this stack as its special fuel. */
    public static boolean isFuel(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.PRIMORDIAL_SMELTING.get()).stream()
                .anyMatch(holder -> holder.value().fuel().test(stack));
    }

    public static class Serializer implements RecipeSerializer<PrimordialSmeltingRecipe> {
        public static final MapCodec<PrimordialSmeltingRecipe> CODEC = RecordCodecBuilder.<PrimordialSmeltingRecipe>mapCodec(instance -> instance.group(
                SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(PrimordialSmeltingRecipe::input),
                SizedIngredient.FLAT_CODEC.listOf().optionalFieldOf("catalysts", List.of()).forGetter(PrimordialSmeltingRecipe::catalysts),
                Ingredient.CODEC_NONEMPTY.fieldOf("fuel").forGetter(PrimordialSmeltingRecipe::fuel),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PrimordialSmeltingRecipe::result),
                Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("cookingTime", DEFAULT_COOKING_TIME).forGetter(PrimordialSmeltingRecipe::cookingTime)
        ).apply(instance, PrimordialSmeltingRecipe::new)).validate(Serializer::validate);

        public static final StreamCodec<RegistryFriendlyByteBuf, PrimordialSmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                SizedIngredient.STREAM_CODEC, PrimordialSmeltingRecipe::input,
                SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), PrimordialSmeltingRecipe::catalysts,
                Ingredient.CONTENTS_STREAM_CODEC, PrimordialSmeltingRecipe::fuel,
                ItemStack.STREAM_CODEC, PrimordialSmeltingRecipe::result,
                ByteBufCodecs.VAR_INT, PrimordialSmeltingRecipe::cookingTime,
                PrimordialSmeltingRecipe::new);

        private static DataResult<PrimordialSmeltingRecipe> validate(PrimordialSmeltingRecipe recipe) {
            if (recipe.catalysts().size() > MAX_CATALYSTS) {
                return DataResult.error(() -> "A primordial smelting recipe supports at most " + MAX_CATALYSTS + " catalysts");
            }
            return DataResult.success(recipe);
        }

        @Override
        public MapCodec<PrimordialSmeltingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PrimordialSmeltingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
