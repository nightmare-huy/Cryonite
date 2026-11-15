package dev.cryonite.client;

import dev.cryonite.Cryonite;
import dev.cryonite.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Cryonite.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.PRIMORDIAL_SMELTER.get(), PrimordialSmelterScreen::new);
    }
    private ClientModEvents() {}
}
