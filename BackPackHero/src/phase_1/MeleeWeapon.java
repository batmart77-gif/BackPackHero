package phase_1;

import java.util.List;
import java.util.Objects;

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
}
