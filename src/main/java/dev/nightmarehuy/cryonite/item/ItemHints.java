package dev.nightmarehuy.cryonite.item;

import dev.nightmarehuy.cryonite.tier.GearTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * Short coloured inventory hints of a material: where it comes from, what it does to its bearer
 * and what to do with it next.
 *
 * <p>Lines in the accent colour of the tier (aqua for Cryonite, gold for Infernite and Aether, dark purple
 * for Void) describe the item itself - its source, the required pickaxe, its danger. Gray lines are the
 * next processing steps. Every line is the translation {@code tooltip.cryonite.<item>.<suffix>}.</p>
 */
public final class ItemHints {
    private final String item;
    private final ChatFormatting accent;
    private final List<Line> lines = new ArrayList<>();

    private ItemHints(String item, ChatFormatting accent) {
        this.item = item;
        this.accent = accent;
    }

    /**
     * @param item   registry path of the item, the middle part of the translation keys
     * @param accent colour of the lines added with {@link #accent(String...)}
     */
    public static ItemHints of(String item, ChatFormatting accent) {
        return new ItemHints(item, accent);
    }

    /** Accent colour of a tier: aqua for Cryonite, gold for Infernite and Aether, dark purple for Void. */
    public static ChatFormatting accentOf(GearTier tier) {
        return switch (tier) {
            case CRYONITE -> ChatFormatting.AQUA;
            case INFERNITE, AETHER -> ChatFormatting.GOLD;
            case VOID -> ChatFormatting.DARK_PURPLE;
        };
    }

    /** Translation key of one hint line; the JEI info pages reuse some of them. */
    public static String key(String item, String suffix) {
        return "tooltip.cryonite." + item + "." + suffix;
    }

    /** Lines about the item itself, drawn in the accent colour. */
    public ItemHints accent(String... suffixes) {
        for (String suffix : suffixes) {
            lines.add(new Line(key(item, suffix), accent));
        }
        return this;
    }

    /** Lines about the next processing step, drawn in gray. */
    public ItemHints step(String... suffixes) {
        for (String suffix : suffixes) {
            lines.add(new Line(key(item, suffix), ChatFormatting.GRAY));
        }
        return this;
    }

    public void appendTo(List<Component> tooltip) {
        for (Line line : lines) {
            tooltip.add(Component.translatable(line.key()).withStyle(line.color()));
        }
    }

    private record Line(String key, ChatFormatting color) {}
}
