package dev.nightmarehuy.cryonite.gametest;

import com.mojang.authlib.GameProfile;
import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.ability.TierAbilityHandler;
import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import dev.nightmarehuy.cryonite.ability.WeaponAbilities;
import dev.nightmarehuy.cryonite.block.DimensionTerminalBlock;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlock;
import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlockEntity;
import dev.nightmarehuy.cryonite.compat.jei.AcquisitionJeiRecipe;
import dev.nightmarehuy.cryonite.compat.jei.WorldInteractionJeiRecipe;
import dev.nightmarehuy.cryonite.network.ModNetworking;
import dev.nightmarehuy.cryonite.network.ToggleToolModePayload;
import dev.nightmarehuy.cryonite.recipe.PrimordialRecipeInput;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModDataComponents;
import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import dev.nightmarehuy.cryonite.registry.ModFeatures;
import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.registry.ModRecipes;
import dev.nightmarehuy.cryonite.tier.AttributeLimits;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.worldgen.ModWorldgen;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Server-side GameTests ({@code ./gradlew runGameTestServer}). They boot a real dedicated server with the
 * generated data pack and exercise the mechanics end to end: mining tiers, the Primordial Smelter,
 * the cascading abilities, loot modifiers, world generation and the in-world crafting steps.
 */
@GameTestHolder(Cryonite.MODID)
@PrefixGameTestTemplate(false)
public final class CryoniteGameTests {
    private static final String EMPTY = "empty";

    // ------------------------------------------------------------------ step 1/2: tiers and items

    @GameTest(template = EMPTY)
    public static void miningTiersFormAHierarchy(GameTestHelper helper) {
        BlockState cryoniteOre = ModBlocks.CRYONITE_ORE.get().defaultBlockState();
        BlockState inferniteOre = ModBlocks.INFERNITE_ORE.get().defaultBlockState();
        BlockState voidOre = ModBlocks.VOID_ORE.get().defaultBlockState();

        harvest(Items.DIAMOND_PICKAXE, cryoniteOre, false);
        harvest(Items.NETHERITE_PICKAXE, cryoniteOre, true);
        harvest(Items.NETHERITE_PICKAXE, inferniteOre, false);
        harvest(Items.NETHERITE_PICKAXE, voidOre, false);

        Item cryonite = ModItems.gear(GearTier.CRYONITE).pickaxe().get();
        harvest(cryonite, cryoniteOre, true);
        harvest(cryonite, inferniteOre, true);
        harvest(cryonite, voidOre, false);

        Item infernite = ModItems.gear(GearTier.INFERNITE).pickaxe().get();
        harvest(infernite, inferniteOre, true);
        harvest(infernite, voidOre, true);

        for (GearTier tier : List.of(GearTier.VOID, GearTier.AETHER)) {
            Item pickaxe = ModItems.gear(tier).pickaxe().get();
            harvest(pickaxe, cryoniteOre, true);
            harvest(pickaxe, inferniteOre, true);
            harvest(pickaxe, voidOre, true);
            harvest(pickaxe, Blocks.OBSIDIAN.defaultBlockState(), true);
        }
        helper.succeed();
    }

    private static void harvest(Item tool, BlockState state, boolean expected) {
        check(new ItemStack(tool).isCorrectToolForDrops(state) == expected,
                tool + (expected ? " must harvest " : " must not harvest ") + state.getBlock());
    }

    @GameTest(template = EMPTY)
    public static void gearIsUnbreakableAndHasTheSpecifiedStats(GameTestHelper helper) {
        for (GearTier tier : GearTier.values()) {
            ModItems.GearSet gear = ModItems.gear(tier);
            gear.all().forEach(holder -> {
                ItemStack stack = new ItemStack(holder.get());
                check(stack.has(DataComponents.UNBREAKABLE), holder.getId() + " must be unbreakable");
                check(stack.has(DataComponents.FIRE_RESISTANT), holder.getId() + " must be fire resistant");
                check(!stack.isDamageableItem(), holder.getId() + " must not take durability damage");
            });

            // FINAL tooltip values: 1.0 base damage and 4.0 base speed included.
            stats(gear.sword().get(), tier.swordDamage(), GearTier.SWORD_SPEED);
            stats(gear.axe().get(), tier.axeDamage(), GearTier.AXE_SPEED);
            stats(gear.pickaxe().get(), tier.pickaxeDamage(), GearTier.PICKAXE_SPEED);
            stats(gear.shovel().get(), tier.shovelDamage(), GearTier.SHOVEL_SPEED);
            stats(gear.multitool().get(), tier.multitoolDamage(), GearTier.MULTITOOL_SPEED);

            double armor = 0.0;
            double toughness = 0.0;
            double knockback = 0.0;
            for (var piece : gear.armor()) {
                ItemAttributeModifiers modifiers = new ItemStack(piece.get()).getAttributeModifiers();
                armor += sum(modifiers, Attributes.ARMOR);
                toughness += sum(modifiers, Attributes.ARMOR_TOUGHNESS);
                knockback += sum(modifiers, Attributes.KNOCKBACK_RESISTANCE);
            }
            int expectedArmor = tier.helmetDefense() + tier.chestplateDefense() + tier.leggingsDefense() + tier.bootsDefense();
            near(armor, expectedArmor, tier + " armor points");
            near(toughness, tier.toughness() * 4.0, tier + " armor toughness");
            near(knockback, tier.knockbackResistance() * 4.0, tier + " knockback resistance");
        }
        near(ModItems.gear(GearTier.CRYONITE).pickaxe().get().getTier().getSpeed(), 22.0, "cryonite dig speed");
        near(ModItems.gear(GearTier.AETHER).pickaxe().get().getTier().getSpeed(), 50.0, "aether dig speed");

        // The 2x rebalance, spelled out: tooltip damage and attack speed of the Aether sword, hoe and axe ...
        stats(ModItems.gear(GearTier.AETHER).sword().get(), 48.0, 3.2);
        stats(ModItems.gear(GearTier.AETHER).axe().get(), 56.0, 2.0);
        stats(ModItems.gear(GearTier.AETHER).hoe().get(), 2.0, 8.0);
        stats(ModItems.gear(GearTier.CRYONITE).sword().get(), 20.0, 3.2);
        stats(ModItems.gear(GearTier.CRYONITE).pickaxe().get(), 14.0, 2.4);
        // ... and the armor points of a full set of every tier.
        int[] sets = {44, 48, 54, 60};
        for (GearTier tier : GearTier.values()) {
            int points = tier.helmetDefense() + tier.chestplateDefense() + tier.leggingsDefense() + tier.bootsDefense();
            check(points == sets[tier.level() - 1], tier + " set must give " + sets[tier.level() - 1] + " armor points but gives " + points);
        }
        helper.succeed();
    }

    /**
     * Doubled armor only counts if the entity really gets it: vanilla caps armor at 30 and toughness at 20,
     * {@link AttributeLimits} lifts both caps. Knockback resistance is a chance and stays capped at 100%.
     */
    @GameTest(template = EMPTY)
    public static void doubledArmorIsNotCutOffByTheVanillaCaps(GameTestHelper helper) {
        check(Attributes.ARMOR.value() instanceof RangedAttribute armor && armor.getMaxValue() >= 60.0, "the cap of armor must leave room for 60 points");
        check(Attributes.ARMOR_TOUGHNESS.value() instanceof RangedAttribute toughness && toughness.getMaxValue() >= 64.0, "the cap of toughness must leave room for 64 points");

        double[] armor = {44.0, 48.0, 54.0, 60.0};
        double[] toughness = {32.0, 40.0, 52.0, 64.0};
        List<Pillager> wearers = new ArrayList<>();
        for (GearTier tier : GearTier.values()) {
            // Pillagers have no armor of their own (zombies and husks start with 2 points).
            Pillager wearer = helper.spawnWithNoFreeWill(EntityType.PILLAGER, new BlockPos(1 + 2 * (tier.level() - 1), 2, 4));
            equip(wearer, tier);
            wearers.add(wearer);
        }
        // Equipment turns into attribute modifiers when the entity ticks.
        helper.runAfterDelay(3, () -> {
            for (GearTier tier : GearTier.values()) {
                Pillager wearer = wearers.get(tier.level() - 1);
                near(wearer.getAttributeValue(Attributes.ARMOR), armor[tier.level() - 1], "armor of a mob in a full " + tier + " set");
                near(wearer.getAttributeValue(Attributes.ARMOR_TOUGHNESS), toughness[tier.level() - 1], "toughness of a mob in a full " + tier + " set");
                near(wearer.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 1.0, "knockback resistance of a mob in a full " + tier + " set");
                wearer.discard();
            }
            helper.succeed();
        });
    }

