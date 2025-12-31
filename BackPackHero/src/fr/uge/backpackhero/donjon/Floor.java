package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Floor {
  private final Room[][] map;
  private final boolean[][] explored; // État de visibilité de chaque salle
  private final int startX;
  private final int startY;

  public Floor(Room[][] map, int startX, int startY) {
    this.map = Objects.requireNonNull(map);
    Objects.requireNonNull(map);
    if (map.length == 0 || map[0].length == 0) {
      throw new IllegalArgumentException("The map cannot be empty.");
    }
    if (startX < 0 || startY < 0 || startY >= map.length || startX >= map[0].length) {
      throw new IllegalArgumentException("Starting position is out of bounds!");
    }
    this.explored = new boolean[map.length][map[0].length];
    this.startX = startX;
    this.startY = startY;
    
  }

  public void revealRoom(int x, int y) {
    if (isInside(x, y)) {
      explored[y][x] = true;
    }
  }

  public boolean isExplored(int x, int y) {
    return isInside(x, y) && explored[y][x];
  }
  
  private boolean isInside(int x, int y) {
    return y >= 0 && y < map.length && x >= 0 && x < map[0].length;
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
  
  public int startX() { return startX; }
  public int startY() { return startY; }
  
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
  
  
  /**
   * Vérifie s'il existe un chemin de couloirs jusqu'à la cible.
   * On utilise une ArrayList pour stocker les cases à explorer.
   */
  public boolean isPathPossible(int curX, int curY, int targetX, int targetY) {
    if (curX == targetX && curY == targetY) return true;

    var toVisit = new ArrayList<PositionInDungeon>();
    var visited = new boolean[map.length][map[0].length]; // 5x11

    toVisit.add(new PositionInDungeon(curY, curX));
    visited[curY][curX] = true;

    // On parcourt la liste des cases à visiter une par une
    for (int i = 0; i < toVisit.size(); i++) {
      PositionInDungeon current = toVisit.get(i);
      if (exploreNeighbors(current, targetX, targetY, toVisit, visited)) {
        return true;
      }
    }
    return false;
  }

  private boolean exploreNeighbors(PositionInDungeon p, int tx, int ty, 
                                   List<PositionInDungeon> list, boolean[][] v) {
    int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
    for (int[] d : dirs) {
      int nx = p.col() + d[1], ny = p.row() + d[0];
      
      if (nx == tx && ny == ty) return true; // Destination trouvée !
      
      if (isInside(nx, ny) && !v[ny][nx] && getRoom(nx, ny).isTraversable()) {
        v[ny][nx] = true;
        list.add(new PositionInDungeon(ny, nx));
      }
    }
    return false;
  }
}