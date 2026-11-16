package dev.nightmarehuy.cryonite.compat.create;

import com.simibubi.create.infrastructure.config.AllConfigs;
import dev.nightmarehuy.cryonite.Cryonite;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * Lifts Create's rotation speed limit from 256 to {@value #MAX_RPM} RPM.
 *
 * <p>The limit is not a constant of Create but its server option {@code kinetics.maxRotationSpeed}
 * (default 256, minimum 64, no upper bound). The only place where the limit has consequences is
 * {@code RotationPropagator}, which breaks a block that would have to turn faster than the option allows -
 * everything else (shafts, cogwheels, belts, machines) simply takes the speed of its network. So instead of
 * patching Create with a Mixin the option itself is raised: this uses nothing but Create's own, supported
 * setting, keeps working when Create is updated and is synchronized to the clients by NeoForge like any
 * other server option.</p>
 *
 * <p>The option is raised once per server start, right after the server configs were loaded and before any
 * chunk is. A larger value chosen by the server owner is kept. A value lowered while the server is running is
 * respected until the next start: the converter never generates more than the current limit, see
 * {@link KineticEnergyConverterBlockEntity#updateGeneratedRotation()}.</p>
 */
public final class KineticSpeedLimit {
    /** Highest speed of the converter and the lowest value the Create limit is raised to. */
    public static final int MAX_RPM = 8048;

    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        raise();
    }

    /** Raises {@code kinetics.maxRotationSpeed} to at least {@value #MAX_RPM} and saves the server config. */
    public static void raise() {
        try {
            var limit = AllConfigs.server().kinetics.maxRotationSpeed;
            int before = limit.get();
            if (before < MAX_RPM) {
                limit.set(MAX_RPM);
                Cryonite.LOGGER.info("Create's kinetics.maxRotationSpeed raised from {} to {} RPM for the Kinetic Energy Converter", before, MAX_RPM);
            }
        } catch (RuntimeException | AssertionError e) {
            Cryonite.LOGGER.error("Could not raise Create's kinetics.maxRotationSpeed to {} RPM", MAX_RPM, e);
        }
    }

    /** The speed limit of kinetic networks that is in force right now. */
    public static int current() {
        try {
            return AllConfigs.server().kinetics.maxRotationSpeed.get();
        } catch (RuntimeException | AssertionError e) {
            return 256;     // the server config is not loaded (yet): Create's default
        }
    }

    private KineticSpeedLimit() {}
}
