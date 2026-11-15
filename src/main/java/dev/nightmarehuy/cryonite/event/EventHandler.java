package dev.nightmarehuy.cryonite.event;

import dev.nightmarehuy.cryonite.ability.ArmorAbilities;
import dev.nightmarehuy.cryonite.ability.TierAbilityHandler;
import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import dev.nightmarehuy.cryonite.ability.WeaponAbilities;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModTags;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The single game-bus subscriber of the mod. It contains no ability logic itself: every event is routed to
 * {@link ArmorAbilities}, {@link WeaponAbilities} or {@link ToolAbilities}, which cascade through the tiers
 * with {@link TierAbilityHandler}.
 */
public final class EventHandler {
    /** Void Ore anomaly: downward pull and duration of the blindness. */
    private static final double ANOMALY_PULL = -1.8;
    private static final int ANOMALY_BLINDNESS_TICKS = 60;

    // ------------------------------------------------------------------ armor

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        ArmorAbilities.onPlayerTick(event.getEntity());
    }

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        ArmorAbilities.onEffectApplicable(event);
    }

    // ------------------------------------------------------------------ armor + weapons

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        if (!ArmorAbilities.onIncomingDamage(event)) {
            WeaponAbilities.onIncomingDamage(event);
        }
    }

    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        WeaponAbilities.onDamageDealt(event);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        WeaponAbilities.flushPendingBursts();
    }

    // ------------------------------------------------------------------ tools

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ToolAbilities.onBreakSpeed(event);
    }

    /** Runs last so protection mods had their chance to cancel the break before the anomaly and 3x3 mining start. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getState().is(ModBlocks.VOID_ORE) && !player.isCreative()) {
            triggerVoidAnomaly(player);
        }
        ToolAbilities.mineArea(player, event.getPos(), event.getState());
    }

    /** Breaking Void Ore: a gravity pulse drags the miner towards the void and blinds them for 3 seconds. */
    private static void triggerVoidAnomaly(ServerPlayer player) {
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, ANOMALY_PULL, motion.z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, ANOMALY_BLINDNESS_TICKS));
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        ToolAbilities.onBlockDrops(event);
    }

    /** Tier 3 telekinesis for mob loot. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && hasTelekinesis(player)) {
            ToolAbilities.collect(player, event.getDrops());
        }
    }

    /** Tier 3 telekinesis for mob experience. */
    @SubscribeEvent
    public static void onExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getAttackingPlayer() instanceof ServerPlayer player && hasTelekinesis(player) && event.getDroppedExperience() > 0) {
            player.giveExperiencePoints(event.getDroppedExperience());
            event.setCanceled(true);
        }
    }

    private static boolean hasTelekinesis(Player player) {
        return GearTier.VOID.unlockedAt(TierAbilityHandler.handheldLevel(player.getMainHandItem()));
    }

    /** Shift + right-click with an Infernite+ tool toggles auto-smelt (when the click is not used by a block). */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !ToolAbilities.Mode.AUTO_SMELT.isSupported(event.getItemStack())) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ToolAbilities.toggle(serverPlayer, ToolAbilities.Mode.AUTO_SMELT);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ------------------------------------------------------------------ dropped items

    /** Dropped gear of the four tiers shrugs off explosions, cacti, fire, lightning, ... - everything but the void. */
    @SubscribeEvent
    public static void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (event.getEntity() instanceof ItemEntity item && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            ItemStack stack = item.getItem();
            if (stack.getItem() instanceof TieredGear || stack.is(ModTags.Items.INDESTRUCTIBLE)) {
                event.setInvulnerable(true);
            }
        }
    }

    private EventHandler() {}
}
