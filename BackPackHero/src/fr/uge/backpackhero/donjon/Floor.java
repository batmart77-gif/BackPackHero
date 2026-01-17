package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single floor of the dungeon.
 * Manages the layout of rooms, exploration status, and pathfinding logic.
 */
public class Floor {
  private final Room[][] map;
  private final boolean[][] explored;
  private final int startX;
  private final int startY;

  /**
   * Constructs a Floor with a given map and starting coordinates.
   *
   * @param map The grid of rooms composing the floor.
   * @param startX The starting column for the hero.
   * @param startY The starting row for the hero.
   * @throws NullPointerException if the map is null.
   * @throws IllegalArgumentException if the map is empty or coordinates are out of bounds.
   */
  public Floor(Room[][] map, int startX, int startY) {
    this.map = Objects.requireNonNull(map);
    validateMapBounds(map);
    validateStartingPosition(map, startX, startY);
    this.explored = new boolean[map.length][map[0].length];
    this.startX = startX;
    this.startY = startY;
  }

  private void validateMapBounds(Room[][] map) {
    if (map.length == 0 || map[0].length == 0) {
      throw new IllegalArgumentException("The map cannot be empty.");
    }
  }

  private void validateStartingPosition(Room[][] map, int x, int y) {
    if (x < 0 || y < 0 || y >= map.length || x >= map[0].length) {
      throw new IllegalArgumentException("Starting position is out of bounds!");
    }
  }

  /**
   * Marks a specific room as explored.
   * @param x The column coordinate.
   * @param y The row coordinate.
   */
  public void revealRoom(int x, int y) {
    if (isInside(x, y)) {
      explored[y][x] = true;
    }
  }

  /**
   * Checks if a room has been explored by the hero.
   * @param x The column coordinate.
   * @param y The row coordinate.
   * @return true if explored and inside bounds, false otherwise.
   */
  public boolean isExplored(int x, int y) {
    return isInside(x, y) && explored[y][x];
  }
  
  private boolean isInside(int x, int y) {
    return y >= 0 && y < map.length && x >= 0 && x < map[0].length;
  }
  
  /**
   * Retrieves the room at the given coordinates.
   * @param x The column coordinate.
   * @param y The row coordinate.
   * @return The Room object, or null if outside boundaries.
   */
  public Room getRoom(int x, int y) {
    if (!isInside(x, y)) {
      return null;
    }
    return map[y][x];
  }
  
  public int startX() { return startX; }
  public int startY() { return startY; }
  public int height() { return map.length; }
  public int width() { return map[0].length; }

  /**
   * Checks if a path consisting only of traversable rooms exists to the target.
   * Uses a Breadth-First Search (BFS) approach.
   *
   * @param curX Current column.
   * @param curY Current row.
   * @param targetX Target column.
   * @param targetY Target row.
   * @return true if a path exists, false otherwise.
   */
  public boolean isPathPossible(int curX, int curY, int targetX, int targetY) {
    if (curX == targetX && curY == targetY) { return true; }
    var toVisit = new ArrayList<PositionInDungeon>();
    var visited = new boolean[map.length][map[0].length];
    toVisit.add(new PositionInDungeon(curY, curX));
    visited[curY][curX] = true;
    for (int i = 0; i < toVisit.size(); i++) {
      if (exploreNeighbors(toVisit.get(i), targetX, targetY, toVisit, visited)) {
        return true;
      }
    }
    return false;
  }

  private boolean exploreNeighbors(PositionInDungeon p, int tx, int ty, 
                                   List<PositionInDungeon> list, boolean[][] v) {
    int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    for (int[] d : dirs) {
      int nx = p.col() + d[1];
      int ny = p.row() + d[0];
      if (nx == tx && ny == ty) { return true; }
      if (canExplore(nx, ny, v)) {
        v[ny][nx] = true;
        list.add(new PositionInDungeon(ny, nx));
      }
    }
    return false;
  }

  private boolean canExplore(int nx, int ny, boolean[][] v) {
    return isInside(nx, ny) && !v[ny][nx] && getRoom(nx, ny).isTraversable();
  }
}