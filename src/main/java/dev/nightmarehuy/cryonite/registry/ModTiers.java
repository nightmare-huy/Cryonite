package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * Vanilla {@link Tier}s of the four materials.
 * The mining level is the {@code incorrect_for_<tier>_tool} block tag of each tier.
 */
public final class ModTiers {
    private static final Map<GearTier, Tier> TIERS = new EnumMap<>(GearTier.class);

    public static final Tier CRYONITE = create(GearTier.CRYONITE);
    public static final Tier INFERNITE = create(GearTier.INFERNITE);
    public static final Tier VOID = create(GearTier.VOID);
    public static final Tier AETHER = create(GearTier.AETHER);

    private static Tier create(GearTier tier) {
        Tier result = new SimpleTier(
                tier.incorrectBlocksForDrops(),
                tier.uses(),
                tier.miningSpeed(),
                tier.attackDamageBonus(),
                tier.enchantability(),
                () -> Ingredient.of(tier.ingot()));
        TIERS.put(tier, result);
        return result;
    }

    public static Tier get(GearTier tier) {
        return TIERS.get(tier);
    }

    private ModTiers() {}
}
