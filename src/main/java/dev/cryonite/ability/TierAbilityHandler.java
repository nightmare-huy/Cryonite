package dev.cryonite.ability;

import dev.cryonite.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Single progression table for gear abilities. A tier check is deliberately cumulative:
 * tier N grants every capability whose minimum tier is less than or equal to N.
 */
public final class TierAbilityHandler {
    public enum Tier {
        NONE(0), CRYONITE(1), INFERNITE(2), VOID(3), AETHER(4);
        private final int level;
        Tier(int level) { this.level = level; }
        public int level() { return level; }
    }
    public enum Ability {
        ICE_AND_SNOW(1), FREEZE_ON_HIT(1), BONUS_VS_FIRE(1),
        AUTO_SMELT(2), FIRE_AND_WITHER_IMMUNITY(2), BONUS_VS_UNDEAD(2),
        TELEPORT_DASH(3), ARMOR_PENETRATION(3), TELEKINETIC_LOOT(3), VOID_FALL_RESCUE(3),
        AREA_MINING(4), TRUE_DAMAGE_AND_LIFESTEAL(4), FLIGHT_AND_REGENERATION(4);
        private final int minimumTier;
        Ability(int minimumTier) { this.minimumTier = minimumTier; }
    }

    public static Tier tierOf(ItemStack stack) {
        if (stack.isEmpty()) return Tier.NONE;
        var item = stack.getItem();
        if (item == ModItems.AETHER_SWORD.get() || item == ModItems.AETHER_PICKAXE.get() || item == ModItems.AETHER_AXE.get() || item == ModItems.AETHER_SHOVEL.get() || item == ModItems.AETHER_HOE.get() || item == ModItems.AETHER_HELMET.get() || item == ModItems.AETHER_CHESTPLATE.get() || item == ModItems.AETHER_LEGGINGS.get() || item == ModItems.AETHER_BOOTS.get()) return Tier.AETHER;
        if (item == ModItems.VOID_SWORD.get() || item == ModItems.VOID_PICKAXE.get() || item == ModItems.VOID_AXE.get() || item == ModItems.VOID_SHOVEL.get() || item == ModItems.VOID_HOE.get() || item == ModItems.VOID_HELMET.get() || item == ModItems.VOID_CHESTPLATE.get() || item == ModItems.VOID_LEGGINGS.get() || item == ModItems.VOID_BOOTS.get()) return Tier.VOID;
        if (item == ModItems.INFERNITE_SWORD.get() || item == ModItems.INFERNITE_PICKAXE.get() || item == ModItems.INFERNITE_AXE.get() || item == ModItems.INFERNITE_SHOVEL.get() || item == ModItems.INFERNITE_HOE.get() || item == ModItems.INFERNITE_HELMET.get() || item == ModItems.INFERNITE_CHESTPLATE.get() || item == ModItems.INFERNITE_LEGGINGS.get() || item == ModItems.INFERNITE_BOOTS.get()) return Tier.INFERNITE;
        if (item == ModItems.CRYONITE_SWORD.get() || item == ModItems.CRYONITE_PICKAXE.get() || item == ModItems.CRYONITE_AXE.get() || item == ModItems.CRYONITE_SHOVEL.get() || item == ModItems.CRYONITE_HOE.get() || item == ModItems.CRYONITE_HELMET.get() || item == ModItems.CRYONITE_CHESTPLATE.get() || item == ModItems.CRYONITE_LEGGINGS.get() || item == ModItems.CRYONITE_BOOTS.get()) return Tier.CRYONITE;
        return Tier.NONE;
    }
    public static boolean has(ItemStack stack, Ability ability) { return tierOf(stack).level >= ability.minimumTier; }
    private TierAbilityHandler() {}
}
