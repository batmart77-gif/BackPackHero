package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

public record MagicItem(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item {
	
	public MagicItem {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(pos);
		
		if (cost < 0 || stats < 0 || price < 0) {
			throw new IllegalStateException("cost, stats and price must be positive or null");
		}
	}
	
	public String details() {
		return "Magic Item " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Electric Wand" -> "EW";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    Objects.requireNonNull(heros);
    if (cible == null || !cible.estVivant()) return false;

    if (heros.depenserEnergie(cost)) {
      cible.recevoirDegats(stats);
      System.out.println(heros + " lance un sort avec " + name + " (" + stats + " dégâts) !");
      return true;
    }
    return false;
	}
}
