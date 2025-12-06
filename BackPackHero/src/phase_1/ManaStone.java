package phase_1;

import java.util.List;
import java.util.Objects;

public record ManaStone(String name, List<Position> pos, int stats, int price) implements Item {
	
	public ManaStone {
		Objects.requireNonNull(pos);
		Objects.requireNonNull(name);
		
		if (stats < 0 || price < 0) {
			throw new IllegalArgumentException("stats and price cant be negative");
		}
	}
	
	public String details() {
		return "Mana Stone " + name + ", gives " + stats + " mana, can be sold or bought to a merchant for " + price;
	}
	
	@Override
	public String toString() {
		return switch(name) {
		case "Mana Stone" -> "MS";
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
}
