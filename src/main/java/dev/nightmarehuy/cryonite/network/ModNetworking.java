package dev.nightmarehuy.cryonite.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "2";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(ToggleToolModePayload.TYPE, ToggleToolModePayload.STREAM_CODEC, ToggleToolModePayload::handle);
    }

    private ModNetworking() {}
}
