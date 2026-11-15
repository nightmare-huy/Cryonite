package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final class Blocks {
        // 1.21.1 has no numeric mining levels: every tier is expressed through a "needs" / "incorrect" tag pair.
        public static final TagKey<Block> NEEDS_CRYONITE_TOOL = tag("needs_cryonite_tool");
        public static final TagKey<Block> NEEDS_INFERNITE_TOOL = tag("needs_infernite_tool");
        public static final TagKey<Block> NEEDS_VOID_TOOL = tag("needs_void_tool");
        public static final TagKey<Block> NEEDS_AETHER_TOOL = tag("needs_aether_tool");

        public static final TagKey<Block> INCORRECT_FOR_CRYONITE_TOOL = tag("incorrect_for_cryonite_tool");
        public static final TagKey<Block> INCORRECT_FOR_INFERNITE_TOOL = tag("incorrect_for_infernite_tool");
        public static final TagKey<Block> INCORRECT_FOR_VOID_TOOL = tag("incorrect_for_void_tool");
        public static final TagKey<Block> INCORRECT_FOR_AETHER_TOOL = tag("incorrect_for_aether_tool");

        /** Blocks an Infernite cluster may grow into inside the Basalt Deltas. */
        public static final TagKey<Block> INFERNITE_CLUSTER_REPLACEABLES = tag("infernite_cluster_replaceables");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, Cryonite.id(name));
        }

        private Blocks() {}
    }

    public static final class Items {
        /** Carrying any of these keeps a Molten Infernite Shard from igniting its bearer. */
        public static final TagKey<Item> COOLANTS = tag("coolants");
        /** Wooden / stone items a Molten Infernite Shard may burn out of an inventory. */
        public static final TagKey<Item> BURNABLE_BY_INFERNITE = tag("burnable_by_infernite");
        /** Dropped items that ignore explosions, cacti, fire and every other non-void damage. */
        public static final TagKey<Item> INDESTRUCTIBLE = tag("indestructible");

        public static final TagKey<Item> CRYONITE_GEAR = tag("cryonite_gear");
        public static final TagKey<Item> INFERNITE_GEAR = tag("infernite_gear");
        public static final TagKey<Item> VOID_GEAR = tag("void_gear");
        public static final TagKey<Item> AETHER_GEAR = tag("aether_gear");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, Cryonite.id(name));
        }

        private Items() {}
    }

    public static final class EntityTypes {
        /** Extra targets of the Cryonite weapon bonus (anything fire immune counts automatically). */
        public static final TagKey<EntityType<?>> FIRE_CREATURES = tag("fire_creatures");
        /** Ice and water mobs hit harder by Infernite weapons. */
        public static final TagKey<EntityType<?>> FROST_AND_WATER_CREATURES = tag("frost_and_water_creatures");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Cryonite.id(name));
        }

        private EntityTypes() {}
    }

    public static final class Biomes {
        /** Deep Dark + cold overworld biomes: dense Cryonite veins. */
        public static final TagKey<Biome> RICH_IN_CRYONITE = tag("rich_in_cryonite");
        /** Outer End islands (everything except the central dragon island). */
        public static final TagKey<Biome> OUTER_END_ISLANDS = tag("outer_end_islands");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, Cryonite.id(name));
        }

        private Biomes() {}
    }

    private ModTags() {}
}
