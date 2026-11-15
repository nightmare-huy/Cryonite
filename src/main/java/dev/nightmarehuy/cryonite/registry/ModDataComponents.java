package dev.nightmarehuy.cryonite.registry;

import com.mojang.serialization.Codec;
import dev.nightmarehuy.cryonite.Cryonite;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item state lives exclusively in data components (no ItemStack NBT).
 * Both toggles are optional: when a stack has no explicit value the default of the mode is used
 * (auto-smelt: on, 3x3 mining: off), so smithing upgrades and freshly crafted tools just work.
 */
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents REGISTER =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Cryonite.MODID);

    /** Auto-smelt switch of Infernite+ tools. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AUTO_SMELT =
            REGISTER.registerComponentType("auto_smelt", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** 3x3 mining switch of Aether tools. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AREA_MINING =
            REGISTER.registerComponentType("area_mining", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    private ModDataComponents() {}
}
