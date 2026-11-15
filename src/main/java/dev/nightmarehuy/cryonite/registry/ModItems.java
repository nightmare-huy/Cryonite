package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.item.HintedBlockItem;
import dev.nightmarehuy.cryonite.item.HintedItem;
import dev.nightmarehuy.cryonite.item.ItemHints;
import dev.nightmarehuy.cryonite.item.MoltenInferniteShardItem;
import dev.nightmarehuy.cryonite.item.MoltenVoidBlankItem;
import dev.nightmarehuy.cryonite.item.RawCryoniteItem;
import dev.nightmarehuy.cryonite.item.TieredArmorItem;
import dev.nightmarehuy.cryonite.item.TieredAxeItem;
import dev.nightmarehuy.cryonite.item.TieredHoeItem;
import dev.nightmarehuy.cryonite.item.TieredMultitoolItem;
import dev.nightmarehuy.cryonite.item.TieredPickaxeItem;
import dev.nightmarehuy.cryonite.item.TieredShovelItem;
import dev.nightmarehuy.cryonite.item.TieredSwordItem;
import dev.nightmarehuy.cryonite.item.UnstableVoidShardItem;
import dev.nightmarehuy.cryonite.item.UpgradeTemplateItem;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(Cryonite.MODID);

    // ------------------------------------------------------------------ blocks
    public static final DeferredItem<BlockItem> PRIMORDIAL_SMELTER = hintedBlock(ModBlocks.PRIMORDIAL_SMELTER,
            ItemHints.of("primordial_smelter", ChatFormatting.GOLD).accent("purpose").step("slots", "fuel"));
    public static final DeferredItem<BlockItem> CRYONITE_ORE = hintedBlock(ModBlocks.CRYONITE_ORE,
            ItemHints.of("cryonite_ore", ChatFormatting.AQUA).accent("source", "tool").step("biomes"));
    public static final DeferredItem<BlockItem> INFERNITE_ORE = hintedBlock(ModBlocks.INFERNITE_ORE,
            ItemHints.of("infernite_ore", ChatFormatting.GOLD).accent("source", "tool").step("shell"));
    public static final DeferredItem<BlockItem> VOID_ORE = hintedBlock(ModBlocks.VOID_ORE,
            ItemHints.of("void_ore", ChatFormatting.DARK_PURPLE).accent("source", "tool", "danger"));
    public static final DeferredItem<BlockItem> INFERNITE_BLOCK = hintedBlock(ModBlocks.INFERNITE_BLOCK,
            ItemHints.of("infernite_block", ChatFormatting.GOLD).accent("source").step("next"));

    // ------------------------------------------------------------------ tier 1: Cryonite
    public static final DeferredItem<RawCryoniteItem> RAW_CRYONITE = REGISTER.registerItem("raw_cryonite", RawCryoniteItem::new, material());
    public static final DeferredItem<Item> CRYONITE_CRYSTAL_DUST = hinted("cryonite_crystal_dust", material(),
            ItemHints.of("cryonite_crystal_dust", ChatFormatting.AQUA).accent("source").step("next", "also"));
    public static final DeferredItem<Item> CRYONITE_INGOT = hinted("cryonite_ingot", material().rarity(Rarity.RARE),
            ItemHints.of("cryonite_ingot", ChatFormatting.AQUA).accent("source").step("next", "also"));
    public static final DeferredItem<UpgradeTemplateItem> CRYONITE_UPGRADE_SMITHING_TEMPLATE =
            REGISTER.register("cryonite_upgrade_smithing_template", () -> new UpgradeTemplateItem(GearTier.CRYONITE));

    // ------------------------------------------------------------------ tier 2: Infernite
    public static final DeferredItem<MoltenInferniteShardItem> MOLTEN_INFERNITE_SHARD = REGISTER.registerItem("molten_infernite_shard", MoltenInferniteShardItem::new, material());
    public static final DeferredItem<Item> COOLED_INFERNITE_BLANK = hinted("cooled_infernite_blank", material(),
            ItemHints.of("cooled_infernite_blank", ChatFormatting.GOLD).accent("source").step("next", "fuel"));
    public static final DeferredItem<Item> INFERNITE_INGOT = hinted("infernite_ingot", material().rarity(Rarity.UNCOMMON),
            ItemHints.of("infernite_ingot", ChatFormatting.GOLD).accent("source").step("next", "also"));
    public static final DeferredItem<UpgradeTemplateItem> INFERNITE_UPGRADE_SMITHING_TEMPLATE =
            REGISTER.register("infernite_upgrade_smithing_template", () -> new UpgradeTemplateItem(GearTier.INFERNITE));

    // ------------------------------------------------------------------ tier 3: Void
    public static final DeferredItem<UnstableVoidShardItem> UNSTABLE_VOID_SHARD = REGISTER.registerItem("unstable_void_shard", UnstableVoidShardItem::new, material());
    public static final DeferredItem<Item> STABILIZED_VOID_CLUSTER = hinted("stabilized_void_cluster", material(),
            ItemHints.of("stabilized_void_cluster", ChatFormatting.DARK_PURPLE).accent("source").step("next", "where"));
    public static final DeferredItem<MoltenVoidBlankItem> MOLTEN_VOID_BLANK = REGISTER.registerItem("molten_void_blank", MoltenVoidBlankItem::new, material());
    public static final DeferredItem<Item> VOID_INGOT = hinted("void_ingot", material().rarity(Rarity.EPIC),
            ItemHints.of("void_ingot", ChatFormatting.DARK_PURPLE).accent("source").step("next", "also"));
    public static final DeferredItem<UpgradeTemplateItem> VOID_UPGRADE_SMITHING_TEMPLATE =
            REGISTER.register("void_upgrade_smithing_template", () -> new UpgradeTemplateItem(GearTier.VOID));

    // ------------------------------------------------------------------ tier 4: Aether
    public static final DeferredItem<Item> INACTIVE_AETHER = hinted("inactive_aether", material().rarity(Rarity.UNCOMMON),
            ItemHints.of("inactive_aether", ChatFormatting.GOLD).accent("source").step("next"));
    public static final DeferredItem<Item> AETHER_INGOT = hinted("aether_ingot", material().rarity(Rarity.EPIC),
            ItemHints.of("aether_ingot", ChatFormatting.GOLD).accent("source").step("next"));

    // ------------------------------------------------------------------ equipment of all four tiers
    private static final Map<GearTier, GearSet> GEAR = new EnumMap<>(GearTier.class);

    static {
        for (GearTier tier : GearTier.values()) {
            GEAR.put(tier, registerGear(tier));
        }
    }

    public static GearSet gear(GearTier tier) {
        return GEAR.get(tier);
    }

    public static DeferredItem<UpgradeTemplateItem> upgradeTemplate(GearTier tier) {
        return switch (tier) {
            case CRYONITE -> CRYONITE_UPGRADE_SMITHING_TEMPLATE;
            case INFERNITE -> INFERNITE_UPGRADE_SMITHING_TEMPLATE;
            case VOID -> VOID_UPGRADE_SMITHING_TEMPLATE;
            case AETHER -> throw new IllegalArgumentException("Aether gear is crafted, not upgraded");
        };
    }

    /** A plain material with a tooltip that tells where it comes from and what to do with it next. */
    private static DeferredItem<Item> hinted(String name, Item.Properties properties, ItemHints hints) {
        return REGISTER.register(name, () -> new HintedItem(properties, hints));
    }

    /** The item of a block, with the same kind of tooltip. */
    private static DeferredItem<BlockItem> hintedBlock(DeferredBlock<? extends Block> block, ItemHints hints) {
        return REGISTER.register(block.getId().getPath(), () -> new HintedBlockItem(block.get(), material(), hints));
    }

    /** Every material of the mod survives fire and lava like Netherite does. */
    private static Item.Properties material() {
        return new Item.Properties().fireResistant();
    }

    /** Tools, weapons and armor of all four tiers: unbreakable and fire resistant. */
    private static Item.Properties gearProperties(GearTier tier) {
        return new Item.Properties()
                .fireResistant()
                .rarity(tier.rarity())
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true));
    }

    /**
     * Converts the FINAL tooltip values into the modifiers vanilla expects:
     * tooltip damage = 1.0 (hand) + tier bonus + modifier, tooltip speed = 4.0 (hand) + modifier.
     */
    private static float damageModifier(Tier tier, float finalDamage) {
        return finalDamage - 1.0F - tier.getAttackDamageBonus();
    }

    private static float speedModifier(float finalSpeed) {
        return finalSpeed - 4.0F;
    }

    private static ItemAttributeModifiers diggerAttributes(Tier tier, float finalDamage, float finalSpeed) {
        return DiggerItem.createAttributes(tier, damageModifier(tier, finalDamage), speedModifier(finalSpeed));
    }

    private static GearSet registerGear(GearTier tier) {
        String prefix = tier.id() + "_";
        Tier toolTier = ModTiers.get(tier);
        Holder<ArmorMaterial> armor = ModArmorMaterials.get(tier);
        return new GearSet(
                REGISTER.register(prefix + "sword", () -> new TieredSwordItem(tier, toolTier, gearProperties(tier)
                        .attributes(SwordItem.createAttributes(toolTier, damageModifier(toolTier, tier.swordDamage()), speedModifier(GearTier.SWORD_SPEED))))),
                REGISTER.register(prefix + "pickaxe", () -> new TieredPickaxeItem(tier, toolTier, gearProperties(tier)
                        .attributes(diggerAttributes(toolTier, tier.pickaxeDamage(), GearTier.PICKAXE_SPEED)))),
                REGISTER.register(prefix + "axe", () -> new TieredAxeItem(tier, toolTier, gearProperties(tier)
                        .attributes(diggerAttributes(toolTier, tier.axeDamage(), GearTier.AXE_SPEED)))),
                REGISTER.register(prefix + "shovel", () -> new TieredShovelItem(tier, toolTier, gearProperties(tier)
                        .attributes(diggerAttributes(toolTier, tier.shovelDamage(), GearTier.SHOVEL_SPEED)))),
                REGISTER.register(prefix + "hoe", () -> new TieredHoeItem(tier, toolTier, gearProperties(tier)
                        .attributes(diggerAttributes(toolTier, GearTier.HOE_DAMAGE, GearTier.HOE_SPEED)))),
                REGISTER.register(prefix + "multitool", () -> new TieredMultitoolItem(tier, toolTier, gearProperties(tier)
                        .attributes(diggerAttributes(toolTier, tier.multitoolDamage(), GearTier.MULTITOOL_SPEED)))),
                registerArmor(tier, armor, "helmet", ArmorItem.Type.HELMET),
                registerArmor(tier, armor, "chestplate", ArmorItem.Type.CHESTPLATE),
                registerArmor(tier, armor, "leggings", ArmorItem.Type.LEGGINGS),
                registerArmor(tier, armor, "boots", ArmorItem.Type.BOOTS));
    }

    private static DeferredItem<TieredArmorItem> registerArmor(GearTier tier, Holder<ArmorMaterial> material, String name, ArmorItem.Type type) {
        // A max damage value is still required: vanilla only lets items with one be enchanted.
        return REGISTER.register(tier.id() + "_" + name, () -> new TieredArmorItem(tier, material, type,
                gearProperties(tier).durability(type.getDurability(tier.armorDurabilityMultiplier()))));
    }

    public record GearSet(
            DeferredItem<TieredSwordItem> sword,
            DeferredItem<TieredPickaxeItem> pickaxe,
            DeferredItem<TieredAxeItem> axe,
            DeferredItem<TieredShovelItem> shovel,
            DeferredItem<TieredHoeItem> hoe,
            DeferredItem<TieredMultitoolItem> multitool,
            DeferredItem<TieredArmorItem> helmet,
            DeferredItem<TieredArmorItem> chestplate,
            DeferredItem<TieredArmorItem> leggings,
            DeferredItem<TieredArmorItem> boots) {

        public List<DeferredItem<? extends Item>> handheld() {
            return List.of(sword, pickaxe, axe, shovel, hoe, multitool);
        }

        public List<DeferredItem<? extends Item>> armor() {
            return List.of(helmet, chestplate, leggings, boots);
        }

        /** The nine pieces that have a Netherite counterpart, in the order of the smithing upgrade chain. */
        public List<DeferredItem<? extends Item>> classic() {
            return List.of(sword, pickaxe, axe, shovel, hoe, helmet, chestplate, leggings, boots);
        }

        public List<DeferredItem<? extends Item>> all() {
            return List.of(sword, pickaxe, axe, shovel, hoe, multitool, helmet, chestplate, leggings, boots);
        }
    }

    private ModItems() {}
}
