package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.worldgen.InferniteClusterFeature;
import dev.nightmarehuy.cryonite.worldgen.UniversalModpackOreFeature;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> REGISTER = DeferredRegister.create(Registries.FEATURE, Cryonite.MODID);

    /** Infernite Ore core wrapped in magma and blackstone, see {@link InferniteClusterFeature}. */
    public static final Supplier<InferniteClusterFeature> INFERNITE_CLUSTER =
            REGISTER.register("infernite_cluster", () -> new InferniteClusterFeature(NoneFeatureConfiguration.CODEC));

    /** Scans {@code #c:ores} and spreads mod ores across the geological layers of the mining dimension. */
    public static final Supplier<UniversalModpackOreFeature> UNIVERSAL_ORE =
            REGISTER.register("universal_ore", () -> new UniversalModpackOreFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}
}
