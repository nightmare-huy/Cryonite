package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlock;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Cryonite.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cube(ModBlocks.CRYONITE_ORE);
        cube(ModBlocks.INFERNITE_ORE);
        cube(ModBlocks.VOID_ORE);
        cube(ModBlocks.INFERNITE_BLOCK);
        cube(ModBlocks.CRYONITE_BLOCK);
        cube(ModBlocks.VOID_BLOCK);
        cube(ModBlocks.AETHER_BLOCK);
        cube(ModBlocks.RAW_CRYONITE_BLOCK);
        cube(ModBlocks.RAW_INFERNITE_BLOCK);
        cube(ModBlocks.RAW_VOID_BLOCK);
        smelter();
        kineticEnergyConverter();
    }

    /**
     * Create integration: the hand-made model points its shaft side up and is turned towards FACING.
     * The block only exists while Create is installed, which is the case for every dev run (see build.gradle).
     */
    private void kineticEnergyConverter() {
        Block converter = BuiltInRegistries.BLOCK.getOptional(Cryonite.KINETIC_ENERGY_CONVERTER)
                .orElseThrow(() -> new IllegalStateException("The Kinetic Energy Converter is not registered: run the data generators with Create"));
        ModelFile model = models().getExistingFile(modLoc("block/kinetic_energy_converter"));
        directionalBlock(converter, model);
        simpleBlockItem(converter, model);
    }

    private void cube(DeferredBlock<? extends Block> block) {
        simpleBlockWithItem(block.get(), cubeAll(block.get()));
    }

    /** Furnace-like model: one front face that changes with LIT, rotated by HORIZONTAL_FACING. */
    private void smelter() {
        ModelFile off = models().orientable("primordial_smelter",
                modLoc("block/primordial_smelter_side"), modLoc("block/primordial_smelter_front"), modLoc("block/primordial_smelter_top"));
        ModelFile on = models().orientable("primordial_smelter_on",
                modLoc("block/primordial_smelter_side"), modLoc("block/primordial_smelter_front_on"), modLoc("block/primordial_smelter_top"));

        getVariantBuilder(ModBlocks.PRIMORDIAL_SMELTER.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(PrimordialSmelterBlock.LIT) ? on : off)
                .rotationY(((int) state.getValue(PrimordialSmelterBlock.FACING).toYRot() + 180) % 360)
                .build());
        simpleBlockItem(ModBlocks.PRIMORDIAL_SMELTER.get(), off);
    }
}
