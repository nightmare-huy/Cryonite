package dev.nightmarehuy.cryonite.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.nightmarehuy.cryonite.Cryonite;
import java.nio.file.Path;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import java.util.concurrent.CompletableFuture;

/**
 * Generates the mining dimension JSON data files so that {@code cryonite:mining_dimension}
 * is a valid, loadable dimension on the server (fixes teleport to mining dimension).
 */
public final class ModDimensionProvider implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PackOutput output;

    public ModDimensionProvider(PackOutput output) {
        this.output = output;
    }

    public static void register(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new ModDimensionProvider(event.getGenerator().getPackOutput()));
    }

    @Override
    public CompletableFuture<?> run(PackOutput output) {
        // Dimension type: cryonite:mining_dimension
        JsonObject dimensionType = new JsonObject();
        dimensionType.addProperty("has_ceiling", false);
        dimensionType.addProperty("has_skylight", true);
        dimensionType.addProperty("ultrawarm", false);
        dimensionType.addProperty("natural", true);
        dimensionType.addProperty("coordinate_scale", 1.0);
        dimensionType.addProperty("height", 256);
        dimensionType.addProperty("min_y", -64);
        dimensionType.addProperty("carvers", "minecraft:air");
        dimensionType.addProperty("has_raids", false);
        dimensionType.addProperty("logical_height", 256);
        dimensionType.addProperty("infiniburn", "minecraft:infiniburn_overworld");
        dimensionType.addProperty("respawn_anchor_works", false);
        dimensionType.addProperty("bed_works", true);
        dimensionType.addProperty("effects", "minecraft:overworld");
        dimensionType.addProperty("ambient_light", 0.0);
        dimensionType.addProperty("fixed_time", -1);
        dimensionType.addProperty("shrunk", false);
        dimensionType.addProperty("piglin_safe", false);
        dimensionType.addProperty("spawn_cost_multiplier", 1.0);
        dimensionType.addProperty("monster_spawn_block_light_limit", 0);
        dimensionType.addProperty("monster_spawn_light_level", 0);

        // Dimension: cryonite:mining_dimension
        JsonObject dimension = new JsonObject();
        dimension.addProperty("type", "cryonite:mining_dimension");
        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("seed", 0L);
        generator.addProperty("settings", "minecraft:overworld");
        JsonObject biomeSource = new JsonObject();
        biomeSource.addProperty("type", "minecraft:multi_noise");
        JsonArray biomes = new JsonArray();
        JsonObject biome = new JsonObject();
        biome.addProperty("biome", "minecraft:plains");
        JsonArray params = new JsonArray();
        for (int i = 0; i < 4; i++) {
            params.add(0.0);
        }
        biome.add("parameters", params);
        biomes.add(biome);
        biomeSource.add("biomes", biomes);
        generator.add("biome_source", biomeSource);
        dimension.add("generator", generator);

        Path typePath = output.getOutputFolder(PackOutput.Target.DATA_PATH)
                .resolve("cryonite/dimension_type/mining_dimension.json");
        Path dimPath = output.getOutputFolder(PackOutput.Target.DATA_PATH)
                .resolve("cryonite/dimension/mining_dimension.json");

        return CompletableFuture.allOf(
                DataProvider.saveStable(GSON, dimensionType, typePath),
                DataProvider.saveStable(GSON, dimension, dimPath)
        );
    }

    @Override
    public String getName() {
        return "Cryonite Mining Dimension JSON";
    }

    private ModDimensionProvider() {}
}
