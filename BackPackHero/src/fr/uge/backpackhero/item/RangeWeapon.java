package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a ranged weapon used for attacking enemies from a distance.
 * This weapon has an energy cost but relies on a fixed or default damage value (not defined in the record fields).
 *
 * @param name The name of the ranged weapon.
 * @param pos The list of relative positions describing the shape of the item in the backpack.
 * @param rarity The rarity level of the ranged weapon.
 * @param cost The energy cost required to use the weapon in combat.
 * @param price The buying and selling price of the ranged weapon.
 */
public record RangeWeapon(String name, List<Position> pos, Rarity rarity, int cost, int price) implements Item {

	/**
     * Compact constructor with validation.
     *
     * @throws NullPointerException if name, pos, or rarity is null.
     * @throws IllegalArgumentException if cost or price is negative.
     */
	public RangeWeapon {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pos);
		Objects.requireNonNull(rarity);
		
		if (cost < 0 || price < 0) {
			throw new IllegalArgumentException("cost and price cant be negative");
		}
	}
	
	/**
     * Returns a human-readable description of the ranged weapon.
     *
     * @return A string describing the weapon and its cost.
     */
	public String details() {
		return "Range Weapon " + name + ", " + rarity.toString() + " needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	/**
     * Returns a compact identifier for display purposes.
     *
     * @return A short code representing this ranged weapon.
     * @throws IllegalArgumentException if no short form is defined for this weapon name.
     */
	@Override
	public String toString() {
		return switch(name) {
		case "Composite Bow" -> "CB";
		case "Mouse Bow" -> "MB";
		default -> throw new IllegalArgumentException("Unknown ranged weapon type: " + name);
		};
	}
	
	/**
     * Attempts to use the ranged weapon in combat, consuming energy and dealing a fixed amount of damage (2).
     *
     * @param heros The hero using the item.
     * @param target The enemy to target.
     * @return {@code true} if the item was successfully used, {@code false} otherwise (e.g., target is dead or not enough energy).
     */
	@Override
  public boolean use(Heros heros, Ennemi target) {
    Objects.requireNonNull(heros);
    if (target == null || !target.estVivant()) return false;

    if (heros.depenserEnergie(cost)) {
      // Dégâts par défaut car pas de stats dans le record RangeWeapon
      int degatsDefaut = 2; 
      target.recevoirDegats(degatsDefaut);
      System.out.println(heros + " shoots with " + name + " (" + degatsDefaut + " damage).");
      return true;
    }
    return false;
  }
}
