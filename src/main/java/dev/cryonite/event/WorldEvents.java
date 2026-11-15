package dev.cryonite.event;

import dev.cryonite.Cryonite;
import dev.cryonite.registry.ModBlocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Cryonite.MOD_ID)
public final class WorldEvents {
    @SubscribeEvent public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!event.getState().is(ModBlocks.VOID_ORE.get())) return;
        Player player = event.getPlayer();
        var velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, -1.8, velocity.z);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));
    }
    private WorldEvents() {}
}
