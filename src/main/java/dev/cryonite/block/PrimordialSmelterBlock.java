package dev.cryonite.block;

import dev.cryonite.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

public class PrimordialSmelterBlock extends BaseEntityBlock {
    public static final MapCodec<PrimordialSmelterBlock> CODEC = simpleCodec(PrimordialSmelterBlock::new);
    @Override public MapCodec<PrimordialSmelterBlock> codec() { return CODEC; }
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public PrimordialSmelterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PrimordialSmelterBlockEntity(pos, state); }
    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof PrimordialSmelterBlockEntity smelter) {
            serverPlayer.openMenu(smelter, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.PRIMORDIAL_SMELTER.get(), PrimordialSmelterBlockEntity::serverTick);
    }
}
