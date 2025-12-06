package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;



public record Arrow(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {

	public Arrow {
		Objects.requireNonNull(name);
		Objects.requireNonNull(pos);
		Objects.requireNonNull(rarity);
		
		if (stats < 0 || price < 0) {
			throw new IllegalArgumentException("stats and price cant be negative");
		}
	}
	
	public String details() {
		return "Arrow " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Short Arrow" -> "SA";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    Objects.requireNonNull(heros);
    if (cible == null || !cible.estVivant()) return false;

    // Pas de coût en énergie pour la flèche (dans ce record)
    cible.recevoirDegats(stats);
    System.out.println(heros + " tire " + name + " (" + stats + " dégâts) !");
    return true;
  }
	
}
