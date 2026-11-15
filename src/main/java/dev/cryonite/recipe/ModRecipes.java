package dev.cryonite.recipe;

import dev.cryonite.Cryonite;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Cryonite.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Cryonite.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PrimordialSmeltingRecipe>> PRIMORDIAL_SERIALIZER = SERIALIZERS.register("primordial_smelting", PrimordialSmeltingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PrimordialSmeltingRecipe>> PRIMORDIAL_TYPE = TYPES.register("primordial_smelting", () -> new RecipeType<>() {});
    private ModRecipes() {}
}