    private static void stats(Item item, double damage, double speed) {
        ItemAttributeModifiers modifiers = new ItemStack(item).getAttributeModifiers();
        near(1.0 + sum(modifiers, Attributes.ATTACK_DAMAGE), damage, item + " attack damage");
        near(4.0 + sum(modifiers, Attributes.ATTACK_SPEED), speed, item + " attack speed");
    }

    private static double sum(ItemAttributeModifiers modifiers, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        return modifiers.modifiers().stream()
                .filter(entry -> entry.attribute().equals(attribute))
                .mapToDouble(entry -> entry.modifier().amount())
                .sum();
    }

    @GameTest(template = EMPTY)
    public static void droppedGearSurvivesExplosionsAndCacti(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemEntity gear = helper.spawnItem(ModItems.gear(GearTier.CRYONITE).chestplate().get(), new BlockPos(2, 2, 2));
        ItemEntity control = helper.spawnItem(Items.DIAMOND_CHESTPLATE, new BlockPos(6, 2, 6));

        gear.hurt(level.damageSources().explosion(null, null), 100.0F);
        gear.hurt(level.damageSources().cactus(), 100.0F);
        gear.hurt(level.damageSources().lava(), 100.0F);
        control.hurt(level.damageSources().explosion(null, null), 100.0F);

        check(gear.isAlive(), "dropped Cryonite gear must survive explosions, cacti and lava");
        check(!control.isAlive(), "a dropped vanilla item must still be destroyed by an explosion");
        helper.succeed();
    }

    // ------------------------------------------------------------------ step 3: Primordial Smelter

