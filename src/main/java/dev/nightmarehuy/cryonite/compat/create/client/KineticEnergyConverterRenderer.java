package dev.nightmarehuy.cryonite.compat.create.client;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.nightmarehuy.cryonite.compat.create.KineticEnergyConverterBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

/** The half shaft that sticks out of the front of the converter and turns with its network. */
public class KineticEnergyConverterRenderer extends KineticBlockEntityRenderer<KineticEnergyConverterBlockEntity> {
    public KineticEnergyConverterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(KineticEnergyConverterBlockEntity blockEntity, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
    }
}
