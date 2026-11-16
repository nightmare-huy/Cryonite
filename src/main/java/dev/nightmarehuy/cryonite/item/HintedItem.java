package dev.nightmarehuy.cryonite.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A plain material whose tooltip tells where it comes from and what to do with it next. */
public class HintedItem extends Item {
    private final ItemHints hints;

    public HintedItem(Properties properties, ItemHints hints) {
        super(properties);
        this.hints = hints;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        hints.appendTo(tooltip);
    }
}
