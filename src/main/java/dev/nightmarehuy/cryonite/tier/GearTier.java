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
 * <p>Damage values are the FINAL tooltip values (1.0 hand damage included); the attribute modifiers
 * handed to the vanilla item constructors are derived from them in {@code ModItems}.
 * Tiers are ordered: a tier of level N owns every ability of the levels 1..N
 * (see {@link dev.nightmarehuy.cryonite.ability.TierAbilityHandler}).</p>
 */
public enum GearTier {
    //         lvl  id           rarity            speed  bonus  uses  ench  sword  axe    pick   shovel  helmet/chest/legs/boots   tough  kbRes   armor durability
    CRYONITE(1, "cryonite", Rarity.RARE, 11.0F, 5.0F, 3072, 18, 10.0F, 12.0F, 7.0F, 7.5F, new int[] {4, 8, 7, 3}, 4.0F, 0.125F, 44),
    INFERNITE(2, "infernite", Rarity.UNCOMMON, 14.0F, 7.0F, 4096, 20, 13.0F, 15.5F, 8.5F, 9.0F, new int[] {4, 9, 7, 4}, 5.0F, 0.15F, 50),
    VOID(3, "void", Rarity.EPIC, 18.0F, 9.0F, 6144, 22, 17.0F, 20.0F, 10.0F, 10.5F, new int[] {5, 10, 8, 4}, 6.5F, 0.20F, 58),
    AETHER(4, "aether", Rarity.EPIC, 25.0F, 12.0F, 8192, 25, 24.0F, 28.0F, 13.0F, 13.5F, new int[] {5, 11, 9, 5}, 8.0F, 0.25F, 66);

    /** Final attack speeds, identical for all tiers. */
    public static final float SWORD_SPEED = 1.6F;
    public static final float AXE_SPEED = 1.0F;
    public static final float PICKAXE_SPEED = 1.2F;
    public static final float SHOVEL_SPEED = 1.0F;
    public static final float HOE_SPEED = 4.0F;
    public static final float HOE_DAMAGE = 1.0F;

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

    /** Knockback resistance granted by every single armor piece. */
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
