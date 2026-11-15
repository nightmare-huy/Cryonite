package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.ability.WeaponAbilities;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

/**
 * Sword of the four tiers. On-hit abilities live in {@link WeaponAbilities};
 * the only item hook is the right-click dash unlocked by the Void tier.
 */
public class TieredSwordItem extends SwordItem implements TieredGear {
    private final GearTier gearTier;

    public TieredSwordItem(GearTier gearTier, Tier tier, Properties properties) {
        super(tier, properties);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier gearTier() {
        return gearTier;
    }

    @Override
    public Kind gearKind() {
        return Kind.SWORD;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!GearTier.VOID.unlockedAt(gearTier.level())) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            // The cooldown is applied (and synced to the client) by the server only when the dash succeeded.
            return WeaponAbilities.voidDash(serverPlayer, stack)
                    ? InteractionResultHolder.success(stack)
                    : InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.success(stack);
    }
}
