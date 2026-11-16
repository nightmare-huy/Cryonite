package dev.nightmarehuy.cryonite.client;

import dev.nightmarehuy.cryonite.ability.TierAbilityHandler;
import dev.nightmarehuy.cryonite.network.ToggleToolModePayload;
import dev.nightmarehuy.cryonite.tier.GearTier;
import dev.nightmarehuy.cryonite.tier.TieredGear;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FogType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Client-side game events: hotkeys, lava vision of the Infernite set and gear tooltips. */
public final class ClientEvents {
    /** How far the wearer of an Infernite set can see through lava, in blocks. */
    private static final float LAVA_VIEW_DISTANCE = 64.0F;

    // ------------------------------------------------------------------ hotkeys

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        // The server owns the data components: the client only reports the key press.
        while (ClientSetup.KEY_3X3.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleToolModePayload(ToggleToolModePayload.MODE_3X3));
        }
        while (ClientSetup.KEY_AUTOSMELT.consumeClick()) {
            PacketDistributor.sendToServer(new ToggleToolModePayload(ToggleToolModePayload.MODE_AUTO_SMELT));
        }
    }

    // ------------------------------------------------------------------ tier 2 armor: lava vision

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getType() == FogType.LAVA && hasInferniteSet()) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(LAVA_VIEW_DISTANCE);
            // NeoForge only applies the changed distances when the event is cancelled.
            event.setCanceled(true);
        }
    }

    /** The wearer does not burn, so the fire overlay would only block the view. */
    @SubscribeEvent
    public static void onScreenOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && hasInferniteSet()) {
            event.setCanceled(true);
        }
    }

    private static boolean hasInferniteSet() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && GearTier.INFERNITE.unlockedAt(TierAbilityHandler.armorSetLevel(player));
    }

    // ------------------------------------------------------------------ tooltips

    /**
     * Lists the abilities of a piece of gear. The list is built with the same cascade as the abilities
     * themselves: a tier N item shows the lines of the tiers 1..N.
     */
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof TieredGear gear)) {
            return;
        }
        List<Component> tooltip = event.getToolTip();

        // The state of auto-smelt and 3x3 is added by the items themselves (appendHoverText -> ToolAbilities).
        if (!Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.cryonite.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        TieredGear.Kind kind = gear.gearKind();
        int level = gear.gearTier().level();
        if (kind == TieredGear.Kind.ARMOR) {
            tooltip.add(Component.translatable("tooltip.cryonite.full_set").withStyle(ChatFormatting.GRAY));
            addAbilityLines(tooltip, "armor", level);
        }
        if (kind.isWeapon()) {
            tooltip.add(Component.translatable("tooltip.cryonite.on_hit").withStyle(ChatFormatting.GRAY));
            addAbilityLines(tooltip, "weapon", level);
        }
        if (kind.isTool()) {
            tooltip.add(Component.translatable("tooltip.cryonite.when_mining").withStyle(ChatFormatting.GRAY));
            addAbilityLines(tooltip, "tool", level);
        }
    }

    private static void addAbilityLines(List<Component> tooltip, String group, int level) {
        for (GearTier tier : GearTier.values()) {
            if (tier.unlockedAt(level)) {
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.cryonite.ability." + group + "." + tier.id()))
                        .withStyle(colorOf(tier)));
            }
        }
    }

    private static ChatFormatting colorOf(GearTier tier) {
        return switch (tier) {
            case CRYONITE -> ChatFormatting.AQUA;
            case INFERNITE -> ChatFormatting.GOLD;
            case VOID -> ChatFormatting.DARK_PURPLE;
            case AETHER -> ChatFormatting.YELLOW;
        };
    }

    private ClientEvents() {}
}
