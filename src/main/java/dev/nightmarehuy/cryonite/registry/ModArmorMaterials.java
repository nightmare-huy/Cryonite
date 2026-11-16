package dev.nightmarehuy.cryonite.registry;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> REGISTER = DeferredRegister.create(Registries.ARMOR_MATERIAL, Cryonite.MODID);

    private static final Map<GearTier, Holder<ArmorMaterial>> MATERIALS = new EnumMap<>(GearTier.class);

    public static final Holder<ArmorMaterial> CRYONITE = register(GearTier.CRYONITE, SoundEvents.ARMOR_EQUIP_DIAMOND);
    public static final Holder<ArmorMaterial> INFERNITE = register(GearTier.INFERNITE, SoundEvents.ARMOR_EQUIP_NETHERITE);
    public static final Holder<ArmorMaterial> VOID = register(GearTier.VOID, SoundEvents.ARMOR_EQUIP_NETHERITE);
    public static final Holder<ArmorMaterial> AETHER = register(GearTier.AETHER, SoundEvents.ARMOR_EQUIP_NETHERITE);

    private static Holder<ArmorMaterial> register(GearTier tier, Holder<SoundEvent> equipSound) {
        Holder<ArmorMaterial> holder = REGISTER.register(tier.id(), () -> {
            EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
            defense.put(ArmorItem.Type.HELMET, tier.helmetDefense());
            defense.put(ArmorItem.Type.CHESTPLATE, tier.chestplateDefense());
            defense.put(ArmorItem.Type.LEGGINGS, tier.leggingsDefense());
            defense.put(ArmorItem.Type.BOOTS, tier.bootsDefense());
            defense.put(ArmorItem.Type.BODY, tier.chestplateDefense());
            return new ArmorMaterial(
                    defense,
                    tier.enchantability(),
                    equipSound,
                    () -> Ingredient.of(tier.ingot()),
                    // textures/models/armor/<tier>_layer_1.png and <tier>_layer_2.png
                    List.of(new ArmorMaterial.Layer(Cryonite.id(tier.id()))),
                    tier.toughness(),
                    tier.knockbackResistance());
        });
        MATERIALS.put(tier, holder);
        return holder;
    }

    public static Holder<ArmorMaterial> get(GearTier tier) {
        return MATERIALS.get(tier);
    }

    private ModArmorMaterials() {}
}
