package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Persistent, synchronized per-stack toggles. No ItemStack NBT is used. */
public final class ModComponents {
    public static final DeferredRegister.DataComponents REGISTER = DeferredRegister.createDataComponents(Cryonite.MOD_ID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AUTO_SMELT = REGISTER.registerComponentType(
            "auto_smelt", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AREA_MINING = REGISTER.registerComponentType(
            "area_mining", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private ModComponents() {}
}
