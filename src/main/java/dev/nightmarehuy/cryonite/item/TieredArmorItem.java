package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Armor piece of the four tiers. Full-set bonuses are handled by
 * {@link dev.nightmarehuy.cryonite.ability.ArmorAbilities}; this class only carries the per-piece item hooks.
 */
public class TieredArmorItem extends ArmorItem implements TieredGear {
    private final GearTier gearTier;

    public TieredArmorItem(GearTier gearTier, Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier gearTier() {
        return gearTier;
    }

    @Override
    public Kind gearKind() {
        return Kind.ARMOR;
    }

    /** Tier 1+: boots walk on powder snow. */
    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return getType() == Type.BOOTS && GearTier.CRYONITE.unlockedAt(gearTier.level());
    }

    /** Tier 3+: the helmet hides the wearer's gaze from Endermen. */
    @Override
    public boolean isEnderMask(ItemStack stack, Player player, EnderMan enderman) {
        return getType() == Type.HELMET && GearTier.VOID.unlockedAt(gearTier.level());
    }
}
