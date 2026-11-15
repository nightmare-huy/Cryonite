package dev.nightmarehuy.cryonite.datagen;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.item.UpgradeTemplateItem;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.tier.GearTier;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** {@code en_us.json} and {@code ru_ru.json}. Both classes fill the same set of keys. */
public final class ModLanguageProvider {

    public static class English extends LanguageProvider {
        public English(PackOutput output) {
            super(output, Cryonite.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.cryonite", "Cryonite");
            add("container.cryonite.primordial_smelter", "Primordial Smelter");

            addBlock(ModBlocks.PRIMORDIAL_SMELTER, "Primordial Smelter");
            addBlock(ModBlocks.CRYONITE_ORE, "Cryonite Ore");
            addBlock(ModBlocks.INFERNITE_ORE, "Infernite Ore");
            addBlock(ModBlocks.VOID_ORE, "Void Ore");
            addBlock(ModBlocks.INFERNITE_BLOCK, "Block of Infernite");

            addItem(ModItems.RAW_CRYONITE, "Raw Cryonite");
            addItem(ModItems.CRYONITE_CRYSTAL_DUST, "Cryonite Crystal Dust");
            addItem(ModItems.CRYONITE_INGOT, "Cryonite Ingot");
            addItem(ModItems.MOLTEN_INFERNITE_SHARD, "Molten Infernite Shard");
            addItem(ModItems.COOLED_INFERNITE_BLANK, "Cooled Infernite Blank");
            addItem(ModItems.INFERNITE_INGOT, "Infernite Ingot");
            addItem(ModItems.UNSTABLE_VOID_SHARD, "Unstable Void Shard");
            addItem(ModItems.STABILIZED_VOID_CLUSTER, "Stabilized Void Cluster");
            addItem(ModItems.MOLTEN_VOID_BLANK, "Molten Void Blank");
            addItem(ModItems.VOID_INGOT, "Void Ingot");
            addItem(ModItems.INACTIVE_AETHER, "Inactive Aether");
            addItem(ModItems.AETHER_INGOT, "Aether Ingot");

            gear(GearTier.CRYONITE, "Cryonite");
            gear(GearTier.INFERNITE, "Infernite");
            gear(GearTier.VOID, "Void");
            gear(GearTier.AETHER, "Aether");

            template(GearTier.CRYONITE, "Cryonite", "Netherite Equipment", "Add a piece of Netherite armor, a weapon or a tool");
            template(GearTier.INFERNITE, "Infernite", "Cryonite Equipment", "Add a piece of Cryonite armor, a weapon or a tool");
            template(GearTier.VOID, "Void", "Infernite Equipment", "Add a piece of Infernite armor, a weapon or a tool");

            guide();
            add("tooltip.cryonite.hold_shift", "Hold Shift for abilities");
            add("tooltip.cryonite.full_set", "Full set:");
            add("tooltip.cryonite.on_hit", "On hit:");
            add("tooltip.cryonite.when_mining", "When mining:");

            add("tooltip.cryonite.ability.armor.cryonite", "Immune to freezing and Slowness, walks on powder snow, attackers are slowed and weakened");
            add("tooltip.cryonite.ability.armor.infernite", "Immune to fire, lava and Wither, swims and sees in lava, attackers are set ablaze");
            add("tooltip.cryonite.ability.armor.void", "Void rescue (2 min cooldown), no fall or wall damage, Endermen ignore your gaze");
            add("tooltip.cryonite.ability.armor.aether", "Creative flight, strong regeneration, immune to harmful effects and magic");
            add("tooltip.cryonite.ability.weapon.cryonite", "Freezes and slows, +25% damage to creatures of fire");
            add("tooltip.cryonite.ability.weapon.infernite", "Blue fire, +25% damage to undead, frost and water creatures and Cryonite armor");
            add("tooltip.cryonite.ability.weapon.void", "Ignores 30% of armor; sword and multitool: right-click to dash 7 blocks");
            add("tooltip.cryonite.ability.weapon.aether", "Half of the damage is pure magic (ignores armor and shields), heals 1 heart per hit");
            add("tooltip.cryonite.ability.tool.cryonite", "Breaks ice and snow instantly");
            add("tooltip.cryonite.ability.tool.infernite", "Auto-smelt, works with Fortune");
            add("tooltip.cryonite.ability.tool.void", "Telekinesis: drops and experience go straight to you");
            add("tooltip.cryonite.ability.tool.aether", "3×3 mining");
            add("tooltip.cryonite.mode.auto_smelt", "Auto-smelt: %s [Key: %s]");
            add("tooltip.cryonite.mode.auto_smelt.on", "ON");
            add("tooltip.cryonite.mode.auto_smelt.off", "OFF");
            add("tooltip.cryonite.mode.area_mining", "Mining mode: %s [Key: %s]");
            add("tooltip.cryonite.mode.area_mining.on", "3×3");
            add("tooltip.cryonite.mode.area_mining.off", "1×1");

            add("message.cryonite.auto_smelt", "Auto-smelt");
            add("message.cryonite.auto_smelt.on", "ENABLED");
            add("message.cryonite.auto_smelt.off", "DISABLED");
            add("message.cryonite.area_mining", "3×3 mining mode");
            add("message.cryonite.area_mining.on", "ENABLED");
            add("message.cryonite.area_mining.off", "DISABLED");
            add("message.cryonite.mode_state", "%s: %s");
            add("message.cryonite.mode_unsupported", "The held item does not support %s");
            add("message.cryonite.item_burned", "The Molten Infernite Shard burned %s!");
            add("message.cryonite.void_rescue", "The Void armor pulled you back");

            add("key.categories.cryonite", "Cryonite");
            add("key.cryonite.toggle_3x3", "Toggle 3×3 Mining Mode");
            add("key.cryonite.toggle_autosmelt", "Toggle Auto-smelt");

            add("gui.cryonite.slot.input", "Main ingredient");
            add("gui.cryonite.slot.catalyst", "Catalyst");
            add("gui.cryonite.slot.fuel", "Special fuel");

            // Create integration: the Kinetic Energy Converter (compat/create) and its control panel.
            add("block.cryonite.kinetic_energy_converter", "Kinetic Energy Converter");
            add("container.cryonite.kinetic_energy_converter", "Kinetic Energy Converter");
            add("gui.cryonite.kinetic_converter.target", "Target speed, 0–8048 RPM");
            add("gui.cryonite.kinetic_converter.apply", "Apply");
            add("gui.cryonite.kinetic_converter.slider", "Speed: %s RPM");
            add("gui.cryonite.kinetic_converter.preset.min", "MIN (0)");
            add("gui.cryonite.kinetic_converter.preset.max", "MAX (8048)");
            add("gui.cryonite.kinetic_converter.direction", "Direction: %s");
            add("gui.cryonite.kinetic_converter.direction.cw", "CW ⟳");
            add("gui.cryonite.kinetic_converter.direction.ccw", "CCW ⟲");
            add("gui.cryonite.kinetic_converter.power.on", "Power: ON");
            add("gui.cryonite.kinetic_converter.power.off", "Power: OFF");
            add("gui.cryonite.kinetic_converter.speed", "Speed: %s RPM");
            add("gui.cryonite.kinetic_converter.output", "Power output: %s SU");
            add("gui.cryonite.kinetic_converter.consumption", "Consumption: %s FE/t");
            add("gui.cryonite.kinetic_converter.energy", "Energy: %s / %s FE");
            add("gui.cryonite.kinetic_converter.energy.input", "Input: up to %s FE/t");
            add("gui.cryonite.kinetic_converter.status.running", "Running");
            add("gui.cryonite.kinetic_converter.status.off", "Switched off");
            add("gui.cryonite.kinetic_converter.status.idle", "Idle");
            add("gui.cryonite.kinetic_converter.status.no_energy", "No energy");
            add("gui.cryonite.kinetic_converter.status.overstressed", "Overstressed");

            add("death.attack.cryonite.aether_burst", "%1$s was erased by Aether");
            add("death.attack.cryonite.aether_burst.player", "%1$s was erased by the Aether of %2$s");
            add("death.attack.cryonite.aether_burst.item", "%1$s was erased by %2$s using %3$s");
        }

        /** Inventory hints and the JEI documentation; the Russian class fills the same keys. */
        private void guide() {
            // Inventory hints (item/ItemHints): accent lines describe the item, gray lines the next step.
            add("tooltip.cryonite.cryonite_ore.source", "Overworld: deepslate and tuff, Y -64 to 0");
            add("tooltip.cryonite.cryonite_ore.tool", "Pickaxe: Netherite or better");
            add("tooltip.cryonite.cryonite_ore.biomes", "More common in the Deep Dark and frozen biomes");
            add("tooltip.cryonite.raw_cryonite.source", "From Cryonite Ore (Netherite pickaxe)");
            add("tooltip.cryonite.raw_cryonite.danger", "Freezing cold: carrying it slows and freezes you");
            add("tooltip.cryonite.raw_cryonite.next", "Smelt it into dust in the Primordial Smelter");
            add("tooltip.cryonite.raw_cryonite.fuel", "Fuel: Blaze Rod or Magma Block");
            add("tooltip.cryonite.cryonite_crystal_dust.source", "Raw Cryonite out of the Primordial Smelter");
            add("tooltip.cryonite.cryonite_crystal_dust.next", "With Netherite Scrap or an Echo Shard: an ingot");
            add("tooltip.cryonite.cryonite_crystal_dust.also", "Cools a Molten Infernite Shard on the crafting table");
            add("tooltip.cryonite.cryonite_ingot.source", "Cryonite dust + Netherite Scrap or an Echo Shard");
            add("tooltip.cryonite.cryonite_ingot.next", "Smithing table: upgrades Netherite gear");
            add("tooltip.cryonite.cryonite_ingot.also", "Needed for Inactive Aether");
            add("tooltip.cryonite.cryonite_upgrade_smithing_template.source", "Ancient City chests (25% chance)");
            add("tooltip.cryonite.cryonite_upgrade_smithing_template.duplicate", "Duplicate with 7 Netherite Ingots + Deepslate");

            add("tooltip.cryonite.infernite_ore.source", "Nether: Basalt Deltas, Y 8 to 96");
            add("tooltip.cryonite.infernite_ore.tool", "Pickaxe: Cryonite or better");
            add("tooltip.cryonite.infernite_ore.shell", "Veins hide inside a shell of magma and blackstone");
            add("tooltip.cryonite.molten_infernite_shard.source", "From Infernite Ore (Cryonite pickaxe)");
            add("tooltip.cryonite.molten_infernite_shard.danger", "Scorching: sets you on fire, burns wooden and stone items");
            add("tooltip.cryonite.molten_infernite_shard.coolant", "Snowballs or ice in the inventory keep it in check");
            add("tooltip.cryonite.molten_infernite_shard.next", "Cool it in a Powder Snow Cauldron, on Blue Ice or with Cryonite dust");
            add("tooltip.cryonite.cooled_infernite_blank.source", "A quenched Molten Infernite Shard");
            add("tooltip.cryonite.cooled_infernite_blank.next", "Smelter: with a Block of Gold and a Ghast Tear: an ingot");
            add("tooltip.cryonite.cooled_infernite_blank.fuel", "Fuel: Blaze Powder, Blaze Rod or Lava Bucket");
            add("tooltip.cryonite.infernite_ingot.source", "A Cooled Blank out of the Primordial Smelter");
            add("tooltip.cryonite.infernite_ingot.next", "Smithing table: upgrades Cryonite gear");
            add("tooltip.cryonite.infernite_ingot.also", "Needed for the Void blank, Inactive Aether and the Block of Infernite");
            add("tooltip.cryonite.infernite_block.source", "Storage block: 9 Infernite Ingots");
            add("tooltip.cryonite.infernite_block.next", "The only fuel hot enough to awaken Inactive Aether");
            add("tooltip.cryonite.infernite_upgrade_smithing_template.source", "Bastion treasure chests (7.5% chance)");
            add("tooltip.cryonite.infernite_upgrade_smithing_template.duplicate", "Duplicate with 7 Cryonite Ingots + Basalt");

            add("tooltip.cryonite.void_ore.source", "The End: outer islands, Y 0 to 12");
            add("tooltip.cryonite.void_ore.tool", "Pickaxe: Infernite or better");
            add("tooltip.cryonite.void_ore.danger", "Danger: breaking it drags you down and blinds you for 3 s");
            add("tooltip.cryonite.unstable_void_shard.source", "From Void Ore (Infernite pickaxe)");
            add("tooltip.cryonite.unstable_void_shard.next", "Drop it into a cloud of Dragon's Breath");
            add("tooltip.cryonite.unstable_void_shard.craft", "or craft 8 shards around a bottle of Dragon's Breath");
            add("tooltip.cryonite.stabilized_void_cluster.source", "A Void shard tamed by Dragon's Breath");
            add("tooltip.cryonite.stabilized_void_cluster.next", "4 clusters + 4 Infernite Ingots + a Shulker Shell");
            add("tooltip.cryonite.stabilized_void_cluster.where", "Crafting table or the Smelter (Blaze Rod, Lava Bucket)");
            add("tooltip.cryonite.molten_void_blank.source", "An alloy of Void clusters, Infernite and a Shulker Shell");
            add("tooltip.cryonite.molten_void_blank.next", "Drop it onto Crying Obsidian for 5 seconds");
            add("tooltip.cryonite.molten_void_blank.craft", "or combine it with Blue Ice on the crafting table");
            add("tooltip.cryonite.void_ingot.source", "A Void blank quenched on Crying Obsidian");
            add("tooltip.cryonite.void_ingot.next", "Smithing table: upgrades Infernite gear");
            add("tooltip.cryonite.void_ingot.also", "Needed for Inactive Aether");
            add("tooltip.cryonite.void_upgrade_smithing_template.source", "End City treasure chests (17.5% chance)");
            add("tooltip.cryonite.void_upgrade_smithing_template.duplicate", "Duplicate with 7 Infernite Ingots + Dragon's Breath");

            add("tooltip.cryonite.inactive_aether.source", "Netherite, Cryonite, Infernite and Void ingots");
            add("tooltip.cryonite.inactive_aether.next", "Smelt it in the Primordial Smelter with a Block of Infernite as fuel");
            add("tooltip.cryonite.aether_ingot.source", "Aether awakened in the Primordial Smelter");
            add("tooltip.cryonite.aether_ingot.next", "Aether gear is crafted on the crafting table, no template");
            add("tooltip.cryonite.primordial_smelter.purpose", "Smelts the materials of all four tiers");
            add("tooltip.cryonite.primordial_smelter.slots", "Slots: input, up to two catalysts, fuel");
            add("tooltip.cryonite.primordial_smelter.fuel", "Every recipe burns its own fuel");
            add("tooltip.cryonite.kinetic_energy_converter.purpose", "Turns Forge Energy into rotation for Create");
            add("tooltip.cryonite.kinetic_energy_converter.energy", "Buffer: 10,000,000 FE, input up to 1,000,000 FE/t on five sides");
            add("tooltip.cryonite.kinetic_energy_converter.speed", "0–8048 RPM, 1024 SU and 4 FE/t per RPM");
            add("tooltip.cryonite.kinetic_energy_converter.gui", "Right-click to set speed and direction");

            // JEI (compat/jei): the two documentation categories.
            add("jei.cryonite.category.world_interaction", "World Processing (Quenching & Stabilizing)");
            add("jei.cryonite.world_interaction.powder_snow_cauldron", "Right-click a Powder Snow Cauldron with the molten shard. One layer of snow quenches up to 16 shards.");
            add("jei.cryonite.world_interaction.blue_ice", "Right-click a block of Blue Ice with the molten shard (the ice thaws into regular Ice). One block quenches up to 16 shards.");
            add("jei.cryonite.world_interaction.dragon_breath", "Drop the shards (Q) straight into a cloud of the Ender Dragon's breath during the fight.");
            add("jei.cryonite.world_interaction.crying_obsidian", "Drop the blank (Q) onto a block of Crying Obsidian and wait 5 seconds.");

            add("jei.cryonite.category.acquisition", "Ore Mining & Artifact Hunting");
            add("jei.cryonite.acquisition.silk_touch", "The ore itself drops with Silk Touch");
            add("jei.cryonite.acquisition.cryonite_ore", "Overworld: Deepslate and Tuff (Y -64 to 0), more common in the Deep Dark and frozen biomes. Freezes you while in the inventory!");
            add("jei.cryonite.acquisition.infernite_ore", "Nether: Basalt Deltas (Y 8 to 96), wrapped in magma and blackstone. Burns the player and melts wood and stone in the inventory unless ice or snowballs are carried!");
            add("jei.cryonite.acquisition.void_ore", "The End: outer islands, the lowest layer above the void (Y = 0..12). DANGER: breaking it drags you down into the void and blinds you for 3 seconds!");
            add("jei.cryonite.acquisition.cryonite_template", "Structure: Ancient City, 25% chance per chest. Duplicated on the crafting table with 7 Netherite Ingots and Deepslate.");
            add("jei.cryonite.acquisition.infernite_template", "Structure: treasure rooms of Piglin Bastions (low chance: 7.5% per chest). Duplicated with 7 Cryonite Ingots and Basalt.");
            add("jei.cryonite.acquisition.void_template", "Structure: End City treasure chests, End Ships included (17.5% chance per chest). Duplicated with 7 Infernite Ingots and Dragon's Breath.");

            // JEI info pages: what the categories cannot show.
            add("jei.cryonite.info.raw_cryonite", "Raw Cryonite radiates cold: while you carry it you are slowed and slowly freeze. Leather or Cryonite armor and the Nether keep you warm.\n\nSmelt it in the Primordial Smelter (fuel: Blaze Rod or Magma Block) into Cryonite Crystal Dust.");
            add("jei.cryonite.info.cryonite_crystal_dust", "Smelted from Raw Cryonite in the Primordial Smelter (fuel: Blaze Rod or Magma Block).\n\nDust + Netherite Scrap or an Echo Shard = Cryonite Ingot, either on the crafting table or in the Smelter (scrap or shard in a catalyst slot).\n\nOn the crafting table the dust also cools a Molten Infernite Shard.");
            add("jei.cryonite.info.cryonite_ingot", "Cryonite Crystal Dust + Netherite Scrap or an Echo Shard (crafting table or Primordial Smelter).\n\nSmithing table: Cryonite Upgrade template + a Netherite item + the ingot. 7 ingots duplicate the Infernite Upgrade template. Also needed for Inactive Aether.");
            add("jei.cryonite.info.molten_infernite_shard", "Third way: combine the shard with Cryonite Crystal Dust on the crafting table.\n\nSnowballs, Ice, Packed Ice or Blue Ice in the inventory stop the shard from burning you and your items.");
            add("jei.cryonite.info.cooled_infernite_blank", "A Molten Infernite Shard cooled in a Powder Snow Cauldron, on Blue Ice or with Cryonite Crystal Dust.\n\nPrimordial Smelter: the blank + a Block of Gold and a Ghast Tear (catalysts), fuel: Blaze Powder, Blaze Rod or Lava Bucket. The result is an Infernite Ingot.");
            add("jei.cryonite.info.infernite_ingot", "Smelted from a Cooled Infernite Blank in the Primordial Smelter (Block of Gold + Ghast Tear).\n\nSmithing table: Infernite Upgrade template + a Cryonite item + the ingot. 9 ingots make a Block of Infernite. Also needed for the Molten Void Blank, Inactive Aether and to duplicate the Void Upgrade template.");
            add("jei.cryonite.info.infernite_block", "9 Infernite Ingots on the crafting table (and back).\n\nThe only fuel of the Primordial Smelter that is hot enough to awaken Inactive Aether.");
            add("jei.cryonite.info.unstable_void_shard", "Without a dragon: 8 shards around a bottle of Dragon's Breath on the crafting table give 8 Stabilized Void Clusters.");
            add("jei.cryonite.info.stabilized_void_cluster", "An Unstable Void Shard stabilized in a cloud of Dragon's Breath or on the crafting table.\n\n4 clusters + 4 Infernite Ingots + a Shulker Shell = Molten Void Blank, either on the crafting table or in the Primordial Smelter (fuel: Blaze Rod or Lava Bucket).");
            add("jei.cryonite.info.molten_void_blank", "Made of 4 Stabilized Void Clusters, 4 Infernite Ingots and a Shulker Shell (crafting table or Primordial Smelter).");
            add("jei.cryonite.info.molten_void_blank.craft", "Without obsidian: combine the blank with Blue Ice on the crafting table.");
            add("jei.cryonite.info.void_ingot", "A Molten Void Blank quenched on Crying Obsidian or with Blue Ice.\n\nSmithing table: Void Upgrade template + an Infernite item + the ingot. Also needed for Inactive Aether.");
            add("jei.cryonite.info.inactive_aether", "Crafting table: Netherite Ingot + Cryonite Ingot + Infernite Ingot + Void Ingot.\n\nSmelt it in the Primordial Smelter with a Block of Infernite as fuel to get an Aether Ingot.");
            add("jei.cryonite.info.aether_ingot", "Inactive Aether smelted in the Primordial Smelter on a Block of Infernite.\n\nAether weapons, tools and armor are crafted on the crafting table in the usual shapes (ingots and sticks), no template needed.");
            add("jei.cryonite.info.primordial_smelter", "Crafting: 4 Obsidian, 2 Blaze Rods, 2 Netherite Scrap and a Blast Furnace.\n\nSlots: main ingredient, up to two catalysts, fuel and result. Catalyst slots a recipe does not use must stay empty. Every recipe burns its own fuel, ordinary fuel does not work:\n- Raw Cryonite, Cryonite dust: Blaze Rod or Magma Block\n- Cooled Infernite Blank: Blaze Powder, Blaze Rod or Lava Bucket\n- Stabilized Void Clusters: Blaze Rod or Lava Bucket\n- Inactive Aether: Block of Infernite");
            add("jei.cryonite.info.kinetic_energy_converter", "Needs the Create mod. Crafting: Cryonite, Infernite, Void and Aether Ingots in the corners, a Block of Redstone on top, an Electron Tube, a Brass Casing and a Shaft in the middle row, a Precision Mechanism at the bottom.\n\nTurns Forge Energy into rotation. The shaft leaves through the front; the other five sides accept up to 1,000,000 FE/t from the cables of any mod into a buffer of 10,000,000 FE.\n\nRight-click opens the control panel: speed from 0 to 8048 RPM, direction and a power switch.\n- Stress capacity: 1024 SU per RPM (8,241,152 SU at 8048 RPM)\n- Consumption: 4 FE/t per RPM (32,192 FE/t at 8048 RPM)\nWithout energy the generator stops.\n\nWith this block installed Create's speed limit (maxRotationSpeed) is raised from 256 to 8048 RPM.");
        }

        private void gear(GearTier tier, String name) {
            ModItems.GearSet gear = ModItems.gear(tier);
            addItem(gear.sword(), name + " Sword");
            addItem(gear.pickaxe(), name + " Pickaxe");
            addItem(gear.axe(), name + " Axe");
            addItem(gear.shovel(), name + " Shovel");
            addItem(gear.hoe(), name + " Hoe");
            addItem(gear.multitool(), name + " Multitool");
            addItem(gear.helmet(), name + " Helmet");
            addItem(gear.chestplate(), name + " Chestplate");
            addItem(gear.leggings(), name + " Leggings");
            addItem(gear.boots(), name + " Boots");
        }

        private void template(GearTier tier, String name, String appliesTo, String baseSlot) {
            addItem(ModItems.upgradeTemplate(tier), name + " Upgrade Smithing Template");
            add(UpgradeTemplateItem.key(tier, "title"), name + " Upgrade");
            add(UpgradeTemplateItem.key(tier, "applies_to"), appliesTo);
            add(UpgradeTemplateItem.key(tier, "ingredients"), name + " Ingot");
            add(UpgradeTemplateItem.key(tier, "base_slot_description"), baseSlot);
            add(UpgradeTemplateItem.key(tier, "additions_slot_description"), "Add a " + name + " Ingot");
        }
    }