    @GameTest(template = EMPTY)
    public static void smelterRecipesDemandTheirSpecialFuel(GameTestHelper helper) {
        ItemStack raw = new ItemStack(ModItems.RAW_CRYONITE.get());
        check(matches(helper, raw, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD), ModItems.CRYONITE_CRYSTAL_DUST.get()), "raw cryonite + blaze rod");
        check(matches(helper, raw, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.MAGMA_BLOCK), ModItems.CRYONITE_CRYSTAL_DUST.get()), "raw cryonite + magma block");
        check(noRecipe(helper, raw, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.COAL)), "coal must not smelt raw cryonite");
        check(noRecipe(helper, raw, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.LAVA_BUCKET)), "lava must not smelt raw cryonite");
        check(noRecipe(helper, raw, new ItemStack(Items.GOLD_BLOCK), ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD)), "an unused catalyst slot must stay empty");

        ItemStack dust = new ItemStack(ModItems.CRYONITE_CRYSTAL_DUST.get());
        check(matches(helper, dust, new ItemStack(Items.NETHERITE_SCRAP), ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD), ModItems.CRYONITE_INGOT.get()), "dust + scrap");
        check(matches(helper, dust, ItemStack.EMPTY, new ItemStack(Items.ECHO_SHARD), new ItemStack(Items.BLAZE_ROD), ModItems.CRYONITE_INGOT.get()), "dust + echo shard in the second slot");

        ItemStack blank = new ItemStack(ModItems.COOLED_INFERNITE_BLANK.get());
        ItemStack gold = new ItemStack(Items.GOLD_BLOCK);
        ItemStack tear = new ItemStack(Items.GHAST_TEAR);
        for (Item fuel : List.of(Items.BLAZE_POWDER, Items.BLAZE_ROD, Items.LAVA_BUCKET)) {
            check(matches(helper, blank, gold, tear, new ItemStack(fuel), ModItems.INFERNITE_INGOT.get()), "infernite ingot with " + fuel);
        }
        check(matches(helper, blank, tear, gold, new ItemStack(Items.BLAZE_ROD), ModItems.INFERNITE_INGOT.get()), "catalysts may be swapped");
        check(noRecipe(helper, blank, gold, ItemStack.EMPTY, new ItemStack(Items.BLAZE_ROD)), "both catalysts are required");
        check(noRecipe(helper, blank, gold, tear, new ItemStack(Items.MAGMA_BLOCK)), "magma is not an infernite fuel");

        check(matches(helper, new ItemStack(ModItems.STABILIZED_VOID_CLUSTER.get(), 4), new ItemStack(ModItems.INFERNITE_INGOT.get(), 4),
                new ItemStack(Items.SHULKER_SHELL), new ItemStack(Items.LAVA_BUCKET), ModItems.MOLTEN_VOID_BLANK.get()), "molten void blank");
        check(noRecipe(helper, new ItemStack(ModItems.STABILIZED_VOID_CLUSTER.get(), 3), new ItemStack(ModItems.INFERNITE_INGOT.get(), 4),
                new ItemStack(Items.SHULKER_SHELL), new ItemStack(Items.LAVA_BUCKET)), "three clusters are not enough");

        ItemStack aether = new ItemStack(ModItems.INACTIVE_AETHER.get());
        check(matches(helper, aether, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(ModItems.INFERNITE_BLOCK.get()), ModItems.AETHER_INGOT.get()), "aether + infernite block");
        for (Item fuel : List.of(Items.BLAZE_ROD, Items.LAVA_BUCKET, Items.MAGMA_BLOCK, Items.COAL_BLOCK)) {
            check(noRecipe(helper, aether, ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(fuel)), fuel + " must not awaken aether");
        }
        helper.succeed();
    }

    private static Optional<ItemStack> smelt(GameTestHelper helper, ItemStack input, ItemStack catalystA, ItemStack catalystB, ItemStack fuel) {
        ServerLevel level = helper.getLevel();
        PrimordialRecipeInput recipeInput = new PrimordialRecipeInput(input, catalystA, catalystB, fuel);
        return level.getRecipeManager().getRecipeFor(ModRecipes.PRIMORDIAL_SMELTING.get(), recipeInput, level)
                .map(holder -> holder.value().assemble(recipeInput, level.registryAccess()));
    }

    private static boolean matches(GameTestHelper helper, ItemStack input, ItemStack catalystA, ItemStack catalystB, ItemStack fuel, Item result) {
        return smelt(helper, input, catalystA, catalystB, fuel).filter(stack -> stack.is(result)).isPresent();
    }

    private static boolean noRecipe(GameTestHelper helper, ItemStack input, ItemStack catalystA, ItemStack catalystB, ItemStack fuel) {
        return smelt(helper, input, catalystA, catalystB, fuel).isEmpty();
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void smelterBlockSmeltsOnlyWithTheRightFuel(GameTestHelper helper) {
        BlockPos working = new BlockPos(2, 2, 2);
        BlockPos idle = new BlockPos(6, 2, 6);
        helper.setBlock(working, ModBlocks.PRIMORDIAL_SMELTER.get());
        helper.setBlock(idle, ModBlocks.PRIMORDIAL_SMELTER.get());

        ItemStackHandler items = smelterItems(helper, working);
        items.setStackInSlot(PrimordialSmelterBlockEntity.SLOT_INPUT, new ItemStack(ModItems.RAW_CRYONITE.get(), 2));
        items.setStackInSlot(PrimordialSmelterBlockEntity.SLOT_FUEL, new ItemStack(Items.BLAZE_ROD, 3));

        ItemStackHandler wrongFuel = smelterItems(helper, idle);
        wrongFuel.setStackInSlot(PrimordialSmelterBlockEntity.SLOT_INPUT, new ItemStack(ModItems.RAW_CRYONITE.get(), 2));
        wrongFuel.setStackInSlot(PrimordialSmelterBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL, 3));

        helper.runAfterDelay(20, () -> {
            check(helper.getBlockState(working).getValue(PrimordialSmelterBlock.LIT), "a working smelter must be lit");
            check(helper.getBlockState(working).getLightEmission(helper.getLevel(), helper.absolutePos(working)) == 15, "a lit smelter must emit light level 15");
            check(!helper.getBlockState(idle).getValue(PrimordialSmelterBlock.LIT), "a smelter with the wrong fuel must stay dark");
        });
        // one operation takes 200 ticks
        helper.runAfterDelay(230, () -> {
            ItemStack output = items.getStackInSlot(PrimordialSmelterBlockEntity.SLOT_OUTPUT);
            check(output.is(ModItems.CRYONITE_CRYSTAL_DUST.get()) && output.getCount() == 1, "expected one dust after one operation, got " + output);
            check(items.getStackInSlot(PrimordialSmelterBlockEntity.SLOT_INPUT).getCount() == 1, "one raw cryonite must be consumed");
            check(items.getStackInSlot(PrimordialSmelterBlockEntity.SLOT_FUEL).getCount() == 2, "exactly one fuel item must be consumed");
            check(wrongFuel.getStackInSlot(PrimordialSmelterBlockEntity.SLOT_OUTPUT).isEmpty(), "coal must not produce anything");
            check(wrongFuel.getStackInSlot(PrimordialSmelterBlockEntity.SLOT_FUEL).getCount() == 3, "wrong fuel must not be consumed");
            helper.succeed();
        });
    }

    private static ItemStackHandler smelterItems(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof PrimordialSmelterBlockEntity smelter) {
            return smelter.getItems();
        }
        throw new GameTestAssertException("no smelter block entity at " + pos);
    }

    // ------------------------------------------------------------------ step 2: in-world mechanics

    @GameTest(template = EMPTY)
    public static void voidOreTriggersTheGravityAnomaly(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 2, 4);
        helper.setBlock(pos, ModBlocks.VOID_ORE.get());
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.gear(GearTier.INFERNITE).pickaxe().get()));

        NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos), player));

        near(player.getDeltaMovement().y, -1.8, "downward pull");
        check(player.hurtMarked, "the velocity change must be synced to the client");
        MobEffectInstance blindness = player.getEffect(MobEffects.BLINDNESS);
        check(blindness != null && blindness.getDuration() == 60, "Blindness for 60 ticks expected");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void moltenVoidBlankIsQuenchedOnCryingObsidian(GameTestHelper helper) {
        BlockPos obsidian = new BlockPos(4, 1, 4);
        helper.setBlock(obsidian, Blocks.CRYING_OBSIDIAN);
        ItemEntity item = helper.spawnItem(ModItems.MOLTEN_VOID_BLANK.get(), obsidian.above());
        item.getItem().setCount(3);

        helper.runAfterDelay(60, () -> check(item.getItem().is(ModItems.MOLTEN_VOID_BLANK.get()), "quenching takes 100 ticks"));
        helper.succeedWhen(() -> {
            check(item.isAlive() && item.getItem().is(ModItems.VOID_INGOT.get()), "the blank has not been quenched yet");
            check(item.getItem().getCount() == 3, "the whole stack must be converted");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void unstableShardIsStabilizedByDragonBreath(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos floor = new BlockPos(4, 1, 4);
        helper.setBlock(floor, Blocks.END_STONE);
        Vec3 centre = helper.absoluteVec(new Vec3(4.5, 2.0, 4.5));

        AreaEffectCloud cloud = new AreaEffectCloud(level, centre.x, centre.y, centre.z);
        cloud.setParticle(ParticleTypes.DRAGON_BREATH);
        cloud.setRadius(3.0F);
        cloud.setDuration(200);
        level.addFreshEntity(cloud);

        ItemEntity item = helper.spawnItem(ModItems.UNSTABLE_VOID_SHARD.get(), floor.above());
        helper.succeedWhen(() -> check(item.isAlive() && item.getItem().is(ModItems.STABILIZED_VOID_CLUSTER.get()), "the shard is still unstable"));
    }

    @GameTest(template = EMPTY)
    public static void moltenShardIsCooledOnBlueIce(GameTestHelper helper) {
        BlockPos ice = new BlockPos(4, 2, 4);
        helper.setBlock(ice, Blocks.BLUE_ICE);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack shards = new ItemStack(ModItems.MOLTEN_INFERNITE_SHARD.get(), 5);
        player.setItemInHand(InteractionHand.MAIN_HAND, shards);

        BlockPos absolute = helper.absolutePos(ice);
        shards.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));

        check(helper.getBlockState(ice).is(Blocks.ICE), "Blue Ice must melt down to Ice");
        check(player.getInventory().countItem(ModItems.COOLED_INFERNITE_BLANK.get()) == 5, "five cooled blanks expected");
        check(player.getInventory().countItem(ModItems.MOLTEN_INFERNITE_SHARD.get()) == 0, "the shards must be used up");
        helper.succeed();
    }

    // ------------------------------------------------------------------ step 4: cascading abilities

    @GameTest(template = EMPTY)
    public static void armorSetLevelIsTheWeakestPiece(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        check(TierAbilityHandler.armorSetLevel(player) == 0, "no armor = level 0");
        for (GearTier tier : GearTier.values()) {
            equip(player, tier);
            check(TierAbilityHandler.armorSetLevel(player) == tier.level(), "full " + tier + " set");
        }
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.gear(GearTier.INFERNITE).boots().get()));
        check(TierAbilityHandler.armorSetLevel(player) == GearTier.INFERNITE.level(), "a mixed set counts as its weakest piece");
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
        check(TierAbilityHandler.armorSetLevel(player) == 0, "a foreign piece breaks the set");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void armorImmunitiesCascade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var sources = level.damageSources();

        // Each row: the lowest tier that is immune to the damage source.
        immune(helper, sources.freeze(), GearTier.CRYONITE);
        immune(helper, sources.lava(), GearTier.INFERNITE);
        immune(helper, sources.onFire(), GearTier.INFERNITE);
        immune(helper, sources.inFire(), GearTier.INFERNITE);
        immune(helper, sources.hotFloor(), GearTier.INFERNITE);
        immune(helper, sources.wither(), GearTier.INFERNITE);
        immune(helper, sources.fall(), GearTier.VOID);
        immune(helper, sources.flyIntoWall(), GearTier.VOID);
        immune(helper, sources.magic(), GearTier.AETHER);
        helper.succeed();
    }

    private static void immune(GameTestHelper helper, net.minecraft.world.damagesource.DamageSource source, GearTier firstImmune) {
        for (GearTier tier : GearTier.values()) {
            Husk husk = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(4, 2, 4));
            equip(husk, tier);
            float before = husk.getHealth();
            husk.hurt(source, 4.0F);
            boolean tookDamage = husk.getHealth() < before;
            boolean expectedImmune = tier.level() >= firstImmune.level();
            husk.discard();
            check(tookDamage != expectedImmune, tier + " armor vs " + source.getMsgId() + ": expected " + (expectedImmune ? "immunity" : "damage"));
        }
    }

    @GameTest(template = EMPTY)
    public static void armorBlocksEffectsAndPunishesAttackers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // Pillagers: undead mobs ignore Poison on their own and would hide a broken check.
        Pillager cryonite = helper.spawnWithNoFreeWill(EntityType.PILLAGER, new BlockPos(2, 2, 2));
        equip(cryonite, GearTier.CRYONITE);
        check(!cryonite.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100)), "Cryonite armor blocks Slowness");
        check(cryonite.addEffect(new MobEffectInstance(MobEffects.POISON, 100)), "Cryonite armor does not block Poison");
        check(cryonite.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)), "Cryonite armor does not block Wither");

        Pillager infernite = helper.spawnWithNoFreeWill(EntityType.PILLAGER, new BlockPos(6, 2, 2));
        equip(infernite, GearTier.INFERNITE);
        check(!infernite.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100)), "Infernite armor inherits the Slowness immunity");
        check(!infernite.addEffect(new MobEffectInstance(MobEffects.WITHER, 100)), "Infernite armor blocks Wither");
        check(infernite.addEffect(new MobEffectInstance(MobEffects.POISON, 100)), "Infernite armor does not block Poison");

        Pillager aether = helper.spawnWithNoFreeWill(EntityType.PILLAGER, new BlockPos(2, 2, 6));
        equip(aether, GearTier.AETHER);
        check(!aether.addEffect(new MobEffectInstance(MobEffects.POISON, 100)), "Aether armor blocks every harmful effect");
        check(!aether.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100)), "Aether armor blocks every harmful effect");
        check(aether.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100)), "beneficial effects still apply");

        // Counterattack: tier 1 chills, tier 2 additionally ignites.
        Husk attacker = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(6, 2, 6));
        cryonite.hurt(level.damageSources().mobAttack(attacker), 2.0F);
        MobEffectInstance slowness = attacker.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        MobEffectInstance weakness = attacker.getEffect(MobEffects.WEAKNESS);
        check(slowness != null && slowness.getAmplifier() == 1 && slowness.getDuration() == 80, "attacker gets Slowness II for 4 s");
        check(weakness != null && weakness.getAmplifier() == 0 && weakness.getDuration() == 80, "attacker gets Weakness I for 4 s");
        check(!attacker.isOnFire(), "Cryonite armor does not ignite");

        Husk second = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(4, 2, 4));
        infernite.hurt(level.damageSources().mobAttack(second), 2.0F);
        check(second.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && second.hasEffect(MobEffects.WEAKNESS), "Infernite armor inherits the chill");
        check(second.getRemainingFireTicks() == 120, "attacker burns for 6 s, got " + second.getRemainingFireTicks() + " ticks");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void weaponAbilitiesCascade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);

        // Tier 1: frost, +25% against creatures of fire (Blaze: 20 HP, no armor).
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.gear(GearTier.CRYONITE).sword().get()));
        Husk frozen = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(2, 2, 2));
        frozen.hurt(level.damageSources().playerAttack(player), 4.0F);
        check(frozen.getTicksFrozen() >= 300, "Cryonite weapons freeze the target");
        MobEffectInstance slowness = frozen.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
        check(slowness != null && slowness.getAmplifier() == 1, "Cryonite weapons apply Slowness II");
        check(!frozen.isOnFire(), "Cryonite weapons do not ignite");

        Blaze blaze = helper.spawnWithNoFreeWill(EntityType.BLAZE, new BlockPos(6, 2, 2));
        blaze.hurt(level.damageSources().playerAttack(player), 8.0F);
        near(blaze.getHealth(), 20.0 - 8.0 * 1.25, "Cryonite weapon vs Blaze (+25%)");

        // Tier 2: inherits the frost, ignites, +25% against undead.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.gear(GearTier.INFERNITE).axe().get()));
        Husk burning = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(2, 2, 6));
        burning.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
        burning.hurt(level.damageSources().playerAttack(player), 8.0F);
        check(burning.isOnFire(), "Infernite weapons ignite");
        check(burning.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "Infernite weapons inherit the frost");
        near(burning.getHealth(), 20.0 - 8.0 * 1.25, "Infernite weapon vs undead (+25%)");

        // Tier 4: everything above; half of the hit is dealt as pure magic at the end of the tick, 2 HP life steal.
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.gear(GearTier.AETHER).sword().get()));
        player.setHealth(10.0F);
        Blaze victim = helper.spawnWithNoFreeWill(EntityType.BLAZE, new BlockPos(6, 2, 6));
        victim.hurt(level.damageSources().playerAttack(player), 4.0F);
        double total = 4.0 * 1.25; // inherited +25% against creatures of fire
        near(victim.getHealth(), 20.0 - total / 2.0, "the physical half of the hit");
        near(player.getHealth(), 12.0, "life steal of 2 HP");

        helper.runAfterDelay(3, () -> {
            near(victim.getHealth(), 20.0 - total, "physical half + magic half");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void autoSmeltIsAToggleOfInfernitePlusTools(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockState ironOre = Blocks.IRON_ORE.defaultBlockState();

        ItemStack cryonite = new ItemStack(ModItems.gear(GearTier.CRYONITE).pickaxe().get());
        check(drops(level, ironOre, pos, cryonite, Items.RAW_IRON), "Cryonite tools do not smelt");

        for (GearTier tier : List.of(GearTier.INFERNITE, GearTier.VOID, GearTier.AETHER)) {
            ItemStack tool = new ItemStack(ModItems.gear(tier).pickaxe().get());
            check(ToolAbilities.Mode.AUTO_SMELT.isActive(tool), "auto-smelt is on by default");
            check(drops(level, ironOre, pos, tool, Items.IRON_INGOT), tier + " pickaxe must smelt iron ore");
            check(drops(level, Blocks.COBBLESTONE.defaultBlockState(), pos, tool, Items.STONE), tier + " pickaxe must smelt cobblestone");
            check(drops(level, Blocks.DIAMOND_ORE.defaultBlockState(), pos, tool, Items.DIAMOND), "diamonds have no better furnace result");

            tool.set(ModDataComponents.AUTO_SMELT.get(), false);
            check(drops(level, ironOre, pos, tool, Items.RAW_IRON), "switched off, the tool drops raw iron again");
        }

        ItemStack shovel = new ItemStack(ModItems.gear(GearTier.INFERNITE).shovel().get());
        check(drops(level, Blocks.SAND.defaultBlockState(), pos, shovel, Items.GLASS), "auto-smelt works on every tool type");
        check(!ToolAbilities.Mode.AUTO_SMELT.isSupported(new ItemStack(ModItems.gear(GearTier.AETHER).sword().get())), "swords are no mining tools");
        check(ToolAbilities.Mode.AREA_MINING.isSupported(new ItemStack(ModItems.gear(GearTier.AETHER).pickaxe().get())), "Aether tools support 3x3");
        check(!ToolAbilities.Mode.AREA_MINING.isSupported(new ItemStack(ModItems.gear(GearTier.VOID).pickaxe().get())), "Void tools do not support 3x3");
        check(!ToolAbilities.Mode.AREA_MINING.isActive(new ItemStack(ModItems.gear(GearTier.AETHER).pickaxe().get())), "3x3 is off by default");
        helper.succeed();
    }

    private static boolean drops(ServerLevel level, BlockState state, BlockPos pos, ItemStack tool, Item expected) {
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, tool);
        return !drops.isEmpty() && drops.stream().allMatch(stack -> stack.is(expected));
    }

    @GameTest(template = EMPTY)
    public static void aetherPickaxeMinesThreeByThreeWithTelekinesis(GameTestHelper helper) {
        // 3x3 wall of cobblestone in the X/Y plane, the player looks at its centre from the north.
        for (int x = 3; x <= 5; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(x, y, 5), Blocks.COBBLESTONE);
            }
        }
        BlockPos centre = new BlockPos(4, 3, 5);
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            place(helper, player, new Vec3(4.5, 2.0, 2.5), new Vec3(4.5, 2.0, 8.5));

            ItemStack pickaxe = new ItemStack(ModItems.gear(GearTier.AETHER).pickaxe().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);

            // 3x3 is off by default: only the targeted block breaks.
            player.gameMode.destroyBlock(helper.absolutePos(centre));
            check(helper.getBlockState(centre).isAir(), "the targeted block must break");
            check(helper.getBlockState(new BlockPos(3, 2, 5)).is(Blocks.COBBLESTONE), "3x3 is off by default");
            helper.setBlock(centre, Blocks.COBBLESTONE);

            ToolAbilities.toggle(player, ToolAbilities.Mode.AREA_MINING);
            check(ToolAbilities.Mode.AREA_MINING.isActive(player.getMainHandItem()), "the toggle must switch 3x3 on");
            player.gameMode.destroyBlock(helper.absolutePos(centre));

            for (int x = 3; x <= 5; x++) {
                for (int y = 2; y <= 4; y++) {
                    check(helper.getBlockState(new BlockPos(x, y, 5)).isAir(), "block " + x + "/" + y + " of the 3x3 must be mined");
                }
            }
            // Telekinesis (tier 3) + auto-smelt (tier 2): 1 + 9 cobblestone arrive as stone in the inventory.
            check(player.getInventory().countItem(Items.STONE) == 10, "expected 10 smelted stone in the inventory, got "
                    + player.getInventory().countItem(Items.STONE) + " stone / " + player.getInventory().countItem(Items.COBBLESTONE) + " cobblestone");
            List<ItemEntity> lying = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds());
            check(lying.isEmpty(), "telekinesis must not leave drops on the ground");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void voidSwordDashes(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            place(helper, player, new Vec3(1.5, 2.0, 4.5), new Vec3(8.5, 2.0, 4.5));
            Vec3 start = player.position();

            ItemStack sword = new ItemStack(ModItems.gear(GearTier.VOID).sword().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, sword);
            check(WeaponAbilities.voidDash(player, sword), "the dash must succeed in open space");

            double travelled = player.position().distanceTo(start);
            check(travelled > 3.0 && travelled <= WeaponAbilities.DASH_DISTANCE + 0.01, "dash distance was " + travelled);
            check(player.getCooldowns().isOnCooldown(sword.getItem()), "the dash must start its cooldown");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ multitools, hotkeys and the 3x3 planes

    @GameTest(template = EMPTY)
    public static void multitoolsDigEverythingOfTheirTier(GameTestHelper helper) {
        List<BlockState> diggable = List.of(
                Blocks.STONE.defaultBlockState(), Blocks.OBSIDIAN.defaultBlockState(),      // pickaxe
                Blocks.OAK_LOG.defaultBlockState(),                                          // axe
                Blocks.DIRT.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(),          // shovel
                Blocks.HAY_BLOCK.defaultBlockState(),                                        // hoe
                Blocks.OAK_LEAVES.defaultBlockState());
        BlockState cobweb = Blocks.COBWEB.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        double[] expected = {22.0, 28.0, 36.0, 50.0};   // damage and dig speed happen to be the same numbers

        for (GearTier tier : GearTier.values()) {
            Item multitool = ModItems.gear(tier).multitool().get();
            ItemStack stack = new ItemStack(multitool);
            double value = expected[tier.level() - 1];

            stats(multitool, value, 2.4);
            near(tier.multitoolDamage(), tier.swordDamage() + 2.0, tier + " multitool damage is the sword + 2");
            for (BlockState state : diggable) {
                harvest(multitool, state, true);
                near(stack.getDestroySpeed(state), value, tier + " multitool speed on " + state.getBlock());
            }
            harvest(multitool, cobweb, true);
            near(stack.getDestroySpeed(cobweb), Math.max(15.0, value), tier + " multitool speed on cobwebs");
            harvest(multitool, glass, false);
            near(stack.getDestroySpeed(glass), 1.0, tier + " multitool speed on a block of no tool");

            // Weapon and tool at once: both ability cascades see the full tier.
            check(TierAbilityHandler.weaponLevel(stack) == tier.level(), tier + " multitool must count as a weapon");
            check(TierAbilityHandler.toolLevel(stack) == tier.level(), tier + " multitool must count as a tool");
            check(ToolAbilities.Mode.AUTO_SMELT.isSupported(stack) == (tier.level() >= 2), tier + " multitool: auto-smelt starts with Infernite");
            check(ToolAbilities.Mode.AREA_MINING.isSupported(stack) == (tier == GearTier.AETHER), tier + " multitool: 3x3 is Aether only");
        }

        // The tier gate is the one of the pickaxes: Void Ore needs Infernite.
        Item cryonite = ModItems.gear(GearTier.CRYONITE).multitool().get();
        BlockState voidOre = ModBlocks.VOID_ORE.get().defaultBlockState();
        harvest(cryonite, ModBlocks.CRYONITE_ORE.get().defaultBlockState(), true);
        harvest(cryonite, ModBlocks.INFERNITE_ORE.get().defaultBlockState(), true);
        harvest(cryonite, voidOre, false);
        near(new ItemStack(cryonite).getDestroySpeed(voidOre), 1.0, "a block above the tier is dug at hand speed");
        Item infernite = ModItems.gear(GearTier.INFERNITE).multitool().get();
        harvest(infernite, voidOre, true);

        // Inherited mining abilities: auto-smelt from Infernite on.
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(4, 2, 4));
        check(drops(level, Blocks.IRON_ORE.defaultBlockState(), pos, new ItemStack(cryonite), Items.RAW_IRON), "the Cryonite multitool does not smelt");
        check(drops(level, Blocks.IRON_ORE.defaultBlockState(), pos, new ItemStack(infernite), Items.IRON_INGOT), "the Infernite multitool smelts");

        // Crafting recipes of all four tiers and the two smithing upgrades.
        for (String recipe : List.of("cryonite_multitool", "infernite_multitool", "void_multitool", "aether_multitool",
                "infernite_multitool_smithing", "void_multitool_smithing")) {
            check(level.getRecipeManager().byKey(Cryonite.id(recipe)).isPresent(), "missing recipe " + recipe);
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void multitoolStripsFlattensAndTills(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.gear(GearTier.CRYONITE).multitool().get()));

        BlockPos log = new BlockPos(2, 2, 2);
        BlockPos path = new BlockPos(4, 2, 2);
        BlockPos field = new BlockPos(6, 2, 2);
        BlockPos stone = new BlockPos(4, 2, 6);
        helper.setBlock(log, Blocks.OAK_LOG);
        helper.setBlock(path, Blocks.GRASS_BLOCK);
        helper.setBlock(field, Blocks.GRASS_BLOCK);
        helper.setBlock(stone, Blocks.STONE);

        check(rightClick(helper, player, log).consumesAction(), "stripping must consume the click");
        check(helper.getBlockState(log).is(Blocks.STRIPPED_OAK_LOG), "right-click strips logs like an axe");
        check(rightClick(helper, player, path).consumesAction(), "flattening must consume the click");
        check(helper.getBlockState(path).is(Blocks.DIRT_PATH), "right-click makes a path like a shovel");
        check(rightClick(helper, player, stone) == InteractionResult.PASS, "nothing to do on stone: the click must pass through");

        player.setShiftKeyDown(true);
        check(rightClick(helper, player, field).consumesAction(), "tilling must consume the click");
        check(helper.getBlockState(field).is(Blocks.FARMLAND), "Shift + right-click tills like a hoe");
        helper.setBlock(log, Blocks.BIRCH_LOG);
        rightClick(helper, player, log);
        check(helper.getBlockState(log).is(Blocks.STRIPPED_BIRCH_LOG), "with nothing to till, Shift + right-click still strips");
        helper.succeed();
    }

    private static InteractionResult rightClick(GameTestHelper helper, Player player, BlockPos pos) {
        BlockPos absolute = helper.absolutePos(pos);
        return player.getMainHandItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
    }

    @GameTest(template = EMPTY)
    public static void voidMultitoolDashesOnlyInTheAir(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            place(helper, player, new Vec3(1.5, 2.0, 4.5), new Vec3(8.5, 2.0, 4.5));
            Vec3 start = player.position();

            ItemStack cryonite = new ItemStack(ModItems.gear(GearTier.CRYONITE).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, cryonite);
            check(cryonite.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.PASS, "no dash below the Void tier");

            ItemStack multitool = new ItemStack(ModItems.gear(GearTier.VOID).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, multitool);

            // A block within reach at eye level: that click belongs to the tool, not to the dash.
            BlockPos wall = new BlockPos(3, 3, 4);
            helper.setBlock(wall, Blocks.STONE);
            check(multitool.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult() == InteractionResult.PASS, "no dash when a block is targeted");
            check(player.position().distanceTo(start) < 1.0E-6, "the player must not move");
            check(!player.getCooldowns().isOnCooldown(multitool.getItem()), "a refused dash must not start the cooldown");

            helper.setBlock(wall, Blocks.AIR);
            check(multitool.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(), "right-click in the air must dash");
            double travelled = player.position().distanceTo(start);
            check(travelled > 3.0 && travelled <= WeaponAbilities.DASH_DISTANCE + 0.01, "dash distance was " + travelled);
            check(player.getCooldowns().isOnCooldown(multitool.getItem()), "the dash must start its cooldown");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** The server side of the two hotkeys, and the tooltip lines that show the state and the key. */
    @GameTest(template = EMPTY)
    public static void hotkeysToggleTheModesOnTheServerStack(GameTestHelper helper) {
        check(ToggleToolModePayload.MODE_3X3 == 0 && ToggleToolModePayload.MODE_AUTO_SMELT == 1, "payload mode types are 0 = 3x3, 1 = auto-smelt");
        check(ToolAbilities.Mode.byNetworkId(0) == ToolAbilities.Mode.AREA_MINING, "mode type 0 is 3x3 mining");
        check(ToolAbilities.Mode.byNetworkId(1) == ToolAbilities.Mode.AUTO_SMELT, "mode type 1 is auto-smelt");
        check(ToolAbilities.Mode.byNetworkId(7) == null && ToolAbilities.Mode.byNetworkId(-1) == null, "unknown mode types are ignored");
        for (String key : List.of("key.categories.cryonite", "key.cryonite.toggle_3x3", "key.cryonite.toggle_autosmelt")) {
            check(Language.getInstance().has(key), "no en_us translation for " + key);
        }
        check(ToolAbilities.Mode.AREA_MINING.keyName().equals("key.cryonite.toggle_3x3"), "3x3 tooltip must name its own key");
        check(ToolAbilities.Mode.AUTO_SMELT.keyName().equals("key.cryonite.toggle_autosmelt"), "auto-smelt tooltip must name its own key");

        ServerPlayer player = mockPlayer(helper);
        try {
            ItemStack aether = new ItemStack(ModItems.gear(GearTier.AETHER).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, aether);
            ToolAbilities.toggle(player, ToolAbilities.Mode.AREA_MINING);
            check(Boolean.TRUE.equals(player.getMainHandItem().get(ModDataComponents.AREA_MINING.get())), "V must write the 3x3 component on the server stack");
            ToolAbilities.toggle(player, ToolAbilities.Mode.AREA_MINING);
            check(!ToolAbilities.Mode.AREA_MINING.isActive(player.getMainHandItem()), "a second press switches 3x3 off again");
            ToolAbilities.toggle(player, ToolAbilities.Mode.AUTO_SMELT);
            check(Boolean.FALSE.equals(player.getMainHandItem().get(ModDataComponents.AUTO_SMELT.get())), "B must switch auto-smelt off");

            // The tier is checked on the server: a Void multitool has no 3x3, a Cryonite one has neither mode.
            ItemStack voidTool = new ItemStack(ModItems.gear(GearTier.VOID).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, voidTool);
            ToolAbilities.toggle(player, ToolAbilities.Mode.AREA_MINING);
            check(!player.getMainHandItem().has(ModDataComponents.AREA_MINING.get()), "3x3 must not be switchable below Aether");
            ItemStack cryonite = new ItemStack(ModItems.gear(GearTier.CRYONITE).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, cryonite);
            ToolAbilities.toggle(player, ToolAbilities.Mode.AUTO_SMELT);
            check(!player.getMainHandItem().has(ModDataComponents.AUTO_SMELT.get()), "auto-smelt must not be switchable below Infernite");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }

        // Tooltip: one line per supported mode, on every tool and on the multitools.
        for (GearTier tier : GearTier.values()) {
            ModItems.GearSet gear = ModItems.gear(tier);
            int lines = tier == GearTier.AETHER ? 2 : tier.level() >= 2 ? 1 : 0;
            for (var tool : List.of(gear.pickaxe(), gear.axe(), gear.shovel(), gear.hoe(), gear.multitool())) {
                check(modeLines(new ItemStack(tool.get())) == lines, tool.getId() + " must show " + lines + " mode line(s)");
            }
            check(modeLines(new ItemStack(gear.sword().get())) == 0, "swords have no tool modes");
        }
        helper.succeed();
    }

    private static int modeLines(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.NORMAL);
        lines.forEach(CryoniteGameTests::assertTranslated);
        return lines.size();
    }

    /** UP / DOWN give the X/Z plane, NORTH / SOUTH the X/Y wall, WEST / EAST the Z/Y wall. */
    @GameTest(template = EMPTY)
    public static void areaMiningPlaneFollowsTheMinedFace(GameTestHelper helper) {
        BlockPos origin = new BlockPos(10, 20, 30);
        for (Direction face : Direction.values()) {
            List<BlockPos> area = ToolAbilities.areaAround(origin, face);
            check(area.size() == 8 && area.stream().distinct().count() == 8 && !area.contains(origin), face + ": eight distinct neighbours expected");
            for (BlockPos pos : area) {
                check(pos.get(face.getAxis()) == origin.get(face.getAxis()), face + ": " + pos + " left the plane of the face");
                for (Direction.Axis axis : Direction.Axis.values()) {
                    check(Math.abs(pos.get(axis) - origin.get(axis)) <= 1, face + ": " + pos + " is not adjacent");
                }
            }
        }
        helper.succeed();
    }

    /** Looking straight down: the Aether multitool takes a 3x3 of stone, dirt, gravel and wood out of the floor at once. */
    @GameTest(template = EMPTY)
    public static void aetherMultitoolMinesAMixedFloor(GameTestHelper helper) {
        Block[] mix = {Blocks.STONE, Blocks.DIRT, Blocks.GRAVEL, Blocks.OAK_LOG, Blocks.COBBLESTONE,
                Blocks.OAK_PLANKS, Blocks.SAND, Blocks.HAY_BLOCK, Blocks.OAK_LEAVES};
        // 5x5 floor: the mixed 3x3 in the middle, a ring of cobblestone around it that must survive.
        for (int x = 2; x <= 6; x++) {
            for (int z = 2; z <= 6; z++) {
                boolean inner = x >= 3 && x <= 5 && z >= 3 && z <= 5;
                helper.setBlock(new BlockPos(x, 2, z), inner ? mix[(x - 3) * 3 + (z - 3)] : Blocks.COBBLESTONE);
            }
        }
        BlockPos centre = new BlockPos(4, 2, 4);
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            Vec3 above = helper.absoluteVec(new Vec3(4.5, 3.0, 4.5));
            player.moveTo(above.x, above.y, above.z, 0.0F, 90.0F);

            ItemStack multitool = new ItemStack(ModItems.gear(GearTier.AETHER).multitool().get());
            player.setItemInHand(InteractionHand.MAIN_HAND, multitool);
            ToolAbilities.toggle(player, ToolAbilities.Mode.AREA_MINING);
            player.gameMode.destroyBlock(helper.absolutePos(centre));

            for (int x = 2; x <= 6; x++) {
                for (int z = 2; z <= 6; z++) {
                    BlockPos pos = new BlockPos(x, 2, z);
                    boolean inner = x >= 3 && x <= 5 && z >= 3 && z <= 5;
                    if (inner) {
                        check(helper.getBlockState(pos).isAir(), "floor block " + x + "/" + z + " of the 3x3 must be mined");
                    } else {
                        check(helper.getBlockState(pos).is(Blocks.COBBLESTONE), "block " + x + "/" + z + " outside the 3x3 must stay (no chain reaction)");
                    }
                }
            }
            // Telekinesis and auto-smelt are inherited: nothing lies around, stone and cobblestone arrive as stone.
            check(player.getInventory().countItem(Items.STONE) == 2, "expected 2 smelted stone, got " + player.getInventory().countItem(Items.STONE));
            check(player.getInventory().countItem(Items.DIRT) == 1, "expected the dirt in the inventory");
            List<ItemEntity> lying = helper.getLevel().getEntitiesOfClass(ItemEntity.class, helper.getBounds());
            check(lying.isEmpty(), "telekinesis must not leave drops on the ground");
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    /** The third plane: a wall the player faces along the X axis is mined in Z/Y. */
    @GameTest(template = EMPTY)
    public static void aetherShovelMinesASideWall(GameTestHelper helper) {
        for (int z = 3; z <= 5; z++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(6, y, z), Blocks.DIRT);
            }
        }
        // Same plane but outside the 3x3, and directly behind the centre: both must survive.
        List<BlockPos> bystanders = List.of(new BlockPos(6, 3, 2), new BlockPos(6, 3, 6), new BlockPos(7, 3, 4));
        bystanders.forEach(pos -> helper.setBlock(pos, Blocks.DIRT));
        BlockPos centre = new BlockPos(6, 3, 4);

        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            place(helper, player, new Vec3(3.5, 2.0, 4.5), new Vec3(8.5, 2.0, 4.5));

            ItemStack shovel = new ItemStack(ModItems.gear(GearTier.AETHER).shovel().get());
            shovel.set(ModDataComponents.AREA_MINING.get(), true);
            player.setItemInHand(InteractionHand.MAIN_HAND, shovel);
            player.gameMode.destroyBlock(helper.absolutePos(centre));

            for (int z = 3; z <= 5; z++) {
                for (int y = 2; y <= 4; y++) {
                    check(helper.getBlockState(new BlockPos(6, y, z)).isAir(), "wall block " + z + "/" + y + " of the 3x3 must be mined");
                }
            }
            for (BlockPos pos : bystanders) {
                check(helper.getBlockState(pos).is(Blocks.DIRT), "block " + pos + " is outside the 3x3 plane and must stay");
            }
            check(player.getInventory().countItem(Items.DIRT) == 9, "expected 9 dirt in the inventory, got " + player.getInventory().countItem(Items.DIRT));
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ step 5: data and world generation

    @GameTest(template = EMPTY)
    public static void templatesAppearInStructureChests(GameTestHelper helper) {
        check(rollsContain(helper, BuiltInLootTables.ANCIENT_CITY, ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE.get()), "Ancient City chests must contain the Cryonite template");
        check(rollsContain(helper, BuiltInLootTables.BASTION_TREASURE, ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE.get()), "Bastion Treasure must contain the Infernite template");
        check(rollsContain(helper, BuiltInLootTables.END_CITY_TREASURE, ModItems.VOID_UPGRADE_SMITHING_TEMPLATE.get()), "End City Treasure must contain the Void template");
        check(!rollsContain(helper, BuiltInLootTables.SIMPLE_DUNGEON, ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE.get()), "other chests must not contain templates");
        helper.succeed();
    }

    private static boolean rollsContain(GameTestHelper helper, ResourceKey<LootTable> key, Item item) {
        ServerLevel level = helper.getLevel();
        LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 2, 4))))
                .create(LootContextParamSets.CHEST);
        for (int roll = 0; roll < 400; roll++) {
            if (table.getRandomItems(params).stream().anyMatch(stack -> stack.is(item))) {
                return true;
            }
        }
        return false;
    }

    @GameTest(template = EMPTY)
    public static void oresAreInjectedIntoTheirBiomes(GameTestHelper helper) {
        check(generates(helper, Biomes.PLAINS, ModWorldgen.CRYONITE_ORE_PLACED), "cryonite generates everywhere in the Overworld");
        check(!generates(helper, Biomes.PLAINS, ModWorldgen.CRYONITE_ORE_RICH_PLACED), "plains are not rich in cryonite");
        check(generates(helper, Biomes.DEEP_DARK, ModWorldgen.CRYONITE_ORE_RICH_PLACED), "the Deep Dark is rich in cryonite");
        check(generates(helper, Biomes.FROZEN_PEAKS, ModWorldgen.CRYONITE_ORE_RICH_PLACED), "cold biomes are rich in cryonite");
        check(generates(helper, Biomes.BASALT_DELTAS, ModWorldgen.INFERNITE_CLUSTER_PLACED), "infernite generates in Basalt Deltas");
        check(!generates(helper, Biomes.NETHER_WASTES, ModWorldgen.INFERNITE_CLUSTER_PLACED), "infernite generates only in Basalt Deltas");
        check(generates(helper, Biomes.END_HIGHLANDS, ModWorldgen.VOID_ORE_PLACED), "void ore generates on the outer islands");
        check(generates(helper, Biomes.SMALL_END_ISLANDS, ModWorldgen.VOID_ORE_PLACED), "void ore generates on the outer islands");
        check(!generates(helper, Biomes.THE_END, ModWorldgen.VOID_ORE_PLACED), "void ore must not generate on the central island");
        helper.succeed();
    }

    private static boolean generates(GameTestHelper helper, ResourceKey<Biome> biome, ResourceKey<PlacedFeature> feature) {
        Holder<Biome> holder = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
        return holder.value().getGenerationSettings().features().stream()
                .flatMap(HolderSet::stream)
                .anyMatch(placed -> placed.is(feature));
    }

    @GameTest(template = EMPTY)
    public static void inferniteClusterIsWrappedInMagmaAndBlackstone(GameTestHelper helper) {
        for (int x = 1; x <= 7; x++) {
            for (int y = 0; y <= 6; y++) {
                for (int z = 1; z <= 7; z++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.BASALT);
                }
            }
        }
        ServerLevel level = helper.getLevel();
        BlockPos centre = new BlockPos(4, 3, 4);
        boolean placed = ModFeatures.INFERNITE_CLUSTER.get().place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(centre));

        check(placed, "the cluster must generate inside basalt");
        check(helper.getBlockState(centre).is(ModBlocks.INFERNITE_ORE.get()), "the core is Infernite Ore");
        for (Direction direction : Direction.values()) {
            BlockState inner = helper.getBlockState(centre.relative(direction));
            check(inner.is(Blocks.MAGMA_BLOCK) || inner.is(ModBlocks.INFERNITE_ORE.get()), "the core is wrapped in magma, found " + inner);
            check(helper.getBlockState(centre.relative(direction, 2)).is(Blocks.BLACKSTONE), "the magma is wrapped in blackstone");
            check(helper.getBlockState(centre.relative(direction, 3)).is(Blocks.BASALT), "the surrounding basalt stays untouched");
        }

        // It must never start in the open.
        BlockPos air = new BlockPos(4, 3, 4);
        helper.setBlock(air, Blocks.AIR);
        check(!ModFeatures.INFERNITE_CLUSTER.get().place(NoneFeatureConfiguration.INSTANCE, level,
                level.getChunkSource().getGenerator(), level.getRandom(), helper.absolutePos(air)), "the cluster must not generate in air");
        helper.succeed();
    }

    // ------------------------------------------------------------------ documentation: tooltips and JEI

    /** Every material tells in its tooltip where it comes from and what to do next, and every line is translated. */
    @GameTest(template = EMPTY)
    public static void materialsCarryTranslatedHints(GameTestHelper helper) {
        List<ItemLike> materials = List.of(
                ModItems.CRYONITE_ORE, ModItems.INFERNITE_ORE, ModItems.VOID_ORE, ModItems.INFERNITE_BLOCK, ModItems.PRIMORDIAL_SMELTER,
                ModItems.RAW_CRYONITE, ModItems.CRYONITE_CRYSTAL_DUST, ModItems.CRYONITE_INGOT,
                ModItems.MOLTEN_INFERNITE_SHARD, ModItems.COOLED_INFERNITE_BLANK, ModItems.INFERNITE_INGOT,
                ModItems.UNSTABLE_VOID_SHARD, ModItems.STABILIZED_VOID_CLUSTER, ModItems.MOLTEN_VOID_BLANK, ModItems.VOID_INGOT,
                ModItems.INACTIVE_AETHER, ModItems.AETHER_INGOT,
                ModItems.CRYONITE_UPGRADE_SMITHING_TEMPLATE, ModItems.INFERNITE_UPGRADE_SMITHING_TEMPLATE, ModItems.VOID_UPGRADE_SMITHING_TEMPLATE);
        for (ItemLike material : materials) {
            ItemStack stack = new ItemStack(material);
            List<Component> lines = new ArrayList<>();
            stack.getItem().appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.NORMAL);

            long hints = lines.stream()
                    .filter(line -> line.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith("tooltip.cryonite."))
                    .count();
            check(hints >= 2, stack.getItem() + " must carry at least two hint lines, found " + hints);
            lines.forEach(CryoniteGameTests::assertTranslated);
        }
        helper.succeed();
    }

    /** The JEI documentation recipes are complete, translated and agree with the mining tiers. */
    @GameTest(template = EMPTY)
    public static void jeiDocumentationMatchesTheMechanics(GameTestHelper helper) {
        List<WorldInteractionJeiRecipe> interactions = WorldInteractionJeiRecipe.all();
        check(interactions.size() == 4, "expected 4 world interactions, found " + interactions.size());
        for (WorldInteractionJeiRecipe recipe : interactions) {
            assertTranslated(recipe.description());
            check(!recipe.input().isEmpty() && !recipe.output().isEmpty(), recipe.id() + " has an empty input or output");
            check(!recipe.medium().isEmpty() && recipe.medium().stream().noneMatch(ItemStack::isEmpty), recipe.id() + " has no medium");
            check(recipe.ticks() > 0, recipe.id() + " needs a positive arrow cycle");
        }

        List<AcquisitionJeiRecipe> acquisitions = AcquisitionJeiRecipe.all();
        check(acquisitions.size() == 6, "expected 6 acquisition entries, found " + acquisitions.size());
        int ores = 0;
        for (AcquisitionJeiRecipe recipe : acquisitions) {
            assertTranslated(recipe.description());
            check(!recipe.result().isEmpty(), recipe.id() + " has no result");
            check(!recipe.means().isEmpty() && recipe.means().stream().noneMatch(ItemStack::isEmpty), recipe.id() + " has no tool or chest");
            check(!recipe.surroundings().isEmpty() && recipe.surroundings().stream().noneMatch(ItemStack::isEmpty), recipe.id() + " has no surroundings");

            // Every pickaxe an ore entry shows must really harvest that ore.
            if (recipe.silkTouch().getItem() instanceof BlockItem ore) {
                ores++;
                for (ItemStack pickaxe : recipe.means()) {
                    harvest(pickaxe.getItem(), ore.getBlock().defaultBlockState(), true);
                }
            }
        }
        check(ores == 3, "expected 3 ore entries, found " + ores);
        helper.succeed();
    }

    /** Fails when {@code text} or one of its siblings is a translatable component without an {@code en_us} entry. */
    private static void assertTranslated(Component text) {
        if (text.getContents() instanceof TranslatableContents contents) {
            check(Language.getInstance().has(contents.getKey()), "no en_us translation for " + contents.getKey());
            for (Object argument : contents.getArgs()) {
                if (argument instanceof Component component) {
                    assertTranslated(component);
                }
            }
        }
        text.getSiblings().forEach(CryoniteGameTests::assertTranslated);
    }

    // ------------------------------------------------------------------ helpers

    private static void equip(LivingEntity entity, GearTier tier) {
        ModItems.GearSet gear = ModItems.gear(tier);
        entity.setItemSlot(EquipmentSlot.HEAD, new ItemStack(gear.helmet().get()));
        entity.setItemSlot(EquipmentSlot.CHEST, new ItemStack(gear.chestplate().get()));
        entity.setItemSlot(EquipmentSlot.LEGS, new ItemStack(gear.leggings().get()));
        entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(gear.boots().get()));
    }

    // ================================================================ step 6: Dimension Terminal teleportation

    @GameTest(template = EMPTY)
    public static void dimensionTerminalRightClickOpensMenu(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ModBlocks.DIMENSION_TERMINAL.get());

        // DIMENSION_TERMINAL must be DimensionTerminalBlock (with right-click logic),
        // not a plain Block that does nothing on right-click.
        Block block = helper.getBlockState(pos).getBlock();
        check(block instanceof DimensionTerminalBlock,
                "terminal block must be DimensionTerminalBlock, got " + block.getClass().getName());

        // The menu type must be registered so the screen can open.
        check(ModMenuTypes.DIMENSION_TERMINAL.get() != null,
                "DIMENSION_TERMINAL menu type must be registered");

        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void dimensionTerminalTeleportsToMiningDimension(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            ServerLevel source = player.serverLevel();

            // Place the player at a known position so we can verify X/Z preservation
            BlockPos startPos = helper.absolutePos(new BlockPos(4, 4, 4));
            player.moveTo(startPos.getX() + 0.5, startPos.getY(), startPos.getZ() + 0.5, 0.0F, 0.0F);

            // Place a terminal at the start position (return terminal will be placed nearby)
            helper.setBlock(startPos, ModBlocks.DIMENSION_TERMINAL.get());

            // Жёсткая проверка: тип измерения шахты должен быть зарегистрирован в реестре сервера.
            var dimTypeRegistry = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
            var miningTypeKey = ResourceKey.create(Registries.DIMENSION_TYPE, Cryonite.id("mining_dimension"));
            check(dimTypeRegistry.getHolder(miningTypeKey).isPresent(),
                    "mining_dimension type must be registered in dimension type registry (JSON invalid?)");

            // Жёсткая проверка: уровень Шахтёрского измерения должен быть доступен для загрузки.
            var miningLevelKey = ResourceKey.create(Registries.DIMENSION, Cryonite.id("mining_dimension"));
            var miningRegistry = helper.getLevel().getServer().registryAccess().registryOrThrow(Registries.DIMENSION);
            check(miningRegistry.getHolder(miningLevelKey).isPresent(),
                    "mining_dimension must be registered in dimension registry (JSON invalid?)");

            // Телепортируем в Шахтёрское измерение (метод сам загрузит уровень если нужно).
            ModNetworking.teleportPlayer(player, Cryonite.id("mining_dimension"));

            // Игрок ДОЛЖЕН оказаться в Шахтёрском измерении, а не в Обычном мире.
            check(player.serverLevel().dimension().equals(miningLevelKey),
                    "player must be in mining_dimension after teleport, still in " + player.serverLevel().dimension());
            check(!player.serverLevel().equals(source),
                    "player must leave the source dimension, still in " + source.dimension());

            // Verify X, Z are preserved 1:1 (no /8 Nether scaling, no reset to 0,0)
            double dx = Math.abs(player.getX() - startPos.getX() - 0.5);
            double dz = Math.abs(player.getZ() - startPos.getZ() - 0.5);
            check(dx < 1.0 && dz < 1.0,
                    "X,Z must be preserved 1:1, got dx=" + dx + " dz=" + dz);

            helper.succeed();
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void dimensionTerminalNetherNotOnRoof(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);
            ServerLevel source = player.serverLevel();

            // Place the player at a known position
            BlockPos startPos = helper.absolutePos(new BlockPos(8, 8, 8));
            player.moveTo(startPos.getX() + 0.5, startPos.getY(), startPos.getZ() + 0.5, 0.0F, 0.0F);
            helper.setBlock(startPos, ModBlocks.DIMENSION_TERMINAL.get());

            // Телепортируем в Незер.
            ModNetworking.teleportPlayer(player, ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"));

            // Игрок должен оказаться в Незере.
            check(player.serverLevel().dimension().equals(ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"))),
                    "player must be in the_nether after teleport, in " + player.serverLevel().dimension());

            // Игрок НЕ должен быть на бедроковой крыше (Y >= 120). Ожидаем Y = 35..95.
            int playerY = player.getBlockY();
            check(playerY > 25 && playerY < 115,
                    "player must not be on Nether roof, Y=" + playerY + " (expected 35..95)");

            helper.succeed();
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void dimensionTerminalTeleportsDoesNotCrashWhenLevelMissing(GameTestHelper helper) {
        ServerPlayer player = mockPlayer(helper);
        try {
            player.setGameMode(GameType.SURVIVAL);

            // Teleport to a dimension that is not loaded on the test server.
            // teleportPlayer must handle this gracefully (send a message, return).
            ModNetworking.teleportPlayer(player, Cryonite.id("the_nether"));

            // Player should still be in the source dimension since the target level was not loaded.
            check(player.serverLevel() == helper.getLevel(),
                    "player must stay in the source dimension when target level is missing");

            helper.succeed();
        } finally {
            helper.getLevel().getServer().getPlayerList().remove(player);
        }
    }

    /**
     * {@link GameTestHelper#makeMockServerPlayerInLevel()} with a connection that takes the packets of every mod.
     *
     * <p>The vanilla mock connection has negotiated no channels, so NeoForge rejects every modded packet sent
     * to it. Create (part of the test server for the Kinetic Energy Converter) greets every player that logs
     * in with such packets and broadcasts more of them every tick, which failed the login of the mock player
     * and then the whole server. NeoForge's {@code configureMockConnection} makes the connection behave like
     * a NeoForge client that knows all registered payloads.</p>
     */
    private static ServerPlayer mockPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        NetworkRegistry.configureMockConnection(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    /** Puts a mock player at {@code from} (structure coordinates) looking horizontally towards {@code towards}. */
    private static void place(GameTestHelper helper, ServerPlayer player, Vec3 from, Vec3 towards) {
        Vec3 start = helper.absoluteVec(from);
        Vec3 direction = helper.absoluteVec(towards).subtract(start);
        float yaw = (float) (Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0);
        player.moveTo(start.x, start.y, start.z, yaw, 0.0F);
        // A real player's head follows the body every tick; a mock player is never ticked.
        player.setYHeadRot(yaw);
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static void near(double actual, double expected, String what) {
        if (Math.abs(actual - expected) > 1.0E-3) {
            throw new GameTestAssertException(what + ": expected " + expected + " but was " + actual);
        }
    }

    private CryoniteGameTests() {}
}
