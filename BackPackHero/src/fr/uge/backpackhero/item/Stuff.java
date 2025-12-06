package fr.uge.backpackhero.item;

/**
 * Enumeration listing all known item types available for generation in the game.
 * Each item type is associated with a specific {@code Rarity}.
 */
public enum Stuff {
	/** A common melee weapon. */
    WoodSword(Rarity.COMMON),
    
    /** An uncommon ranged weapon. */
    MouseBow(Rarity.UNCOMMON),
    
    /** A common consumable item (implied, based on name). */
    ShortArrow(Rarity.COMMON),
    
    /** A rare piece of armor. */
    LeatherCap(Rarity.RARE),
    
    /** A common shield. */
    RoughBuckler(Rarity.COMMON),
    
    /** An uncommon magic item. */
    ElectricWand(Rarity.UNCOMMON),
    
    /** A legendary melee weapon. */
    CloudSword(Rarity.LEGENDARY),
    
    /** A special item representing a negative effect or malus. */
    Curse(Rarity.CURSE),
    
    /** A common consumable item (implied, based on name). */
    ManaStone(Rarity.COMMON);

    private final Rarity rarity;

    /**
     * Constructs a {@code Stuff} type with its corresponding rarity.
     *
     * @param rarity The rarity level associated with this item type.
     */
    Stuff(Rarity rarity) {
        this.rarity = rarity;
    }

    /**
     * Returns the rarity level of this specific item type.
     *
     * @return The {@code Rarity} of the item.
     */
    public Rarity rarity() {
        return rarity;
    }
}
