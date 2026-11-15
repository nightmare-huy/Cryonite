package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

/**
 * Pickaxe of the four tiers. It carries no ability code of its own: everything is resolved from
 * {@link #gearTier()} by {@link dev.nightmarehuy.cryonite.ability.ToolAbilities}.
 */
public class TieredPickaxeItem extends PickaxeItem implements TieredGear {
    private final GearTier gearTier;

    public TieredPickaxeItem(GearTier gearTier, Tier tier, Properties properties) {
        super(tier, properties);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier gearTier() {
        return gearTier;
    }

    @Override
    public Kind gearKind() {
        return Kind.PICKAXE;
    }
}
