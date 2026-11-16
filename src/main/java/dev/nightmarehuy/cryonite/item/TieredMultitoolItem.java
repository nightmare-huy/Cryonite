package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import dev.nightmarehuy.cryonite.ability.WeaponAbilities;
import dev.nightmarehuy.cryonite.registry.ModTags;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Multitool ("paxel") of the four tiers: pickaxe, axe, shovel and hoe in one head, and a weapon one point
 * stronger than the sword of its tier.
 *
 * <p>Like every other piece of gear it carries no ability code of its own. It reports
 * {@link Kind#MULTITOOL}, which is both a weapon and a tool, so the cascade in
 * {@link dev.nightmarehuy.cryonite.ability.TierAbilityHandler} hands it every weapon ability
 * ({@link WeaponAbilities}) and every mining ability ({@link ToolAbilities}) of its tier.</p>
 */
public class TieredMultitoolItem extends DiggerItem implements TieredGear {
    /** Vanilla swords cut cobwebs at this speed; a multitool is never slower. */
    private static final float COBWEB_SPEED = 15.0F;
    private static final float INEFFECTIVE_SPEED = 1.0F;
    /** Block.UPDATE_ALL_IMMEDIATE, the flags vanilla tools use for their right-click actions. */
    private static final int UPDATE_FLAGS = 11;
    private static final int SCRAPE_EVENT = 3005;
    private static final int WAX_OFF_EVENT = 3004;
    private static final int EXTINGUISH_EVENT = 1009;

    private final GearTier gearTier;

    public TieredMultitoolItem(GearTier gearTier, Tier tier, Properties properties) {
        // The tag feeds the vanilla Tool component; the two overrides below answer the same questions directly.
        super(tier, ModTags.Blocks.MINEABLE_WITH_MULTITOOL, properties);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier gearTier() {
        return gearTier;
    }

    @Override
    public Kind gearKind() {
        return Kind.MULTITOOL;
    }

    // ------------------------------------------------------------------ mining

    /** Pickaxe, axe, shovel and hoe blocks, plus leaves and cobwebs. */
    public static boolean isMineable(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || state.is(BlockTags.MINEABLE_WITH_AXE)
                || state.is(BlockTags.MINEABLE_WITH_SHOVEL)
                || state.is(BlockTags.MINEABLE_WITH_HOE)
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.COBWEB);
    }

    /** {@code true} when the block is a multitool block that does not need a tier above this one. */
    public boolean isEffectiveOn(BlockState state) {
        return isMineable(state) && !state.is(gearTier.incorrectBlocksForDrops());
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return isEffectiveOn(state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!isEffectiveOn(state)) {
            return INEFFECTIVE_SPEED;
        }
        float speed = getTier().getSpeed();
        return state.is(Blocks.COBWEB) ? Math.max(speed, COBWEB_SPEED) : speed;
    }

    /** Everything a pickaxe, an axe, a shovel and a hoe can do, including their right-click actions. */
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
    }

    /** The axe blade knocks a raised shield down, exactly like a vanilla axe. */
    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    // ------------------------------------------------------------------ right click on a block

    /**
     * Sneaking: till like a hoe. Otherwise (and when there was nothing to till): strip, scrape or unwax like
     * an axe, then flatten a path or put out a campfire like a shovel.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.isSecondaryUseActive()) {
            InteractionResult tilled = till(context);
            if (tilled != InteractionResult.PASS) {
                return tilled;
            }
        }
        InteractionResult chopped = chop(context);
        return chopped != InteractionResult.PASS ? chopped : dig(context);
    }

    private static InteractionResult till(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState tilled = level.getBlockState(pos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (tilled == null) {
            return InteractionResult.PASS;
        }
        level.playSound(context.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide) {
            change(context, tilled);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult chop(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);

        BlockState result = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
        if (result != null) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if ((result = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false)) != null) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, SCRAPE_EVENT, pos, 0);
        } else if ((result = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false)) != null) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, WAX_OFF_EVENT, pos, 0);
        } else {
            return InteractionResult.PASS;
        }
        // Like the vanilla axe, the new block is set on both sides so the click feels instant.
        change(context, result);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static InteractionResult dig(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        BlockState flattened = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
        BlockState result;
        if (flattened != null && level.getBlockState(pos.above()).isAir()) {
            level.playSound(context.getPlayer(), pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            result = flattened;
        } else {
            result = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);
            if (result == null) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide) {
                level.levelEvent(null, EXTINGUISH_EVENT, pos, 0);
            }
        }
        if (!level.isClientSide) {
            change(context, result);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void change(UseOnContext context, BlockState state) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, context.getItemInHand());
        }
        level.setBlock(pos, state, UPDATE_FLAGS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
    }

    // ------------------------------------------------------------------ right click in the air

    /** Void dash of the tier 3+ weapons. Only in the air: a click on a block belongs to the tool actions above. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GearTier.VOID.unlockedAt(gearTier.level())
                || getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE).getType() == HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            // The cooldown is applied (and synced to the client) by the server only when the dash succeeded.
            return WeaponAbilities.voidDash(serverPlayer, stack)
                    ? InteractionResultHolder.success(stack)
                    : InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    // ------------------------------------------------------------------ tooltip

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ToolAbilities.appendModeTooltip(stack, tooltip);
    }
}
