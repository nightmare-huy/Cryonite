package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Tier;

/**
 * Axe of the four tiers. It carries no ability code of its own: everything is resolved from
 * {@link #gearTier()} by {@link dev.nightmarehuy.cryonite.ability.ToolAbilities}.
 */
public class TieredAxeItem extends AxeItem implements TieredGear {
    private final GearTier gearTier;

    public TieredAxeItem(GearTier gearTier, Tier tier, Properties properties) {
        super(tier, properties);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier gearTier() {
        return gearTier;
    }

    @Override
    public Kind gearKind() {
        return Kind.AXE;
    }
}
