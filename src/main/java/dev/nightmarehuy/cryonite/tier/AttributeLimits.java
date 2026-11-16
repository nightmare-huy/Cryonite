package dev.nightmarehuy.cryonite.tier;

import dev.nightmarehuy.cryonite.Cryonite;
import java.lang.reflect.Field;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Raises the vanilla caps of armor (30) and armor toughness (20).
 *
 * <p>Vanilla clamps both attributes on every entity, whatever the items say. The doubled sets of this mod
 * reach 44 - 60 armor points and 32 - 64 toughness, so with the vanilla caps all four tiers would end up
 * with the very same 30 / 20 and the doubled numbers would only exist in the tooltip. NeoForge has no API
 * for the range of an attribute, the cap is the private field {@code RangedAttribute.maxValue}; it is
 * written once, while the mod is constructed, on the client and on the server alike (both clamp).</p>
 *
 * <p>The caps are only ever raised: a higher cap set by another mod stays as it is. The change is global,
 * armor of other mods is no longer cut off at 30 either.</p>
 */
public final class AttributeLimits {
    /** New cap of armor points and of armor toughness: the cap vanilla uses for health. */
    public static final double CAP = 1024.0;

    public static void raise() {
        raise(Attributes.ARMOR, "armor");
        raise(Attributes.ARMOR_TOUGHNESS, "armor toughness");
    }

    private static void raise(Holder<Attribute> holder, String name) {
        if (!(holder.value() instanceof RangedAttribute attribute)) {
            Cryonite.LOGGER.warn("The {} attribute is not a ranged attribute any more, its cap stays as it is", name);
            return;
        }
        double before = attribute.getMaxValue();
        if (before >= CAP) {
            return;
        }
        try {
            Field maxValue = RangedAttribute.class.getDeclaredField("maxValue");
            maxValue.setAccessible(true);
            maxValue.setDouble(attribute, CAP);
            Cryonite.LOGGER.info("Cap of {} raised from {} to {}", name, before, CAP);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Cryonite.LOGGER.error("Could not raise the cap of {}: it stays at {}, doubled armor above it has no effect", name, before, e);
        }
    }

    private AttributeLimits() {}
}
