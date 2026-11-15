package dev.nightmarehuy.cryonite.worldgen;

import com.mojang.serialization.Codec;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * An Infernite deposit of the Basalt Deltas: one to three ore blocks in the middle, a shell of magma
 * around them and a crust of blackstone on the outside.
 *
 * <p>The deposit only starts inside solid delta rock ({@code #cryonite:infernite_cluster_replaceables}) and
 * only replaces such rock, so it never floats in the open and has to be dug out through the magma.</p>
 */
public class InferniteClusterFeature extends Feature<NoneFeatureConfiguration> {
    private static final int RADIUS = 2;

    public InferniteClusterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!isReplaceable(level, origin)) {
            return false;
        }

        // Crust (distance 2) and magma shell (distance 1). Corners of the cube are skipped for a rounder shape.
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    int distance = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (distance == 0 || distance > RADIUS + 1) {
                        continue;
                    }
                    cursor.setWithOffset(origin, dx, dy, dz);
                    if (!isReplaceable(level, cursor)) {
                        continue;
                    }
                    boolean inner = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz))) <= 1;
                    BlockState shell = inner ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState();
                    level.setBlock(cursor, shell, UPDATE_CLIENTS);
                }
            }
        }

        // Core: the origin plus up to two neighbours.
        BlockState ore = ModBlocks.INFERNITE_ORE.get().defaultBlockState();
        level.setBlock(origin, ore, UPDATE_CLIENTS);
        int extra = random.nextInt(3);
        for (int i = 0; i < extra; i++) {
            BlockPos neighbour = origin.relative(Direction.getRandom(random));
            BlockState current = level.getBlockState(neighbour);
            if (current.is(Blocks.MAGMA_BLOCK) || isReplaceable(level, neighbour)) {
                level.setBlock(neighbour, ore, UPDATE_CLIENTS);
            }
        }
        return true;
    }

    private static boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ModTags.Blocks.INFERNITE_CLUSTER_REPLACEABLES);
    }
}
