package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.registry.ModTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Molten Infernite Shard: far too hot to carry or to smelt.
 * <ul>
 *   <li>Without a coolant in the inventory it sets its bearer on fire and every 100 ticks burns
 *       one random wooden or stone item.</li>
 *   <li>Right-click a Powder Snow Cauldron or Blue Ice to quench it into Cooled Infernite Blanks.</li>
 * </ul>
 */
public class MoltenInferniteShardItem extends Item {
    private static final int CHECK_INTERVAL = 20;
    private static final int BURN_INTERVAL = 100;
    private static final float IGNITE_SECONDS = 3.0F;
    /** Shards quenched by one cauldron layer / one block of Blue Ice. */
    public static final int QUENCH_BATCH = 16;

    public MoltenInferniteShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        Inventory inventory = player.getInventory();
        // Several shard stacks must not multiply the effect: only the first stack of the inventory acts.
        if (firstShardStack(inventory) != stack || inventory.contains(ModTags.Items.COOLANTS)) {
            return;
        }

        player.igniteForSeconds(IGNITE_SECONDS);
        if (player.tickCount % BURN_INTERVAL == 0) {
            burnRandomItem(player, inventory);
        }
    }

    private ItemStack firstShardStack(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.is(this)) {
                return candidate;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void burnRandomItem(Player player, Inventory inventory) {
        List<ItemStack> burnable = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (candidate.is(ModTags.Items.BURNABLE_BY_INFERNITE)) {
                burnable.add(candidate);
            }
        }
        if (burnable.isEmpty()) {
            return;
        }
        ItemStack victim = burnable.get(player.getRandom().nextInt(burnable.size()));
        Component name = victim.getHoverName();
        victim.shrink(1);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.6F);
        player.displayClientMessage(Component.translatable("message.cryonite.item_burned", name).withStyle(ChatFormatting.GOLD), true);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean cauldron = state.is(Blocks.POWDER_SNOW_CAULDRON);
        if (!cauldron && !state.is(Blocks.BLUE_ICE)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            if (cauldron) {
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            } else {
                level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
            }

            ItemStack held = context.getItemInHand();
            int amount = Math.min(held.getCount(), QUENCH_BATCH);
            held.shrink(amount);
            ItemStack blanks = new ItemStack(ModItems.COOLED_INFERNITE_BLANK.get(), amount);
            Player player = context.getPlayer();
            if (player != null) {
                ItemHandlerHelper.giveItemToPlayer(player, blanks);
            } else {
                Block.popResource(level, pos.above(), blanks);
            }

            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.3F);
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 14, 0.25, 0.1, 0.25, 0.02);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cryonite.molten_infernite_shard").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.cryonite.molten_infernite_shard.quench").withStyle(ChatFormatting.GRAY));
    }
}
