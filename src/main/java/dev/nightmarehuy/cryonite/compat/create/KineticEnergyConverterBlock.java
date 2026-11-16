package dev.nightmarehuy.cryonite.compat.create;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Kinetic Energy Converter: a generator of Create's rotation that runs on Forge Energy.
 *
 * <p>The shaft leaves the block on its {@link #FACING} side, the other five sides accept energy
 * ({@link KineticEnergyConverterBlockEntity#getEnergyStorage(Direction)}). A right click opens the control panel.</p>
 */
public class KineticEnergyConverterBlock extends DirectionalKineticBlock implements IBE<KineticEnergyConverterBlockEntity> {
    /** The casing is 12 pixels deep, a collar and the shaft fill the 4 pixels in front of it. */
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.values()) {
            SHAPES.put(facing, Shapes.or(
                    along(facing, 0, 16, 0, 12),      // casing
                    along(facing, 4, 12, 12, 14),     // collar
                    along(facing, 6, 10, 14, 16)));   // shaft
        }
    }

    public KineticEnergyConverterBlock(Properties properties) {
        super(properties);
    }

    /**
     * A box that spans {@code min..max} on both axes across {@code facing} and {@code from..to} along it,
     * measured from the back of the block towards its front.
     */
    private static VoxelShape along(Direction facing, double min, double max, double from, double to) {
        return switch (facing) {
            case UP -> Block.box(min, from, min, max, to, max);
            case DOWN -> Block.box(min, 16 - to, min, max, 16 - from, max);
            case SOUTH -> Block.box(min, min, from, max, max, to);
            case NORTH -> Block.box(min, min, 16 - to, max, max, 16 - from);
            case EAST -> Block.box(from, min, min, to, max, max);
            case WEST -> Block.box(16 - to, min, min, 16 - from, max, max);
        };
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** Next to a shaft the converter turns its own shaft towards it, otherwise the shaft faces the player. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if (preferred == null || (context.getPlayer() != null && context.getPlayer().isShiftKeyDown())) {
            return super.getStateForPlacement(context);
        }
        return defaultBlockState().setValue(FACING, preferred);
    }

    // ------------------------------------------------------------------ control panel

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof KineticEnergyConverterBlockEntity converter) {
            serverPlayer.openMenu(converter, converter::writeMenuData);
        }
        return InteractionResult.CONSUME;
    }

    /** The wrench keeps rotating the block, and a shaft or cogwheel clicked onto the shaft side is placed there. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IRotate
                && hit.getDirection() == state.getValue(FACING)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // ------------------------------------------------------------------ IRotate

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    /** A generator has no stress impact of its own. */
    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    // ------------------------------------------------------------------ IBE

    @Override
    public Class<KineticEnergyConverterBlockEntity> getBlockEntityClass() {
        return KineticEnergyConverterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends KineticEnergyConverterBlockEntity> getBlockEntityType() {
        return CreateCompat.KINETIC_ENERGY_CONVERTER_ENTITY.get();
    }
}
