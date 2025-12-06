package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a mana stone that can be used in combat.
 * Without a mana stone, a magic item can't be used.
 * @param name   The name of the mana stone.
 * @param pos    The list of relative positions describing the shape of the item in the backpack.
 * @param rarity The rarity level of the mana stone.
 * @param stats  The amount of mana the mana stone provides to the magic item.
 * @param price  The buying and selling price of the mana stone.
 */
public record ManaStone(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {
	
	/**
     * Compact constructor with validation.
     *
     * @throws NullPointerException     if name, rarity, or pos is null.
     * @throws IllegalArgumentException if stats or price is negative.
     */
	public ManaStone {
		Objects.requireNonNull(pos);
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		
		if (stats < 0 || price < 0) {
			throw new IllegalArgumentException("stats and price cant be negative");
		}
	}
	
	/**
     * Returns a human-readable description of the mana stone.
     *
     * @return A string describing the armor and its gameplay effect.
     */
	public String details() {
		return "Mana Stone " + name + ", gives " + stats + " mana, can be sold or bought to a merchant for " + price;
	}
	
	/**
     * Returns a compact identifier for display purposes.
     *
     * @return A short code representing this mana stone.
     *
     * @throws IllegalArgumentException if no short form is defined for this mana stone name.
     */
	@Override
	public String toString() {
		return switch(name) {
		case "Mana Stone" -> "MS";
		default -> throw new IllegalArgumentException("Unknown magic item type: " + name);
		};
	}
	
	/**
     * Mana Stone is a passive item and cannot be actively used during combat.
     *
     * @param hero  The hero attempting to use the mana stone.
     * @param target The enemy targeted (ignored since stone is passive).
     * @return Always false since armor cannot be actively used.
     */
	@Override
  public boolean use(Heros heros, Ennemi target) {
    Objects.requireNonNull(heros);
    heros.soigner(stats);
    System.out.println(heros + " uses " + name + ".");
    return true;
  }
}
