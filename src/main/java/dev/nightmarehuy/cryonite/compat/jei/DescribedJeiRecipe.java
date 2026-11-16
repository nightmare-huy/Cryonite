package dev.nightmarehuy.cryonite.compat.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** A virtual JEI recipe that documents a world mechanic: a row of slots with an explanation below it. */
public interface DescribedJeiRecipe {
    /** Stable id, lets JEI bookmark the recipe. */
    ResourceLocation id();

    /** The text drawn below the slots; the JEI info pages show the same text. */
    Component description();
}
