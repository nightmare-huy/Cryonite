package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModDamageTypes;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.registry.ModTags;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** All tag providers of the mod. */
public final class ModTagProviders {

    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Cryonite.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                    ModBlocks.PRIMORDIAL_SMELTER.get(),
                    ModBlocks.CRYONITE_ORE.get(),
                    ModBlocks.INFERNITE_ORE.get(),
                    ModBlocks.VOID_ORE.get(),
                    ModBlocks.INFERNITE_BLOCK.get())
                    .addOptional(Cryonite.KINETIC_ENERGY_CONVERTER);

            // ---- what a multitool digs (this tag only feeds its vanilla Tool component)
            tag(ModTags.Blocks.MINEABLE_WITH_MULTITOOL)
                    .add(net.minecraft.world.level.block.Blocks.COBWEB)
                    .addTag(BlockTags.MINEABLE_WITH_PICKAXE)
                    .addTag(BlockTags.MINEABLE_WITH_AXE)
                    .addTag(BlockTags.MINEABLE_WITH_SHOVEL)
                    .addTag(BlockTags.MINEABLE_WITH_HOE)
                    .addTag(BlockTags.LEAVES);

            // ---- what each block needs
            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.PRIMORDIAL_SMELTER.get(), ModBlocks.INFERNITE_BLOCK.get())
                    .addOptional(Cryonite.KINETIC_ENERGY_CONVERTER);     // exists only with Create installed
            tag(Tags.Blocks.NEEDS_NETHERITE_TOOL).add(ModBlocks.CRYONITE_ORE.get());
            tag(ModTags.Blocks.NEEDS_CRYONITE_TOOL).add(ModBlocks.INFERNITE_ORE.get());
            tag(ModTags.Blocks.NEEDS_INFERNITE_TOOL).add(ModBlocks.VOID_ORE.get());
            tag(ModTags.Blocks.NEEDS_VOID_TOOL);
            tag(ModTags.Blocks.NEEDS_AETHER_TOOL);

            // ---- what each tier can NOT harvest: everything that needs a higher tier
            List<TagKey<Block>> needs = List.of(
                    ModTags.Blocks.NEEDS_CRYONITE_TOOL,
                    ModTags.Blocks.NEEDS_INFERNITE_TOOL,
                    ModTags.Blocks.NEEDS_VOID_TOOL,
                    ModTags.Blocks.NEEDS_AETHER_TOOL);
            List<TagKey<Block>> incorrect = List.of(
                    ModTags.Blocks.INCORRECT_FOR_CRYONITE_TOOL,
                    ModTags.Blocks.INCORRECT_FOR_INFERNITE_TOOL,
                    ModTags.Blocks.INCORRECT_FOR_VOID_TOOL,
                    ModTags.Blocks.INCORRECT_FOR_AETHER_TOOL);
            for (int tier = 0; tier < incorrect.size(); tier++) {
                var builder = tag(incorrect.get(tier));
                for (int higher = tier + 1; higher < needs.size(); higher++) {
                    builder.addTag(needs.get(higher));
                }
            }
            // Every vanilla tier (Netherite included) is below Cryonite and fails on all four.
            for (TagKey<Block> vanilla : List.of(
                    BlockTags.INCORRECT_FOR_WOODEN_TOOL,
                    BlockTags.INCORRECT_FOR_GOLD_TOOL,
                    BlockTags.INCORRECT_FOR_STONE_TOOL,
                    BlockTags.INCORRECT_FOR_IRON_TOOL,
                    BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
                    BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
                var builder = tag(vanilla);
                needs.forEach(builder::addTag);
            }

            tag(ModTags.Blocks.INFERNITE_CLUSTER_REPLACEABLES).add(
                    net.minecraft.world.level.block.Blocks.BASALT,
                    net.minecraft.world.level.block.Blocks.BLACKSTONE,
                    net.minecraft.world.level.block.Blocks.NETHERRACK,
                    net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);

            tag(Tags.Blocks.ORES).add(ModBlocks.CRYONITE_ORE.get(), ModBlocks.INFERNITE_ORE.get(), ModBlocks.VOID_ORE.get());
            tag(BlockTags.BEACON_BASE_BLOCKS).add(ModBlocks.INFERNITE_BLOCK.get());
            tag(BlockTags.DRAGON_IMMUNE).add(ModBlocks.VOID_ORE.get());
        }
    }

    public static class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
            super(output, lookup, blockTags, Cryonite.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Items.COOLANTS).add(
                    net.minecraft.world.item.Items.SNOWBALL,
                    net.minecraft.world.item.Items.ICE,
                    net.minecraft.world.item.Items.PACKED_ICE,
                    net.minecraft.world.item.Items.BLUE_ICE);

            tag(ModTags.Items.BURNABLE_BY_INFERNITE)
                    .addTag(ItemTags.PLANKS)
                    .addTag(ItemTags.LOGS)
                    .addTag(ItemTags.WOODEN_SLABS)
                    .addTag(ItemTags.WOODEN_STAIRS)
                    .addTag(ItemTags.WOODEN_FENCES)
                    .addTag(ItemTags.FENCE_GATES)
                    .addTag(ItemTags.WOODEN_DOORS)
                    .addTag(ItemTags.WOODEN_TRAPDOORS)
                    .addTag(ItemTags.WOODEN_BUTTONS)
                    .addTag(ItemTags.WOODEN_PRESSURE_PLATES)
                    .addTag(ItemTags.SIGNS)
                    .addTag(ItemTags.HANGING_SIGNS)
                    .addTag(ItemTags.BOATS)
                    .addTag(ItemTags.STONE_TOOL_MATERIALS)
                    .addTag(ItemTags.STONE_CRAFTING_MATERIALS)
                    .add(
                            net.minecraft.world.item.Items.STICK,
                            net.minecraft.world.item.Items.BOWL,
                            net.minecraft.world.item.Items.CRAFTING_TABLE,
                            net.minecraft.world.item.Items.CHEST,
                            net.minecraft.world.item.Items.BARREL,
                            net.minecraft.world.item.Items.LADDER,
                            net.minecraft.world.item.Items.WOODEN_SWORD,
                            net.minecraft.world.item.Items.WOODEN_PICKAXE,
                            net.minecraft.world.item.Items.WOODEN_AXE,
                            net.minecraft.world.item.Items.WOODEN_SHOVEL,
                            net.minecraft.world.item.Items.WOODEN_HOE,
                            net.minecraft.world.item.Items.STONE_SWORD,
                            net.minecraft.world.item.Items.STONE_PICKAXE,
                            net.minecraft.world.item.Items.STONE_AXE,
                            net.minecraft.world.item.Items.STONE_SHOVEL,
                            net.minecraft.world.item.Items.STONE_HOE,
                            net.minecraft.world.item.Items.STONE,
                            net.minecraft.world.item.Items.STONE_BRICKS,
                            net.minecraft.world.item.Items.STONE_SLAB,
                            net.minecraft.world.item.Items.STONE_STAIRS,
                            net.minecraft.world.item.Items.COBBLESTONE_SLAB,
                            net.minecraft.world.item.Items.COBBLESTONE_STAIRS,
                            net.minecraft.world.item.Items.COBBLESTONE_WALL,
                            net.minecraft.world.item.Items.STONE_BUTTON,
                            net.minecraft.world.item.Items.STONE_PRESSURE_PLATE,
                            net.minecraft.world.item.Items.FURNACE);

            var indestructible = tag(ModTags.Items.INDESTRUCTIBLE);
            for (GearTier tier : GearTier.values()) {
                ModItems.GearSet gear = ModItems.gear(tier);
                var tierTag = tag(tier.gearTag());
                gear.all().forEach(item -> tierTag.add(item.get()));
                indestructible.addTag(tier.gearTag());

                // Vanilla tags drive enchanting, mining and armor trims.
                tag(ItemTags.SWORDS).add(gear.sword().get());
                tag(ItemTags.PICKAXES).add(gear.pickaxe().get());
                tag(ItemTags.AXES).add(gear.axe().get());
                tag(ItemTags.SHOVELS).add(gear.shovel().get());
                tag(ItemTags.HOES).add(gear.hoe().get());
                // A multitool is all four tools at once: it takes every mining enchantment and, as an axe, Sharpness.
                tag(ModTags.Items.MULTITOOLS).add(gear.multitool().get());
                tag(ItemTags.PICKAXES).add(gear.multitool().get());
                tag(ItemTags.AXES).add(gear.multitool().get());
                tag(ItemTags.SHOVELS).add(gear.multitool().get());
                tag(ItemTags.HOES).add(gear.multitool().get());
                tag(ItemTags.HEAD_ARMOR).add(gear.helmet().get());
                tag(ItemTags.CHEST_ARMOR).add(gear.chestplate().get());
                tag(ItemTags.LEG_ARMOR).add(gear.leggings().get());
                tag(ItemTags.FOOT_ARMOR).add(gear.boots().get());
                gear.armor().forEach(item -> tag(ItemTags.TRIMMABLE_ARMOR).add(item.get()));
            }

            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(
                    ModItems.CRYONITE_INGOT.get(), ModItems.INFERNITE_INGOT.get(), ModItems.VOID_INGOT.get(), ModItems.AETHER_INGOT.get());
            tag(Tags.Items.INGOTS).add(
                    ModItems.CRYONITE_INGOT.get(), ModItems.INFERNITE_INGOT.get(), ModItems.VOID_INGOT.get(), ModItems.AETHER_INGOT.get());
            tag(Tags.Items.RAW_MATERIALS).add(ModItems.RAW_CRYONITE.get());
            tag(Tags.Items.DUSTS).add(ModItems.CRYONITE_CRYSTAL_DUST.get());
            tag(Tags.Items.ORES).add(ModItems.CRYONITE_ORE.get(), ModItems.INFERNITE_ORE.get(), ModItems.VOID_ORE.get());
            tag(Tags.Items.STORAGE_BLOCKS).add(ModItems.INFERNITE_BLOCK.get());
        }
    }

    public static class EntityTypes extends EntityTypeTagsProvider {
        public EntityTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Cryonite.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            // Cryonite weapons: +25% (every fire immune mob counts as well, see WeaponAbilities).
            tag(ModTags.EntityTypes.FIRE_CREATURES).add(EntityType.BLAZE, EntityType.MAGMA_CUBE, EntityType.GHAST, EntityType.STRIDER);
            // Infernite weapons: +25% (all undead count as well).
            tag(ModTags.EntityTypes.FROST_AND_WATER_CREATURES).add(
                    EntityType.STRAY, EntityType.SNOW_GOLEM, EntityType.POLAR_BEAR,
                    EntityType.DROWNED, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN,
                    EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.AXOLOTL, EntityType.TURTLE, EntityType.DOLPHIN,
                    EntityType.COD, EntityType.SALMON, EntityType.PUFFERFISH, EntityType.TROPICAL_FISH);
        }
    }

    public static class Biomes extends BiomeTagsProvider {
        public Biomes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Cryonite.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            tag(ModTags.Biomes.RICH_IN_CRYONITE)
                    .add(net.minecraft.world.level.biome.Biomes.DEEP_DARK)
                    .add(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS)
                    .add(net.minecraft.world.level.biome.Biomes.ICE_SPIKES)
                    .add(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA)
                    .add(net.minecraft.world.level.biome.Biomes.SNOWY_BEACH)
                    .add(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES)
                    .add(net.minecraft.world.level.biome.Biomes.GROVE)
                    .add(net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS)
                    .add(net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS)
                    .add(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER)
                    .add(net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN)
                    .add(net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN)
                    // cold biomes of other mods
                    .addOptionalTag(Tags.Biomes.IS_COLD_OVERWORLD);

            // Everything but the central island (minecraft:the_end).
            tag(ModTags.Biomes.OUTER_END_ISLANDS)
                    .add(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS)
                    .add(net.minecraft.world.level.biome.Biomes.END_MIDLANDS)
                    .add(net.minecraft.world.level.biome.Biomes.END_BARRENS)
                    .add(net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS);
        }
    }

    public static class DamageTypes extends DamageTypeTagsProvider {
        public DamageTypes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, Cryonite.MODID, files);
        }

        /** The Aether burst is "pure" damage: no armor, shield, enchantment or hit cooldown stops it. */
        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (var tag : List.of(
                    DamageTypeTags.BYPASSES_ARMOR,
                    DamageTypeTags.BYPASSES_SHIELD,
                    DamageTypeTags.BYPASSES_ENCHANTMENTS,
                    DamageTypeTags.BYPASSES_COOLDOWN,
                    DamageTypeTags.NO_KNOCKBACK)) {
                // Optional entry: the damage type is generated by the same data run and not part of the vanilla lookup.
                tag(tag).addOptional(ModDamageTypes.AETHER_BURST.location());
            }
        }
    }

    private ModTagProviders() {}
}
