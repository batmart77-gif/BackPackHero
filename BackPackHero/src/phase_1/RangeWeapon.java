package phase_1;

import java.util.List;
import java.util.Objects;

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
		default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
		};
	}
}
