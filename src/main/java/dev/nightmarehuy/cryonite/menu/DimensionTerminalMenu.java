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
        // Нет слотов: терминал телепортации не хранит предметы.
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
