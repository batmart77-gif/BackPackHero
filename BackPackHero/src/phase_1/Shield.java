package phase_1;

import java.util.List;
import java.util.Objects;

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
}
