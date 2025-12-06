package fr.uge.backpackhero.item;

public record Position(int row, int column) {
	
	public Position {
	}
	
	@Override
	public String toString() {
		return "(row: " + row + ", column: " + column + ")";
	}
}
