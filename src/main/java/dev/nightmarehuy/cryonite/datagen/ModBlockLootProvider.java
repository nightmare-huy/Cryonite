package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

/** Ores drop their raw material (Fortune and Silk Touch behave like on vanilla ores), the rest drops itself. */
public class ModBlockLootProvider extends BlockLootSubProvider {
    public ModBlockLootProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.PRIMORDIAL_SMELTER.get());
        dropSelf(ModBlocks.INFERNITE_BLOCK.get());
        dropSelf(ModBlocks.CRYONITE_BLOCK.get());
        dropSelf(ModBlocks.VOID_BLOCK.get());
        dropSelf(ModBlocks.AETHER_BLOCK.get());
        dropSelf(ModBlocks.RAW_CRYONITE_BLOCK.get());
        dropSelf(ModBlocks.RAW_INFERNITE_BLOCK.get());
        dropSelf(ModBlocks.RAW_VOID_BLOCK.get());
        add(ModBlocks.CRYONITE_ORE.get(), block -> createOreDrop(block, ModItems.RAW_CRYONITE.get()));
        add(ModBlocks.INFERNITE_ORE.get(), block -> createOreDrop(block, ModItems.MOLTEN_INFERNITE_SHARD.get()));
        add(ModBlocks.VOID_ORE.get(), block -> createOreDrop(block, ModItems.UNSTABLE_VOID_SHARD.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.REGISTER.getEntries().stream().map(Holder::value).map(Block.class::cast).toList();
    }
}
