package dev.nightmarehuy.cryonite.worldgen;

import com.mojang.serialization.Codec;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Универсальный генератор руд для шахтерского измерения.
 * <p>
 * Читает тег {@code #c:ores} (все руды сборки), определяет по ID блока, к какому пласту он
 * относится, и расселяет жилы подходящих руд там, где сейчас камень, сланец, незеритовый камень
 * или камень Энда. Наши руды добавляются с повышенной частотой.
 */
public class UniversalModpackOreFeature extends Feature<NoneFeatureConfiguration> {

    private static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("c", "ores"));

    public UniversalModpackOreFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /** Пласт по высоте и базовый блок, который руда замещает на этом пласте. */
    private enum Layer {
        STONE(64, Blocks.STONE, BlockTags.STONE_ORE_REPLACEABLES),
        DEEPSLATE(0, Blocks.DEEPSLATE, BlockTags.DEEPSLATE_ORE_REPLACEABLES),
        NETHER(-48, Blocks.NETHERRACK, null),
        END(-63, Blocks.END_STONE, null);

        final int minY;
        final Block baseBlock;
        final TagKey<Block> replaceables;

        Layer(int minY, Block baseBlock, TagKey<Block> replaceables) {
            this.minY = minY;
            this.baseBlock = baseBlock;
            this.replaceables = replaceables;
        }

        static Layer of(int y) {
            if (y >= STONE.minY) return STONE;
            if (y >= DEEPSLATE.minY) return DEEPSLATE;
            if (y >= NETHER.minY) return NETHER;
            return END;
        }

        /** Верно, если руда с таким путём реестра относится к этому пласту. */
        boolean matches(ResourceLocation id) {
            String path = id.getPath();
            switch (this) {
                case END:
                    return path.contains("end");
                case NETHER:
                    return path.contains("nether") || path.contains("cobalt");
                case DEEPSLATE:
                    return path.contains("deepslate");
                default:
                    return !path.contains("end") && !path.contains("nether")
                            && !path.contains("cobalt") && !path.contains("deepslate");
            }
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        int y = origin.getY();
        Layer layer = Layer.of(y);

        // Все руды сборки из тега #c:ores.
        Registry<Block> registry = level.registryAccess().registryOrThrow(Registries.BLOCK);
        HolderSet<Block> allOres = registry.getTags().getTag(ORES_TAG).orElse(HolderSet.empty());
        List<Holder<Block>> layerOres = new ArrayList<>();
        for (Holder<Block> ore : allOres) {
            if (layer.matches(registry.getKey(ore.value()))) {
                layerOres.add(ore);
            }
        }
        if (layerOres.isEmpty()) {
            return false;
        }

        // Наши руды — повышенная частота.
        List<Holder<Block>> boosted = new ArrayList<>();
        boosted.add(Holder.direct(ModBlocks.CRYONITE_ORE.get()));
        if (y < 0) {
            boosted.add(Holder.direct(ModBlocks.INFERNITE_ORE.get()));
        }
        if (y < -48) {
            boosted.add(Holder.direct(ModBlocks.VOID_ORE.get()));
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int veins = 3 + random.nextInt(4);
        for (int v = 0; v < veins; v++) {
            cursor.setWithOffset(origin, random.nextInt(16) - 8, 0, random.nextInt(16) - 8);
            cursor.setY(y);
            if (!canReplace(layer, level, cursor)) {
                continue;
            }
            List<Holder<Block>> pool = random.nextFloat() < 0.5F && !boosted.isEmpty() ? boosted : layerOres;
            BlockState ore = pool.get(random.nextInt(pool.size())).value().defaultBlockState();
            int size = 2 + random.nextInt(4);
            for (int i = 0; i < size; i++) {
                BlockPos spot = cursor.offset(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1);
                if (canReplace(layer, level, spot)) {
                    level.setBlock(spot, ore, Block.UPDATE_CLIENTS);
                }
            }
        }
        return true;
    }

    private static boolean canReplace(Layer layer, WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(layer.baseBlock) || (layer.replaceables != null && state.is(layer.replaceables));
    }
}
