package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;



public record Armor(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {
	
	public Armor {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(pos);
		
		if (stats < 0 || price < 0) {
			throw new IllegalArgumentException("stats and price cant be negative");
		}
	}
	
	public String details() {
		return "Armor " + name + ", " + rarity.toString() + ", adds " + stats + " protection each turn, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Leather Cap" -> "LC";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    System.out.println(name + " est un objet passif.");
    return false;
	}
}
