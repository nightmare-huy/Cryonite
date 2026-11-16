package dev.nightmarehuy.cryonite.compat.jei;

import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.item.MoltenVoidBlankItem;
import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * Virtual recipe of an in-world interaction (quenching, stabilization) that no crafting screen shows.
 *
 * @param input       the item the player uses or drops
 * @param medium      the block or medium it reacts with; several stacks are shown in turn
 * @param output      what the item turns into
 * @param description how to do it
 * @param ticks       cycle of the progress arrow, the real duration where the interaction has one
 */
public record WorldInteractionJeiRecipe(ResourceLocation id, ItemStack input, List<ItemStack> medium, ItemStack output, Component description, int ticks)
        implements DescribedJeiRecipe {

    /** Arrow cycle of the interactions that happen at once. */
    private static final int INSTANT = 40;

    public static String descriptionKey(String name) {
        return "jei.cryonite.world_interaction." + name;
    }

    /** Every in-world interaction of the mod, see the item classes of the inputs for the mechanics. */
    public static List<WorldInteractionJeiRecipe> all() {
        return List.of(
                of("powder_snow_cauldron", ModItems.MOLTEN_INFERNITE_SHARD, ModItems.COOLED_INFERNITE_BLANK, INSTANT,
                        Items.CAULDRON, Items.POWDER_SNOW_BUCKET),
                of("blue_ice", ModItems.MOLTEN_INFERNITE_SHARD, ModItems.COOLED_INFERNITE_BLANK, INSTANT,
                        Items.BLUE_ICE),
                of("dragon_breath", ModItems.UNSTABLE_VOID_SHARD, ModItems.STABILIZED_VOID_CLUSTER, INSTANT,
                        Items.DRAGON_BREATH),
                of("crying_obsidian", ModItems.MOLTEN_VOID_BLANK, ModItems.VOID_INGOT, MoltenVoidBlankItem.QUENCH_TICKS,
                        Items.CRYING_OBSIDIAN));
    }

    private static WorldInteractionJeiRecipe of(String name, ItemLike input, ItemLike output, int ticks, ItemLike... medium) {
        return new WorldInteractionJeiRecipe(
                Cryonite.id("world_interaction/" + name),
                new ItemStack(input),
                JeiStacks.of(medium),
                new ItemStack(output),
                Component.translatable(descriptionKey(name)),
                ticks);
    }
}
