package dev.nightmarehuy.cryonite.tier;

/**
 * Implemented by every tool, weapon and armor item of the four tiers.
 * All ability code only ever looks at this interface, never at concrete item classes.
 */
public interface TieredGear {
    GearTier gearTier();

    Kind gearKind();

    enum Kind {
        SWORD(true, false),
        AXE(true, true),
        PICKAXE(false, true),
        SHOVEL(false, true),
        HOE(false, true),
        ARMOR(false, false);

        private final boolean weapon;
        private final boolean tool;

        Kind(boolean weapon, boolean tool) {
            this.weapon = weapon;
            this.tool = tool;
        }

        /** Triggers the on-hit weapon abilities. */
        public boolean isWeapon() {
            return weapon;
        }

        /** Triggers the mining abilities (auto-smelt, telekinesis, 3x3, ...). */
        public boolean isTool() {
            return tool;
        }
    }
}
