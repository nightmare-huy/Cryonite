package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: the player pressed one of the tool mode keys.
 *
 * @param modeType {@link #MODE_3X3} (0) toggles 3x3 mining, {@link #MODE_AUTO_SMELT} (1) toggles auto-smelt
 */
public record ToggleToolModePayload(int modeType) implements CustomPacketPayload {
    public static final int MODE_3X3 = 0;
    public static final int MODE_AUTO_SMELT = 1;

    public static final Type<ToggleToolModePayload> TYPE = new Type<>(Cryonite.id("toggle_tool_mode"));

    public static final StreamCodec<ByteBuf, ToggleToolModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ToggleToolModePayload::modeType,
            ToggleToolModePayload::new);

    @Override
    public Type<ToggleToolModePayload> type() {
        return TYPE;
    }

    /**
     * Runs on the server main thread. The client only says which key was pressed: the server looks at the
     * item in the player's main hand itself, checks its tier and flips the data component on its own stack.
     */
    public static void handle(ToggleToolModePayload payload, IPayloadContext context) {
        ToolAbilities.Mode mode = ToolAbilities.Mode.byNetworkId(payload.modeType());
        if (mode != null && context.player() instanceof ServerPlayer player) {
            ToolAbilities.toggle(player, mode);
        }
    }
}
