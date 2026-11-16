package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.compat.create.CreateCompat;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: the settings chosen in the control panel of the Kinetic Energy Converter. Sent when a
 * button is pressed, the slider is moved or the text field changes.
 *
 * @param pos       position of the converter
 * @param targetRpm requested speed; the server clamps it to 0..8048
 * @param reversed  true for counter-clockwise rotation
 * @param enabled   power switch
 */
public record UpdateKineticConverterPayload(BlockPos pos, int targetRpm, boolean reversed, boolean enabled) implements CustomPacketPayload {
    public static final Type<UpdateKineticConverterPayload> TYPE = new Type<>(Cryonite.id("update_kinetic_converter"));

    public static final StreamCodec<ByteBuf, UpdateKineticConverterPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateKineticConverterPayload::pos,
            ByteBufCodecs.VAR_INT, UpdateKineticConverterPayload::targetRpm,
            ByteBufCodecs.BOOL, UpdateKineticConverterPayload::reversed,
            ByteBufCodecs.BOOL, UpdateKineticConverterPayload::enabled,
            UpdateKineticConverterPayload::new);

    @Override
    public Type<UpdateKineticConverterPayload> type() {
        return TYPE;
    }

    /**
     * Runs on the server main thread. The converter only exists while Create is installed, and only then the
     * classes of {@code compat/create} may be touched: {@link CreateCompat#handleUpdate} validates the request,
     * clamps the speed, updates the block entity and has the kinetic network rebuilt.
     */
    public static void handle(UpdateKineticConverterPayload payload, IPayloadContext context) {
        if (Cryonite.isCreateLoaded()) {
            CreateCompat.handleUpdate(payload, context);
        }
    }
}
