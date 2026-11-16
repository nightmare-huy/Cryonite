package dev.nightmarehuy.cryonite.tier;

import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.registry.ModTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/**
 * Single source of truth for the numbers of the four tiers.
 *
 * <p>Every number below is twice its original value (the "2x" rebalance): damage, attack speed, dig speed,
 * durability, enchantability, armor points, toughness and knockback resistance. Full sets now add up to
 * 44 / 48 / 54 / 60 armor points and 32 / 40 / 52 / 64 toughness, far above the vanilla caps of 30 and 20,
 * so {@link AttributeLimits} raises those caps; knockback resistance cannot exceed 100% and stays capped.</p>
 *
 * <p>Damage values are the FINAL tooltip values (1.0 hand damage included); the attribute modifiers
 * handed to the vanilla item constructors are derived from them in {@code ModItems}.
 * Tiers are ordered: a tier of level N owns every ability of the levels 1..N
 * (see {@link dev.nightmarehuy.cryonite.ability.TierAbilityHandler}).</p>
 */
public enum GearTier {
    //         lvl  id           rarity            speed  bonus  uses  ench  sword  axe    pick   shovel  helmet/chest/legs/boots   tough  kbRes   armor durability
    CRYONITE(1, "cryonite", Rarity.RARE, 22.0F, 10.0F, 6144, 36, 20.0F, 24.0F, 14.0F, 15.0F, new int[] {8, 16, 14, 6}, 8.0F, 0.25F, 88),
    INFERNITE(2, "infernite", Rarity.UNCOMMON, 28.0F, 14.0F, 8192, 40, 26.0F, 31.0F, 17.0F, 18.0F, new int[] {8, 18, 14, 8}, 10.0F, 0.30F, 100),
    VOID(3, "void", Rarity.EPIC, 36.0F, 18.0F, 12288, 44, 34.0F, 40.0F, 20.0F, 21.0F, new int[] {10, 20, 16, 8}, 13.0F, 0.40F, 116),
    AETHER(4, "aether", Rarity.EPIC, 50.0F, 24.0F, 16384, 50, 48.0F, 56.0F, 26.0F, 27.0F, new int[] {10, 22, 18, 10}, 16.0F, 0.50F, 132);

    /** Final attack speeds, identical for all tiers. */
    public static final float SWORD_SPEED = 3.2F;
    public static final float AXE_SPEED = 2.0F;
    public static final float PICKAXE_SPEED = 2.4F;
    public static final float SHOVEL_SPEED = 2.0F;
    public static final float HOE_SPEED = 8.0F;
    public static final float HOE_DAMAGE = 2.0F;
    public static final float MULTITOOL_SPEED = 2.4F;
    /** A multitool hits two points harder than the sword of its tier. */
    public static final float MULTITOOL_DAMAGE_BONUS = 2.0F;

    private final int level;
    private final String id;
    private final Rarity rarity;
    private final float miningSpeed;
    private final float attackDamageBonus;
    private final int uses;
    private final int enchantability;
    private final float swordDamage;
    private final float axeDamage;
    private final float pickaxeDamage;
    private final float shovelDamage;
    private final int[] defense;
    private final float toughness;
    private final float knockbackResistance;
    private final int armorDurabilityMultiplier;

    GearTier(int level, String id, Rarity rarity, float miningSpeed, float attackDamageBonus, int uses, int enchantability,
             float swordDamage, float axeDamage, float pickaxeDamage, float shovelDamage,
             int[] defense, float toughness, float knockbackResistance, int armorDurabilityMultiplier) {
        this.level = level;
        this.id = id;
        this.rarity = rarity;
        this.miningSpeed = miningSpeed;
        this.attackDamageBonus = attackDamageBonus;
        this.uses = uses;
        this.enchantability = enchantability;
        this.swordDamage = swordDamage;
        this.axeDamage = axeDamage;
        this.pickaxeDamage = pickaxeDamage;
        this.shovelDamage = shovelDamage;
        this.defense = defense;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.armorDurabilityMultiplier = armorDurabilityMultiplier;
    }

    /** 1 (Cryonite) .. 4 (Aether). 0 is reserved for "no tier". */
    public int level() {
        return level;
    }

    /** Registry name prefix, e.g. {@code cryonite} in {@code cryonite_sword}. */
    public String id() {
        return id;
    }

    public Rarity rarity() {
        return rarity;
    }

    public float miningSpeed() {
        return miningSpeed;
    }

    public float attackDamageBonus() {
        return attackDamageBonus;
    }

    public int uses() {
        return uses;
    }

    public int enchantability() {
        return enchantability;
    }

    public float swordDamage() {
        return swordDamage;
    }

    public float axeDamage() {
        return axeDamage;
    }

    public float pickaxeDamage() {
        return pickaxeDamage;
    }

    public float shovelDamage() {
        return shovelDamage;
    }

    /** Final tooltip damage of the multitool: 22 / 28 / 36 / 50. */
    public float multitoolDamage() {
        return swordDamage + MULTITOOL_DAMAGE_BONUS;
    }

    public int helmetDefense() {
        return defense[0];
    }

    public int chestplateDefense() {
        return defense[1];
    }

    public int leggingsDefense() {
        return defense[2];
    }

    public int bootsDefense() {
        return defense[3];
    }

    public float toughness() {
        return toughness;
    }

    /** Knockback resistance granted by every single armor piece; a full set is immune to knockback in every tier. */
    public float knockbackResistance() {
        return knockbackResistance;
    }

    public int armorDurabilityMultiplier() {
        return armorDurabilityMultiplier;
    }

    /** {@code true} when gear of the given level (0..4) owns the abilities of this tier. */
    public boolean unlockedAt(int gearLevel) {
        return gearLevel >= level;
    }

    /** Blocks this tier can NOT harvest (the 1.21.1 replacement of numeric mining levels). */
    public TagKey<Block> incorrectBlocksForDrops() {
        return switch (this) {
            case CRYONITE -> ModTags.Blocks.INCORRECT_FOR_CRYONITE_TOOL;
            case INFERNITE -> ModTags.Blocks.INCORRECT_FOR_INFERNITE_TOOL;
            case VOID -> ModTags.Blocks.INCORRECT_FOR_VOID_TOOL;
            case AETHER -> ModTags.Blocks.INCORRECT_FOR_AETHER_TOOL;
        };
    }

    public TagKey<Item> gearTag() {
        return switch (this) {
            case CRYONITE -> ModTags.Items.CRYONITE_GEAR;
            case INFERNITE -> ModTags.Items.INFERNITE_GEAR;
            case VOID -> ModTags.Items.VOID_GEAR;
            case AETHER -> ModTags.Items.AETHER_GEAR;
        };
    }

    public ItemLike ingot() {
        return switch (this) {
            case CRYONITE -> ModItems.CRYONITE_INGOT;
            case INFERNITE -> ModItems.INFERNITE_INGOT;
            case VOID -> ModItems.VOID_INGOT;
            case AETHER -> ModItems.AETHER_INGOT;
        };
    }
}
