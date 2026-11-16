package dev.nightmarehuy.cryonite.menu;

import dev.nightmarehuy.cryonite.block.PrimordialSmelterBlockEntity;
import dev.nightmarehuy.cryonite.recipe.PrimordialSmeltingRecipe;
import dev.nightmarehuy.cryonite.registry.ModBlocks;
import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Menu of the Primordial Smelter.
 * Slot indices 0..4 mirror the block entity (input, catalyst 1, catalyst 2, special fuel, output),
 * followed by the 27 inventory and 9 hotbar slots of the player.
 */
public class PrimordialSmelterMenu extends AbstractContainerMenu {
    public static final int INPUT_X = 52;
    public static final int INPUT_Y = 17;
    public static final int CATALYST_A_X = 26;
    public static final int CATALYST_B_X = 78;
    public static final int FUEL_X = 52;
    public static final int FUEL_Y = 53;
    public static final int OUTPUT_X = 138;
    public static final int OUTPUT_Y = 35;

    private static final int MACHINE_SLOTS = PrimordialSmelterBlockEntity.SLOT_COUNT;
    private static final int PLAYER_START = MACHINE_SLOTS;
    private static final int PLAYER_END = PLAYER_START + 36;

    private final ContainerLevelAccess access;
    private final ContainerData data;
    private final Level level;

    /** Client side constructor: the real contents arrive through the vanilla slot sync. */
    public PrimordialSmelterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ItemStackHandler(MACHINE_SLOTS), new SimpleContainerData(PrimordialSmelterBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    /** Server side constructor. */
    public PrimordialSmelterMenu(int containerId, Inventory playerInventory, PrimordialSmelterBlockEntity smelter, ContainerData data) {
        this(containerId, playerInventory, smelter.getItems(), data, ContainerLevelAccess.create(smelter.getLevel(), smelter.getBlockPos()));
    }

    private PrimordialSmelterMenu(int containerId, Inventory playerInventory, IItemHandler items, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.PRIMORDIAL_SMELTER.get(), containerId);
        checkContainerDataCount(data, PrimordialSmelterBlockEntity.DATA_COUNT);
        this.access = access;
        this.data = data;
        this.level = playerInventory.player.level();

        addSlot(new SlotItemHandler(items, PrimordialSmelterBlockEntity.SLOT_INPUT, INPUT_X, INPUT_Y));
        addSlot(new SlotItemHandler(items, PrimordialSmelterBlockEntity.SLOT_CATALYST_A, CATALYST_A_X, INPUT_Y));
        addSlot(new SlotItemHandler(items, PrimordialSmelterBlockEntity.SLOT_CATALYST_B, CATALYST_B_X, INPUT_Y));
        addSlot(new SlotItemHandler(items, PrimordialSmelterBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PrimordialSmeltingRecipe.isFuel(level, stack);
            }
        });
        addSlot(new SlotItemHandler(items, PrimordialSmelterBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addDataSlots(data);
    }

    public boolean isWorking() {
        return data.get(PrimordialSmelterBlockEntity.DATA_PROGRESS) > 0;
    }

    /** Progress of the current operation scaled to {@code pixels}. */
    public int scaledProgress(int pixels) {
        int progress = data.get(PrimordialSmelterBlockEntity.DATA_PROGRESS);
        int total = data.get(PrimordialSmelterBlockEntity.DATA_TOTAL_TIME);
        return total > 0 && progress > 0 ? Math.min(pixels, progress * pixels / total) : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.PRIMORDIAL_SMELTER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (index < MACHINE_SLOTS) {
            // machine -> player (output prefers the hotbar end, like a furnace)
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, index == PrimordialSmelterBlockEntity.SLOT_OUTPUT)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
        } else {
            // player -> machine: fuels go to the fuel slot first, everything else to input / catalysts
            boolean moved = false;
            if (PrimordialSmeltingRecipe.isFuel(level, stack)) {
                moved = moveItemStackTo(stack, PrimordialSmelterBlockEntity.SLOT_FUEL, PrimordialSmelterBlockEntity.SLOT_FUEL + 1, false);
            }
            if (!moved) {
                moved = moveItemStackTo(stack, PrimordialSmelterBlockEntity.SLOT_INPUT, PrimordialSmelterBlockEntity.SLOT_CATALYST_B + 1, false);
            }
            if (!moved) {
                int hotbarStart = PLAYER_START + 27;
                moved = index < hotbarStart
                        ? moveItemStackTo(stack, hotbarStart, PLAYER_END, false)
                        : moveItemStackTo(stack, PLAYER_START, hotbarStart, false);
            }
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
