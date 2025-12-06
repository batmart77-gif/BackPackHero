package phase_1;

public record Position(int row, int column) {
	
	public Position {
	}
	
	@Override
	public String toString() {
		return "(row: " + row + ", column: " + column + ")";
	}
}
