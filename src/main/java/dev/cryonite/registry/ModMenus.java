package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import dev.cryonite.menu.PrimordialSmelterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IForgeMenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Cryonite.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<PrimordialSmelterMenu>> PRIMORDIAL_SMELTER = MENUS.register("primordial_smelter", () -> IForgeMenuType.create(PrimordialSmelterMenu::new));
    private ModMenus() {}
}
