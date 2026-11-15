package dev.nightmarehuy.cryonite.ability;

import dev.nightmarehuy.cryonite.registry.ModDamageTypes;
import dev.nightmarehuy.cryonite.registry.ModTags;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * On-hit abilities of swords and axes. A hit resolves the weapon level once and cascades:
 * an Aether blade freezes, burns, pierces armor and finally adds its own magic burst and life steal.
 */
public final class WeaponAbilities {
    /** Tier 1: freeze meter applied to the target (fully frozen at 140). */
    private static final int FREEZE_TICKS = 300;
    private static final int SLOWNESS_TICKS = 100;
    /** Tier 1 / 2: +25% against the opposite element. */
    private static final float ELEMENT_BONUS = 0.25F;
    /** Tier 2: targets burn with blue flames for 6 seconds. */
    private static final float BURN_SECONDS = 6.0F;
    /** Tier 3: fraction of the target's armor reduction that is ignored. */
    private static final float ARMOR_IGNORED = 0.30F;
    /** Tier 3: dash distance in blocks and cooldown in ticks. */
    public static final double DASH_DISTANCE = 7.0;
    public static final int DASH_COOLDOWN_TICKS = 100;
    /** Tier 4: extra pure magic damage relative to the hit, and health stolen per hit. */
    private static final float MAGIC_FRACTION = 0.50F;
    private static final float LIFE_STEAL = 2.0F;

    /** Magic bursts are dealt at the end of the server tick so they never interfere with the hit that caused them. */
    private static final List<PendingBurst> PENDING_BURSTS = new ArrayList<>();

    private record PendingBurst(LivingEntity target, LivingEntity attacker, float amount) {}

    // ------------------------------------------------------------------ hits

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity attacker = meleeAttacker(event.getSource());
        if (attacker == null) {
            return;
        }
        int level = TierAbilityHandler.weaponLevel(attacker.getMainHandItem());
        if (level == 0) {
            return;
        }
        LivingEntity target = event.getEntity();
        float damage = event.getAmount();
        float[] bonus = {0.0F};

        TierAbilityHandler.cascade(level,
                () -> { // Tier 1 - Cryonite: frostbite, +25% against creatures of fire
                    if (target.canFreeze()) {
                        target.setTicksFrozen(Math.max(target.getTicksFrozen(), FREEZE_TICKS));
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TICKS, 1), attacker);
                    TierAbilityHandler.burst(target, ParticleTypes.SNOWFLAKE, 8, 0.02);
                    if (isFireCreature(target)) {
                        bonus[0] += ELEMENT_BONUS;
                    }
                },
                () -> { // Tier 2 - Infernite: blue fire, +25% against undead, frost / water mobs and Cryonite armor
                    target.igniteForSeconds(BURN_SECONDS);
                    TierAbilityHandler.burst(target, ParticleTypes.SOUL_FIRE_FLAME, 10, 0.03);
                    if (isWeakToInfernite(target)) {
                        bonus[0] += ELEMENT_BONUS;
                    }
                },
                () -> // Tier 3 - Void: 30% of the armor is ignored
                        event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> reduction * (1.0F - ARMOR_IGNORED)),
                () -> // Tier 4 - Aether: +50% as pure magic that no armor or shield can stop
                        PENDING_BURSTS.add(new PendingBurst(target, attacker, damage * MAGIC_FRACTION)));

        if (bonus[0] > 0.0F) {
            event.setAmount(damage * (1.0F + bonus[0]));
        }
    }

    /** Tier 4: every hit that actually hurt heals the attacker by one heart. */
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        LivingEntity attacker = meleeAttacker(event.getSource());
        if (attacker == null || event.getNewDamage() <= 0.0F) {
            return;
        }
        if (GearTier.AETHER.unlockedAt(TierAbilityHandler.weaponLevel(attacker.getMainHandItem()))) {
            attacker.heal(LIFE_STEAL);
        }
    }

    /** Deals the queued Aether bursts. Called once at the end of every server tick. */
    public static void flushPendingBursts() {
        if (PENDING_BURSTS.isEmpty()) {
            return;
        }
        List<PendingBurst> bursts = List.copyOf(PENDING_BURSTS);
        PENDING_BURSTS.clear();
        for (PendingBurst burst : bursts) {
            LivingEntity target = burst.target();
            if (target.isAlive() && !target.isRemoved() && burst.attacker().level() == target.level()) {
                target.hurt(target.damageSources().source(ModDamageTypes.AETHER_BURST, burst.attacker()), burst.amount());
                TierAbilityHandler.burst(target, ParticleTypes.END_ROD, 8, 0.05);
            }
        }
    }

    /** The living entity that struck with its main hand, or {@code null} for anything that is not a melee hit. */
    @Nullable
    private static LivingEntity meleeAttacker(DamageSource source) {
        boolean melee = source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);
        if (melee && source.getDirectEntity() instanceof LivingEntity attacker && source.getEntity() == attacker) {
            return attacker;
        }
        return null;
    }

    private static boolean isFireCreature(LivingEntity target) {
        return target.fireImmune() || target.getType().is(ModTags.EntityTypes.FIRE_CREATURES);
    }

    private static boolean isWeakToInfernite(LivingEntity target) {
        return target.getType().is(EntityTypeTags.UNDEAD)
                || target.getType().is(ModTags.EntityTypes.FROST_AND_WATER_CREATURES)
                || TierAbilityHandler.wearsArmorOf(target, GearTier.CRYONITE);
    }

    // ------------------------------------------------------------------ void dash

    /**
     * Tier 3 sword ability: blink up to 7 blocks along the line of sight, stopping in front of walls.
     *
     * @return {@code true} when the player was moved (the cooldown has been started)
     */
    public static boolean voidDash(ServerPlayer player, ItemStack sword) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(DASH_DISTANCE)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double reach = hit.getType() == HitResult.Type.MISS ? DASH_DISTANCE : Math.max(0.0, hit.getLocation().distanceTo(eye) - 0.4);

        Vec3 destination = null;
        AABB box = player.getBoundingBox();
        for (double distance = reach; distance >= 1.0; distance -= 0.5) {
            Vec3 offset = look.scale(distance);
            if (level.noCollision(player, box.move(offset))) {
                destination = origin.add(offset);
                break;
            }
        }
        if (destination == null) {
            return false;
        }

        level.sendParticles(ParticleTypes.PORTAL, origin.x, origin.y + 1.0, origin.z, 40, 0.3, 0.6, 0.3, 0.4);
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        player.stopRiding();
        player.teleportTo(destination.x, destination.y, destination.z);
        player.resetFallDistance();

        level.sendParticles(ParticleTypes.PORTAL, destination.x, destination.y + 1.0, destination.z, 40, 0.3, 0.6, 0.3, 0.4);
        level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.getCooldowns().addCooldown(sword.getItem(), DASH_COOLDOWN_TICKS);
        return true;
    }

    private WeaponAbilities() {}
}
