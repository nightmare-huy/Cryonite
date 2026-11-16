package dev.nightmarehuy.cryonite.block;

import dev.nightmarehuy.cryonite.network.ModNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Пространственный Терминал — высокотехнологичный блок со светящимся экраном (light level 12).
 * <p>
 * При нажатии ПКМ запрашивает с сервера список всех доступных измерений и открывает на клиенте
 * {@code DimensionTerminalScreen} с каруселью Cover Flow. Выбор измерения и кнопка "ТЕЛЕПОРТАЦИЯ"
 * отправляют на сервер {@link TeleportDimensionPayload}, и сервер безопасно телепортирует игрока.
 */
public class DimensionTerminalBlock extends Block {

    public static final IntegerProperty ACTIVE = IntegerProperty.create("active", 0, 2);

    public DimensionTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ACTIVE, 0);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        ModNetworking.sendDimensionsList(serverPlayer);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected int getLightLevel(BlockState state, BlockGetter level, BlockPos pos) {
        return 12;
    }

    public static Component teleportingMessage() {
        return Component.translatable("message.cryonite.terminal.teleporting");
    }
}
