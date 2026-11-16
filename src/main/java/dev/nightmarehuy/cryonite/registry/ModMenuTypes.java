package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.menu.DimensionTerminalMenu;
import dev.nightmarehuy.cryonite.menu.PrimordialSmelterMenu;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, Cryonite.MODID);

    public static final Supplier<MenuType<PrimordialSmelterMenu>> PRIMORDIAL_SMELTER = REGISTER.register("primordial_smelter",
            () -> new MenuType<>(PrimordialSmelterMenu::new, FeatureFlags.DEFAULT_FLAGS));

    /** Пространственный Терминал: пустой контейнер, только карусель измерений. */
    public static final Supplier<MenuType<DimensionTerminalMenu>> DIMENSION_TERMINAL = REGISTER.register("dimension_terminal",
            () -> new MenuType<>(DimensionTerminalMenu::new, FeatureFlags.DEFAULT_FLAGS));

    private ModMenuTypes() {}
}
