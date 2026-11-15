package dev.nightmarehuy.cryonite.compat.jei;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Virtual recipe that tells where something is obtained: an ore with its drop, or a smithing template.
 *
 * @param means        the required pickaxe (the weakest one first), or a chest for loot
 * @param surroundings the blocks to look for: the stone the ore sits in, or a landmark of the structure
 * @param result       the drop of the ore, or the template
 * @param silkTouch    the ore block itself (it only drops with Silk Touch), empty for loot
 * @param description  dimension, biome, height and warnings
 */
public record AcquisitionJeiRecipe(ResourceLocation id, List<ItemStack> means, List<ItemStack> surroundings, ItemStack result, ItemStack silkTouch,
        Component description) implements DescribedJeiRecipe {

    public static String descriptionKey(String name) {
        return "jei.cryonite.acquisition." + name;
    }

    /**
     * Ores follow {@code worldgen/ModWorldgen} and the block tags of {@code datagen/ModTagProviders},
     * templates follow {@code datagen/ModGlobalLootModifierProvider}.
     */
    public static List<AcquisitionJeiRecipe> all() {
        List<ItemStack> chest = JeiStacks.of(Items.CHEST);
        return List.of(
                ore("cryonite_ore", JeiStacks.netheritePickaxeOrBetter(), ModItems.RAW_CRYONITE, ModItems.CRYONITE_ORE,
                        Items.DEEPSLATE, Items.TUFF),
                ore("infernite_ore", JeiStacks.pickaxeOrBetter(GearTier.CRYONITE), ModItems.MOLTEN_INFERNITE_SHARD, ModItems.INFERNITE_ORE,
                        Items.BASALT, Items.MAGMA_BLOCK, Items.BLACKSTONE),
                ore("void_ore", JeiStacks.pickaxeOrBetter(GearTier.INFERNITE), ModItems.UNSTABLE_VOID_SHARD, ModItems.VOID_ORE,
                        Items.END_STONE),
                loot("cryonite_template", chest, ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE,
                        Items.ECHO_SHARD, Items.SCULK_SHRIEKER),
                loot("infernite_template", chest, ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE,
                        Items.GILDED_BLACKSTONE),
                loot("void_template", chest, ModItems.VOID_UPGRADE_SMITHING_TEMPLATE,
                        Items.DRAGON_HEAD, Items.PURPUR_BLOCK));
    }

    private static AcquisitionJeiRecipe ore(String name, List<ItemStack> pickaxes, ItemLike drop, ItemLike oreBlock, ItemLike... surroundings) {
        return new AcquisitionJeiRecipe(Cryonite.id("acquisition/" + name), pickaxes, JeiStacks.of(surroundings),
                new ItemStack(drop), new ItemStack(oreBlock), Component.translatable(descriptionKey(name)));
    }

    private static AcquisitionJeiRecipe loot(String name, List<ItemStack> chest, ItemLike template, ItemLike... landmarks) {
        return new AcquisitionJeiRecipe(Cryonite.id("acquisition/" + name), chest, JeiStacks.of(landmarks),
                new ItemStack(template), ItemStack.EMPTY, Component.translatable(descriptionKey(name)));
    }
}
