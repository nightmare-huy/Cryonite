package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "3";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(ToggleToolModePayload.TYPE, ToggleToolModePayload.STREAM_CODEC, ToggleToolModePayload::handle);
        registrar.playToServer(UpdateKineticConverterPayload.TYPE, UpdateKineticConverterPayload.STREAM_CODEC, UpdateKineticConverterPayload::handle);
        registrar.playToServer(ListDimensionsRequestPayload.TYPE, ListDimensionsRequestPayload.STREAM_CODEC, ListDimensionsRequestPayload::handle);
        registrar.playToClient(ListDimensionsResponsePayload.TYPE, ListDimensionsResponsePayload.STREAM_CODEC, ListDimensionsResponsePayload::handle);
        registrar.playToServer(TeleportDimensionPayload.TYPE, TeleportDimensionPayload.STREAM_CODEC, TeleportDimensionPayload::handle);
    }

    /** Server -> client: the full list of dimensions the server knows, so the terminal can build its carousel. */
    public static void sendDimensionsList(ServerPlayer player) {
        ServerLevel source = player.serverLevel();
        RegistryAccess registryAccess = source.registryAccess();
        Registry<Level> levels = registryAccess.registryOrThrow(Registries.DIMENSION);
        List<ListDimensionsResponsePayload.Entry> entries = new ArrayList<>();
        for (Holder<Level> holder : levels) {
            ResourceKey<Level> key = levels.getKey(holder);
            if (key == null) {
                continue;
            }
            ResourceLocation location = key.location();
            String name = Component.translatable("dimension." + location.getNamespace() + "." + location.getPath())
                    .getString();
            if (name.equals("dimension." + location.getNamespace() + "." + location.getPath())) {
                name = location.getPath();
            }
            entries.add(new ListDimensionsResponsePayload.Entry(location, name));
        }
        player.connection.send(new ListDimensionsResponsePayload(entries));
    }

    /**
     * Server -> server: teleport the player to the target dimension, safely.
     * <p>
     * Finds a safe landing spot (top block with three air blocks above, or builds a 3×3 obsidian platform
     * on the surface), teleports the player there and, on the first visit of the mining dimension, places a
     * return terminal next to the player.
     */
    public static void teleportPlayer(ServerPlayer player, ResourceLocation target) {
        ServerLevel source = player.serverLevel();
        ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, target);
        ServerLevel targetLevel = source.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            player.displayClientMessage(Component.literal("Unknown dimension: " + target), true);
            return;
        }

        BlockPos spot = findSafeSpot(targetLevel);
        player.changeLevel(targetKey);
        player.teleportTo(spot.getX() + 0.5, spot.getY() + 1.0, spot.getZ() + 0.5);
        player.setYRot(player.getRandom().nextFloat() * 360.0F);

        if (target.equals(Cryonite.id("mining_dimension"))) {
            // First visit: leave a return terminal next to the player so they can always come back.
            targetLevel.setBlock(spot.above(2), Blocks.OBSIDIAN.defaultBlockState(), 3);
            player.displayClientMessage(Component.translatable("message.cryonite.terminal.teleported"), true);
        } else {
            player.displayClientMessage(Component.translatable("message.cryonite.terminal.teleported"), true);
        }
    }

    private static BlockPos findSafeSpot(ServerLevel level) {
        BlockPos spawn = level.getSharedSpawnPos();
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        for (int radius = 0; radius <= 64; radius += 4) {
            for (int dx = -radius; dx <= radius; dx += 4) {
                for (int dz = -radius; dz <= radius; dz += 4) {
                    BlockPos candidate = spawn.offset(dx, surfaceY, dz);
                    if (isSafeLanding(level, candidate)) {
                        return candidate;
                    }
                }
            }
        }
        BlockPos platform = spawn.offset(0, surfaceY + 1, 0);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(platform.offset(dx, 0, dz), Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }
        return platform;
    }

    private static boolean isSafeLanding(ServerLevel level, BlockPos pos) {
        BlockPos feet = pos;
        BlockPos head = pos.above(1);
        BlockPos aboveHead = pos.above(2);
        BlockPos below = pos.below();
        return level.getBlockState(feet).isAir()
                && level.getBlockState(head).isAir()
                && level.getBlockState(aboveHead).isAir()
                && !level.getBlockState(below).isAir()
                && !level.getBlockState(below).is(net.minecraft.tags.BlockTags.LAVA);
    }

    private ModNetworking() {}
}
