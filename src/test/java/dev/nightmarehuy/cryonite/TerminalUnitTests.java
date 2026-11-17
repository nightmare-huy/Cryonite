package dev.nightmarehuy.cryonite;

import dev.nightmarehuy.cryonite.block.DimensionTerminalBlock;
import dev.nightmarehuy.cryonite.network.ListDimensionsResponsePayload;
import dev.nightmarehuy.cryonite.network.TeleportDimensionPayload;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Dimension Terminal: block registration, menu/screen registration,
 * and network payload (de)serialization. These run headless on every CI build
 * ({@code ./gradlew test}) and must pass before a release is published.
 */
public class TerminalUnitTests {

    @Test
    public void dimensionTerminalBlockIsRegistered() {
        Block block = ModBlocks.DIMENSION_TERMINAL.get();
        assertNotNull(block, "DIMENSION_TERMINAL block must be registered");
        assertInstanceOf(DimensionTerminalBlock.class, block,
                "DIMENSION_TERMINAL must be a DimensionTerminalBlock (right-click opens the screen)");
    }

    @Test
    public void dimensionTerminalEmitsLight() {
        Block block = ModBlocks.DIMENSION_TERMINAL.get();
        BlockState state = block.defaultBlockState();
        int light = state.getLightLevel(null, null);
        assertEquals(12, light, "Dimension Terminal must emit light level 12");
    }

    @Test
    public void terminalMenuTypeIsRegistered() {
        assertNotNull(ModMenuTypes.DIMENSION_TERMINAL.get(),
                "DIMENSION_TERMINAL menu type must be registered");
    }

    @Test
    public void listDimensionsResponsePacketRoundTrip() throws Exception {
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath("cryonite", "mining_dimension");
        ListDimensionsResponsePayload.Entry entry =
                new ListDimensionsResponsePayload.Entry(key, "Mining Dimension");
        List<ListDimensionsResponsePayload.Entry> entries = List.of(entry);
        ListDimensionsResponsePayload payload = new ListDimensionsResponsePayload(entries);

        ByteBuf buf = Unpooled.buffer();
        try {
            ListDimensionsResponsePayload.STREAM_CODEC.encode(buf, payload);
            ListDimensionsResponsePayload decoded =
                    ListDimensionsResponsePayload.STREAM_CODEC.decode(buf);

            assertEquals(1, decoded.entries().size());
            assertEquals(key, decoded.entries().get(0).key());
            assertEquals("Mining Dimension", decoded.entries().get(0).name());
        } finally {
            buf.release();
        }
    }

    @Test
    public void teleportDimensionPayloadRoundTrip() throws Exception {
        ResourceLocation target = ResourceLocation.fromNamespaceAndPath("cryonite", "the_nether");
        TeleportDimensionPayload payload = new TeleportDimensionPayload(target);

        ByteBuf buf = Unpooled.buffer();
        try {
            TeleportDimensionPayload.STREAM_CODEC.encode(buf, payload);
            TeleportDimensionPayload decoded = TeleportDimensionPayload.STREAM_CODEC.decode(buf);

            assertEquals(target, decoded.target());
        } finally {
            buf.release();
        }
    }

    @Test
    public void dimensionTerminalIsNotAPlainBlock() {
        // Regression test: DIMENSION_TERMINAL must use DimensionTerminalBlock (with right-click logic),
        // not a plain Block that does nothing on right-click.
        Block block = ModBlocks.DIMENSION_TERMINAL.get();
        assertNotSame(Block.class, block.getClass(),
                "DIMENSION_TERMINAL must not be a plain Block; it must be DimensionTerminalBlock");
    }
}
