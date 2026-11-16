package dev.nightmarehuy.cryonite.menu;

import dev.nightmarehuy.cryonite.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Пустой контейнер Пространственного Терминала: экран не хранит предметы, он только показывает карусель
 * измерений и отправляет запрос на телепортацию.
 */
public class DimensionTerminalMenu extends AbstractContainerMenu {

    public DimensionTerminalMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public DimensionTerminalMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuTypes.DIMENSION_TERMINAL.get(), containerId);
        // Нет собственных слотов: инвентарь игрока добавляется базовым классом через контейнер данных?
        // Добавим обычные слоты игрока для корректной работы меню (вкладка инвентаря открывается как у любого контейнера).
        int playerStart = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new net.minecraft.world.inventory.Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new net.minecraft.world.inventory.Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
