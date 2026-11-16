package dev.nightmarehuy.cryonite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compact storage of raw Infernite: glows with light level 12 and burns everything that stands on it,
 * just like a magma block.
 */
public class RawInferniteBlock extends Block {

    public RawInferniteBlock(Properties properties) {
        super(properties);
    }

    /** Deal fire damage to every entity standing inside the block each tick. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && living.isAlive() && living.tickCount % 20 == 0) {
            living.hurt(level.damageSources().inFire(), 2.0F);
        }
    }
}
