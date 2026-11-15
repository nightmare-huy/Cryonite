package dev.nightmarehuy.cryonite;

import com.mojang.logging.LogUtils;
import dev.nightmarehuy.cryonite.client.ClientSetup;
import dev.nightmarehuy.cryonite.compat.create.CreateCompat;
import dev.nightmarehuy.cryonite.datagen.DataGenerators;
import dev.nightmarehuy.cryonite.event.EventHandler;
import dev.nightmarehuy.cryonite.network.ModNetworking;
import dev.nightmarehuy.cryonite.registry.ModArmorMaterials;
import dev.nightmarehuy.cryonite.registry.ModAttachments;
import dev.nightmarehuy.cryonite.registry.ModBlockEntities;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModCreativeTabs;
import dev.nightmarehuy.cryonite.registry.ModDataComponents;
import dev.nightmarehuy.cryonite.registry.ModFeatures;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.registry.ModLootModifiers;
import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import dev.nightmarehuy.cryonite.registry.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * Cryonite: four cascading endgame tiers (Cryonite, Infernite, Void, Aether) forged in the Primordial Smelter.
 */
@Mod(Cryonite.MODID)
public final class Cryonite {
    public static final String MODID = "cryonite";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** Mod id of Create. It is optional: everything that touches its classes lives in {@code compat/create}. */
    public static final String CREATE_MODID = "create";
    /** Registry name of the Kinetic Energy Converter, which only exists while Create is installed. */
    public static final ResourceLocation KINETIC_ENERGY_CONVERTER = id("kinetic_energy_converter");

    public Cryonite(IEventBus modBus) {
        // Step 1: base registries (components and attachments first, they are referenced by everything else).
        ModDataComponents.REGISTER.register(modBus);
        ModAttachments.REGISTER.register(modBus);
        ModArmorMaterials.REGISTER.register(modBus);

        // Step 2/3: content.
        ModBlocks.REGISTER.register(modBus);
        ModItems.REGISTER.register(modBus);
        ModBlockEntities.REGISTER.register(modBus);
        ModMenuTypes.REGISTER.register(modBus);
        ModRecipes.TYPES.register(modBus);
        ModRecipes.SERIALIZERS.register(modBus);
        ModLootModifiers.REGISTER.register(modBus);
        ModFeatures.REGISTER.register(modBus);
        ModCreativeTabs.REGISTER.register(modBus);

        modBus.addListener(ModNetworking::register);
        modBus.addListener(ModBlockEntities::registerCapabilities);
        modBus.addListener(DataGenerators::gatherData);

        // Step 4: one handler class drives every cascading ability.
        NeoForge.EVENT_BUS.register(EventHandler.class);

        // Screens, key mappings and render hooks exist on the physical client only.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.init(modBus);
        }

        // Create integration: the FE -> rotation converter. The class is only touched (and loaded) with Create present.
        if (isCreateLoaded()) {
            CreateCompat.init(modBus);
        }
    }

    public static boolean isCreateLoaded() {
        return ModList.get().isLoaded(CREATE_MODID);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
