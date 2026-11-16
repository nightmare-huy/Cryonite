package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.recipe.PrimordialSmeltingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Cryonite.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Cryonite.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<PrimordialSmeltingRecipe>> PRIMORDIAL_SMELTING =
            TYPES.register("primordial_smelting", () -> RecipeType.simple(Cryonite.id("primordial_smelting")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PrimordialSmeltingRecipe>> PRIMORDIAL_SMELTING_SERIALIZER =
            SERIALIZERS.register("primordial_smelting", PrimordialSmeltingRecipe.Serializer::new);

    private ModRecipes() {}
}
