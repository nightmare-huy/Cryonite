package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModDamageTypes;
import dev.nightmarehuy.cryonite.worldgen.ModWorldgen;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Entry point of {@code ./gradlew runData}. Everything under {@code src/generated/resources} comes from here:
 * models, block states, recipes, tags, loot tables, global loot modifiers, worldgen and both language files.
 */
public final class DataGenerators {
    private static final RegistrySetBuilder DATAPACK_ENTRIES = new RegistrySetBuilder()
            .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
            .add(Registries.CONFIGURED_FEATURE, ModWorldgen::configuredFeatures)
            .add(Registries.PLACED_FEATURE, ModWorldgen::placedFeatures)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModWorldgen::biomeModifiers);

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // ---- client assets
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, files));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.English(output));
        generator.addProvider(event.includeClient(), new ModLanguageProvider.Russian(output));

        // ---- server data
        ModDimensionProvider.register(event);

        ModTagProviders.Blocks blockTags = new ModTagProviders.Blocks(output, lookup, files);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModTagProviders.Items(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new ModTagProviders.EntityTypes(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModTagProviders.Biomes(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModTagProviders.DamageTypes(output, lookup, files));

        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, lookup));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)), lookup));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifierProvider(output, lookup));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, lookup, DATAPACK_ENTRIES, Set.of(Cryonite.MODID)));
    }

    private DataGenerators() {}
}
