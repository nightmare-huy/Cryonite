package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.client.DimensionTerminalClient;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server -> client: the full list of dimensions the server knows, one entry per dimension.
 * Used by {@code DimensionTerminalScreen} to build its Cover Flow carousel.
 *
 * @param entries pairs of {@code dimension key location -> display name}
 */
public record ListDimensionsResponsePayload(List<Entry> entries) implements CustomPacketPayload {

    public record Entry(ResourceLocation key, String name) {
        public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Entry::key,
                ByteBufCodecs.STRING_UTF8, Entry::name,
                Entry::new);
    }

    public static final Type<ListDimensionsResponsePayload> TYPE =
            new Type<>(Cryonite.id("list_dimensions_response"));

    public static final StreamCodec<ByteBuf, ListDimensionsResponsePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(ArrayList::new, Entry.STREAM_CODEC),
                    ListDimensionsResponsePayload::entries,
                    ListDimensionsResponsePayload::new);

    @Override
    public Type<ListDimensionsResponsePayload> type() {
        return TYPE;
    }

    public static void handle(ListDimensionsResponsePayload payload, IPayloadContext context) {
        // Клиентская часть: запомнить список измерений для экрана терминала.
        List<DimensionTerminalClient.DimensionEntry> entries = new ArrayList<>();
        for (Entry entry : payload.entries()) {
            entries.add(new DimensionTerminalClient.DimensionEntry(entry.key(), entry.name()));
        }
        DimensionTerminalClient.setDimensions(entries);
    }
}
