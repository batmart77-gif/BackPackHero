package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

public record MeleeWeapon(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item{
	
	public MeleeWeapon {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pos);
		Objects.requireNonNull(rarity);
		
		if (cost < 0 || stats < 0 || price < 0) {
			throw new IllegalStateException("cost, stats and price must be positive or null");
		}
	}
	
	public String details() {
		return "Melee Weapon " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Wood Sword" -> "WS";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    Objects.requireNonNull(heros);

    if (cible == null || !cible.estVivant()) return false;

    if (heros.depenserEnergie(cost)) {

      int realDamage = heros.calculateDamageOutput(stats);
      cible.recevoirDegats(realDamage);
      System.out.println(heros + " attaque avec " + name + " (" + stats + " dégâts) !");
      return true;
    }
    return false;
  }
}
