package dev.nightmarehuy.cryonite.worldgen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModFeatures;
import dev.nightmarehuy.cryonite.registry.ModTags;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Datagen bootstraps for the three ores: configured features, placed features and the NeoForge biome
 * modifiers that inject them into the vanilla biomes.
 */
public final class ModWorldgen {
    // ------------------------------------------------------------------ keys
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRYONITE_ORE = configured("cryonite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> INFERNITE_CLUSTER = configured("infernite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VOID_ORE = configured("void_ore");
    /** Universal ore feature for the mining dimension: none configuration, layer is chosen at placement time. */
    public static final ResourceKey<ConfiguredFeature<?, ?>> UNIVERSAL_ORE = configured("universal_ore");

    /** Thin veins anywhere in the deepslate layers of the Overworld. */
    public static final ResourceKey<PlacedFeature> CRYONITE_ORE_PLACED = placed("cryonite_ore");
    /** Extra veins in the Deep Dark and in cold biomes. */
    public static final ResourceKey<PlacedFeature> CRYONITE_ORE_RICH_PLACED = placed("cryonite_ore_rich");
    public static final ResourceKey<PlacedFeature> INFERNITE_CLUSTER_PLACED = placed("infernite_cluster");
    public static final ResourceKey<PlacedFeature> VOID_ORE_PLACED = placed("void_ore");

    /** All ores of the modpack, one placement per geological layer of the mining dimension. */
    public static final ResourceKey<PlacedFeature> UNIVERSAL_ORE_STONE = placed("universal_ore_stone");
    public static final ResourceKey<PlacedFeature> UNIVERSAL_ORE_DEEPSLATE = placed("universal_ore_deepslate");
    public static final ResourceKey<PlacedFeature> UNIVERSAL_ORE_NETHER = placed("universal_ore_nether");
    public static final ResourceKey<PlacedFeature> UNIVERSAL_ORE_END = placed("universal_ore_end");

    public static final ResourceKey<BiomeModifier> ADD_CRYONITE_ORE = modifier("add_cryonite_ore");
    public static final ResourceKey<BiomeModifier> ADD_CRYONITE_ORE_RICH = modifier("add_cryonite_ore_rich");
    public static final ResourceKey<BiomeModifier> ADD_INFERNITE_CLUSTER = modifier("add_infernite_cluster");
    public static final ResourceKey<BiomeModifier> ADD_VOID_ORE = modifier("add_void_ore");

    /** Void Ore only exists in the lowest End Stone, between these heights. */
    public static final int VOID_ORE_MIN_Y = 0;
    public static final int VOID_ORE_MAX_Y = 12;

    // ------------------------------------------------------------------ bootstraps

    public static void configuredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(CRYONITE_ORE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(
                List.of(OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES), ModBlocks.CRYONITE_ORE.get().defaultBlockState())),
                4, 0.3F)));
        context.register(INFERNITE_CLUSTER, new ConfiguredFeature<>(ModFeatures.INFERNITE_CLUSTER.get(), NoneFeatureConfiguration.INSTANCE));
        context.register(VOID_ORE, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(
                List.of(OreConfiguration.target(new BlockMatchTest(Blocks.END_STONE), ModBlocks.VOID_ORE.get().defaultBlockState())),
                5, 0.0F)));
        context.register(UNIVERSAL_ORE, new ConfiguredFeature<>(ModFeatures.UNIVERSAL_ORE.get(), NoneFeatureConfiguration.INSTANCE));
    }

    public static void placedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(CRYONITE_ORE_PLACED, new PlacedFeature(features.getOrThrow(CRYONITE_ORE), List.of(
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.absolute(0)),
                BiomeFilter.biome())));
        context.register(CRYONITE_ORE_RICH_PLACED, new PlacedFeature(features.getOrThrow(CRYONITE_ORE), List.of(
                CountPlacement.of(6),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0)),
                BiomeFilter.biome())));
        context.register(INFERNITE_CLUSTER_PLACED, new PlacedFeature(features.getOrThrow(INFERNITE_CLUSTER), List.of(
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(8), VerticalAnchor.absolute(96)),
                BiomeFilter.biome())));
        context.register(VOID_ORE_PLACED, new PlacedFeature(features.getOrThrow(VOID_ORE), List.of(
                CountPlacement.of(24),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(VOID_ORE_MIN_Y), VerticalAnchor.absolute(VOID_ORE_MAX_Y)),
                BiomeFilter.biome())));

        Holder<ConfiguredFeature<?, ?>> universal = features.getOrThrow(UNIVERSAL_ORE);
        context.register(UNIVERSAL_ORE_STONE, new PlacedFeature(universal, List.of(
                CountPlacement.of(6),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(64), VerticalAnchor.absolute(128)),
                BiomeFilter.biome())));
        context.register(UNIVERSAL_ORE_DEEPSLATE, new PlacedFeature(universal, List.of(
                CountPlacement.of(8),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(0), VerticalAnchor.absolute(64)),
                BiomeFilter.biome())));
        context.register(UNIVERSAL_ORE_NETHER, new PlacedFeature(universal, List.of(
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(0)),
                BiomeFilter.biome())));
        context.register(UNIVERSAL_ORE_END, new PlacedFeature(universal, List.of(
                CountPlacement.of(12),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-63), VerticalAnchor.absolute(-48)),
                BiomeFilter.biome())));
    }

    public static void biomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);

        context.register(ADD_CRYONITE_ORE, addOre(biomes.getOrThrow(BiomeTags.IS_OVERWORLD), placed.getOrThrow(CRYONITE_ORE_PLACED), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_CRYONITE_ORE_RICH, addOre(biomes.getOrThrow(ModTags.Biomes.RICH_IN_CRYONITE), placed.getOrThrow(CRYONITE_ORE_RICH_PLACED), GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_INFERNITE_CLUSTER, addOre(HolderSet.direct(biomes.getOrThrow(Biomes.BASALT_DELTAS)), placed.getOrThrow(INFERNITE_CLUSTER_PLACED), GenerationStep.Decoration.UNDERGROUND_DECORATION));
        context.register(ADD_VOID_ORE, addOre(biomes.getOrThrow(ModTags.Biomes.OUTER_END_ISLANDS), placed.getOrThrow(VOID_ORE_PLACED), GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static BiomeModifier addOre(HolderSet<Biome> biomes, Holder<PlacedFeature> feature, GenerationStep.Decoration step) {
        return new BiomeModifiers.AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature), step);
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Cryonite.id(name));
    }

    private static ResourceKey<PlacedFeature> placed(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Cryonite.id(name));
    }

    private static ResourceKey<BiomeModifier> modifier(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Cryonite.id(name));
    }

    private ModWorldgen() {}
}
