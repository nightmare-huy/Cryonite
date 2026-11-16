package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.loot.AddItemLootModifier;
import dev.nightmarehuy.cryonite.loot.AutoSmeltLootModifier;
import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

public class ModGlobalLootModifierProvider extends GlobalLootModifierProvider {
    /** Chance per chest. Bastion Treasure: 5-10%, End City Treasure: 15-20% (middle of the requested ranges). */
    public static final float ANCIENT_CITY_CHANCE = 0.25F;
    public static final float BASTION_TREASURE_CHANCE = 0.075F;
    public static final float END_CITY_TREASURE_CHANCE = 0.175F;

    public ModGlobalLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, Cryonite.MODID);
    }

    @Override
    protected void start() {
        // The modifier decides itself (data component of the tool), so it needs no loot conditions.
        add("auto_smelt", new AutoSmeltLootModifier(new LootItemCondition[0]));

        template("cryonite_template_in_ancient_city", BuiltInLootTables.ANCIENT_CITY, ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE.get(), ANCIENT_CITY_CHANCE);
        template("infernite_template_in_bastion_treasure", BuiltInLootTables.BASTION_TREASURE, ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE.get(), BASTION_TREASURE_CHANCE);
        template("void_template_in_end_city_treasure", BuiltInLootTables.END_CITY_TREASURE, ModItems.VOID_UPGRADE_SMITHING_TEMPLATE.get(), END_CITY_TREASURE_CHANCE);
    }

    private void template(String name, ResourceKey<LootTable> table, Item template, float chance) {
        add(name, new AddItemLootModifier(
                new LootItemCondition[] {LootTableIdCondition.builder(table.location()).build()}, template, chance, 1));
    }
}
