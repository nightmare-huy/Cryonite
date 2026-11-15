package dev.nightmarehuy.cryonite.ability;

import dev.nightmarehuy.cryonite.registry.ModDataComponents;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Mining abilities of pickaxes, axes, shovels and hoes.
 * Tier 1: ice and snow break instantly. Tier 2: auto-smelt (the loot itself is rewritten by
 * {@link dev.nightmarehuy.cryonite.loot.AutoSmeltLootModifier}). Tier 3: telekinesis. Tier 4: 3x3 mining.
 */
public final class ToolAbilities {
    /** Blocks more than this much harder than the block actually mined are left alone by 3x3 mining. */
    private static final float AREA_HARDNESS_TOLERANCE = 5.0F;
    private static final float INSTANT_BREAK_SPEED = 1.0E6F;
    private static final int BLOCK_BREAK_EVENT = 2001;

    /** Players currently inside a 3x3 break, so the extra blocks do not start their own 3x3. */
    private static final Set<UUID> AREA_MINERS = new HashSet<>();

    /** The two switchable tool modes; their state is stored in data components on the stack. */
    public enum Mode {
        AUTO_SMELT(GearTier.INFERNITE, true, "message.cryonite.auto_smelt"),
        AREA_MINING(GearTier.AETHER, false, "message.cryonite.area_mining");

        private final GearTier requiredTier;
        private final boolean enabledByDefault;
        private final String messageKey;

        Mode(GearTier requiredTier, boolean enabledByDefault, String messageKey) {
            this.requiredTier = requiredTier;
            this.enabledByDefault = enabledByDefault;
            this.messageKey = messageKey;
        }

        public GearTier requiredTier() {
            return requiredTier;
        }

        public String messageKey() {
            return messageKey;
        }

        private DataComponentType<Boolean> component() {
            return this == AUTO_SMELT ? ModDataComponents.AUTO_SMELT.get() : ModDataComponents.AREA_MINING.get();
        }

        /** {@code true} when the stack is a tool of a sufficient tier and has the mode switched on. */
        public boolean isActive(ItemStack stack) {
            return isSupported(stack) && stack.getOrDefault(component(), enabledByDefault);
        }

        public boolean isSupported(ItemStack stack) {
            return requiredTier.unlockedAt(TierAbilityHandler.toolLevel(stack));
        }
    }

    // ------------------------------------------------------------------ mode switching

    /** Flips a mode of the tool in the player's main hand and reports the new state in the action bar. */
    public static void toggle(ServerPlayer player, Mode mode) {
        ItemStack stack = player.getMainHandItem();
        if (!mode.isSupported(stack)) {
            player.displayClientMessage(Component.translatable("message.cryonite.mode_unsupported",
                    Component.translatable(mode.messageKey())).withStyle(ChatFormatting.RED), true);
            return;
        }
        boolean enabled = !mode.isActive(stack);
        stack.set(mode.component(), enabled);
        Component state = Component.translatable(enabled ? "message.cryonite.enabled" : "message.cryonite.disabled")
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
        player.displayClientMessage(Component.translatable("message.cryonite.mode_state", Component.translatable(mode.messageKey()), state), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, enabled ? 1.4F : 0.8F);
    }

    // ------------------------------------------------------------------ tier 1: ice breaker

    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        int level = TierAbilityHandler.toolLevel(event.getEntity().getMainHandItem());
        BlockState state = event.getState();
        if (GearTier.CRYONITE.unlockedAt(level) && (state.is(BlockTags.ICE) || state.is(BlockTags.SNOW))) {
            event.setNewSpeed(INSTANT_BREAK_SPEED);
        }
    }

    // ------------------------------------------------------------------ tier 3: telekinesis

    /** Block loot and its experience go straight into the inventory of the miner. */
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof ServerPlayer player) || !GearTier.VOID.unlockedAt(TierAbilityHandler.handheldLevel(event.getTool()))) {
            return;
        }
        collect(player, event.getDrops());
        int experience = event.getDroppedExperience();
        if (experience > 0) {
            player.giveExperiencePoints(experience);
            event.setDroppedExperience(0);
        }
    }

    /** Moves as much as fits into the inventory; whatever does not fit stays in the collection and drops normally. */
    public static void collect(Player player, Collection<ItemEntity> drops) {
        boolean collected = false;
        var iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next().getItem();
            int before = stack.getCount();
            player.getInventory().add(stack);
            collected |= stack.getCount() < before;
            if (stack.isEmpty()) {
                iterator.remove();
            }
        }
        if (collected) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F,
                    (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 1.4F + 2.0F);
        }
    }

    // ------------------------------------------------------------------ tier 4: 3x3 mining

    /**
     * Breaks the eight neighbours of {@code origin} in the plane of the face the player is looking at.
     * Every extra block goes through the normal player break logic, so drops, auto-smelt, telekinesis,
     * experience and claim protection all behave exactly as for a hand-mined block.
     */
    public static void mineArea(ServerPlayer player, BlockPos origin, BlockState originState) {
        ItemStack tool = player.getMainHandItem();
        if (!Mode.AREA_MINING.isActive(tool) || player.isShiftKeyDown() || !tool.isCorrectToolForDrops(originState)) {
            return;
        }
        if (!AREA_MINERS.add(player.getUUID())) {
            return;
        }
        try {
            ServerLevel level = player.serverLevel();
            float originHardness = originState.getDestroySpeed(level, origin);
            Direction.Axis normal = minedFace(player, origin).getAxis();
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) {
                        continue;
                    }
                    BlockPos pos = switch (normal) {
                        case X -> origin.offset(0, a, b);
                        case Y -> origin.offset(a, 0, b);
                        case Z -> origin.offset(a, b, 0);
                    };
                    BlockState state = level.getBlockState(pos);
                    float hardness = state.getDestroySpeed(level, pos);
                    if (state.isAir() || hardness < 0.0F || hardness > originHardness + AREA_HARDNESS_TOLERANCE
                            || !tool.isCorrectToolForDrops(state) || level.getBlockEntity(pos) != null) {
                        continue;
                    }
                    if (player.gameMode.destroyBlock(pos)) {
                        // The breaking player is skipped by the vanilla break effect, so send it explicitly.
                        player.connection.send(new ClientboundLevelEventPacket(BLOCK_BREAK_EVENT, pos, Block.getId(state), false));
                    }
                }
            }
        } finally {
            AREA_MINERS.remove(player.getUUID());
        }
    }

    /** The block face the player is mining: taken from a server side ray cast, falling back to the view direction. */
    private static Direction minedFace(ServerPlayer player, BlockPos origin) {
        HitResult hit = player.pick(player.blockInteractionRange() + 1.0, 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK && blockHit.getBlockPos().equals(origin)) {
            return blockHit.getDirection();
        }
        return Direction.orderedByNearest(player)[0].getOpposite();
    }

    private ToolAbilities() {}
}
