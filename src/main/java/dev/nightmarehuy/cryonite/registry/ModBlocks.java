package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(Cryonite.MODID);

    public static final DeferredBlock<PrimordialSmelterBlock> PRIMORDIAL_SMELTER = REGISTER.registerBlock(
            "primordial_smelter",
            PrimordialSmelterBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 15 : 0));

    /** Deepslate ore of tier 1, needs a Netherite pickaxe. */
    public static final DeferredBlock<DropExperienceBlock> CRYONITE_ORE = REGISTER.register("cryonite_ore",
            () -> new DropExperienceBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(6.0F, 9.0F)
                    .sound(SoundType.DEEPSLATE)));

    /** Basalt Deltas ore of tier 2, needs a Cryonite pickaxe. */
    public static final DeferredBlock<DropExperienceBlock> INFERNITE_ORE = REGISTER.register("infernite_ore",
            () -> new DropExperienceBlock(UniformInt.of(5, 10), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(8.0F, 12.0F)
                    .sound(SoundType.NETHER_ORE)
                    .lightLevel(state -> 7)));

    /** Outer End ore of tier 3, needs an Infernite pickaxe. Breaking it triggers a gravity anomaly. */
    public static final DeferredBlock<DropExperienceBlock> VOID_ORE = REGISTER.register("void_ore",
            () -> new DropExperienceBlock(UniformInt.of(8, 14), BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(12.0F, 30.0F)
                    .sound(SoundType.STONE)));

    /** Storage block of Infernite and the only fuel hot enough to awaken Aether. */
    public static final DeferredBlock<Block> INFERNITE_BLOCK = REGISTER.registerSimpleBlock("infernite_block",
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(12.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .lightLevel(state -> 9));

    private ModBlocks() {}
}
