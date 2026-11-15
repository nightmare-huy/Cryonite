package dev.nightmarehuy.cryonite.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Block item (ores, Block of Infernite, Primordial Smelter) with {@link ItemHints} in its tooltip. */
public class HintedBlockItem extends BlockItem {
    private final ItemHints hints;

    public HintedBlockItem(Block block, Properties properties, ItemHints hints) {
        super(block, properties);
        this.hints = hints;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        hints.appendTo(tooltip);
    }
}
