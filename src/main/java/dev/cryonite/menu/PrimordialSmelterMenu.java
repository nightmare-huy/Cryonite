package dev.cryonite.menu;

import dev.cryonite.registry.ModMenus;
import dev.cryonite.block.PrimordialSmelterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

public class PrimordialSmelterMenu extends AbstractContainerMenu {
    private final Container inventory;
    private final ContainerLevelAccess access;
    private int synchronizedProgress;
    private int synchronizedTotal = 200;
    public PrimordialSmelterMenu(int id, Inventory playerInventory, FriendlyByteBuf data) {
        this(id, playerInventory, (PrimordialSmelterBlockEntity) playerInventory.player.level().getBlockEntity(data.readBlockPos()));
    }
    public PrimordialSmelterMenu(int id, Inventory playerInventory, PrimordialSmelterBlockEntity blockEntity) {
        super(ModMenus.PRIMORDIAL_SMELTER.get(), id);
        this.inventory = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        checkContainerSize(blockEntity, 5);
        blockEntity.startOpen(playerInventory.player);
        addDataSlot(new DataSlot() { @Override public int get() { return blockEntity.getProgress(); } @Override public void set(int value) { synchronizedProgress = value; } });
        addDataSlot(new DataSlot() { @Override public int get() { return blockEntity.getTotalTime(); } @Override public void set(int value) { synchronizedTotal = Math.max(1, value); } });
        addSlot(new Slot(blockEntity, 0, 26, 35));
        addSlot(new Slot(blockEntity, 1, 53, 35));
        addSlot(new Slot(blockEntity, 2, 80, 35));
        addSlot(new Slot(blockEntity, 3, 107, 35));
        addSlot(new Slot(blockEntity, 4, 143, 35) { @Override public boolean mayPlace(ItemStack stack) { return false; } });
        for (int row=0; row<3; row++) for (int col=0; col<9; col++) addSlot(new Slot(playerInventory, col+row*9+9, 8+col*18, 84+row*18));
        for (int col=0; col<9; col++) addSlot(new Slot(playerInventory, col, 8+col*18, 142));
    }
    @Override public boolean stillValid(Player player) { return stillValid(access, player, dev.cryonite.registry.ModBlocks.PRIMORDIAL_SMELTER.get()); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public void removed(Player player) { super.removed(player); inventory.stopOpen(player); }
    public int getProgress() { return synchronizedProgress; }
    public int getTotalTime() { return synchronizedTotal; }
}
