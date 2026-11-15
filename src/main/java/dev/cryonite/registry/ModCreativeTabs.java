package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cryonite.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cryonite"))
            .icon(() -> ModItems.AETHER_INGOT.get().getDefaultInstance())
            .displayItems((parameters, output) -> ModItems.ITEMS.getEntries().forEach(output::accept))
            .build());
    private ModCreativeTabs() {}
}
