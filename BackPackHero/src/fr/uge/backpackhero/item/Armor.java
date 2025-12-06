package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;


/**
 * Represents a piece of armor that the hero can equip.
 * Armor provides passive protection during combat.
 *
 * @param name   The name of the armor.
 * @param pos    The list of relative positions describing the shape of the item in the backpack.
 * @param rarity The rarity level of the armor.
 * @param stats  The amount of protection the armor provides each turn.
 * @param price  The buying and selling price of the armor.
 */
public record Armor(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {
	
	/**
     * Compact constructor with validation.
     *
     * @throws NullPointerException     if name, rarity, or pos is null.
     * @throws IllegalArgumentException if stats or price is negative.
     */
	public Armor {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(pos);
		
		if (stats < 0 || price < 0) {
			throw new IllegalArgumentException("stats and price cant be negative");
		}
	}
	
	/**
     * Returns a human-readable description of the armor.
     *
     * @return A string describing the armor and its gameplay effect.
     */
	public String details() {
		return "Armor " + name + ", " + rarity.toString() + ", adds " + stats + " protection each turn, can be sold or bought to a merchant for " + price;
	}
	
	/**
     * Returns a compact identifier for display purposes.
     *
     * @return A short code representing this armor.
     *
     * @throws IllegalArgumentException if no short form is defined for this armor name.
     */
	@Override
	public String toString() {
		return switch(name) {
		case "Leather Cap" -> "LC";
		default -> throw new IllegalArgumentException("Unknown arrow type: " + name);
		};
	}
	
	/**
     * Armor is a passive item and cannot be actively used during combat.
     *
     * @param hero  The hero attempting to use the armor.
     * @param target The enemy targeted (ignored since armor is passive).
     * @return Always false since armor cannot be actively used.
     */
	@Override
  public boolean use(Heros heros, Ennemi target) {
    System.out.println(name + " is a passive item.");
    return false;
	}
}
