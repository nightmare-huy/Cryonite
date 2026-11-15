package dev.cryonite.registry;

import dev.cryonite.Cryonite;
import dev.cryonite.item.RawCryoniteItem;
import dev.cryonite.item.MoltenInferniteShardItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Cryonite.MOD_ID);
    private static DeferredItem<Item> item(String id) { return ITEMS.registerSimpleItem(id, new Item.Properties()); }
    private static DeferredItem<Item> indestructible(String id) { return ITEMS.register(id, () -> new Item(new Item.Properties().fireResistant().stacksTo(1).component(net.minecraft.core.component.DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE).component(ModComponents.AUTO_SMELT.get(), false).component(ModComponents.AREA_MINING.get(), false))); }

    public static final DeferredItem<Item> RAW_CRYONITE = ITEMS.register("raw_cryonite", () -> new RawCryoniteItem(new Item.Properties()));
    public static final DeferredItem<Item> CRYONITE_CRYSTAL_DUST = item("cryonite_crystal_dust");
    public static final DeferredItem<Item> CRYONITE_INGOT = item("cryonite_ingot");
    public static final DeferredItem<Item> MOLTEN_INFERNITE_SHARD = ITEMS.register("molten_infernite_shard", () -> new MoltenInferniteShardItem(new Item.Properties().fireResistant()));
    public static final DeferredItem<Item> COOLED_INFERNITE_BLANK = item("cooled_infernite_blank");
    public static final DeferredItem<Item> INFERNITE_INGOT = item("infernite_ingot");
    public static final DeferredItem<Item> UNSTABLE_VOID_SHARD = item("unstable_void_shard");
    public static final DeferredItem<Item> STABILIZED_VOID_CLUSTER = item("stabilized_void_cluster");
    public static final DeferredItem<Item> MOLTEN_VOID_BLANK = item("molten_void_blank");
    public static final DeferredItem<Item> VOID_INGOT = item("void_ingot");
    public static final DeferredItem<Item> INACTIVE_AETHER = item("inactive_aether");
    public static final DeferredItem<Item> AETHER_INGOT = item("aether_ingot");
    public static final DeferredItem<Item> CRYONITE_ORE_ITEM = ITEMS.registerSimpleBlockItem("cryonite_ore", ModBlocks.CRYONITE_ORE);
    public static final DeferredItem<Item> INFERNITE_ORE_ITEM = ITEMS.registerSimpleBlockItem("infernite_ore", ModBlocks.INFERNITE_ORE);
    public static final DeferredItem<Item> VOID_ORE_ITEM = ITEMS.registerSimpleBlockItem("void_ore", ModBlocks.VOID_ORE);
    public static final DeferredItem<Item> INFERNITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("infernite_block", ModBlocks.INFERNITE_BLOCK);
    public static final DeferredItem<Item> PRIMORDIAL_SMELTER_ITEM = ITEMS.registerSimpleBlockItem("primordial_smelter", ModBlocks.PRIMORDIAL_SMELTER);
    public static final DeferredItem<Item> CRYONITE_TEMPLATE = item("cryonite_upgrade_smithing_template");
    public static final DeferredItem<Item> INFERNITE_TEMPLATE = item("infernite_upgrade_smithing_template");
    public static final DeferredItem<Item> VOID_TEMPLATE = item("void_upgrade_smithing_template");

    // Equipment identifiers are reserved as individual items. Tier behavior is centralized in TierAbilityHandler.
    public static final DeferredItem<Item> CRYONITE_SWORD = indestructible("cryonite_sword");
    public static final DeferredItem<Item> INFERNITE_SWORD = indestructible("infernite_sword");
    public static final DeferredItem<Item> VOID_SWORD = indestructible("void_sword");
    public static final DeferredItem<Item> AETHER_SWORD = indestructible("aether_sword");
    public static final DeferredItem<Item> CRYONITE_PICKAXE = indestructible("cryonite_pickaxe");
    public static final DeferredItem<Item> INFERNITE_PICKAXE = indestructible("infernite_pickaxe");
    public static final DeferredItem<Item> VOID_PICKAXE = indestructible("void_pickaxe");
    public static final DeferredItem<Item> AETHER_PICKAXE = indestructible("aether_pickaxe");
    public static final DeferredItem<Item> CRYONITE_AXE = indestructible("cryonite_axe");
    public static final DeferredItem<Item> INFERNITE_AXE = indestructible("infernite_axe");
    public static final DeferredItem<Item> VOID_AXE = indestructible("void_axe");
    public static final DeferredItem<Item> AETHER_AXE = indestructible("aether_axe");
    public static final DeferredItem<Item> CRYONITE_SHOVEL = indestructible("cryonite_shovel");
    public static final DeferredItem<Item> INFERNITE_SHOVEL = indestructible("infernite_shovel");
    public static final DeferredItem<Item> VOID_SHOVEL = indestructible("void_shovel");
    public static final DeferredItem<Item> AETHER_SHOVEL = indestructible("aether_shovel");
    public static final DeferredItem<Item> CRYONITE_HOE = indestructible("cryonite_hoe");
    public static final DeferredItem<Item> INFERNITE_HOE = indestructible("infernite_hoe");
    public static final DeferredItem<Item> VOID_HOE = indestructible("void_hoe");
    public static final DeferredItem<Item> AETHER_HOE = indestructible("aether_hoe");
    public static final DeferredItem<Item> CRYONITE_HELMET = indestructible("cryonite_helmet");
    public static final DeferredItem<Item> CRYONITE_CHESTPLATE = indestructible("cryonite_chestplate");
    public static final DeferredItem<Item> CRYONITE_LEGGINGS = indestructible("cryonite_leggings");
    public static final DeferredItem<Item> CRYONITE_BOOTS = indestructible("cryonite_boots");
    public static final DeferredItem<Item> INFERNITE_HELMET = indestructible("infernite_helmet");
    public static final DeferredItem<Item> INFERNITE_CHESTPLATE = indestructible("infernite_chestplate");
    public static final DeferredItem<Item> INFERNITE_LEGGINGS = indestructible("infernite_leggings");
    public static final DeferredItem<Item> INFERNITE_BOOTS = indestructible("infernite_boots");
    public static final DeferredItem<Item> VOID_HELMET = indestructible("void_helmet");
    public static final DeferredItem<Item> VOID_CHESTPLATE = indestructible("void_chestplate");
    public static final DeferredItem<Item> VOID_LEGGINGS = indestructible("void_leggings");
    public static final DeferredItem<Item> VOID_BOOTS = indestructible("void_boots");
    public static final DeferredItem<Item> AETHER_HELMET = indestructible("aether_helmet");
    public static final DeferredItem<Item> AETHER_CHESTPLATE = indestructible("aether_chestplate");
    public static final DeferredItem<Item> AETHER_LEGGINGS = indestructible("aether_leggings");
    public static final DeferredItem<Item> AETHER_BOOTS = indestructible("aether_boots");

    private ModItems() {}
}
