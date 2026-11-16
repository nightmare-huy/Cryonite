package dev.nightmarehuy.cryonite.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.GlobalPos;

/**
 * Player attachment of the Void armor: the last solid block the wearer stood on and the game time
 * until which the void rescue is on cooldown.
 */
public record VoidRescueData(Optional<GlobalPos> lastSafePos, long cooldownUntil) {
    public static final VoidRescueData EMPTY = new VoidRescueData(Optional.empty(), 0L);

    public static final Codec<VoidRescueData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.optionalFieldOf("last_safe_pos").forGetter(VoidRescueData::lastSafePos),
            Codec.LONG.optionalFieldOf("cooldown_until", 0L).forGetter(VoidRescueData::cooldownUntil)
    ).apply(instance, VoidRescueData::new));

    public VoidRescueData withSafePos(GlobalPos pos) {
        return new VoidRescueData(Optional.of(pos), cooldownUntil);
    }

    public VoidRescueData withCooldownUntil(long gameTime) {
        return new VoidRescueData(lastSafePos, gameTime);
    }
}
