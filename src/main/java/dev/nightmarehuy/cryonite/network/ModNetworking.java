package dev.nightmarehuy.cryonite.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(ToggleModePayload.TYPE, ToggleModePayload.STREAM_CODEC, ToggleModePayload::handle);
    }

    private ModNetworking() {}
}
