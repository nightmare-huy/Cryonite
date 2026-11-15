package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the player pressed one of the mode keys (auto-smelt / 3x3 mining). */
public record ToggleModePayload(ToolAbilities.Mode mode) implements CustomPacketPayload {
    public static final Type<ToggleModePayload> TYPE = new Type<>(Cryonite.id("toggle_mode"));

    public static final StreamCodec<FriendlyByteBuf, ToggleModePayload> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(ToolAbilities.Mode.class), ToggleModePayload::mode,
            ToggleModePayload::new);

    @Override
    public Type<ToggleModePayload> type() {
        return TYPE;
    }

    /** Runs on the server main thread. The server re-validates the held tool, the client is never trusted. */
    public static void handle(ToggleModePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ToolAbilities.toggle(player, payload.mode());
        }
    }
}
