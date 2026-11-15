package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.ability.TierAbilityHandler;
import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Raw Cryonite radiates cold: while carried it slowly freezes its bearer
 * (Slowness I -> III together with the vanilla freezing meter).
 * Anything that makes an entity freeze-immune (leather, Cryonite armor) or an ultra-warm dimension stops it.
 */
public class RawCryoniteItem extends Item {
    /** The vanilla meter thaws by 2 per tick; +2 holds it, the extra point every 4th tick makes it creep up. */
    private static final int HOLD_GAIN = 2;
    private static final int CREEP_INTERVAL = 4;
    private static final int EFFECT_INTERVAL = 20;
    private static final int EFFECT_DURATION = 60;

    public RawCryoniteItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide || !(entity instanceof LivingEntity living) || !isAffected(living, level)) {
            return;
        }

        int required = living.getTicksRequiredToFreeze();
        int gain = HOLD_GAIN + (living.tickCount % CREEP_INTERVAL == 0 ? 1 : 0);
        living.setTicksFrozen(Math.min(required, living.getTicksFrozen() + gain));

        if (living.tickCount % EFFECT_INTERVAL == 0) {
            float frozen = living.getPercentFrozen();
            int amplifier = frozen < 0.34F ? 0 : frozen < 0.67F ? 1 : 2;
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION, amplifier, true, false, true));
        }
    }

    private static boolean isAffected(LivingEntity living, Level level) {
        if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (level.dimensionType().ultraWarm() || living.isOnFire() || !living.canFreeze()) {
            return false;
        }
        return !GearTier.CRYONITE.unlockedAt(TierAbilityHandler.armorSetLevel(living));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cryonite.raw_cryonite").withStyle(ChatFormatting.AQUA));
    }
}
