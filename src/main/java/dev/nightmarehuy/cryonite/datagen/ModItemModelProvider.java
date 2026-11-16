package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.tier.GearTier;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Cryonite.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.RAW_CRYONITE.get());
        basicItem(ModItems.CRYONITE_CRYSTAL_DUST.get());
        basicItem(ModItems.CRYONITE_INGOT.get());
        basicItem(ModItems.CRYONITE_NUGGET.get());
        basicItem(ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE.get());

        basicItem(ModItems.MOLTEN_INFERNITE_SHARD.get());
        basicItem(ModItems.INFERNITE_NUGGET.get());
        basicItem(ModItems.COOLED_INFERNITE_BLANK.get());
        basicItem(ModItems.INFERNITE_INGOT.get());
        basicItem(ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE.get());

        basicItem(ModItems.UNSTABLE_VOID_SHARD.get());
        basicItem(ModItems.STABILIZED_VOID_CLUSTER.get());
        basicItem(ModItems.MOLTEN_VOID_BLANK.get());
        basicItem(ModItems.VOID_INGOT.get());
        basicItem(ModItems.VOID_NUGGET.get());
        basicItem(ModItems.VOID_UPGRADE_SMITHING_TEMPLATE.get());

        basicItem(ModItems.INACTIVE_AETHER.get());
        basicItem(ModItems.AETHER_NUGGET.get());
        basicItem(ModItems.AETHER_INGOT.get());

        for (GearTier tier : GearTier.values()) {
            ModItems.GearSet gear = ModItems.gear(tier);
            gear.handheld().forEach(item -> handheldItem(item.get()));
            gear.armor().forEach(item -> basicItem(item.get()));
        }
    }
}
