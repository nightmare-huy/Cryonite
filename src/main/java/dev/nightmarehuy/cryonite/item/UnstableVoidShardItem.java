package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Unstable Void Shard. Thrown into a cloud of Dragon's Breath (the lingering clouds of the dragon fight)
 * the whole stack settles into Stabilized Void Clusters.
 */
public class UnstableVoidShardItem extends Item {
    private static final int CHECK_INTERVAL = 5;
    private static final ItemHints HINTS = ItemHints.of("unstable_void_shard", ChatFormatting.DARK_PURPLE)
            .accent("source")
            .step("next", "craft");

    public UnstableVoidShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && entity.tickCount % CHECK_INTERVAL == 0 && isInsideDragonBreath(level, entity)) {
            entity.setItem(new ItemStack(ModItems.STABILIZED_VOID_CLUSTER.get(), stack.getCount()));
            level.sendParticles(ParticleTypes.DRAGON_BREATH, entity.getX(), entity.getY() + 0.25, entity.getZ(), 24, 0.2, 0.2, 0.2, 0.03);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 0.6F);
        }
        return false;
    }

    private static boolean isInsideDragonBreath(ServerLevel level, ItemEntity entity) {
        List<AreaEffectCloud> clouds = level.getEntitiesOfClass(AreaEffectCloud.class, entity.getBoundingBox().inflate(8.0, 1.5, 8.0),
                cloud -> cloud.getParticle().getType() == ParticleTypes.DRAGON_BREATH);
        for (AreaEffectCloud cloud : clouds) {
            double dx = entity.getX() - cloud.getX();
            double dz = entity.getZ() - cloud.getZ();
            if (dx * dx + dz * dz <= cloud.getRadius() * cloud.getRadius()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        HINTS.appendTo(tooltip);
    }
}
