package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public final class ModDamageTypes {
    /**
     * The pure magic half of an Aether weapon hit. Its tags (see the damage type tag provider) make it ignore
     * armor, shields, protection enchantments and invulnerability frames.
     */
    public static final ResourceKey<DamageType> AETHER_BURST = ResourceKey.create(Registries.DAMAGE_TYPE, Cryonite.id("aether_burst"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(AETHER_BURST, new DamageType("cryonite.aether_burst", DamageScaling.NEVER, 0.1F));
    }

    private ModDamageTypes() {}
}