    public static class Russian extends LanguageProvider {
        public Russian(PackOutput output) {
            super(output, Cryonite.MODID, "ru_ru");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.cryonite", "Крионит");
            add("container.cryonite.primordial_smelter", "Первородная Плавильня");

            addBlock(ModBlocks.PRIMORDIAL_SMELTER, "Первородная Плавильня");
            addBlock(ModBlocks.CRYONITE_ORE, "Крионитовая руда");
            addBlock(ModBlocks.INFERNITE_ORE, "Инфернитовая руда");
            addBlock(ModBlocks.VOID_ORE, "Руда Войда");
            addBlock(ModBlocks.INFERNITE_BLOCK, "Блок инфернита");

            addItem(ModItems.RAW_CRYONITE, "Сырой крионит");
            addItem(ModItems.CRYONITE_CRYSTAL_DUST, "Кристаллическая пыль крионита");
            addItem(ModItems.CRYONITE_INGOT, "Крионитовый слиток");
            addItem(ModItems.MOLTEN_INFERNITE_SHARD, "Раскалённый осколок инфернита");
            addItem(ModItems.COOLED_INFERNITE_BLANK, "Охлаждённая инфернитовая заготовка");
            addItem(ModItems.INFERNITE_INGOT, "Инфернитовый слиток");
            addItem(ModItems.UNSTABLE_VOID_SHARD, "Нестабильный осколок Войда");
            addItem(ModItems.STABILIZED_VOID_CLUSTER, "Стабилизированный кластер Войда");
            addItem(ModItems.MOLTEN_VOID_BLANK, "Расплавленная заготовка Войда");
            addItem(ModItems.VOID_INGOT, "Слиток Войда");
            addItem(ModItems.INACTIVE_AETHER, "Неактивный Эфир");
            addItem(ModItems.AETHER_INGOT, "Эфирный слиток");

            gear(GearTier.CRYONITE, "Крионитовый", "Крионитовая", "Крионитовые");
            gear(GearTier.INFERNITE, "Инфернитовый", "Инфернитовая", "Инфернитовые");
            gear(GearTier.VOID, "Войдовый", "Войдовая", "Войдовые");
            gear(GearTier.AETHER, "Эфирный", "Эфирная", "Эфирные");

            template(GearTier.CRYONITE, "Крионитовое улучшение", "Незеритовое снаряжение", "Крионитовый слиток",
                    "Поместите незеритовую броню, оружие или инструмент", "Добавьте крионитовый слиток");
            template(GearTier.INFERNITE, "Инфернитовое улучшение", "Крионитовое снаряжение", "Инфернитовый слиток",
                    "Поместите крионитовую броню, оружие или инструмент", "Добавьте инфернитовый слиток");
            template(GearTier.VOID, "Улучшение Войда", "Инфернитовое снаряжение", "Слиток Войда",
                    "Поместите инфернитовую броню, оружие или инструмент", "Добавьте слиток Войда");

            guide();
            add("tooltip.cryonite.hold_shift", "Зажмите Shift — способности");
            add("tooltip.cryonite.full_set", "Полный комплект:");
            add("tooltip.cryonite.on_hit", "При ударе:");
            add("tooltip.cryonite.when_mining", "При добыче:");

            add("tooltip.cryonite.ability.armor.cryonite", "Иммунитет к заморозке и замедлению, ходьба по рыхлому снегу, атакующие замедляются и слабеют");
            add("tooltip.cryonite.ability.armor.infernite", "Иммунитет к огню, лаве и иссушению, плавание и обзор в лаве, атакующие загораются");
            add("tooltip.cryonite.ability.armor.void", "Спасение из пустоты (перезарядка 2 мин), нет урона от падения и удара о стену, эндермены не замечают взгляд");
            add("tooltip.cryonite.ability.armor.aether", "Креативный полёт, сильная регенерация, иммунитет к негативным эффектам и магии");
            add("tooltip.cryonite.ability.weapon.cryonite", "Заморозка и замедление, +25% урона огненным существам");
            add("tooltip.cryonite.ability.weapon.infernite", "Синее пламя, +25% урона нежити, ледяным и водным мобам и крионитовой броне");
            add("tooltip.cryonite.ability.weapon.void", "Игнорирует 30% брони; меч и мультитул: ПКМ — рывок на 7 блоков");
            add("tooltip.cryonite.ability.weapon.aether", "50% урона — чистая магия (игнорирует броню и щит), +1 сердце за удар");
            add("tooltip.cryonite.ability.tool.cryonite", "Мгновенно ломает лёд и снег");
            add("tooltip.cryonite.ability.tool.infernite", "Автоплавка, работает с Удачей");
            add("tooltip.cryonite.ability.tool.void", "Телекинез: добыча и опыт сразу попадают к вам");
            add("tooltip.cryonite.ability.tool.aether", "Копание 3×3");
            add("tooltip.cryonite.mode.auto_smelt", "Автоплавка: %s [Клавиша: %s]");
            add("tooltip.cryonite.mode.auto_smelt.on", "ВКЛ");
            add("tooltip.cryonite.mode.auto_smelt.off", "ВЫКЛ");
            add("tooltip.cryonite.mode.area_mining", "Режим копания: %s [Клавиша: %s]");
            add("tooltip.cryonite.mode.area_mining.on", "3×3");
            add("tooltip.cryonite.mode.area_mining.off", "1×1");

            add("message.cryonite.auto_smelt", "Автоплавка");
            add("message.cryonite.auto_smelt.on", "ВКЛЮЧЕНА");
            add("message.cryonite.auto_smelt.off", "ВЫКЛЮЧЕНА");
            add("message.cryonite.area_mining", "Режим копания 3×3");
            add("message.cryonite.area_mining.on", "ВКЛЮЧЕН");
            add("message.cryonite.area_mining.off", "ВЫКЛЮЧЕН");
            add("message.cryonite.mode_state", "%s: %s");
            add("message.cryonite.mode_unsupported", "Предмет в руке не поддерживает режим «%s»");
            add("message.cryonite.item_burned", "Раскалённый осколок инфернита сжёг: %s!");
            add("message.cryonite.void_rescue", "Броня Войда вернула вас обратно");

            add("key.categories.cryonite", "Крионит");
            add("key.cryonite.toggle_3x3", "Переключить режим копания 3×3");
            add("key.cryonite.toggle_autosmelt", "Переключить Автоплавку");

            add("gui.cryonite.slot.input", "Основной ингредиент");
            add("gui.cryonite.slot.catalyst", "Катализатор");
            add("gui.cryonite.slot.fuel", "Особое топливо");

            // Интеграция с Create: Кинетический преобразователь энергии (compat/create) и его панель управления.
            add("block.cryonite.kinetic_energy_converter", "Кинетический преобразователь энергии");
            add("container.cryonite.kinetic_energy_converter", "Кинетический преобразователь энергии");
            add("gui.cryonite.kinetic_converter.target", "Целевая скорость, 0–8048 RPM");
            add("gui.cryonite.kinetic_converter.apply", "Применить");
            add("gui.cryonite.kinetic_converter.slider", "Скорость: %s RPM");
            add("gui.cryonite.kinetic_converter.preset.min", "MIN (0)");
            add("gui.cryonite.kinetic_converter.preset.max", "MAX (8048)");
            add("gui.cryonite.kinetic_converter.direction", "Направление: %s");
            add("gui.cryonite.kinetic_converter.direction.cw", "CW ⟳");
            add("gui.cryonite.kinetic_converter.direction.ccw", "CCW ⟲");
            add("gui.cryonite.kinetic_converter.power.on", "Питание: ВКЛ");
            add("gui.cryonite.kinetic_converter.power.off", "Питание: ВЫКЛ");
            add("gui.cryonite.kinetic_converter.speed", "Скорость: %s RPM");
            add("gui.cryonite.kinetic_converter.output", "Выход мощности: %s SU");
            add("gui.cryonite.kinetic_converter.consumption", "Потребление: %s FE/t");
            add("gui.cryonite.kinetic_converter.energy", "Энергия: %s / %s FE");
            add("gui.cryonite.kinetic_converter.energy.input", "Приём: до %s FE/t");
            add("gui.cryonite.kinetic_converter.status.running", "Работает");
            add("gui.cryonite.kinetic_converter.status.off", "Выключен");
            add("gui.cryonite.kinetic_converter.status.idle", "Простой");
            add("gui.cryonite.kinetic_converter.status.no_energy", "Нет энергии");
            add("gui.cryonite.kinetic_converter.status.overstressed", "Перегрузка");

            add("death.attack.cryonite.aether_burst", "%1$s стёрт Эфиром");
            add("death.attack.cryonite.aether_burst.player", "%1$s стёрт Эфиром игрока %2$s");
            add("death.attack.cryonite.aether_burst.item", "%1$s стёрт игроком %2$s с помощью %3$s");
        }

