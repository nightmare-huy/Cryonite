package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

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

    /** Current state of the switchable modes and the keys that toggle them. */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ToolAbilities.appendModeTooltip(stack, tooltip);
    }
}
