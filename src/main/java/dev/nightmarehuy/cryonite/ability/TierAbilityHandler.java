package dev.nightmarehuy.cryonite.ability;

import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import java.util.function.BooleanSupplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Heart of the cascading inheritance.
 *
 * <p>Abilities are never copied between tiers. Every hook resolves the <em>level</em> (1..4) of the gear
 * involved and hands the per-tier behaviours to {@link #cascade(int, Runnable...)}: the behaviour written
 * for tier {@code i} runs for every gear level {@code >= i}. A tier therefore owns everything of the tiers
 * below it automatically, and a new tier only has to append its own entry.</p>
 */
public final class TierAbilityHandler {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Runs the abilities of the tiers {@code 1..level}.
     *
     * @param level          gear level, 0 = no tiered gear
     * @param abilitiesByTier index 0 = Cryonite, 1 = Infernite, 2 = Void, 3 = Aether
     */
    public static void cascade(int level, Runnable... abilitiesByTier) {
        int unlocked = Math.min(level, abilitiesByTier.length);
        for (int tier = 0; tier < unlocked; tier++) {
            abilitiesByTier[tier].run();
        }
    }

    /** Like {@link #cascade(int, Runnable...)} for yes/no questions: true as soon as one unlocked tier says yes. */
    public static boolean cascadeAny(int level, BooleanSupplier... checksByTier) {
        int unlocked = Math.min(level, checksByTier.length);
        for (int tier = 0; tier < unlocked; tier++) {
            if (checksByTier[tier].getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    /** Tier level of any piece of gear, 0 for everything else. */
    public static int gearLevel(ItemStack stack) {
        return stack.getItem() instanceof TieredGear gear ? gear.gearTier().level() : 0;
    }

    /** Tier level if the stack is a weapon (sword, axe). */
    public static int weaponLevel(ItemStack stack) {
        return stack.getItem() instanceof TieredGear gear && gear.gearKind().isWeapon() ? gear.gearTier().level() : 0;
    }

    /** Tier level if the stack is a mining tool (pickaxe, axe, shovel, hoe). */
    public static int toolLevel(ItemStack stack) {
        return stack.getItem() instanceof TieredGear gear && gear.gearKind().isTool() ? gear.gearTier().level() : 0;
    }

    /** Tier level if the stack is held gear of any kind (tool or weapon). */
    public static int handheldLevel(ItemStack stack) {
        return stack.getItem() instanceof TieredGear gear && gear.gearKind() != TieredGear.Kind.ARMOR ? gear.gearTier().level() : 0;
    }

    /**
     * Level of the full armor set: the lowest tier among the four worn pieces, 0 when a piece is missing.
     * Because tiers inherit, mixed sets simply count as their weakest piece.
     */
    public static int armorSetLevel(LivingEntity entity) {
        int level = Integer.MAX_VALUE;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!(entity.getItemBySlot(slot).getItem() instanceof TieredGear gear) || gear.gearKind() != TieredGear.Kind.ARMOR) {
                return 0;
            }
            level = Math.min(level, gear.gearTier().level());
        }
        return level;
    }

    /** {@code true} when at least one worn armor piece is of exactly this tier. */
    public static boolean wearsArmorOf(LivingEntity entity, GearTier tier) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (entity.getItemBySlot(slot).getItem() instanceof TieredGear gear && gear.gearKind() == TieredGear.Kind.ARMOR && gear.gearTier() == tier) {
                return true;
            }
        }
        return false;
    }

    /** Server side particle burst around the body of an entity. */
    public static void burst(Entity entity, ParticleOptions particle, int count, double speed) {
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(particle, entity.getX(), entity.getY(0.5), entity.getZ(), count,
                    entity.getBbWidth() * 0.35, entity.getBbHeight() * 0.3, entity.getBbWidth() * 0.35, speed);
        }
    }

    private TierAbilityHandler() {}
}
