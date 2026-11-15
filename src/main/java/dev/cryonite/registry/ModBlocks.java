package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import net.minecraft.world.level.block.Block;
import dev.cryonite.block.PrimordialSmelterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Cryonite.MOD_ID);
    public static final DeferredBlock<Block> CRYONITE_ORE = BLOCKS.register("cryonite_ore", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> INFERNITE_ORE = BLOCKS.register("infernite_ore", () -> new Block(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).requiresCorrectToolForDrops().sound(SoundType.BASALT)));
    public static final DeferredBlock<Block> VOID_ORE = BLOCKS.register("void_ore", () -> new Block(BlockBehaviour.Properties.of().strength(7.0F, 9.0F).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> INFERNITE_BLOCK = BLOCKS.register("infernite_block", () -> new Block(BlockBehaviour.Properties.of().strength(6.0F, 12.0F).requiresCorrectToolForDrops().lightLevel(state -> 8)));
    public static final DeferredBlock<Block> PRIMORDIAL_SMELTER = BLOCKS.register("primordial_smelter", () -> new PrimordialSmelterBlock(BlockBehaviour.Properties.of().strength(4.0F, 6.0F).requiresCorrectToolForDrops().lightLevel(state -> state.getValue(PrimordialSmelterBlock.LIT) ? 15 : 0)));
    private ModBlocks() {}
}
