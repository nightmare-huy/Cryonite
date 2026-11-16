package dev.nightmarehuy.cryonite.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Adds {@code count} of {@code item} to a loot table with the given {@code chance}.
 * Used to put the upgrade templates into Ancient City, Bastion Treasure and End City chests.
 */
public class AddItemLootModifier extends LootModifier {
    public static final MapCodec<AddItemLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance).and(instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(modifier -> modifier.item),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(modifier -> modifier.chance),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(modifier -> modifier.count)
    )).apply(instance, AddItemLootModifier::new));

    private final Item item;
    private final float chance;
    private final int count;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, float chance, int count) {
        super(conditions);
        this.item = item;
        this.chance = chance;
        this.count = count;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(new ItemStack(item, count));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
