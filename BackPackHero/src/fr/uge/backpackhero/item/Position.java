package fr.uge.backpackhero.item;

/**
 * Represents a set of coordinates (row and column) used to locate items
 * within the {@code BackPack} or to define the relative shape of an item.
 *
 * @param row The row index (y-coordinate) of the position.
 * @param column The column index (x-coordinate) of the position.
 */
public record Position(int row, int column) {
	
	/**
     * Compact constructor. Performs default initialization and validation (if any).
     */
	public Position {
	}
	
	/**
     * Returns a human-readable string representation of the position.
     *
     * @return A string in the format "(row: R, column: C)".
     */
	@Override
	public String toString() {
		return "(row: " + row + ", column: " + column + ")";
	}
}
