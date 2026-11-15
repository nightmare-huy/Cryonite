package dev.nightmarehuy.cryonite.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.nightmarehuy.cryonite.ability.ToolAbilities;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Tier 2+ tools: every drop that has a furnace recipe is replaced by its smelted result.
 *
 * <p>Global loot modifiers run after the loot table itself, so Fortune has already multiplied the raw ore
 * when it is smelted here - three raw iron become three ingots. The modifier covers block loot (ores, sand,
 * cobblestone, logs, ...) as well as mob loot (raw meat) and only acts while the auto-smelt data component
 * of the tool is switched on.</p>
 */
public class AutoSmeltLootModifier extends LootModifier {
    public static final MapCodec<AutoSmeltLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(instance -> codecStart(instance).apply(instance, AutoSmeltLootModifier::new));

    public AutoSmeltLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (generatedLoot.isEmpty() || !ToolAbilities.Mode.AUTO_SMELT.isActive(toolOf(context))) {
            return generatedLoot;
        }
        ServerLevel level = context.getLevel();
        ObjectArrayList<ItemStack> smelted = new ObjectArrayList<>(generatedLoot.size());
        for (ItemStack stack : generatedLoot) {
            smelted.add(smelt(level, stack));
        }
        return smelted;
    }

    /** The mining tool for block loot, the main hand of the killer for mob loot. */
    private static ItemStack toolOf(LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null) {
            return context.hasParam(LootContextParams.BLOCK_STATE) ? tool : ItemStack.EMPTY;
        }
        if (context.hasParam(LootContextParams.DAMAGE_SOURCE) && context.getParamOrNull(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity killer) {
            return killer.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack smelt(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return stack;
        }
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .map(holder -> holder.value().getResultItem(level.registryAccess()))
                .filter(result -> !result.isEmpty())
                .map(result -> result.copyWithCount(Math.min(stack.getCount() * result.getCount(), result.getMaxStackSize())))
                .orElse(stack);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
