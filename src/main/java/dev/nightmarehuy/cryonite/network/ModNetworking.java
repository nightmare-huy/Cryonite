package dev.nightmarehuy.cryonite.network;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.DimensionTransition;
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
        for (Level level : levels) {
            ResourceLocation key = levels.getKey(level);
            if (key == null) {
                continue;
            }
            String name = Component.translatable("dimension." + key.getNamespace() + "." + key.getPath())
                    .getString();
            if (name.equals("dimension." + key.getNamespace() + "." + key.getPath())) {
                name = key.getPath();
            }
            entries.add(new ListDimensionsResponsePayload.Entry(key, name));
        }
        player.connection.send(new ListDimensionsResponsePayload(entries));
    }

    /**
     * Server -> server: teleport the player to the target dimension at the SAME X, Z coordinates.
     * <p>
     * Preserves the player's exact X and Z (no Nether scaling), finds a safe Y on the same column,
     * ensures the landing spot is safe, and places a return terminal next to the player.
     */
    public static void teleportPlayer(ServerPlayer player, ResourceLocation target) {
        ServerLevel source = player.serverLevel();
        ResourceKey<Level> targetKey = ResourceKey.create(Registries.DIMENSION, target);
        ServerLevel targetLevel = source.getServer().getLevel(targetKey);
        if (targetLevel == null) {
            player.displayClientMessage(Component.literal("Unknown dimension: " + target), true);
            return;
        }

        // 1. Сохраняем точные координаты игрока 1:1 (нет сброса в 0,0, нет деления на 8 для Незера)
        int targetX = player.getBlockX();
        int targetZ = player.getBlockZ();
        int currentY = player.getBlockY();
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        // 2. Принудительно прогружаем целевой чанк для корректных проверок блоков и Heightmap
        targetLevel.getChunk(targetX >> 4, targetZ >> 4);

        // 3. Проверяем, есть ли уже блок Пространственного Терминала рядом на тех же X, Z
        BlockPos existingTerminal = findExistingTerminal(targetLevel, targetX, targetZ);
        int targetY;
        if (existingTerminal != null) {
            // Терминал уже есть — телепортируем игрока прямо рядом с ним
            targetX = existingTerminal.getX() + 1;
            targetZ = existingTerminal.getZ();
            targetY = existingTerminal.getY();
        } else {
            // 4. Терминала нет — ищем безопасную высоту Y на тех же X, Z
            targetY = findSafeY(targetLevel, targetX, targetZ, currentY);

            // 5. Обеспечиваем безопасность точки прибытия
            ensureSafeLanding(targetLevel, targetX, targetY, targetZ);

            // 6. Ставим ответный терминал рядом для обратного телепорта
            placeReturnTerminal(targetLevel, targetX, targetY, targetZ);
        }

        // 7. Перемещаем игрока в целевое измерение и телепортируем на точные координаты
        player.changeDimension(new DimensionTransition(targetKey));
        player.teleportTo(targetX + 0.5, targetY, targetZ + 0.5);
        player.setYRot(yaw);
        player.setXRot(pitch);

        player.displayClientMessage(Component.translatable("message.cryonite.terminal.teleported"), true);
    }

    /**
     * Находит безопасную высоту Y на заданных X, Z в целевом измерении.
     * Для измерений с открытым небом (Overworld, Шахтёрское, Энд) — поверхность.
     * Для пустоты или потолка бедрока (Незер, пустота Энда) — clamping текущей Y игрока.
     */
    private static int findSafeY(ServerLevel level, int x, int z, int currentY) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int minY = level.getMinBuildHeight() + 8;
        int maxY = level.getMaxBuildHeight() - 8;

        boolean isSkyAccessible = targetIsSkyAccessible(level, x, z);

        if (isSkyAccessible && surfaceY > level.getMinBuildHeight()) {
            return surfaceY;
        }

        // Пустота или потолок бедрока: используем clamping текущей высоты игрока
        return Mth.clamp(currentY, minY, maxY);
    }

    private static boolean targetIsSkyAccessible(ServerLevel level, int x, int z) {
        // Проверяем, есть ли небо над точкой (открытое небо или пустота над ней)
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        for (int y = surfaceY + 1; y < level.getMaxBuildHeight(); y++) {
            if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) {
                return false; // Есть сплошной потолок над поверхностью (Незер)
            }
        }
        return true;
    }

    /**
     * Ищет блок Пространственного Терминала в радиусе 8 блоков по горизонтали
     * и по всей высоте колонки на заданных X, Z.
     */
    private static BlockPos findExistingTerminal(ServerLevel level, int centerX, int centerZ) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ);
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - 16);
        int maxY = Math.min(level.getMaxBuildHeight(), surfaceY + 16);

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(centerX + dx, y, centerZ + dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(ModBlocks.DIMENSION_TERMINAL.get())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Обеспечивает безопасность точки прибытия:
     * - Если под ногами воздух, лава или пустота — строит площадку 3×3 обсидиана под игроком.
     * - Если игрок оказался внутри блоков — расчищает пространство 3×3×3 воздуха
     *   вокруг точки прибытия (не ломая бедрок и блок терминала).
     */
    private static void ensureSafeLanding(ServerLevel level, int x, int y, int z) {
        BlockPos feet = new BlockPos(x, y, z);
        BlockPos below = feet.below();

        BlockState feetState = level.getBlockState(feet);
        BlockState belowState = level.getBlockState(below);

        // Если игрок в воздухе/лаве или под ним пустота/лава — строим площадку 3×3 под ногами
        if (feetState.isAir() || feetState.is(Blocks.LAVA) || belowState.isAir() || belowState.is(Blocks.LAVA)) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos platformPos = feet.offset(dx, -1, dz);
                    if (!level.getBlockState(platformPos).is(Blocks.BEDROCK)) {
                        level.setBlock(platformPos, Blocks.OBSIDIAN.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Очищаем пространство над головой (3×3 на уровнях Y+1 и Y+2)
        clearAirAbove(level, x, y + 1, z);
        clearAirAbove(level, x, y + 2, z);

        // Если игрок находится внутри блоков — расчищаем 3×3×3 вокруг точки прибытия
        if (!feetState.isAir() && !feetState.is(Blocks.LAVA)) {
            clearSpaceAround(level, x, y, z);
        }
    }

    private static void clearAirAbove(ServerLevel level, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = new BlockPos(x + dx, y, z + dz);
                BlockState state = level.getBlockState(pos);
                if (!state.is(Blocks.BEDROCK) && !state.is(ModBlocks.DIMENSION_TERMINAL.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private static void clearSpaceAround(ServerLevel level, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.BEDROCK) && !state.is(ModBlocks.DIMENSION_TERMINAL.get())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * Ставит ответный блок Пространственного Терминала рядом с игроком
     * на (targetX + 1, targetY, targetZ), если его там ещё нет.
     */
    private static void placeReturnTerminal(ServerLevel level, int x, int y, int z) {
        BlockPos terminalPos = new BlockPos(x + 1, y, z);
        if (!level.getBlockState(terminalPos).is(ModBlocks.DIMENSION_TERMINAL.get())) {
            level.setBlock(terminalPos, ModBlocks.DIMENSION_TERMINAL.get().defaultBlockState(), 3);
        }
    }

    private ModNetworking() {}
}