        /** Inventory hints and the JEI documentation; same keys as in the English class. */
        private void guide() {
            // Inventory hints (item/ItemHints): accent lines describe the item, gray lines the next step.
            add("tooltip.cryonite.cryonite_ore.source", "Обычный мир: глубинный сланец и туф, Y от -64 до 0");
            add("tooltip.cryonite.cryonite_ore.tool", "Кирка: незеритовая или лучше");
            add("tooltip.cryonite.cryonite_ore.biomes", "Чаще в Тёмных глубинах и ледяных биомах");
            add("tooltip.cryonite.raw_cryonite.source", "Из крионитовой руды (незеритовая кирка)");
            add("tooltip.cryonite.raw_cryonite.danger", "Ледяной холод: в инвентаре замедляет и замораживает");
            add("tooltip.cryonite.raw_cryonite.next", "Переплавьте в пыль в Первородной Плавильне");
            add("tooltip.cryonite.raw_cryonite.fuel", "Топливо: огненный стержень или блок магмы");
            add("tooltip.cryonite.cryonite_crystal_dust.source", "Сырой крионит из Первородной Плавильни");
            add("tooltip.cryonite.cryonite_crystal_dust.next", "С незеритовым ломом или осколком эха — слиток");
            add("tooltip.cryonite.cryonite_crystal_dust.also", "На верстаке остужает раскалённый осколок инфернита");
            add("tooltip.cryonite.cryonite_ingot.source", "Пыль крионита + незеритовый лом или осколок эха");
            add("tooltip.cryonite.cryonite_ingot.next", "Кузнечный стол: улучшает незеритовое снаряжение");
            add("tooltip.cryonite.cryonite_ingot.also", "Нужен для Неактивного Эфира");
            add("tooltip.cryonite.cryonite_upgrade_smithing_template.source", "Древний город: сундуки (шанс 25%)");
            add("tooltip.cryonite.cryonite_upgrade_smithing_template.duplicate", "Дублируется: 7 незеритовых слитков + глубинный сланец");

            add("tooltip.cryonite.infernite_ore.source", "Незер: Базальтовые дельты, Y от 8 до 96");
            add("tooltip.cryonite.infernite_ore.tool", "Кирка: крионитовая или лучше");
            add("tooltip.cryonite.infernite_ore.shell", "Жилы спрятаны в оболочке из магмы и чернита");
            add("tooltip.cryonite.molten_infernite_shard.source", "Из инфернитовой руды (крионитовая кирка)");
            add("tooltip.cryonite.molten_infernite_shard.danger", "Раскалён: поджигает вас, сжигает дерево и камень в инвентаре");
            add("tooltip.cryonite.molten_infernite_shard.coolant", "Снежки или лёд в инвентаре сдерживают жар");
            add("tooltip.cryonite.molten_infernite_shard.next", "Охладите в котле с рыхлым снегом, о Синий лёд или с Крионитовой пылью");
            add("tooltip.cryonite.cooled_infernite_blank.source", "Остуженный раскалённый осколок инфернита");
            add("tooltip.cryonite.cooled_infernite_blank.next", "Плавильня: с блоком золота и слезой гаста — слиток");
            add("tooltip.cryonite.cooled_infernite_blank.fuel", "Топливо: огненный порошок, огненный стержень или ведро лавы");
            add("tooltip.cryonite.infernite_ingot.source", "Охлаждённая заготовка из Первородной Плавильни");
            add("tooltip.cryonite.infernite_ingot.next", "Кузнечный стол: улучшает крионитовое снаряжение");
            add("tooltip.cryonite.infernite_ingot.also", "Нужен для заготовки Войда, Неактивного Эфира и блока инфернита");
            add("tooltip.cryonite.infernite_block.source", "Блок хранения: 9 инфернитовых слитков");
            add("tooltip.cryonite.infernite_block.next", "Единственное топливо, способное пробудить Неактивный Эфир");
            add("tooltip.cryonite.infernite_upgrade_smithing_template.source", "Бастион: сундуки сокровищницы (шанс 7,5%)");
            add("tooltip.cryonite.infernite_upgrade_smithing_template.duplicate", "Дублируется: 7 крионитовых слитков + базальт");

            add("tooltip.cryonite.void_ore.source", "Энд: внешние острова, Y от 0 до 12");
            add("tooltip.cryonite.void_ore.tool", "Кирка: инфернитовая или лучше");
            add("tooltip.cryonite.void_ore.danger", "Опасно: при ломке тянет в бездну и ослепляет на 3 с");
            add("tooltip.cryonite.unstable_void_shard.source", "Из руды Войда (инфернитовая кирка)");
            add("tooltip.cryonite.unstable_void_shard.next", "Бросьте в облако дыхания дракона");
            add("tooltip.cryonite.unstable_void_shard.craft", "или 8 осколков вокруг драконьего дыхания на верстаке");
            add("tooltip.cryonite.stabilized_void_cluster.source", "Осколок Войда, усмирённый дыханием дракона");
            add("tooltip.cryonite.stabilized_void_cluster.next", "4 кластера + 4 инфернитовых слитка + панцирь шалкера");
            add("tooltip.cryonite.stabilized_void_cluster.where", "Верстак или Плавильня (огненный стержень, ведро лавы)");
            add("tooltip.cryonite.molten_void_blank.source", "Сплав кластеров Войда, инфернита и панциря шалкера");
            add("tooltip.cryonite.molten_void_blank.next", "Бросьте на плачущий обсидиан на 5 секунд");
            add("tooltip.cryonite.molten_void_blank.craft", "или соедините с синим льдом на верстаке");
            add("tooltip.cryonite.void_ingot.source", "Заготовка Войда, закалённая на плачущем обсидиане");
            add("tooltip.cryonite.void_ingot.next", "Кузнечный стол: улучшает инфернитовое снаряжение");
            add("tooltip.cryonite.void_ingot.also", "Нужен для Неактивного Эфира");
            add("tooltip.cryonite.void_upgrade_smithing_template.source", "Город Края: сундуки с сокровищами (шанс 17,5%)");
            add("tooltip.cryonite.void_upgrade_smithing_template.duplicate", "Дублируется: 7 инфернитовых слитков + драконье дыхание");

            add("tooltip.cryonite.inactive_aether.source", "Слитки незерита, крионита, инфернита и Войда");
            add("tooltip.cryonite.inactive_aether.next", "Переплавьте в Первородной Плавильне, используя Блок инфернита как топливо");
            add("tooltip.cryonite.aether_ingot.source", "Эфир, пробуждённый в Первородной Плавильне");
            add("tooltip.cryonite.aether_ingot.next", "Эфирное снаряжение крафтится на верстаке, без шаблона");
            add("tooltip.cryonite.primordial_smelter.purpose", "Плавит материалы всех четырёх тиров");
            add("tooltip.cryonite.primordial_smelter.slots", "Слоты: сырьё, до двух катализаторов, топливо");
            add("tooltip.cryonite.primordial_smelter.fuel", "У каждого рецепта своё топливо");
            add("tooltip.cryonite.kinetic_energy_converter.purpose", "Превращает энергию FE во вращение для Create");
            add("tooltip.cryonite.kinetic_energy_converter.energy", "Буфер: 10 000 000 FE, приём до 1 000 000 FE/t с пяти сторон");
            add("tooltip.cryonite.kinetic_energy_converter.speed", "0–8048 RPM, 1024 SU и 4 FE/t на каждый RPM");
            add("tooltip.cryonite.kinetic_energy_converter.gui", "ПКМ: настройка скорости и направления");

            // JEI (compat/jei): the two documentation categories.
            add("jei.cryonite.category.world_interaction", "Мировая переработка (закалка и стабилизация)");
            add("jei.cryonite.world_interaction.powder_snow_cauldron", "Нажмите ПКМ раскалённым осколком по котлу с рыхлым снегом. Один слой снега остужает до 16 осколков.");
            add("jei.cryonite.world_interaction.blue_ice", "Нажмите ПКМ раскалённым осколком по блоку Синего льда (лёд подтает до обычного). Один блок остужает до 16 осколков.");
            add("jei.cryonite.world_interaction.dragon_breath", "Бросьте осколки (Q) прямо в облако дыхания Дракона Края во время битвы.");
            add("jei.cryonite.world_interaction.crying_obsidian", "Бросьте заготовку (Q) на блок Плачущего обсидиана и подождите 5 секунд.");

            add("jei.cryonite.category.acquisition", "Добыча руд и поиск артефактов");
            add("jei.cryonite.acquisition.silk_touch", "Сама руда выпадает с Шёлковым касанием");
            add("jei.cryonite.acquisition.cryonite_ore", "Обычный мир: Глубинный сланец и туф (Y от -64 до 0), чаще в Тёмных глубинах и ледяных биомах. Замораживает в инвентаре!");
            add("jei.cryonite.acquisition.infernite_ore", "Незер: Базальтовые дельты (Y от 8 до 96), в окружении магмы и чернита. Жжёт игрока и плавит дерево/камень в инвентаре без льда или снежков рядом!");
            add("jei.cryonite.acquisition.void_ore", "Энд: Внешние острова, самый нижний слой над бездной (Y = 0..12). ОПАСНО: при ломке тянет вниз в пустоту и ослепляет на 3 сек!");
            add("jei.cryonite.acquisition.cryonite_template", "Структура: Древний город (Ancient City), шанс 25% на сундук. Дублируется на верстаке с помощью 7 слитков Незерита и Глубинного сланца.");
            add("jei.cryonite.acquisition.infernite_template", "Структура: Сокровищницы Бастионов пиглинов (низкий шанс: 7,5% на сундук). Дублируется с помощью 7 Слитков крионита и Базальта.");
            add("jei.cryonite.acquisition.void_template", "Структура: сундуки с сокровищами Городов Края, включая Корабли (шанс 17,5% на сундук). Дублируется с помощью 7 Слитков инфернита и Дыхания дракона.");

            // JEI info pages: what the categories cannot show.
            add("jei.cryonite.info.raw_cryonite", "Сырой крионит излучает холод: пока он в инвентаре, вас замедляет и постепенно замораживает. Спасают кожаная или крионитовая броня и Незер.\n\nПереплавьте его в Первородной Плавильне (топливо: огненный стержень или блок магмы) в Кристаллическую пыль крионита.");
            add("jei.cryonite.info.cryonite_crystal_dust", "Выплавляется из Сырого крионита в Первородной Плавильне (топливо: огненный стержень или блок магмы).\n\nПыль + незеритовый лом или осколок эха = Крионитовый слиток: на верстаке либо в Плавильне (лом или осколок в слот катализатора).\n\nНа верстаке пыль также остужает Раскалённый осколок инфернита.");
            add("jei.cryonite.info.cryonite_ingot", "Кристаллическая пыль крионита + незеритовый лом или осколок эха (верстак или Первородная Плавильня).\n\nКузнечный стол: шаблон «Крионитовое улучшение» + незеритовый предмет + слиток. 7 слитков дублируют шаблон «Инфернитовое улучшение». Также нужен для Неактивного Эфира.");
            add("jei.cryonite.info.molten_infernite_shard", "Третий способ: соедините осколок с Кристаллической пылью крионита на верстаке.\n\nСнежки, лёд, плотный или синий лёд в инвентаре не дают осколку жечь вас и ваши вещи.");
            add("jei.cryonite.info.cooled_infernite_blank", "Раскалённый осколок инфернита, остуженный в котле с рыхлым снегом, о Синий лёд или Кристаллической пылью крионита.\n\nПервородная Плавильня: заготовка + блок золота и слеза гаста (катализаторы), топливо: огненный порошок, огненный стержень или ведро лавы. Получается Инфернитовый слиток.");
            add("jei.cryonite.info.infernite_ingot", "Выплавляется из Охлаждённой инфернитовой заготовки в Первородной Плавильне (блок золота + слеза гаста).\n\nКузнечный стол: шаблон «Инфернитовое улучшение» + крионитовый предмет + слиток. 9 слитков дают Блок инфернита. Также нужен для Расплавленной заготовки Войда, Неактивного Эфира и дублирования шаблона «Улучшение Войда».");
            add("jei.cryonite.info.infernite_block", "9 Инфернитовых слитков на верстаке (разбирается обратно).\n\nЕдинственное топливо Первородной Плавильни, достаточно жаркое, чтобы пробудить Неактивный Эфир.");
            add("jei.cryonite.info.unstable_void_shard", "Без дракона: 8 осколков вокруг бутылки драконьего дыхания на верстаке дают 8 Стабилизированных кластеров Войда.");
            add("jei.cryonite.info.stabilized_void_cluster", "Нестабильный осколок Войда, стабилизированный в облаке дыхания дракона или на верстаке.\n\n4 кластера + 4 Инфернитовых слитка + панцирь шалкера = Расплавленная заготовка Войда: на верстаке либо в Первородной Плавильне (топливо: огненный стержень или ведро лавы).");
            add("jei.cryonite.info.molten_void_blank", "Получается из 4 Стабилизированных кластеров Войда, 4 Инфернитовых слитков и панциря шалкера (верстак или Первородная Плавильня).");
            add("jei.cryonite.info.molten_void_blank.craft", "Без обсидиана: соедините заготовку с синим льдом на верстаке.");
            add("jei.cryonite.info.void_ingot", "Расплавленная заготовка Войда, закалённая на плачущем обсидиане или синим льдом.\n\nКузнечный стол: шаблон «Улучшение Войда» + инфернитовый предмет + слиток. Также нужен для Неактивного Эфира.");
            add("jei.cryonite.info.inactive_aether", "Верстак: незеритовый слиток + Крионитовый слиток + Инфернитовый слиток + Слиток Войда.\n\nПереплавьте в Первородной Плавильне, используя Блок инфернита как топливо, и получите Эфирный слиток.");
            add("jei.cryonite.info.aether_ingot", "Неактивный Эфир, переплавленный в Первородной Плавильне на Блоке инфернита.\n\nЭфирное оружие, инструменты и броня крафтятся на верстаке по обычным схемам (слитки и палки), шаблон не нужен.");
            add("jei.cryonite.info.primordial_smelter", "Крафт: 4 обсидиана, 2 огненных стержня, 2 незеритовых лома и плавильная печь.\n\nСлоты: основной ингредиент, до двух катализаторов, топливо и результат. Слоты катализаторов, не занятые рецептом, должны быть пустыми. У каждого рецепта своё топливо, обычное не подходит:\n- Сырой крионит, пыль крионита: огненный стержень или блок магмы\n- Охлаждённая инфернитовая заготовка: огненный порошок, огненный стержень или ведро лавы\n- Стабилизированные кластеры Войда: огненный стержень или ведро лавы\n- Неактивный Эфир: Блок инфернита");
            add("jei.cryonite.info.kinetic_energy_converter", "Нужен мод Create. Крафт: слитки крионита, инфернита, Войда и Эфира по углам, блок редстоуна сверху, электронная лампа, латунный корпус и вал в среднем ряду, механизм точности снизу.\n\nПревращает энергию FE во вращение. Вал выходит из передней стороны, остальные пять сторон принимают до 1 000 000 FE/t от кабелей любого мода в буфер на 10 000 000 FE.\n\nПКМ открывает панель управления: скорость от 0 до 8048 RPM, направление и выключатель.\n- Мощность: 1024 SU на каждый RPM (8 241 152 SU при 8048 RPM)\n- Потребление: 4 FE/t на каждый RPM (32 192 FE/t при 8048 RPM)\nБез энергии генератор останавливается.\n\nС этим блоком лимит скорости Create (maxRotationSpeed) поднят с 256 до 8048 RPM.");
        }

