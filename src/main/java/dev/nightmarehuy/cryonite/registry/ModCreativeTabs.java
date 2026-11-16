package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Cryonite.MODID);

    public static final Supplier<CreativeModeTab> MAIN = REGISTER.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cryonite"))
            .icon(() -> new ItemStack(ModItems.AETHER_INGOT.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.PRIMORDIAL_SMELTER);

                output.accept(ModItems.CRYONITE_ORE);
                output.accept(ModItems.RAW_CRYONITE);
                output.accept(ModItems.CRYONITE_CRYSTAL_DUST);
                output.accept(ModItems.CRYONITE_NUGGET);
                output.accept(ModItems.CRYONITE_INGOT);
                output.accept(ModItems.CRYONITE_BLOCK);
                output.accept(ModItems.RAW_CRYONITE_BLOCK);
                output.accept(ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE);

                output.accept(ModItems.INFERNITE_ORE);
                output.accept(ModItems.MOLTEN_INFERNITE_SHARD);
                output.accept(ModItems.COOLED_INFERNITE_BLANK);
                output.accept(ModItems.INFERNITE_NUGGET);
                output.accept(ModItems.INFERNITE_INGOT);
                output.accept(ModItems.INFERNITE_BLOCK);
                output.accept(ModItems.RAW_INFERNITE_BLOCK);
                output.accept(ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE);

                output.accept(ModItems.VOID_ORE);
                output.accept(ModItems.UNSTABLE_VOID_SHARD);
                output.accept(ModItems.STABILIZED_VOID_CLUSTER);
                output.accept(ModItems.VOID_NUGGET);
                output.accept(ModItems.VOID_INGOT);
                output.accept(ModItems.VOID_BLOCK);
                output.accept(ModItems.RAW_VOID_BLOCK);
                output.accept(ModItems.VOID_UPGRADE_SMITHING_TEMPLATE);

                output.accept(ModItems.INACTIVE_AETHER);
                output.accept(ModItems.AETHER_NUGGET);
                output.accept(ModItems.AETHER_INGOT);
                output.accept(ModItems.AETHER_BLOCK);

                for (GearTier tier : GearTier.values()) {
                    ModItems.gear(tier).all().forEach(output::accept);
                }

                // Only registered while Create is installed (compat/create).
                BuiltInRegistries.ITEM.getOptional(Cryonite.KINETIC_ENERGY_CONVERTER).ifPresent(output::accept);
            })
            .build());

    private ModCreativeTabs() {}
}
