package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a physical weapon designed for close-quarters combat.
 * Deals damage to a target at a certain energy cost and can be bought or sold.
 *
 * @param name The name of the melee weapon.
 * @param pos The list of relative positions describing the shape of the item in the backpack.
 * @param rarity The rarity level of the melee weapon.
 * @param stats The base damage the melee weapon deals on use.
 * @param cost The energy cost required to use the weapon in combat.
 * @param price The buying and selling price of the melee weapon.
 */
public record MeleeWeapon(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item{
	
	/**
     * Compact constructor with validation.
     *
     * @throws NullPointerException if name, pos, or rarity is null.
     * @throws IllegalStateException if cost, stats, or price is negative.
     */
	public MeleeWeapon {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pos);
		Objects.requireNonNull(rarity);
		
		if (cost < 0 || stats < 0 || price < 0) {
			throw new IllegalStateException("cost, stats and price must be positive or null");
		}
	}
	
	/**
     * Returns a human-readable description of the melee weapon.
     *
     * @return A string describing the weapon and its gameplay effect.
     */
	public String details() {
		return "Melee Weapon " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	/**
     * Returns a compact identifier for display purposes.
     *
     * @return A short code representing this melee weapon.
     * @throws IllegalArgumentException if no short form is defined for this weapon name.
     */
	@Override
	public String toString() {
		return switch(name) {
		case "Wood Sword" -> "WS";
		default -> throw new IllegalArgumentException("Unknown ranged weapon type: " + name);
		};
	}
	
	@Override
	public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance self) {
	    Objects.requireNonNull(heros);
	    if (target == null || !target.estVivant()) return false;
	    if (heros.depenserEnergie(cost)) {
	        // Bonus Gemme de Cœur (Vérifie l'adjacence dans le sac)
	        if (backpack.hasAdjacentItem(self, Item::isHeartGem)) {
	            heros.soigner(1);
	            System.out.println("La Gemme de Cœur soigne 1 PV !");
	        }
	        int realDamage = heros.calculateDamageOutput(stats);
	        target.recevoirDegats(realDamage);
	        System.out.println(name + " inflige " + realDamage + " dégâts.");
	        return true;
	    }
	    return false;
	}

}
