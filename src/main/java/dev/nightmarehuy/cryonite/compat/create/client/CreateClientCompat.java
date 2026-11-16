package dev.nightmarehuy.cryonite.compat.create.client;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import dev.nightmarehuy.cryonite.compat.create.CreateCompat;
import dev.nightmarehuy.cryonite.compat.create.KineticEnergyConverterBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client half of the Create integration: the control panel and the spinning shaft of the converter.
 * Only loaded on the physical client and only with Create installed.
 */
public final class CreateClientCompat {
    public static void init(IEventBus modBus) {
        modBus.addListener(CreateClientCompat::registerScreens);
        modBus.addListener(CreateClientCompat::registerRenderers);
        modBus.addListener(CreateClientCompat::registerVisuals);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CreateCompat.KINETIC_CONVERTER_MENU.get(), KineticConverterScreen::new);
    }

    /** Draws the shaft when Flywheel's instanced rendering is not available. */
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CreateCompat.KINETIC_ENERGY_CONVERTER_ENTITY.get(), KineticEnergyConverterRenderer::new);
    }

    /** The same half shaft as an instanced Flywheel visual, exactly like Create's own motors. */
    private static void registerVisuals(FMLClientSetupEvent event) {
        SimpleBlockEntityVisualizer.builder(CreateCompat.KINETIC_ENERGY_CONVERTER_ENTITY.get())
                .factory(OrientedRotatingVisual.<KineticEnergyConverterBlockEntity>of(AllPartialModels.SHAFT_HALF))
                .skipVanillaRender(blockEntity -> true)
                .apply();
    }

    private CreateClientCompat() {}
}
