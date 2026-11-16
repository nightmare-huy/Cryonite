package dev.nightmarehuy.cryonite.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only bootstrap. This class (and everything else in this package) is only ever loaded on the
 * physical client: the mod constructor calls {@link #init(IEventBus)} behind a {@code Dist.CLIENT} check.
 */
public final class ClientSetup {
    public static final String KEY_CATEGORY = "key.categories.cryonite";

    /** Toggles 3x3 mining of the held Aether tool or multitool. */
    public static final KeyMapping KEY_3X3 = new KeyMapping(
            "key.cryonite.toggle_3x3", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, KEY_CATEGORY);

    /** Toggles auto-smelt of the held Infernite+ tool or multitool. */
    public static final KeyMapping KEY_AUTOSMELT = new KeyMapping(
            "key.cryonite.toggle_autosmelt", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, KEY_CATEGORY);

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientSetup::registerScreens);
        modBus.addListener(ClientSetup::registerKeyMappings);
        NeoForge.EVENT_BUS.register(ClientEvents.class);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.PRIMORDIAL_SMELTER.get(), PrimordialSmelterScreen::new);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_3X3);
        event.register(KEY_AUTOSMELT);
    }

    private ClientSetup() {}
}
