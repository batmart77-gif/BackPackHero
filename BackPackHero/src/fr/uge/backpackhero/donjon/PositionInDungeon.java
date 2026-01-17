package fr.uge.backpackhero.donjon;

/**
 * A utility record used to store coordinates within the Dungeon grid.
 * This immutable structure represents a specific cell location by its row and column.
 *
 * @param row the row index (y-coordinate) in the dungeon grid.
 * @param col the column index (x-coordinate) in the dungeon grid.
 */
public record PositionInDungeon(int row, int col) {
}