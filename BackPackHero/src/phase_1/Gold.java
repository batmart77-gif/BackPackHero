package phase_1;

import java.util.List;

public record Gold(String name, List<Position> pos, int price) implements Item{

	public Gold {}
	
	public String details() {
		return "";
	}
	
	@Override
	public String toString() {
		return "Gold";
	}
}
