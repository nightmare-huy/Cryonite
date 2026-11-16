package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: the player clicked a Dimension Terminal and asks for the list of all available
 * dimensions ({@code server.levelKeys()}).
 */
public record ListDimensionsRequestPayload() implements CustomPacketPayload {

    public static final Type<ListDimensionsRequestPayload> TYPE = new Type<>(Cryonite.id("list_dimensions_request"));

    public static final StreamCodec<ByteBuf, ListDimensionsRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new ListDimensionsRequestPayload());

    @Override
    public Type<ListDimensionsRequestPayload> type() {
        return TYPE;
    }

    public static void handle(ListDimensionsRequestPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ModNetworking.sendDimensionsList(player);
        }
    }
}
