package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.registry.ModAttachments;
import dev.nightmarehuy.cryonite.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;

/**
 * Molten Void Blank. Dropped onto Crying Obsidian it is quenched by the void:
 * after 5 seconds (100 ticks) of portal particles the stack turns into Void Ingots.
 */
public class MoltenVoidBlankItem extends Item {
    public static final int QUENCH_TICKS = 100;

    public MoltenVoidBlankItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return false;
        }
        boolean onObsidian = entity.onGround() && level.getBlockState(entity.getOnPos()).is(Blocks.CRYING_OBSIDIAN);
        if (!onObsidian) {
            if (entity.hasData(ModAttachments.QUENCH_PROGRESS)) {
                entity.removeData(ModAttachments.QUENCH_PROGRESS);
            }
            return false;
        }

        int progress = entity.getData(ModAttachments.QUENCH_PROGRESS) + 1;
        if (progress % 2 == 0) {
            level.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 0.35, entity.getZ(), 6, 0.25, 0.25, 0.25, 0.4);
        }
        if (progress >= QUENCH_TICKS) {
            entity.removeData(ModAttachments.QUENCH_PROGRESS);
            entity.setItem(new ItemStack(ModItems.VOID_INGOT.get(), stack.getCount()));
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + 0.35, entity.getZ(), 40, 0.2, 0.2, 0.2, 0.08);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.NEUTRAL, 0.9F, 1.4F);
        } else {
            entity.setData(ModAttachments.QUENCH_PROGRESS, progress);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.cryonite.molten_void_blank").withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
