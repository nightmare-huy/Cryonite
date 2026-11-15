package dev.cryonite.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import dev.cryonite.registry.ModItems;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Random;

public class MoltenInferniteShardItem extends Item {
    private static final List<String> FRAGILE = List.of("planks", "log", "wood", "cobblestone", "stone", "deepslate");
    public MoltenInferniteShardItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (!state.is(Blocks.BLUE_ICE) && !state.is(Blocks.POWDER_SNOW_CAULDRON)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlock(pos, state.is(Blocks.BLUE_ICE) ? Blocks.ICE.defaultBlockState() : Blocks.CAULDRON.defaultBlockState(), 3);
            ItemStack hand = context.getItemInHand();
            hand.shrink(1);
            ItemStack blank = new ItemStack(ModItems.COOLED_INFERNITE_BLANK.get());
            if (context.getPlayer() == null || !context.getPlayer().getInventory().add(blank)) {
                net.minecraft.world.entity.item.ItemEntity drop = new net.minecraft.world.entity.item.ItemEntity(level, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, blank);
                level.addFreshEntity(drop);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.tickCount % 20 != 0) return;
        boolean cooled = player.getInventory().items.stream().anyMatch(s -> s.is(net.minecraft.world.item.Items.SNOWBALL) || s.is(net.minecraft.world.item.Items.ICE) || s.is(net.minecraft.world.item.Items.PACKED_ICE) || s.is(net.minecraft.world.item.Items.BLUE_ICE));
        if (cooled) return;
        player.setSecondsOnFire(2);
        if (player.tickCount % 100 == 0) {
            var candidates = player.getInventory().items.stream().filter(s -> !s.isEmpty() && FRAGILE.stream().anyMatch(token -> s.getDescriptionId().contains(token))).toList();
            if (!candidates.isEmpty()) candidates.get(player.getRandom().nextInt(candidates.size())).shrink(1);
        }
    }
}
