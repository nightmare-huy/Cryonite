package dev.cryonite.block;

import dev.cryonite.recipe.ModRecipes;
import dev.cryonite.recipe.PrimordialRecipeInput;
import dev.cryonite.recipe.PrimordialSmeltingRecipe;
import dev.cryonite.registry.ModBlockEntities;
import dev.cryonite.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import dev.cryonite.menu.PrimordialSmelterMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.ContainerHelper;
import java.util.List;
import java.util.Optional;

/** Five physical slots: main input, two catalysts, recipe-specific fuel and output. */
public class PrimordialSmelterBlockEntity extends BlockEntity implements Container, MenuProvider {
    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int progress;
    private int totalTime = 200;

    public PrimordialSmelterBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.PRIMORDIAL_SMELTER.get(), pos, state); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrimordialSmelterBlockEntity be) {
        if (level.isClientSide) return;
        boolean changed = false;
        Optional<RecipeHolder<PrimordialSmeltingRecipe>> found = level.getRecipeManager().getRecipeFor(
                ModRecipes.PRIMORDIAL_TYPE.get(), new PrimordialRecipeInput(List.of(be.items.get(0), be.items.get(1), be.items.get(2), be.items.get(3), be.items.get(4))), level);
        if (found.isEmpty() || !be.canAccept(found.get().value().result())) {
            be.progress = 0;
            be.totalTime = found.map(holder -> holder.value().cookingTime()).orElse(200);
        } else {
            PrimordialSmeltingRecipe recipe = found.get().value();
            be.totalTime = recipe.cookingTime();
            if (++be.progress >= be.totalTime) {
                be.progress = 0;
                be.items.get(0).shrink(1);
                if (!recipe.catalyst1().isEmpty()) be.items.get(1).shrink(1);
                if (!recipe.catalyst2().isEmpty()) be.items.get(2).shrink(1);
                be.items.get(3).shrink(1);
                ItemStack out = recipe.result().copy();
                if (be.items.get(4).isEmpty()) be.items.set(4, out);
                else be.items.get(4).grow(out.getCount());
                changed = true;
            }
        }
        boolean lit = be.progress > 0;
        if (state.getValue(PrimordialSmelterBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(PrimordialSmelterBlock.LIT, lit), 3);
            changed = true;
        }
        if (changed) be.setChanged();
    }

    private boolean canAccept(ItemStack result) {
        ItemStack current = items.get(4);
        return current.isEmpty() || (ItemStack.isSameItemSameComponents(current, result) && current.getCount() + result.getCount() <= current.getMaxStackSize());
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Progress", progress);
        tag.putInt("TotalTime", totalTime);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("Progress");
        totalTime = Math.max(1, tag.getInt("TotalTime"));
    }
    @Override public int getContainerSize() { return 5; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack stack = ContainerHelper.removeItem(items, slot, amount); if (!stack.isEmpty()) setChanged(); return stack; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
    @Override public void setChanged() { super.setChanged(); }
    @Override public boolean stillValid(Player player) { return !isRemoved() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64.0; }
    @Override public void clearContent() { items.clear(); setChanged(); }
    public int getProgress() { return progress; }
    public int getTotalTime() { return totalTime; }
    @Override public Component getDisplayName() { return Component.translatable("container.cryonite.primordial_smelter"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new PrimordialSmelterMenu(id, inventory, this); }
}
