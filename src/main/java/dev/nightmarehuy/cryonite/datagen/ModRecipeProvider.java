package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.recipe.PrimordialSmeltingRecipe;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class ModRecipeProvider extends RecipeProvider {
    /** Cooking times of the Primordial Smelter in ticks. */
    private static final int FAST = 200;
    private static final int NORMAL = 300;
    private static final int SLOW = 400;
    private static final int AETHER = 600;

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        primordialSmelter(output);
        cryonite(output);
        infernite(output);
        voidTier(output);
        aether(output);
        nuggetsAndBlocks(output);
        upgrades(output);
        multitools(output);
        kineticEnergyConverter(output);
    }

    // ------------------------------------------------------------------ the smelter itself

    private void primordialSmelter(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModItems.PRIMORDIAL_SMELTER)
                .pattern("OBO")
                .pattern("SFS")
                .pattern("OBO")
                .define('O', Items.OBSIDIAN)
                .define('B', Items.BLAZE_ROD)
                .define('S', Items.NETHERITE_SCRAP)
                .define('F', Items.BLAST_FURNACE)
                .unlockedBy("has_netherite_scrap", has(Items.NETHERITE_SCRAP))
                .save(output);
    }

    // ------------------------------------------------------------------ Create integration

    /**
     * Kinetic Energy Converter: the ingots of all four tiers in the corners of a Brass Casing,
     * energy enters on the left (Electron Tube), rotation leaves on the right (Shaft).
     * The recipe is only loaded while Create is installed.
     */
    private void kineticEnergyConverter(RecipeOutput output) {
        Item converter = BuiltInRegistries.ITEM.getOptional(Cryonite.KINETIC_ENERGY_CONVERTER)
                .orElseThrow(() -> new IllegalStateException("The Kinetic Energy Converter is not registered: run the data generators with Create"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, converter)
                .pattern("CRI")
                .pattern("EBS")
                .pattern("VPA")
                .define('C', ModItems.CRYONITE_INGOT)
                .define('I', ModItems.INFERNITE_INGOT)
                .define('V', ModItems.VOID_INGOT)
                .define('A', ModItems.AETHER_INGOT)
                .define('R', Items.REDSTONE_BLOCK)
                .define('E', createItem("electron_tube"))
                .define('B', createItem("brass_casing"))
                .define('S', createItem("shaft"))
                .define('P', createItem("precision_mechanism"))
                .unlockedBy("has_aether_ingot", has(ModItems.AETHER_INGOT))
                .save(output.withConditions(new ModLoadedCondition(Cryonite.CREATE_MODID)));
    }

    private static Item createItem(String path) {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath(Cryonite.CREATE_MODID, path))
                .orElseThrow(() -> new IllegalStateException("Create has no item named " + path));
    }

    // ------------------------------------------------------------------ tier 1

    private void cryonite(RecipeOutput output) {
        // Raw Cryonite only melts on Blaze Rods or Magma Blocks.
        smelting(output, "cryonite_crystal_dust",
                one(ModItems.RAW_CRYONITE), List.of(),
                Ingredient.of(Items.BLAZE_ROD, Items.MAGMA_BLOCK),
                new ItemStack(ModItems.CRYONITE_CRYSTAL_DUST.get()), FAST);

        // Ingot: crafting table ...
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRYONITE_INGOT)
                .requires(ModItems.CRYONITE_CRYSTAL_DUST)
                .requires(Ingredient.of(Items.NETHERITE_SCRAP, Items.ECHO_SHARD))
                .unlockedBy("has_cryonite_crystal_dust", has(ModItems.CRYONITE_CRYSTAL_DUST))
                .save(output);
        // ... or the smelter, with the scrap / shard as catalyst.
        smelting(output, "cryonite_ingot",
                one(ModItems.CRYONITE_CRYSTAL_DUST), List.of(new SizedIngredient(Ingredient.of(Items.NETHERITE_SCRAP, Items.ECHO_SHARD), 1)),
                Ingredient.of(Items.BLAZE_ROD, Items.MAGMA_BLOCK),
                new ItemStack(ModItems.CRYONITE_INGOT.get()), FAST);

        duplicateTemplate(output, GearTier.CRYONITE, Ingredient.of(Items.NETHERITE_INGOT), Ingredient.of(Items.DEEPSLATE, Items.COBBLED_DEEPSLATE));
    }

    // ------------------------------------------------------------------ tier 2

    private void infernite(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.COOLED_INFERNITE_BLANK)
                .requires(ModItems.MOLTEN_INFERNITE_SHARD)
                .requires(ModItems.CRYONITE_CRYSTAL_DUST)
                .unlockedBy("has_molten_infernite_shard", has(ModItems.MOLTEN_INFERNITE_SHARD))
                .save(output);

        smelting(output, "infernite_ingot",
                one(ModItems.COOLED_INFERNITE_BLANK), List.of(one(Items.GOLD_BLOCK), one(Items.GHAST_TEAR)),
                Ingredient.of(Items.BLAZE_POWDER, Items.BLAZE_ROD, Items.LAVA_BUCKET),
                new ItemStack(ModItems.INFERNITE_INGOT.get()), NORMAL);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.INFERNITE_BLOCK)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.INFERNITE_INGOT)
                .unlockedBy("has_infernite_ingot", has(ModItems.INFERNITE_INGOT))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INFERNITE_INGOT, 9)
                .requires(ModItems.INFERNITE_BLOCK)
                .unlockedBy("has_infernite_block", has(ModItems.INFERNITE_BLOCK))
                .save(output, Cryonite.id("infernite_ingot_from_block"));

        duplicateTemplate(output, GearTier.INFERNITE, Ingredient.of(ModItems.CRYONITE_INGOT), Ingredient.of(Items.BASALT));
    }

    // ------------------------------------------------------------------ tier 3

    private void voidTier(RecipeOutput output) {
        // Stabilization on the crafting table (the in-world way is a dragon's breath cloud).
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STABILIZED_VOID_CLUSTER, 8)
                .pattern("###")
                .pattern("#D#")
                .pattern("###")
                .define('#', ModItems.UNSTABLE_VOID_SHARD)
                .define('D', Items.DRAGON_BREATH)
                .unlockedBy("has_unstable_void_shard", has(ModItems.UNSTABLE_VOID_SHARD))
                .save(output);

        // Molten blank: crafting table ...
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MOLTEN_VOID_BLANK)
                .requires(ModItems.STABILIZED_VOID_CLUSTER, 4)
                .requires(ModItems.INFERNITE_INGOT, 4)
                .requires(Items.SHULKER_SHELL)
                .unlockedBy("has_stabilized_void_cluster", has(ModItems.STABILIZED_VOID_CLUSTER))
                .save(output);
        // ... or the smelter: 4 clusters + (4 ingots, 1 shell) as catalysts.
        smelting(output, "molten_void_blank",
                new SizedIngredient(Ingredient.of(ModItems.STABILIZED_VOID_CLUSTER), 4),
                List.of(new SizedIngredient(Ingredient.of(ModItems.INFERNITE_INGOT), 4), one(Items.SHULKER_SHELL)),
                Ingredient.of(Items.BLAZE_ROD, Items.LAVA_BUCKET),
                new ItemStack(ModItems.MOLTEN_VOID_BLANK.get()), SLOW);

        // Quenching on the crafting table (the in-world way is Crying Obsidian).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_INGOT)
                .requires(ModItems.MOLTEN_VOID_BLANK)
                .requires(Items.BLUE_ICE)
                .unlockedBy("has_molten_void_blank", has(ModItems.MOLTEN_VOID_BLANK))
                .save(output);

        duplicateTemplate(output, GearTier.VOID, Ingredient.of(ModItems.INFERNITE_INGOT), Ingredient.of(Items.DRAGON_BREATH));
    }

    // ------------------------------------------------------------------ tier 4

    private void aether(RecipeOutput output) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INACTIVE_AETHER)
                .requires(Items.NETHERITE_INGOT)
                .requires(ModItems.CRYONITE_INGOT)
                .requires(ModItems.INFERNITE_INGOT)
                .requires(ModItems.VOID_INGOT)
                .unlockedBy("has_void_ingot", has(ModItems.VOID_INGOT))
                .save(output);

        // Only a whole block of Infernite burns hot enough.
        smelting(output, "aether_ingot",
                one(ModItems.INACTIVE_AETHER), List.of(),
                Ingredient.of(ModItems.INFERNITE_BLOCK),
                new ItemStack(ModItems.AETHER_INGOT.get()), AETHER);

        // Aether gear has no template: classic shapes straight from the ingots.
        ModItems.GearSet gear = ModItems.gear(GearTier.AETHER);
        tool(output, gear.sword(), "#", "#", "/");
        tool(output, gear.pickaxe(), "###", " / ", " / ");
        tool(output, gear.axe(), "##", "#/", " /");
        tool(output, gear.shovel(), "#", "/", "/");
        tool(output, gear.hoe(), "##", " /", " /");
        armor(output, gear.helmet(), "###", "# #");
        armor(output, gear.chestplate(), "# #", "###", "###");
        armor(output, gear.leggings(), "###", "# #", "# #");
        armor(output, gear.boots(), "# #", "# #");
    }

    private void tool(RecipeOutput output, ItemLike result, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(result.asItem() == ModItems.gear(GearTier.AETHER).sword().get() ? RecipeCategory.COMBAT : RecipeCategory.TOOLS, result)
                .define('#', ModItems.AETHER_INGOT)
                .define('/', Items.STICK);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.unlockedBy("has_aether_ingot", has(ModItems.AETHER_INGOT)).save(output);
    }

    private void armor(RecipeOutput output, ItemLike result, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result).define('#', ModItems.AETHER_INGOT);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.unlockedBy("has_aether_ingot", has(ModItems.AETHER_INGOT)).save(output);
    }

    // ------------------------------------------------------------------ nuggets and storage blocks

    /** Full reversible cycle: 1 ingot <-> 9 nuggets, 9 ingots <-> 1 block, 9 raw <-> 1 raw block. */
    private void nuggetsAndBlocks(RecipeOutput output) {
        nuggetCycle(output, GearTier.CRYONITE, ModItems.CRYONITE_NUGGET, ModItems.CRYONITE_INGOT, ModItems.CRYONITE_BLOCK,
                ModItems.RAW_CRYONITE, ModItems.RAW_CRYONITE_BLOCK);
        nuggetCycle(output, GearTier.INFERNITE, ModItems.INFERNITE_NUGGET, ModItems.INFERNITE_INGOT, ModItems.INFERNITE_BLOCK,
                ModItems.COOLED_INFERNITE_BLANK, ModItems.RAW_INFERNITE_BLOCK);
        nuggetCycle(output, GearTier.VOID, ModItems.VOID_NUGGET, ModItems.VOID_INGOT, ModItems.VOID_BLOCK,
                ModItems.UNSTABLE_VOID_SHARD, ModItems.RAW_VOID_BLOCK);
        nuggetCycle(output, GearTier.AETHER, ModItems.AETHER_NUGGET, ModItems.AETHER_INGOT, ModItems.AETHER_BLOCK,
                null, null);
    }

    private void nuggetCycle(RecipeOutput output, GearTier tier, DeferredItem<Item> nugget, DeferredItem<Item> ingot,
            DeferredItem<BlockItem> block, DeferredItem<?> raw, DeferredItem<BlockItem> rawBlock) {
        String id = tier.id();
        // 9 nuggets -> 1 ingot
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', nugget)
                .unlockedBy("has_" + id + "_nugget", has(nugget))
                .save(output, Cryonite.id(id + "_ingot_from_nuggets"));
        // 1 ingot -> 9 nuggets
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingot)
                .unlockedBy("has_" + id + "_ingot", has(ingot))
                .save(output, Cryonite.id(id + "_nuggets_from_ingot"));
        // 9 ingots -> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ingot)
                .unlockedBy("has_" + id + "_ingot", has(ingot))
                .save(output, Cryonite.id(id + "_block_from_ingots"));
        // 1 block -> 9 ingots
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block)
                .unlockedBy("has_" + id + "_block", has(block))
                .save(output, Cryonite.id(id + "_ingots_from_block"));
        // 9 raw -> 1 raw block, and back
        if (raw != null && rawBlock != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, rawBlock)
                    .pattern("###")
                    .pattern("###")
                    .pattern("###")
                    .define('#', raw)
                    .unlockedBy("has_" + id + "_raw", has(raw))
                    .save(output, Cryonite.id(id + "_raw_block_from_raw"));
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, raw, 9)
                    .requires(rawBlock)
                    .unlockedBy("has_" + id + "_raw_block", has(rawBlock))
                    .save(output, Cryonite.id(id + "_raw_from_raw_block"));
        }
    }

    // ------------------------------------------------------------------ templates and smithing

    /** Vanilla template layout: 7 materials, the base block in the centre and the template on top give 2 templates. */
    private void duplicateTemplate(RecipeOutput output, GearTier tier, Ingredient material, Ingredient centre) {
        Item template = ModItems.upgradeTemplate(tier).get();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, template, 2)
                .pattern("#T#")
                .pattern("#C#")
                .pattern("###")
                .define('#', material)
                .define('C', centre)
                .define('T', template)
                .unlockedBy("has_template", has(template))
                .save(output, Cryonite.id(tier.id() + "_upgrade_smithing_template_duplication"));
    }

    /** Netherite -> Cryonite -> Infernite -> Void, one smithing recipe per piece of gear. */
    private void upgrades(RecipeOutput output) {
        List<Item> netherite = List.of(
                Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_SHOVEL, Items.NETHERITE_HOE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS);
        List<Item> previous = netherite;
        for (GearTier tier : List.of(GearTier.CRYONITE, GearTier.INFERNITE, GearTier.VOID)) {
            List<Item> current = ModItems.gear(tier).classic().stream().map(holder -> (Item) holder.get()).toList();
            Item template = ModItems.upgradeTemplate(tier).get();
            for (int i = 0; i < current.size(); i++) {
                Item result = current.get(i);
                boolean combat = i == 0 || i >= 5;
                SmithingTransformRecipeBuilder.smithing(
                                Ingredient.of(template), Ingredient.of(previous.get(i)), Ingredient.of(tier.ingot()),
                                combat ? RecipeCategory.COMBAT : RecipeCategory.TOOLS, result)
                        .unlocks("has_" + tier.id() + "_ingot", has(tier.ingot()))
                        .save(output, Cryonite.id(BuiltInRegistries.ITEM.getKey(result).getPath() + "_smithing"));
            }
            previous = current;
        }
    }

    // ------------------------------------------------------------------ multitools

    /**
     * Crafting table, every tier: axe, shovel and pickaxe of the tier over two of its ingots and a handle
     * (a stick or a blaze rod). Smithing table: Cryonite -> Infernite -> Void multitool with the usual
     * template and ingot, so an existing multitool can be carried up instead of being rebuilt.
     */
    private void multitools(RecipeOutput output) {
        for (GearTier tier : GearTier.values()) {
            ModItems.GearSet gear = ModItems.gear(tier);
            ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, gear.multitool())
                    .pattern("ASP")
                    .pattern("#/#")
                    .define('A', gear.axe())
                    .define('S', gear.shovel())
                    .define('P', gear.pickaxe())
                    .define('#', tier.ingot())
                    .define('/', Ingredient.of(Items.STICK, Items.BLAZE_ROD))
                    .unlockedBy("has_" + tier.id() + "_pickaxe", has(gear.pickaxe()))
                    .save(output);
        }

        Item previous = ModItems.gear(GearTier.CRYONITE).multitool().get();
        for (GearTier tier : List.of(GearTier.INFERNITE, GearTier.VOID)) {
            Item result = ModItems.gear(tier).multitool().get();
            SmithingTransformRecipeBuilder.smithing(
                            Ingredient.of(ModItems.upgradeTemplate(tier).get()), Ingredient.of(previous), Ingredient.of(tier.ingot()),
                            RecipeCategory.TOOLS, result)
                    .unlocks("has_" + tier.id() + "_ingot", has(tier.ingot()))
                    .save(output, Cryonite.id(BuiltInRegistries.ITEM.getKey(result).getPath() + "_smithing"));
            previous = result;
        }
    }

    // ------------------------------------------------------------------ helpers

    private static SizedIngredient one(ItemLike item) {
        return new SizedIngredient(Ingredient.of(item), 1);
    }

    private static void smelting(RecipeOutput output, String name, SizedIngredient input, List<SizedIngredient> catalysts, Ingredient fuel, ItemStack result, int cookingTime) {
        output.accept(Cryonite.id("primordial_smelting/" + name), new PrimordialSmeltingRecipe(input, catalysts, fuel, result, cookingTime), null);
    }
}
