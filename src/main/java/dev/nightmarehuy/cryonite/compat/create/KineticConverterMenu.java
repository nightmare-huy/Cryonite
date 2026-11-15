package dev.nightmarehuy.cryonite.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * Control panel of the Kinetic Energy Converter. It has no slots: the menu only carries the readouts
 * (energy, speed, state) to the client. Changes travel the other way with
 * {@link dev.nightmarehuy.cryonite.network.UpdateKineticConverterPayload}.
 *
 * <p>Vanilla synchronizes menu values as 16 bit numbers, so the energy (up to 10 000 000) is split in two.</p>
 */
public class KineticConverterMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final ContainerData data;

    /** Client side: position and first values come with the "open screen" packet. */
    public KineticConverterMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory, extraData.readBlockPos(), initialData(extraData));
    }

    /** Server side, {@code data} reads the block entity. */
    public KineticConverterMenu(int containerId, Inventory inventory, BlockPos pos, ContainerData data) {
        super(CreateCompat.KINETIC_CONVERTER_MENU.get(), containerId);
        checkContainerDataCount(data, KineticEnergyConverterBlockEntity.DATA_COUNT);
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
    }

    private static ContainerData initialData(RegistryFriendlyByteBuf extraData) {
        SimpleContainerData data = new SimpleContainerData(KineticEnergyConverterBlockEntity.DATA_COUNT);
        for (int i = 0; i < KineticEnergyConverterBlockEntity.DATA_COUNT; i++) {
            data.set(i, extraData.readVarInt());
        }
        return data;
    }

    public BlockPos pos() {
        return pos;
    }

    public int energy() {
        return (data.get(KineticEnergyConverterBlockEntity.DATA_ENERGY_HIGH) & 0xFFFF) << 16
                | (data.get(KineticEnergyConverterBlockEntity.DATA_ENERGY_LOW) & 0xFFFF);
    }

    public int targetRpm() {
        return data.get(KineticEnergyConverterBlockEntity.DATA_TARGET_RPM);
    }

    public boolean reversed() {
        return flag(KineticEnergyConverterBlockEntity.FLAG_REVERSED);
    }

    public boolean enabled() {
        return flag(KineticEnergyConverterBlockEntity.FLAG_ENABLED);
    }

    /** Switched on, set to a speed above zero and supplied with energy. */
    public boolean running() {
        return flag(KineticEnergyConverterBlockEntity.FLAG_RUNNING);
    }

    public boolean overstressed() {
        return flag(KineticEnergyConverterBlockEntity.FLAG_OVERSTRESSED);
    }

    /** Speed of the shaft right now in RPM, without its sign; 0 while stopped or overstressed. */
    public int speed() {
        return data.get(KineticEnergyConverterBlockEntity.DATA_SPEED) & 0xFFFF;
    }

    /** Stress capacity the converter adds to its network right now. */
    public long stressOutput() {
        return running() ? (long) data.get(KineticEnergyConverterBlockEntity.DATA_STRESS_MULTIPLIER) * speed() : 0;
    }

    /** Energy drawn from the buffer per tick right now. */
    public int consumption() {
        return running() ? KineticEnergyConverterBlockEntity.energyPerTick(targetRpm()) : 0;
    }

    private boolean flag(int bit) {
        return (data.get(KineticEnergyConverterBlockEntity.DATA_FLAGS) & bit) != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof KineticEnergyConverterBlockEntity
                && player.canInteractWithBlock(pos, 4.0);
    }
}
