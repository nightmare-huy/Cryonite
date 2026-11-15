package dev.cryonite;

import dev.cryonite.registry.ModBlocks;
import dev.cryonite.registry.ModBlockEntities;
import dev.cryonite.registry.ModMenus;
import dev.cryonite.registry.ModComponents;
import dev.cryonite.registry.ModItems;
import dev.cryonite.registry.ModAttachments;
import dev.cryonite.registry.ModCreativeTabs;
import dev.cryonite.recipe.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Cryonite.MOD_ID)
public final class Cryonite {
    public static final String MOD_ID = "cryonite";

    public Cryonite(IEventBus bus) {
        ModComponents.REGISTER.register(bus);
        ModAttachments.TYPES.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlockEntities.TYPES.register(bus);
        ModMenus.MENUS.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModRecipes.SERIALIZERS.register(bus);
        ModRecipes.TYPES.register(bus);
    }
}
