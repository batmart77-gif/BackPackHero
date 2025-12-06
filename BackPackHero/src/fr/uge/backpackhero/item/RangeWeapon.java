package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;


public record RangeWeapon(String name, List<Position> pos, Rarity rarity, int cost, int price) implements Item {

	public RangeWeapon {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pos);
		Objects.requireNonNull(rarity);
		
		if (cost < 0 || price < 0) {
			throw new IllegalArgumentException("cost and price cant be negative");
		}
	}
	
	public String details() {
		return "Range Weapon " + name + ", " + rarity.toString() + " needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Composite Bow" -> "CB";
		case "Mouse Bow" -> "MB";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    Objects.requireNonNull(heros);
    if (cible == null || !cible.estVivant()) return false;

    if (heros.depenserEnergie(cost)) {
      // Dégâts par défaut car pas de stats dans le record RangeWeapon
      int degatsDefaut = 2; 
      cible.recevoirDegats(degatsDefaut);
      System.out.println(heros + " tire avec " + name + " (" + degatsDefaut + " dégâts).");
      return true;
    }
    return false;
  }
}