        private void gear(GearTier tier, String masculine, String feminine, String plural) {
            ModItems.GearSet gear = ModItems.gear(tier);
            addItem(gear.sword(), masculine + " меч");
            addItem(gear.pickaxe(), feminine + " кирка");
            addItem(gear.axe(), masculine + " топор");
            addItem(gear.shovel(), feminine + " лопата");
            addItem(gear.hoe(), feminine + " мотыга");
            // The Void multitool is named the other way round on purpose: "Мультитул войда".
            addItem(gear.multitool(), tier == GearTier.VOID ? "Мультитул войда" : masculine + " мультитул");
            addItem(gear.helmet(), masculine + " шлем");
            addItem(gear.chestplate(), masculine + " нагрудник");
            addItem(gear.leggings(), plural + " поножи");
            addItem(gear.boots(), plural + " ботинки");
        }

        private void template(GearTier tier, String title, String appliesTo, String ingredient, String baseSlot, String additionSlot) {
            addItem(ModItems.upgradeTemplate(tier), "Кузнечный шаблон «" + title + "»");
            add(UpgradeTemplateItem.key(tier, "title"), title);
            add(UpgradeTemplateItem.key(tier, "applies_to"), appliesTo);
            add(UpgradeTemplateItem.key(tier, "ingredients"), ingredient);
            add(UpgradeTemplateItem.key(tier, "base_slot_description"), baseSlot);
            add(UpgradeTemplateItem.key(tier, "additions_slot_description"), additionSlot);
        }
    }

    private ModLanguageProvider() {}
}
