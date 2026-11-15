package dev.nightmarehuy.cryonite.compat.create;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Block entity of the Kinetic Energy Converter: a buffer of Forge Energy that pays for rotation.
 *
 * <ul>
 *   <li>Buffer of {@value #ENERGY_CAPACITY} FE, filled with up to {@value #MAX_TRANSFER} FE per tick through
 *       every side but the one with the shaft.</li>
 *   <li>While it is switched on and the buffer covers the cost of {@code max(1, targetRpm * 4)} FE per tick it
 *       turns its shaft with {@code targetRpm} (0 to {@value #MAX_RPM}) and adds {@code stressMultiplier} SU
 *       per RPM to the network - 8 241 152 SU at full speed.</li>
 *   <li>Out of energy or switched off it generates nothing and stops being a source of its network.</li>
 * </ul>
 *
 * <p>Create asks {@link #getGeneratedSpeed()} at any time, but a network only follows a new speed when
 * {@link #updateGeneratedRotation()} rebuilds it. The speed handed to Create is therefore a snapshot that is
 * taken in exactly that method, and changes made in the control panel reach the network a few ticks after
 * the last one ({@link #UPDATE_DELAY}): dragging the slider does not rebuild the network on every step.</p>
 */
public class KineticEnergyConverterBlockEntity extends GeneratingKineticBlockEntity implements MenuProvider {
    public static final int MAX_RPM = KineticSpeedLimit.MAX_RPM;
    public static final int DEFAULT_RPM = 256;
    public static final int ENERGY_CAPACITY = 10_000_000;
    /** Most energy the buffer accepts within one tick, from all sides together. */
    public static final int MAX_TRANSFER = 1_000_000;
    public static final int ENERGY_PER_RPM = 4;
    /** Stress capacity per RPM: 1024 SU/RPM are 8 241 152 SU at 8048 RPM. */
    public static final int DEFAULT_STRESS_MULTIPLIER = 1024;
    /** Ticks between the last change of the settings and the rebuild of the kinetic network. */
    public static final int UPDATE_DELAY = 5;
    /** A converter that ran dry starts again once it could run for this many ticks, so a weak supply does not make it flicker. */
    public static final int RESTART_RESERVE_TICKS = 20;

    // Values of the control panel, see KineticConverterMenu.
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_TARGET_RPM = 2;
    public static final int DATA_FLAGS = 3;
    public static final int DATA_SPEED = 4;
    public static final int DATA_STRESS_MULTIPLIER = 5;
    public static final int DATA_COUNT = 6;
    public static final int FLAG_REVERSED = 1;
    public static final int FLAG_ENABLED = 2;
    public static final int FLAG_RUNNING = 4;
    public static final int FLAG_OVERSTRESSED = 8;

    private static final String KEY = "gui.cryonite.kinetic_converter.";

    private final Buffer energyBuffer = new Buffer();

    private int targetRpm = DEFAULT_RPM;
    private boolean reversed;
    private boolean enabled = true;
    private int stressMultiplier = DEFAULT_STRESS_MULTIPLIER;

    private int energy;
    private int receivedThisTick;
    /** Switched on, set to a speed above zero and paid for. */
    private boolean running;
    /** The speed the kinetic network was last given, see {@link #updateGeneratedRotation()}. */
    private float generatedSpeed;
    /** Ticks until the changed settings are applied to the network, -1 when nothing is pending. */
    private int pendingUpdate = -1;

    private final ContainerData menuData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> energy & 0xFFFF;
                case DATA_ENERGY_HIGH -> energy >>> 16;
                case DATA_TARGET_RPM -> targetRpm;
                case DATA_FLAGS -> (reversed ? FLAG_REVERSED : 0) | (enabled ? FLAG_ENABLED : 0)
                        | (running ? FLAG_RUNNING : 0) | (isOverStressed() ? FLAG_OVERSTRESSED : 0);
                case DATA_SPEED -> Math.round(Math.abs(getSpeed()));
                case DATA_STRESS_MULTIPLIER -> stressMultiplier;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // The panel only displays these values; changes arrive with UpdateKineticConverterPayload.
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public KineticEnergyConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(20);
    }

    public KineticEnergyConverterBlockEntity(BlockPos pos, BlockState state) {
        this(CreateCompat.KINETIC_ENERGY_CONVERTER_ENTITY.get(), pos, state);
    }

    // ------------------------------------------------------------------ settings

    /**
     * New settings from the control panel. The speed is clamped to 0..{@value #MAX_RPM}; the kinetic network
     * follows {@value #UPDATE_DELAY} ticks after the last change.
     */
    public void applySettings(int rpm, boolean reversed, boolean enabled) {
        rpm = Mth.clamp(rpm, 0, MAX_RPM);
        if (rpm == targetRpm && reversed == this.reversed && enabled == this.enabled) {
            return;
        }
        this.targetRpm = rpm;
        this.reversed = reversed;
        this.enabled = enabled;
        if (level != null && !level.isClientSide) {
            pendingUpdate = UPDATE_DELAY;
            notifyUpdate();
        }
    }

    public int getTargetRpm() {
        return targetRpm;
    }

    public boolean isReversed() {
        return reversed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** True while the converter is switched on, set to a speed above zero and supplied with energy. */
    public boolean isRunning() {
        return running;
    }

    public int getStressMultiplier() {
        return stressMultiplier;
    }

    public int getEnergy() {
        return energy;
    }

    /** Sets the buffer directly (tests, commands); cables go through {@link #getEnergyStorage(Direction)}. */
    public void setEnergy(int energy) {
        this.energy = Mth.clamp(energy, 0, ENERGY_CAPACITY);
        setChanged();
    }

    /** Energy the converter needs per tick to turn with {@code rpm}. */
    public static int energyPerTick(int rpm) {
        return Math.max(1, rpm * ENERGY_PER_RPM);
    }

    /** The buffer as seen from {@code side}: every side accepts energy except the one the shaft leaves through. */
    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        BlockState state = getBlockState();
        if (side != null && state.hasProperty(KineticEnergyConverterBlock.FACING) && side == state.getValue(KineticEnergyConverterBlock.FACING)) {
            return null;
        }
        return energyBuffer;
    }

    // ------------------------------------------------------------------ kinetics

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || Math.abs(getGeneratedSpeed()) > Math.abs(getTheoreticalSpeed())) {
            updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }
        receivedThisTick = 0;

        boolean wasRunning = running;
        boolean wanted = enabled && targetRpm > 0;
        int cost = energyPerTick(targetRpm);
        if (!wanted) {
            running = false;
        } else if (running) {
            if (energy >= cost) {
                energy -= cost;
                setChanged();
            } else {
                running = false;
            }
        } else if (energy >= Math.min((long) cost * RESTART_RESERVE_TICKS, ENERGY_CAPACITY)) {
            energy -= cost;
            running = true;
            setChanged();
        }

        // Starting and stopping reaches the network at once, a new speed or direction after the delay.
        if (running != wasRunning) {
            pendingUpdate = 0;
        }
        if (pendingUpdate == 0) {
            updateGeneratedRotation();
        } else if (pendingUpdate > 0) {
            pendingUpdate--;
        }
    }

    /** Safety net: follows a speed limit of Create that was changed while the converter is running. */
    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide && pendingUpdate < 0 && wantedSpeed() != generatedSpeed) {
            updateGeneratedRotation();
        }
    }

    /** The speed the current settings ask for: 0 unless running, never above the speed limit of Create. */
    private float wantedSpeed() {
        BlockState state = getBlockState();
        if (!running || !state.hasProperty(KineticEnergyConverterBlock.FACING)) {
            return 0;
        }
        int rpm = Math.min(targetRpm, KineticSpeedLimit.current());
        if (rpm <= 0) {
            return 0;
        }
        return convertToDirection(reversed ? -rpm : rpm, state.getValue(KineticEnergyConverterBlock.FACING));
    }

    /** {@code reversed ? -targetRpm : targetRpm} seen along the shaft while running, 0 otherwise. */
    @Override
    public float getGeneratedSpeed() {
        return generatedSpeed;
    }

    /** Takes over the current settings and lets Create rebuild the network around the new speed. */
    @Override
    public void updateGeneratedRotation() {
        if (level != null && !level.isClientSide) {
            pendingUpdate = -1;
            generatedSpeed = wantedSpeed();
        }
        super.updateGeneratedRotation();
    }

    /** Create multiplies this with the generated speed: {@code stressMultiplier} SU per RPM. */
    @Override
    public float calculateAddedStressCapacity() {
        float capacity = stressMultiplier;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    // ------------------------------------------------------------------ persistence and sync

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("TargetRpm", targetRpm);
        tag.putBoolean("Reversed", reversed);
        tag.putBoolean("Enabled", enabled);
        tag.putInt("StressMultiplier", stressMultiplier);
        tag.putInt("Energy", energy);
        tag.putBoolean("Running", running);
        tag.putFloat("GeneratedSpeed", generatedSpeed);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        targetRpm = tag.contains("TargetRpm") ? Mth.clamp(tag.getInt("TargetRpm"), 0, MAX_RPM) : DEFAULT_RPM;
        reversed = tag.getBoolean("Reversed");
        enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled");
        stressMultiplier = tag.contains("StressMultiplier") ? Math.max(0, tag.getInt("StressMultiplier")) : DEFAULT_STRESS_MULTIPLIER;
        energy = Mth.clamp(tag.getInt("Energy"), 0, ENERGY_CAPACITY);
        running = tag.getBoolean("Running");
        generatedSpeed = tag.getFloat("GeneratedSpeed");
    }

    // ------------------------------------------------------------------ control panel

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.cryonite.kinetic_energy_converter");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new KineticConverterMenu(containerId, inventory, worldPosition, menuData);
    }

    /** Extra data of the "open screen" packet: the position and the values the panel shows in its first frame. */
    public void writeMenuData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(worldPosition);
        for (int i = 0; i < DATA_COUNT; i++) {
            buffer.writeVarInt(menuData.get(i));
        }
    }

    // ------------------------------------------------------------------ Engineer's Goggles

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable(KEY + "energy", grouped(energy), grouped(ENERGY_CAPACITY))
                .withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("    ").append(Component.translatable(KEY + "consumption", grouped(running ? energyPerTick(targetRpm) : 0))
                .withStyle(ChatFormatting.AQUA)));
        return true;
    }

    /** 8241152 -> "8,241,152" */
    public static String grouped(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    // ------------------------------------------------------------------ energy

    /** Insert-only view of the buffer for cables. */
    private final class Buffer implements IEnergyStorage {
        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            if (toReceive <= 0) {
                return 0;
            }
            int accepted = Math.min(toReceive, Math.min(ENERGY_CAPACITY - energy, MAX_TRANSFER - receivedThisTick));
            if (accepted <= 0) {
                return 0;
            }
            if (!simulate) {
                energy += accepted;
                receivedThisTick += accepted;
                setChanged();
            }
            return accepted;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return ENERGY_CAPACITY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
