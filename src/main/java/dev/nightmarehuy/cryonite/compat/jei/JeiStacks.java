package dev.nightmarehuy.cryonite.compat.jei;

import dev.nightmarehuy.cryonite.registry.ModItems;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/** Stack lists for the slots of the documentation recipes. */
final class JeiStacks {
    static List<ItemStack> of(ItemLike... items) {
        List<ItemStack> stacks = new ArrayList<>(items.length);
        for (ItemLike item : items) {
            stacks.add(new ItemStack(item));
        }
        return List.copyOf(stacks);
    }

    /** The Netherite pickaxe and every pickaxe of the mod: all of them harvest Netherite-level blocks. */
    static List<ItemStack> netheritePickaxeOrBetter() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.NETHERITE_PICKAXE));
        stacks.addAll(pickaxeOrBetter(GearTier.CRYONITE));
        return List.copyOf(stacks);
    }

    /** The pickaxe of {@code minimum} first, then the pickaxes of the higher tiers. */
    static List<ItemStack> pickaxeOrBetter(GearTier minimum) {
        List<ItemStack> stacks = new ArrayList<>();
        for (GearTier tier : GearTier.values()) {
            if (tier.ordinal() >= minimum.ordinal()) {
                stacks.add(new ItemStack(ModItems.gear(tier).pickaxe().get()));
            }
        }
        return List.copyOf(stacks);
    }

    private JeiStacks() {}
}
