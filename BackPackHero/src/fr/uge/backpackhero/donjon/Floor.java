package fr.uge.backpackhero.donjon;

import java.util.Objects;

/**
 * Represents a complete floor of the dungeon.
 * It consists of a 2D grid (map) of rooms, along with the hero's designated starting position.
 *
 * @param map    The 2D array representing the grid of rooms.
 * @param startX The starting column (X coordinate) for the hero on this floor.
 * @param startY The starting row (Y coordinate) for the hero on this floor.
 */
public record Floor(Room[][] map, int startX, int startY) {
  
  /**
   * Compact constructor to validate the map and the start coordinates.
   *
   * @throws NullPointerException if the map is null.
   * @throws IllegalArgumentException if the map is empty or if start coordinates are out of bounds.
   */
  public Floor {
    Objects.requireNonNull(map);
    if (map.length == 0 || map[0].length == 0) {
      throw new IllegalArgumentException("The map cannot be empty.");
    }
    // Validation des coordonnées de départ
    if (startX < 0 || startY < 0 || startY >= map.length || startX >= map[0].length) {
      throw new IllegalArgumentException("Starting position is out of bounds!");
    }
  }
  
  /**
   * Retrieves the room at the given coordinates (x, y).
   * Returns null if the coordinates are outside the map boundaries.
   *
   * @param x The column coordinate.
   * @param y The row coordinate.
   * @return The {@link Room} object at (x, y), or {@code null} if outside the map.
   */
  public Room getRoom(int x, int y) {
    if (y < 0 || y >= map.length || x < 0 || x >= map[0].length) {
        return null;
    }
    return map[y][x];
  }
  
  /**
   * Gets the height of the map (number of rows).
   *
   * @return The height of the map.
   */
  public int height() { 
    return map.length; 
  }
  
  /**
   * Gets the width of the map (number of columns).
   *
   * @return The width of the map.
   */
  public int width() {
    return map[0].length;
  }
}