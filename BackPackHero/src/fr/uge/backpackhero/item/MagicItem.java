package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a magical item that can be used in combat.
 * Deals damage to a target at a certain energy cost and can be bought or sold.
 * @param name   The name of the magic item.
 * @param pos    The list of relative positions describing the shape of the item in the backpack.
 * @param rarity The rarity level of the magic item.
 * @param stats  The amount of damage the magic item deals on use.
 * @param price  The buying and selling price of the magic item.
 */
public record MagicItem(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item {
	
	/**
     * Compact constructor with validation.
     *
     * @throws NullPointerException     if name, rarity, or pos is null.
     * @throws IllegalArgumentException if cost, stats or price is negative.
     */
	public MagicItem {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(pos);
		
		if (cost < 0 || stats < 0 || price < 0) {
			throw new IllegalStateException("cost, stats and price must be positive or null");
		}
	}
	
	/**
     * Returns a human-readable description of the magic item.
     *
     * @return A string describing the armor and its gameplay effect.
     */
	public String details() {
		return "Magic Item " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	/**
     * Returns a compact identifier for display purposes.
     *
     * @return A short code representing this magic item.
     *
     * @throws IllegalArgumentException if no short form is defined for this magic item name.
     */
	@Override
	public String toString() {
		return switch(name) {
		case "Electric Wand" -> "EW";
		default -> throw new IllegalArgumentException("Unknown magic item type: " + name);
		};
	}
	
	/**
     * Attempts to use the magic item in combat.
     *
     * @param hero the hero using the item
     * @param target the enemy to target (can be null)
     * @return true if the item was successfully used, false otherwise
     */
	@Override
  public boolean use(Heros heros, Ennemi target) {
    Objects.requireNonNull(heros);
    if (target == null || !target.estVivant()) return false;

    if (heros.depenserEnergie(cost)) {
    	target.recevoirDegats(stats);
      System.out.println(heros + " casts a spell with " + name + " (" + stats + " damage) !");
      return true;
    }
    return false;
	}
}
