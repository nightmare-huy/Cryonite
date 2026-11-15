package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import dev.cryonite.block.PrimordialSmelterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Cryonite.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrimordialSmelterBlockEntity>> PRIMORDIAL_SMELTER = TYPES.register("primordial_smelter", () -> BlockEntityType.Builder.of(PrimordialSmelterBlockEntity::new, ModBlocks.PRIMORDIAL_SMELTER.get()).build(null));
    private ModBlockEntities() {}
}
