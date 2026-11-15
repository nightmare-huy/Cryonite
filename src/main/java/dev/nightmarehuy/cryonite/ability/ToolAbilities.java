package dev.nightmarehuy.cryonite.ability;

import dev.nightmarehuy.cryonite.registry.ModDataComponents;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/**
 * Mining abilities of pickaxes, axes, shovels, hoes and multitools.
 * Tier 1: ice and snow break instantly. Tier 2: auto-smelt (the loot itself is rewritten by
 * {@link dev.nightmarehuy.cryonite.loot.AutoSmeltLootModifier}). Tier 3: telekinesis. Tier 4: 3x3 mining.
 */
public final class ToolAbilities {
    /** Blocks more than this much harder than the block actually mined are left alone by 3x3 mining. */
    private static final float AREA_HARDNESS_TOLERANCE = 5.0F;
    private static final float INSTANT_BREAK_SPEED = 1.0E6F;
    private static final int BLOCK_BREAK_EVENT = 2001;

    /**
     * Set while the eight extra blocks of a 3x3 break are being destroyed. Every one of them fires its own
     * break event; without this guard each would start another 3x3 and the area would spread without end.
     */
    private static final ThreadLocal<Boolean> IS_MINING_3X3 = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** The two switchable tool modes; their state is stored in data components on the stack. */
    public enum Mode {
        AUTO_SMELT(1, GearTier.INFERNITE, true, "auto_smelt", "key.cryonite.toggle_autosmelt"),
        AREA_MINING(0, GearTier.AETHER, false, "area_mining", "key.cryonite.toggle_3x3");

        private final int networkId;
        private final GearTier requiredTier;
        private final boolean enabledByDefault;
        private final String name;
        private final String keyName;

        Mode(int networkId, GearTier requiredTier, boolean enabledByDefault, String name, String keyName) {
            this.networkId = networkId;
            this.requiredTier = requiredTier;
            this.enabledByDefault = enabledByDefault;
            this.name = name;
            this.keyName = keyName;
        }

        /** The {@code modeType} of {@link dev.nightmarehuy.cryonite.network.ToggleToolModePayload}: 0 = 3x3, 1 = auto-smelt. */
        public int networkId() {
            return networkId;
        }

        /** The mode behind a {@code modeType} received from a client, {@code null} for anything unknown. */
        @Nullable
        public static Mode byNetworkId(int networkId) {
            for (Mode mode : values()) {
                if (mode.networkId == networkId) {
                    return mode;
                }
            }
            return null;
        }

        public GearTier requiredTier() {
            return requiredTier;
        }

        /** Name of the mode in the action bar, e.g. "Auto-smelt". */
        public String messageKey() {
            return "message.cryonite." + name;
        }

        /** The word for the new state in the action bar (it follows the grammatical gender of the mode name). */
        public String stateKey(boolean enabled) {
            return messageKey() + (enabled ? ".on" : ".off");
        }

        /** Tooltip line with two arguments: the state and the bound key. */
        public String tooltipKey() {
            return "tooltip.cryonite.mode." + name;
        }

        /** The short state shown in the tooltip: ON / OFF for auto-smelt, 3x3 / 1x1 for area mining. */
        public String tooltipStateKey(boolean enabled) {
            return tooltipKey() + (enabled ? ".on" : ".off");
        }

        /** Name of the key mapping that toggles the mode; resolved to the bound key on the client. */
        public String keyName() {
            return keyName;
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
        Component state = Component.translatable(mode.stateKey(enabled)).withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
        player.displayClientMessage(Component.translatable("message.cryonite.mode_state", Component.translatable(mode.messageKey()), state), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.4F, enabled ? 1.4F : 0.8F);
    }

    /**
     * Tooltip lines of the modes the stack supports, e.g. "Auto-smelt: ON [Key: B]" and "Mining mode: 3x3 [Key: V]".
     * The state is read from the stack every time the tooltip is drawn, and the key is a keybind component,
     * so it follows whatever the player bound in the controls screen. Safe to call on a dedicated server.
     */
    public static void appendModeTooltip(ItemStack stack, List<Component> tooltip) {
        for (Mode mode : Mode.values()) {
            if (mode.isSupported(stack)) {
                boolean active = mode.isActive(stack);
                tooltip.add(Component.translatable(mode.tooltipKey(),
                                Component.translatable(mode.tooltipStateKey(active)).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED),
                                Component.keybind(mode.keyName()).withStyle(ChatFormatting.YELLOW))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
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
     * Breaks the eight neighbours of {@code origin} in the plane of the face the player is mining.
     * Every extra block goes through the normal player break logic, so drops, auto-smelt, telekinesis,
     * experience and claim protection all behave exactly as for a hand-mined block.
     *
     * <p>A neighbour is only broken when the held tool is the correct tool for it and the block is
     * breakable at all (hardness {@code >= 0}, so no bedrock, barriers or portal frames).</p>
     */
    public static void mineArea(ServerPlayer player, BlockPos origin, BlockState originState) {
        if (IS_MINING_3X3.get()) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        if (!Mode.AREA_MINING.isActive(tool) || !tool.isCorrectToolForDrops(originState)) {
            return;
        }
        IS_MINING_3X3.set(Boolean.TRUE);
        try {
            ServerLevel level = player.serverLevel();
            float originHardness = originState.getDestroySpeed(level, origin);
            for (BlockPos pos : areaAround(origin, minedFace(player, origin))) {
                BlockState state = level.getBlockState(pos);
                if (!canMineInArea(level, pos, state, tool, originHardness)) {
                    continue;
                }
                if (player.gameMode.destroyBlock(pos)) {
                    // The breaking player is skipped by the vanilla break effect, so send it explicitly.
                    player.connection.send(new ClientboundLevelEventPacket(BLOCK_BREAK_EVENT, pos, Block.getId(state), false));
                }
            }
        } finally {
            IS_MINING_3X3.set(Boolean.FALSE);
        }
    }

    /**
     * The eight neighbours of {@code origin} in the plane of the mined face:
     * UP / DOWN give the horizontal X/Z plane, NORTH / SOUTH the X/Y wall, WEST / EAST the Z/Y wall.
     */
    public static List<BlockPos> areaAround(BlockPos origin, Direction face) {
        List<BlockPos> area = new ArrayList<>(8);
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                area.add(switch (face.getAxis()) {
                    case Y -> origin.offset(a, 0, b);
                    case Z -> origin.offset(a, b, 0);
                    case X -> origin.offset(0, b, a);
                });
            }
        }
        return area;
    }

    private static boolean canMineInArea(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool, float originHardness) {
        if (state.isAir() || !tool.isCorrectToolForDrops(state)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F) {
            return false;
        }
        // Two safety nets on top: chests and other block entities are never swept along, and a block much
        // harder than the one actually mined (obsidian next to stone) has to be mined on purpose.
        return hardness <= originHardness + AREA_HARDNESS_TOLERANCE && level.getBlockEntity(pos) == null;
    }

    /**
     * The block face the player is mining: a server side ray cast along the line of sight, falling back to
     * the view direction when the ray does not end on {@code origin}.
     *
     * <p>The ray is built from the body rotation the last movement packet delivered. {@code player.pick}
     * would use the head rotation, which the server only catches up at its next tick: after a quick turn
     * it can still point at the previous target and pick the wrong plane.</p>
     */
    private static Direction minedFace(ServerPlayer player, BlockPos origin) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(player.blockInteractionRange() + 1.0));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(origin)) {
            return hit.getDirection();
        }
        return Direction.getNearest(look.x, look.y, look.z).getOpposite();
    }

    private ToolAbilities() {}
}
