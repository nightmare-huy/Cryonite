package dev.nightmarehuy.cryonite.ability;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.attachment.VoidRescueData;
import dev.nightmarehuy.cryonite.registry.ModAttachments;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Full-set bonuses of the four armors. Every public entry point resolves the set level once and
 * cascades through the per-tier behaviours, so e.g. an Aether set runs the Cryonite, Infernite, Void
 * and Aether code in that order.
 */
public final class ArmorAbilities {
    /** Tier 3: cooldown of the void rescue (2 minutes). */
    public static final int VOID_RESCUE_COOLDOWN_TICKS = 2400;
    /** Tier 3: the rescue fires this many blocks below the bottom of the world (Y < -60 in the End). */
    public static final double VOID_RESCUE_DEPTH = 60.0;
    /** Tier 1: duration of the ice counterattack. */
    private static final int COUNTER_EFFECT_TICKS = 80;
    /** Tier 2: attackers burn for 6 seconds. */
    private static final float COUNTER_BURN_SECONDS = 6.0F;
    private static final ResourceLocation FLIGHT_MODIFIER_ID = Cryonite.id("aether_armor_flight");

    // ------------------------------------------------------------------ ticking

    public static void onPlayerTick(Player player) {
        int level = TierAbilityHandler.armorSetLevel(player);
        if (player.level().isClientSide) {
            if (player.isLocalPlayer() && GearTier.INFERNITE.unlockedAt(level)) {
                swimInLava(player);
            }
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Flight has to be revoked as well, so it is evaluated for every level including 0.
        updateCreativeFlight(serverPlayer, GearTier.AETHER.unlockedAt(level));
        TierAbilityHandler.cascade(level,
                () -> cryoniteTick(serverPlayer),
                () -> inferniteTick(serverPlayer),
                () -> voidTick(serverPlayer),
                () -> aetherTick(serverPlayer));
    }

    /** Tier 1: never frozen, never slowed. */
    private static void cryoniteTick(ServerPlayer player) {
        if (player.getTicksFrozen() > 0) {
            player.setTicksFrozen(0);
        }
        if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }

    /** Tier 2: never burning, never withering. */
    private static void inferniteTick(ServerPlayer player) {
        if (player.isOnFire()) {
            player.clearFire();
        }
        if (player.hasEffect(MobEffects.WITHER)) {
            player.removeEffect(MobEffects.WITHER);
        }
    }

    /** Tier 3: remember solid ground and catch the wearer before the void kills. */
    private static void voidTick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (player.onGround() && player.tickCount % 5 == 0) {
            BlockPos ground = player.getOnPos();
            if (level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
                GlobalPos safe = GlobalPos.of(level.dimension(), player.blockPosition());
                VoidRescueData data = player.getData(ModAttachments.VOID_RESCUE);
                if (data.lastSafePos().filter(safe::equals).isEmpty()) {
                    player.setData(ModAttachments.VOID_RESCUE, data.withSafePos(safe));
                }
            }
        }
        if (player.getY() < level.getMinBuildHeight() - VOID_RESCUE_DEPTH) {
            tryVoidRescue(player);
        }
    }

