package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlockEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Cryonite.MODID);

    public static final Supplier<BlockEntityType<PrimordialSmelterBlockEntity>> PRIMORDIAL_SMELTER = REGISTER.register("primordial_smelter",
            () -> BlockEntityType.Builder.of(PrimordialSmelterBlockEntity::new, ModBlocks.PRIMORDIAL_SMELTER.get()).build(null));

    /** Lets hoppers and pipes of other mods talk to the smelter. */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PRIMORDIAL_SMELTER.get(), PrimordialSmelterBlockEntity::getItemHandler);
    }

    private ModBlockEntities() {}
}
