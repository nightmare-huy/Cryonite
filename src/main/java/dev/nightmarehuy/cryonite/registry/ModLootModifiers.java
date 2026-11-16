package dev.nightmarehuy.cryonite.registry;

import com.mojang.serialization.MapCodec;
import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.loot.AddItemLootModifier;
import dev.nightmarehuy.cryonite.loot.AutoSmeltLootModifier;
import java.util.function.Supplier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Cryonite.MODID);

    public static final Supplier<MapCodec<AutoSmeltLootModifier>> AUTO_SMELT = REGISTER.register("auto_smelt", () -> AutoSmeltLootModifier.CODEC);
    public static final Supplier<MapCodec<AddItemLootModifier>> ADD_ITEM = REGISTER.register("add_item", () -> AddItemLootModifier.CODEC);

    private ModLootModifiers() {}
}