    /** Tier 4: strong regeneration and no harmful effects (flight is handled by {@link #updateCreativeFlight}). */
    private static void aetherTick(ServerPlayer player) {
        MobEffectInstance regeneration = player.getEffect(MobEffects.REGENERATION);
        if (regeneration == null || player.tickCount % 100 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1, true, false, true));
        }
        if (player.tickCount % 20 == 0 && player.getHealth() < player.getMaxHealth()) {
            player.heal(1.0F);
        }
        if (!player.getActiveEffects().isEmpty()) {
            List<Holder<MobEffect>> harmful = player.getActiveEffects().stream()
                    .map(MobEffectInstance::getEffect)
                    .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL)
                    .toList();
            harmful.forEach(player::removeEffect);
        }
    }

    /** Tier 4: creative flight through the NeoForge attribute, present exactly while the full set is worn. */
    private static void updateCreativeFlight(ServerPlayer player, boolean enabled) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight == null) {
            return;
        }
        boolean active = flight.hasModifier(FLIGHT_MODIFIER_ID);
        if (enabled && !active) {
            flight.addTransientModifier(new AttributeModifier(FLIGHT_MODIFIER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE));
        } else if (!enabled && active) {
            flight.removeModifier(FLIGHT_MODIFIER_ID);
        }
    }

    /**
     * Tier 2 (client): lava drags like water does with Depth Strider III.
     * Vanilla halves the velocity every tick in lava (x0.5) and accelerates by 0.02; water with Depth Strider III
     * uses x0.546 and 0.1. Scaling by 1.092 and adding the missing acceleration reproduces exactly that.
     */
    private static void swimInLava(Player player) {
        if (!player.isInLava() || player.getAbilities().flying) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double vertical = motion.y > 0.0 ? motion.y * 1.6 : motion.y;
        player.setDeltaMovement(motion.x * 1.092, vertical, motion.z * 1.092);
        player.moveRelative(0.043F, new Vec3(player.xxa, 0.0, player.zza));
    }

    // ------------------------------------------------------------------ damage

    /**
     * @return {@code true} when the armor negated the damage completely (the event is cancelled)
     */
    public static boolean onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity wearer = event.getEntity();
        int level = TierAbilityHandler.armorSetLevel(wearer);
        if (level == 0) {
            return false;
        }
        DamageSource source = event.getSource();

        if (source.getEntity() instanceof LivingEntity attacker && attacker != wearer && attacker.isAlive()) {
            TierAbilityHandler.cascade(level,
                    () -> chill(attacker),
                    () -> scorch(attacker));
        }

        boolean immune = TierAbilityHandler.cascadeAny(level,
                () -> source.is(DamageTypes.FREEZE),
                () -> source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.LAVA)
                        || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.WITHER),
                () -> source.is(DamageTypes.FALL) || source.is(DamageTypes.FLY_INTO_WALL)
                        || source.is(DamageTypes.FELL_OUT_OF_WORLD) && wearer instanceof ServerPlayer player && tryVoidRescue(player),
                () -> source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC));
        if (immune) {
            event.setCanceled(true);
        }
        return immune;
    }

    /** Tier 1 counterattack: Slowness II and Weakness I for 4 seconds. */
    private static void chill(LivingEntity attacker) {
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, COUNTER_EFFECT_TICKS, 1));
        attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, COUNTER_EFFECT_TICKS, 0));
        TierAbilityHandler.burst(attacker, ParticleTypes.SNOWFLAKE, 10, 0.02);
    }

    /** Tier 2 counterattack: blue flames for 6 seconds. */
    private static void scorch(LivingEntity attacker) {
        attacker.igniteForSeconds(COUNTER_BURN_SECONDS);
        TierAbilityHandler.burst(attacker, ParticleTypes.SOUL_FIRE_FLAME, 14, 0.03);
    }

    // ------------------------------------------------------------------ effects

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        int level = TierAbilityHandler.armorSetLevel(event.getEntity());
        MobEffectInstance instance = event.getEffectInstance();
        if (level == 0 || instance == null) {
            return;
        }
        MobEffect effect = instance.getEffect().value();
        boolean blocked = TierAbilityHandler.cascadeAny(level,
                () -> effect == MobEffects.MOVEMENT_SLOWDOWN.value(),
                () -> effect == MobEffects.WITHER.value(),
                () -> false,
                () -> effect.getCategory() == MobEffectCategory.HARMFUL);
        if (blocked) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    // ------------------------------------------------------------------ void rescue

    /**
     * Tier 3: teleports the player back to the last solid block they stood on.
     *
     * @return {@code false} when the rescue is on cooldown or no safe position is known yet
     */
    public static boolean tryVoidRescue(ServerPlayer player) {
        VoidRescueData data = player.getData(ModAttachments.VOID_RESCUE);
        long now = player.serverLevel().getGameTime();
        if (now < data.cooldownUntil() || data.lastSafePos().isEmpty()) {
            return false;
        }
        GlobalPos safe = data.lastSafePos().get();
        ServerLevel target = player.server.getLevel(safe.dimension());
        if (target == null) {
            return false;
        }

        BlockPos pos = safe.pos();
        player.stopRiding();
        player.teleportTo(target, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.hurtMarked = true;

        player.setData(ModAttachments.VOID_RESCUE, data.withCooldownUntil(now + VOID_RESCUE_COOLDOWN_TICKS));
        player.getCooldowns().addCooldown(player.getItemBySlot(EquipmentSlot.CHEST).getItem(), VOID_RESCUE_COOLDOWN_TICKS);

        target.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);
        TierAbilityHandler.burst(player, ParticleTypes.REVERSE_PORTAL, 60, 0.1);
        player.displayClientMessage(Component.translatable("message.cryonite.void_rescue").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return true;
    }

    private ArmorAbilities() {}
}
