package dev.cryonite.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RawCryoniteItem extends Item {
    public RawCryoniteItem(Properties properties) { super(properties); }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || player.tickCount % 40 != 0) return;
        int stage = Math.min(2, Math.max(0, (int)(level.getGameTime() / 1200L)));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, stage, true, false));
        player.setTicksFrozen(Math.min(player.getTicksFrozen() + 20, 140));
    }
}
