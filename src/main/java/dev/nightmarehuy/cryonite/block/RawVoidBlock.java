package dev.nightmarehuy.cryonite.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;

/**
 * Compact storage of raw Void: unstable and slightly damaging to anything that touches it.
 * Emits occasional void particles.
 */
public class RawVoidBlock extends Block {

    public RawVoidBlock(Properties properties) {
        super(properties);
    }

    /** Slowly damage entities that stand inside the block. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && living.isAlive() && living.tickCount % 40 == 0) {
            living.hurt(level.damageSources().magic(), 1.0F);
        }
    }

    /** Ambient purple void particles around the block. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        if (random.nextFloat() < 0.3F) {
            level.addParticle(ParticleTypes.PORTAL, x + (random.nextDouble() - 0.5), y, z + (random.nextDouble() - 0.5),
                    0.0, 0.0, 0.0);
        }
    }
}
