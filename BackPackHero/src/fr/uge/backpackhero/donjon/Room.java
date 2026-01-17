package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;
import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * A sealed interface representing a cell or room on the dungeon map.
 * This is the generic type for all elements that compose a floor.
 * Only the types listed in the permits clause can implement this interface.
 */
public sealed interface Room permits 
    Corridor, EnemyRoom, TreasureRoom, MerchantRoom, HealerRoom, ExitRoom, EventRoom, GateRoom {
  
  /**
   * Indicates whether the hero can pass through this room to reach another destination.
   * By default, rooms are not traversable unless overridden (e.g., Corridors).
   * * @return true if the hero can walk through the room, false otherwise.
   */
  default boolean isTraversable() {
    return false;
  }
  
  /**
   * Renders the room's visual representation on the screen.
   * * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the room tile in pixels.
   * @param img the loader providing the textures.
   */
  void draw(Graphics2D g, int x, int y, int size, ImageLoader img);
  
  /**
   * Defines the interaction behavior when the user clicks on this room.
   * * @param jeu the current game instance used to modify game state or notify the player.
   */
  void onClick(Jeu jeu);
}