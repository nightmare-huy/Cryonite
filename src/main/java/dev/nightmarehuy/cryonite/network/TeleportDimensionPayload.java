package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: the player chose a dimension in the terminal and pressed "ТЕЛЕПОРТАЦИЯ".
 *
 * @param target the {@link ResourceLocation} of the target {@code ResourceKey<Level>}
 */
public record TeleportDimensionPayload(ResourceLocation target) implements CustomPacketPayload {

    public static final Type<TeleportDimensionPayload> TYPE = new Type<>(Cryonite.id("teleport_dimension"));

    public static final StreamCodec<ByteBuf, TeleportDimensionPayload> STREAM_CODEC =
            StreamCodec.composite(ResourceLocation.STREAM_CODEC, TeleportDimensionPayload::target,
                    TeleportDimensionPayload::new);

    @Override
    public Type<TeleportDimensionPayload> type() {
        return TYPE;
    }

    public static void handle(TeleportDimensionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ModNetworking.teleportPlayer(player, payload.target());
        }
    }
}
