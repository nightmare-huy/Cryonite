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

            add("tooltip.cryonite.raw_cryonite", "Freezing cold: carrying it slows and freezes you");
            add("tooltip.cryonite.molten_infernite_shard", "Scorching: sets you on fire and burns wooden and stone items unless you carry snow or ice");
            add("tooltip.cryonite.molten_infernite_shard.quench", "Right-click a Powder Snow Cauldron or Blue Ice to cool it");
            add("tooltip.cryonite.unstable_void_shard", "Unstable: stabilize it in a cloud of Dragon's Breath");
            add("tooltip.cryonite.molten_void_blank", "Quench it on Crying Obsidian or with Blue Ice");
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
            add("tooltip.cryonite.ability.weapon.void", "Ignores 30% of armor; sword: right-click to dash 7 blocks");
            add("tooltip.cryonite.ability.weapon.aether", "Half of the damage is pure magic (ignores armor and shields), heals 1 heart per hit");
            add("tooltip.cryonite.ability.tool.cryonite", "Breaks ice and snow instantly");
            add("tooltip.cryonite.ability.tool.infernite", "Auto-smelt, works with Fortune (Shift + right-click or hotkey)");
            add("tooltip.cryonite.ability.tool.void", "Telekinesis: drops and experience go straight to you");
            add("tooltip.cryonite.ability.tool.aether", "3x3 mining (hotkey)");

            add("message.cryonite.auto_smelt", "Auto-smelt");
            add("message.cryonite.area_mining", "3x3 mining");
            add("message.cryonite.enabled", "ON");
            add("message.cryonite.disabled", "OFF");
            add("message.cryonite.mode_state", "%s: %s");
            add("message.cryonite.mode_unsupported", "The held item does not support %s");
            add("message.cryonite.item_burned", "The Molten Infernite Shard burned %s!");
            add("message.cryonite.void_rescue", "The Void armor pulled you back");

            add("key.categories.cryonite", "Cryonite");
            add("key.cryonite.toggle_auto_smelt", "Toggle Auto-smelt");
            add("key.cryonite.toggle_area_mining", "Toggle 3x3 Mining");

            add("gui.cryonite.slot.input", "Main ingredient");
            add("gui.cryonite.slot.catalyst", "Catalyst");
            add("gui.cryonite.slot.fuel", "Special fuel");

            add("death.attack.cryonite.aether_burst", "%1$s was erased by Aether");
            add("death.attack.cryonite.aether_burst.player", "%1$s was erased by the Aether of %2$s");
            add("death.attack.cryonite.aether_burst.item", "%1$s was erased by %2$s using %3$s");
        }

        private void gear(GearTier tier, String name) {
            ModItems.GearSet gear = ModItems.gear(tier);
            addItem(gear.sword(), name + " Sword");
            addItem(gear.pickaxe(), name + " Pickaxe");
            addItem(gear.axe(), name + " Axe");
            addItem(gear.shovel(), name + " Shovel");
            addItem(gear.hoe(), name + " Hoe");
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

            add("tooltip.cryonite.raw_cryonite", "Ледяной холод: в инвентаре замедляет и замораживает");
            add("tooltip.cryonite.molten_infernite_shard", "Раскалён: поджигает и сжигает деревянные и каменные предметы, если рядом нет снега или льда");
            add("tooltip.cryonite.molten_infernite_shard.quench", "ПКМ по котлу с рыхлым снегом или синему льду — охладить");
            add("tooltip.cryonite.unstable_void_shard", "Нестабилен: стабилизируйте в облаке драконьего дыхания");
            add("tooltip.cryonite.molten_void_blank", "Закалите на плачущем обсидиане или синим льдом");
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
            add("tooltip.cryonite.ability.weapon.void", "Игнорирует 30% брони; меч: ПКМ — рывок на 7 блоков");
            add("tooltip.cryonite.ability.weapon.aether", "50% урона — чистая магия (игнорирует броню и щит), +1 сердце за удар");
            add("tooltip.cryonite.ability.tool.cryonite", "Мгновенно ломает лёд и снег");
            add("tooltip.cryonite.ability.tool.infernite", "Автоплавка, работает с Удачей (Shift + ПКМ или клавиша)");
            add("tooltip.cryonite.ability.tool.void", "Телекинез: добыча и опыт сразу попадают к вам");
            add("tooltip.cryonite.ability.tool.aether", "Копание 3x3 (клавиша)");

            add("message.cryonite.auto_smelt", "Автоплавка");
            add("message.cryonite.area_mining", "Копание 3x3");
            add("message.cryonite.enabled", "ВКЛ");
            add("message.cryonite.disabled", "ВЫКЛ");
            add("message.cryonite.mode_state", "%s: %s");
            add("message.cryonite.mode_unsupported", "Предмет в руке не поддерживает режим «%s»");
            add("message.cryonite.item_burned", "Раскалённый осколок инфернита сжёг: %s!");
            add("message.cryonite.void_rescue", "Броня Войда вернула вас обратно");

            add("key.categories.cryonite", "Крионит");
            add("key.cryonite.toggle_auto_smelt", "Переключить автоплавку");
            add("key.cryonite.toggle_area_mining", "Переключить копание 3x3");

            add("gui.cryonite.slot.input", "Основной ингредиент");
            add("gui.cryonite.slot.catalyst", "Катализатор");
            add("gui.cryonite.slot.fuel", "Особое топливо");

            add("death.attack.cryonite.aether_burst", "%1$s стёрт Эфиром");
            add("death.attack.cryonite.aether_burst.player", "%1$s стёрт Эфиром игрока %2$s");
            add("death.attack.cryonite.aether_burst.item", "%1$s стёрт игроком %2$s с помощью %3$s");
        }

        private void gear(GearTier tier, String masculine, String feminine, String plural) {
            ModItems.GearSet gear = ModItems.gear(tier);
            addItem(gear.sword(), masculine + " меч");
            addItem(gear.pickaxe(), feminine + " кирка");
            addItem(gear.axe(), masculine + " топор");
            addItem(gear.shovel(), feminine + " лопата");
            addItem(gear.hoe(), feminine + " мотыга");
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
