package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;


public record Shield(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item {
	
	public Shield {
		Objects.requireNonNull(name);
		Objects.requireNonNull(rarity);
		Objects.requireNonNull(pos);
		
		if (cost < 0 || stats < 0 || price < 0) {
			throw new IllegalStateException("cost,stats and price must be positive or null");
		}
	}
	
	public String details() {
		return "Shield " + name + ", " + rarity.toString() + ", adds " + stats + " protection on use, needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Rough Buckler" -> "RB";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
	
	
	@Override
  public boolean utiliser(Heros heros, Ennemi cible) {
    Objects.requireNonNull(heros);
      
    // Un bouclier n'a pas besoin de cible ennemie
    if (heros.depenserEnergie(cost)) {
      int realBlock = heros.calculateBlockOutput(stats);
      heros.ajouterProtection(realBlock);
      System.out.println(heros + " utilise " + name + " (+ " + stats + " protection).");
      return true;
    }
    return false;
  }
}
