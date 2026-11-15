package dev.nightmarehuy.cryonite.compat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import dev.nightmarehuy.cryonite.Cryonite;
import dev.nightmarehuy.cryonite.network.UpdateKineticConverterPayload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * GameTests of the Create integration. They run on a real dedicated server with Create installed
 * ({@code ./gradlew runGameTestServer}): real shafts are attached to the converter and have to survive 8048 RPM.
 */
@GameTestHolder(Cryonite.MODID)
@PrefixGameTestTemplate(false)
public final class KineticConverterGameTests {
    private static final String EMPTY = "empty";
    private static final BlockPos CONVERTER = new BlockPos(4, 2, 4);
    private static final int MAX_RPM = KineticEnergyConverterBlockEntity.MAX_RPM;

    /** Create's 256 RPM limit is its server option, and the mod raises it when the server starts. */
    @GameTest(template = EMPTY)
    public static void createSpeedLimitIsRaisedTo8048(GameTestHelper helper) {
        check(KineticSpeedLimit.current() >= MAX_RPM,
                "kinetics.maxRotationSpeed is " + KineticSpeedLimit.current() + ", expected at least " + MAX_RPM);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void kineticConverterClampsItsSettings(GameTestHelper helper) {
        KineticEnergyConverterBlockEntity converter = converter(helper, Direction.UP);
        check(converter.getTargetRpm() == KineticEnergyConverterBlockEntity.DEFAULT_RPM, "a new converter is set to 256 RPM");
        check(converter.isEnabled() && !converter.isReversed(), "a new converter is switched on and turns clockwise");

        converter.applySettings(99_999, true, false);
        check(converter.getTargetRpm() == MAX_RPM, "the speed is capped at 8048 RPM but was " + converter.getTargetRpm());
        check(converter.isReversed() && !converter.isEnabled(), "direction and power switch are stored");

        converter.applySettings(-5, false, true);
        check(converter.getTargetRpm() == 0, "the speed cannot be negative but was " + converter.getTargetRpm());

        converter.applySettings(1234, false, true);
        check(converter.getTargetRpm() == 1234, "a speed inside the range is stored as it is");

        check(KineticEnergyConverterBlockEntity.energyPerTick(MAX_RPM) == 32_192, "8048 RPM cost 32,192 FE per tick");
        check(KineticEnergyConverterBlockEntity.energyPerTick(0) == 1, "the cost never drops to zero");

        // The packet of the control panel survives the trip through the network.
        UpdateKineticConverterPayload payload = new UpdateKineticConverterPayload(new BlockPos(1, -60, 3), MAX_RPM, true, false);
        ByteBuf buffer = Unpooled.buffer();
        UpdateKineticConverterPayload.STREAM_CODEC.encode(buffer, payload);
        check(payload.equals(UpdateKineticConverterPayload.STREAM_CODEC.decode(buffer)), "the settings packet is decoded as it was encoded");
        buffer.release();
        helper.succeed();
    }

    /** The full chain: FE in the buffer, 8048 RPM on real Create shafts, the stress capacity, the cost, the reverse switch. */
    @GameTest(template = EMPTY, timeoutTicks = 120)
    public static void kineticConverterSpinsShaftsAt8048Rpm(GameTestHelper helper) {
        KineticEnergyConverterBlockEntity converter = converter(helper, Direction.UP);
        BlockState shaft = AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, Direction.Axis.Y);
        BlockPos lower = CONVERTER.above();
        BlockPos upper = CONVERTER.above(2);
        helper.setBlock(lower, shaft);
        helper.setBlock(upper, shaft);

        converter.setEnergy(KineticEnergyConverterBlockEntity.ENERGY_CAPACITY);
        converter.applySettings(MAX_RPM, false, true);

        int[] energy = new int[1];
        helper.runAfterDelay(20, () -> {
            check(converter.isRunning(), "a full converter runs");
            near(converter.getGeneratedSpeed(), MAX_RPM, "generated speed");
            near(converter.getSpeed(), MAX_RPM, "speed of the converter");
            near(speed(helper, lower), MAX_RPM, "speed of the first shaft");
            near(speed(helper, upper), MAX_RPM, "speed of the second shaft");
            near(converter.calculateAddedStressCapacity(), KineticEnergyConverterBlockEntity.DEFAULT_STRESS_MULTIPLIER, "stress capacity per RPM");
            near(converter.getOrCreateNetwork().calculateCapacity(), 8_241_152, "stress capacity of the network");
            check(!converter.isOverStressed(), "two shafts do not overstress the converter");
            energy[0] = converter.getEnergy();
        });
        helper.runAfterDelay(30, () -> {
            int spent = energy[0] - converter.getEnergy();
            check(spent == 10 * 32_192, "10 ticks at 8048 RPM cost 321,920 FE but " + spent + " FE were spent");
            converter.applySettings(MAX_RPM, true, true);
        });
        helper.runAfterDelay(50, () -> {
            near(converter.getSpeed(), -MAX_RPM, "speed of the reversed converter");
            near(speed(helper, upper), -MAX_RPM, "speed of the second shaft after the reversal");
            converter.applySettings(MAX_RPM, true, false);
        });
        helper.runAfterDelay(65, () -> {
            check(!converter.isRunning(), "a converter that is switched off does not run");
            near(converter.getSpeed(), 0, "speed of the converter that is switched off");
            near(speed(helper, upper), 0, "speed of the second shaft after the converter is switched off");
            int before = converter.getEnergy();
            helper.runAfterDelay(5, () -> {
                check(converter.getEnergy() == before, "a converter that is switched off spends no energy");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void kineticConverterStopsWithoutEnergy(GameTestHelper helper) {
        KineticEnergyConverterBlockEntity converter = converter(helper, Direction.NORTH);
        int cost = KineticEnergyConverterBlockEntity.energyPerTick(1000);
        converter.applySettings(1000, false, true);
        converter.setEnergy(25 * cost);

        helper.runAfterDelay(10, () -> {
            check(converter.isRunning(), "energy for 25 ticks starts the converter");
            near(Math.abs(converter.getSpeed()), 1000, "speed while the energy lasts");
        });
        helper.runAfterDelay(50, () -> {
            check(!converter.isRunning(), "the converter stops when the buffer is empty");
            near(converter.getSpeed(), 0, "speed without energy");
            check(converter.getEnergy() < cost, "the buffer was used up but holds " + converter.getEnergy() + " FE");
            // Less than the restart reserve: it stays off instead of flickering.
            converter.setEnergy((KineticEnergyConverterBlockEntity.RESTART_RESERVE_TICKS - 1) * cost);
        });
        helper.runAfterDelay(60, () -> {
            check(!converter.isRunning(), "a converter that ran dry waits for the restart reserve");
            converter.setEnergy(KineticEnergyConverterBlockEntity.RESTART_RESERVE_TICKS * cost);
        });
        helper.runAfterDelay(70, () -> {
            check(converter.isRunning(), "the converter starts again once the reserve is there");
            near(Math.abs(converter.getSpeed()), 1000, "speed after the restart");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void kineticConverterAcceptsEnergyOnFiveSides(GameTestHelper helper) {
        KineticEnergyConverterBlockEntity converter = converter(helper, Direction.EAST);
        converter.applySettings(0, false, false);       // switched off: the buffer only changes through the cables below
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(CONVERTER);

        for (Direction side : Direction.values()) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
            if (side == Direction.EAST) {
                check(storage == null, "the side with the shaft has no energy connection");
            } else {
                check(storage != null, "side " + side + " accepts energy");
                check(storage.canReceive() && !storage.canExtract(), "the buffer is insert-only");
            }
        }

        IEnergyStorage back = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.WEST);
        check(back != null, "the back accepts energy");
        check(back.receiveEnergy(500, true) == 500 && converter.getEnergy() == 0, "a simulated insert changes nothing");
        int accepted = back.receiveEnergy(2_500_000, false);
        check(accepted == KineticEnergyConverterBlockEntity.MAX_TRANSFER, "at most 1,000,000 FE per tick are accepted but " + accepted + " were");
        check(back.receiveEnergy(1, false) == 0, "the limit counts for the whole tick");
        check(converter.getEnergy() == KineticEnergyConverterBlockEntity.MAX_TRANSFER, "accepted energy is stored");
        check(back.extractEnergy(1000, false) == 0, "energy cannot be pulled out");

        helper.runAfterDelay(2, () -> {
            converter.setEnergy(KineticEnergyConverterBlockEntity.ENERGY_CAPACITY - 10);
            IEnergyStorage top = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.UP);
            check(top != null, "the top accepts energy");
            check(top.receiveEnergy(1000, false) == 10, "the buffer does not overflow");
            check(top.getEnergyStored() == top.getMaxEnergyStored(), "the buffer is full at 10,000,000 FE");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void kineticConverterHasItsData(GameTestHelper helper) {
        KineticEnergyConverterBlockEntity converter = converter(helper, Direction.UP);
        ServerLevel level = helper.getLevel();
        BlockState state = converter.getBlockState();

        check(state.is(BlockTags.MINEABLE_WITH_PICKAXE), "the converter is mined with a pickaxe");
        check(state.is(BlockTags.NEEDS_DIAMOND_TOOL), "the converter needs a diamond pickaxe");
        List<ItemStack> drops = Block.getDrops(state, level, helper.absolutePos(CONVERTER), converter, null, new ItemStack(Items.NETHERITE_PICKAXE));
        check(drops.size() == 1 && drops.get(0).is(CreateCompat.KINETIC_ENERGY_CONVERTER_ITEM.get()), "the converter drops itself but dropped " + drops);
        check(level.getRecipeManager().byKey(Cryonite.KINETIC_ENERGY_CONVERTER).isPresent(), "the crafting recipe of the converter is loaded");

        for (String key : List.of(
                "block.cryonite.kinetic_energy_converter",
                "container.cryonite.kinetic_energy_converter",
                "jei.cryonite.info.kinetic_energy_converter",
                "tooltip.cryonite.kinetic_energy_converter.purpose",
                "tooltip.cryonite.kinetic_energy_converter.energy",
                "tooltip.cryonite.kinetic_energy_converter.speed",
                "tooltip.cryonite.kinetic_energy_converter.gui",
                "gui.cryonite.kinetic_converter.target",
                "gui.cryonite.kinetic_converter.apply",
                "gui.cryonite.kinetic_converter.slider",
                "gui.cryonite.kinetic_converter.preset.min",
                "gui.cryonite.kinetic_converter.preset.max",
                "gui.cryonite.kinetic_converter.direction",
                "gui.cryonite.kinetic_converter.direction.cw",
                "gui.cryonite.kinetic_converter.direction.ccw",
                "gui.cryonite.kinetic_converter.power.on",
                "gui.cryonite.kinetic_converter.power.off",
                "gui.cryonite.kinetic_converter.speed",
                "gui.cryonite.kinetic_converter.output",
                "gui.cryonite.kinetic_converter.consumption",
                "gui.cryonite.kinetic_converter.energy",
                "gui.cryonite.kinetic_converter.energy.input",
                "gui.cryonite.kinetic_converter.status.off",
                "gui.cryonite.kinetic_converter.status.idle",
                "gui.cryonite.kinetic_converter.status.no_energy",
                "gui.cryonite.kinetic_converter.status.overstressed",
                "gui.cryonite.kinetic_converter.status.running")) {
            check(Language.getInstance().has(key), "no en_us translation for " + key);
        }
        helper.succeed();
    }

    // ------------------------------------------------------------------ helpers

    /** Places a converter in the middle of the test structure, its shaft towards {@code facing}. */
    private static KineticEnergyConverterBlockEntity converter(GameTestHelper helper, Direction facing) {
        helper.setBlock(CONVERTER, CreateCompat.KINETIC_ENERGY_CONVERTER.get().defaultBlockState()
                .setValue(KineticEnergyConverterBlock.FACING, facing));
        if (helper.getLevel().getBlockEntity(helper.absolutePos(CONVERTER)) instanceof KineticEnergyConverterBlockEntity converter) {
            return converter;
        }
        throw new GameTestAssertException("the converter has no block entity");
    }

    /** Speed of the kinetic block at {@code pos}; fails when Create broke it. */
    private static float speed(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof KineticBlockEntity kinetic) {
            return kinetic.getSpeed();
        }
        throw new GameTestAssertException("the kinetic block at " + pos + " is gone: " + helper.getBlockState(pos));
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static void near(double actual, double expected, String what) {
        if (Math.abs(actual - expected) > 1.0E-3) {
            throw new GameTestAssertException(what + ": expected " + expected + " but was " + actual);
        }
    }

    private KineticConverterGameTests() {}
}
