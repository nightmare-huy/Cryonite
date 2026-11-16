package dev.nightmarehuy.cryonite.block;

import dev.nightmarehuy.cryonite.menu.PrimordialSmelterMenu;
import dev.nightmarehuy.cryonite.recipe.PrimordialRecipeInput;
import dev.nightmarehuy.cryonite.recipe.PrimordialSmeltingRecipe;
import dev.nightmarehuy.cryonite.registry.ModBlockEntities;
import dev.nightmarehuy.cryonite.registry.ModRecipes;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Block entity of the Primordial Smelter.
 *
 * <p>Five slots in one {@link ItemStackHandler}: main input, two catalysts, the special fuel and the output.
 * Unlike a vanilla furnace there is no generic burn time: a recipe only progresses while the fuel slot
 * holds exactly the fuel that recipe asks for, and one fuel item is burned per finished operation.</p>
 */
public class PrimordialSmelterBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CATALYST_A = 1;
    public static final int SLOT_CATALYST_B = 2;
    public static final int SLOT_FUEL = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int SLOT_COUNT = 5;

    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL_TIME = 1;
    public static final int DATA_COUNT = 2;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IItemHandler topAccess = new SidedAccess(new int[] {SLOT_INPUT}, false);
    private final IItemHandler sideAccess = new SidedAccess(new int[] {SLOT_FUEL, SLOT_CATALYST_A, SLOT_CATALYST_B}, false);
    private final IItemHandler bottomAccess = new SidedAccess(new int[] {SLOT_OUTPUT, SLOT_FUEL}, true);

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == DATA_PROGRESS ? progress : totalTime;
        }

        @Override
        public void set(int index, int value) {
            if (index == DATA_PROGRESS) {
                progress = value;
            } else {
                totalTime = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int progress;
    private int totalTime;
    @Nullable
    private RecipeHolder<PrimordialSmeltingRecipe> lastRecipe;

    public PrimordialSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRIMORDIAL_SMELTER.get(), pos, state);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    /** Item handler exposed to hoppers and pipes. */
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return items;
        }
        return switch (side) {
            case UP -> topAccess;
            case DOWN -> bottomAccess;
            default -> sideAccess;
        };
    }

    // ------------------------------------------------------------------ smelting

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrimordialSmelterBlockEntity smelter) {
        PrimordialRecipeInput input = smelter.recipeInput();
        RecipeHolder<PrimordialSmeltingRecipe> holder = smelter.findRecipe(level, input);
        boolean working = false;

        if (holder != null) {
            PrimordialSmeltingRecipe recipe = holder.value();
            ItemStack result = recipe.assemble(input, level.registryAccess());
            if (smelter.canOutput(result)) {
                working = true;
                smelter.totalTime = recipe.cookingTime();
                smelter.progress++;
                if (smelter.progress >= smelter.totalTime) {
                    smelter.progress = 0;
                    smelter.craft(recipe, input, result);
                }
                smelter.setChanged();
            }
        }

        if (!working && smelter.progress != 0) {
            // Any missing ingredient - including the special fuel - resets the operation.
            smelter.progress = 0;
            smelter.setChanged();
        }
        if (state.getValue(PrimordialSmelterBlock.LIT) != working) {
            level.setBlock(pos, state.setValue(PrimordialSmelterBlock.LIT, working), Block.UPDATE_ALL);
        }
    }

    private PrimordialRecipeInput recipeInput() {
        return new PrimordialRecipeInput(
                items.getStackInSlot(SLOT_INPUT),
                items.getStackInSlot(SLOT_CATALYST_A),
                items.getStackInSlot(SLOT_CATALYST_B),
                items.getStackInSlot(SLOT_FUEL));
    }

    @Nullable
    private RecipeHolder<PrimordialSmeltingRecipe> findRecipe(Level level, PrimordialRecipeInput input) {
        if (input.input().isEmpty() || input.fuel().isEmpty()) {
            lastRecipe = null;
            return null;
        }
        lastRecipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.PRIMORDIAL_SMELTING.get(), input, level, lastRecipe)
                .orElse(null);
        return lastRecipe;
    }

    private boolean canOutput(ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() + result.getCount() <= Math.min(output.getMaxStackSize(), items.getSlotLimit(SLOT_OUTPUT));
    }

    private void craft(PrimordialSmeltingRecipe recipe, PrimordialRecipeInput input, ItemStack result) {
        int[] catalystCosts = recipe.catalystCosts(input.catalystA(), input.catalystB());
        if (catalystCosts == null) {
            return;
        }
        ItemStack fuelRemainder = input.fuel().getCraftingRemainingItem();

        items.extractItem(SLOT_INPUT, recipe.input().count(), false);
        items.extractItem(SLOT_CATALYST_A, catalystCosts[0], false);
        items.extractItem(SLOT_CATALYST_B, catalystCosts[1], false);
        items.extractItem(SLOT_FUEL, 1, false);

        if (!fuelRemainder.isEmpty()) {
            // e.g. the empty bucket of a Lava Bucket or the bottle of Dragon's Breath
            if (items.getStackInSlot(SLOT_FUEL).isEmpty()) {
                items.setStackInSlot(SLOT_FUEL, fuelRemainder);
            } else if (level != null) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, fuelRemainder);
            }
        }

        ItemStack output = items.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.setStackInSlot(SLOT_OUTPUT, result);
        } else {
            items.setStackInSlot(SLOT_OUTPUT, output.copyWithCount(output.getCount() + result.getCount()));
        }
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < items.getSlots(); slot++) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), items.getStackInSlot(slot));
            items.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    // ------------------------------------------------------------------ persistence

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("TotalTime", totalTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Items")) {
            items.deserializeNBT(registries, tag.getCompound("Items"));
        }
        progress = tag.getInt("Progress");
        totalTime = tag.getInt("TotalTime");
    }

    // ------------------------------------------------------------------ menu

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.cryonite.primordial_smelter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PrimordialSmelterMenu(containerId, playerInventory, this, data);
    }

    /**
     * Side-aware view for automation: inserts go to the slots of the touched face (input on top, fuel and
     * catalysts on the sides), extraction is only possible from below - the output plus leftovers in the
     * fuel slot that no recipe can burn (empty buckets, bottles).
     */
    private final class SidedAccess implements IItemHandler {
        private final int[] slots;
        private final boolean extractOnly;

        private SidedAccess(int[] slots, boolean extractOnly) {
            this.slots = slots;
            this.extractOnly = extractOnly;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(slots[slot]);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? items.insertItem(slots[slot], stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!extractOnly) {
                return ItemStack.EMPTY;
            }
            int target = slots[slot];
            if (target == SLOT_FUEL && level != null && PrimordialSmeltingRecipe.isFuel(level, items.getStackInSlot(target))) {
                return ItemStack.EMPTY;
            }
            return items.extractItem(target, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(slots[slot]);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (extractOnly) {
                return false;
            }
            int target = slots[slot];
            boolean fuel = level != null && PrimordialSmeltingRecipe.isFuel(level, stack);
            return target == SLOT_FUEL ? fuel : true;
        }
    }
}
