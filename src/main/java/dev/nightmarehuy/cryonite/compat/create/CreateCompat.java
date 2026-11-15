package dev.nightmarehuy.cryonite.compat.create;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.compat.create.client.CreateClientCompat;
import dev.nightmarehuy.cryonite.item.HintedBlockItem;
import dev.nightmarehuy.cryonite.item.ItemHints;
import dev.nightmarehuy.cryonite.network.UpdateKineticConverterPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Everything of the mod that needs Create: the Kinetic Energy Converter, a generator that turns Forge Energy
 * into rotation of up to {@value KineticSpeedLimit#MAX_RPM} RPM.
 *
 * <p>Create is an optional dependency. The classes of this package extend Create's classes, so nothing outside
 * of it may reference them directly: the mod constructor calls {@link #init(IEventBus)} only when Create is
 * installed, the data generators, the creative tab and JEI find the block by its registry name
 * ({@link Cryonite#KINETIC_ENERGY_CONVERTER}).</p>
 */
public final class CreateCompat {
    private static final String CONVERTER = Cryonite.KINETIC_ENERGY_CONVERTER.getPath();

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Cryonite.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cryonite.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Cryonite.MODID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cryonite.MODID);

    /** Brass and netherite casing: a shaft on its front, Forge Energy through the other five sides. */
    public static final DeferredBlock<KineticEnergyConverterBlock> KINETIC_ENERGY_CONVERTER = BLOCKS.registerBlock(
            CONVERTER,
            KineticEnergyConverterBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_ORANGE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F, 1200.0F)
                    .sound(SoundType.NETHERITE_BLOCK));

    public static final DeferredItem<BlockItem> KINETIC_ENERGY_CONVERTER_ITEM = ITEMS.register(CONVERTER,
            () -> new HintedBlockItem(KINETIC_ENERGY_CONVERTER.get(), new Item.Properties().fireResistant(),
                    ItemHints.of("kinetic_energy_converter", ChatFormatting.GOLD).accent("purpose").step("energy", "speed", "gui")));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticEnergyConverterBlockEntity>> KINETIC_ENERGY_CONVERTER_ENTITY =
            BLOCK_ENTITIES.register(CONVERTER,
                    () -> BlockEntityType.Builder.of(KineticEnergyConverterBlockEntity::new, KINETIC_ENERGY_CONVERTER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<KineticConverterMenu>> KINETIC_CONVERTER_MENU =
            MENUS.register("kinetic_converter", () -> IMenuTypeExtension.create(KineticConverterMenu::new));

    /** Called by the mod constructor, and only when Create is installed. */
    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);

        modBus.addListener(CreateCompat::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(KineticSpeedLimit::onServerAboutToStart);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CreateClientCompat.init(modBus);
        }
    }

    /** Cables of any tech mod find the buffer on every side but the one with the shaft. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, KINETIC_ENERGY_CONVERTER_ENTITY.get(),
                KineticEnergyConverterBlockEntity::getEnergyStorage);
    }

    /**
     * Server side of {@link UpdateKineticConverterPayload}, on the server thread. The client is not trusted:
     * the player has to be able to reach the block and the speed is clamped to the allowed range.
     */
    public static void handleUpdate(UpdateKineticConverterPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        ServerLevel level = player.serverLevel();
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, 4.0)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof KineticEnergyConverterBlockEntity converter) {
            converter.applySettings(payload.targetRpm(), payload.reversed(), payload.enabled());
        }
    }

    private CreateCompat() {}
}
