package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SmithingTemplateItem;

/**
 * Smithing template that lifts the gear of the previous tier to {@code tier}
 * (Netherite -> Cryonite -> Infernite -> Void).
 */
public class UpgradeTemplateItem extends SmithingTemplateItem {
    private static final List<ResourceLocation> EQUIPMENT_ICONS = List.of(
            icon("empty_armor_slot_helmet"),
            icon("empty_slot_sword"),
            icon("empty_armor_slot_chestplate"),
            icon("empty_slot_pickaxe"),
            icon("empty_armor_slot_leggings"),
            icon("empty_slot_axe"),
            icon("empty_armor_slot_boots"),
            icon("empty_slot_hoe"),
            icon("empty_slot_shovel"));
    private static final List<ResourceLocation> INGOT_ICONS = List.of(icon("empty_slot_ingot"));

    public UpgradeTemplateItem(GearTier tier) {
        super(
                Component.translatable(key(tier, "applies_to")).withStyle(ChatFormatting.BLUE),
                Component.translatable(key(tier, "ingredients")).withStyle(ChatFormatting.BLUE),
                Component.translatable(key(tier, "title")).withStyle(ChatFormatting.GRAY),
                Component.translatable(key(tier, "base_slot_description")),
                Component.translatable(key(tier, "additions_slot_description")),
                EQUIPMENT_ICONS,
                INGOT_ICONS);
    }

    public static String key(GearTier tier, String suffix) {
        return "upgrade.cryonite." + tier.id() + "_upgrade." + suffix;
    }

    private static ResourceLocation icon(String name) {
        return ResourceLocation.withDefaultNamespace("item/" + name);
    }

    /** Vanilla names every template "Smithing Template"; ours carry their own name. */
    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }
}
