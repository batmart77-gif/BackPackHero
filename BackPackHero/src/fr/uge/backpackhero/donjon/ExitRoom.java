package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents the room that contains the exit door.
 * Interacting with this room allows the hero to pass to the next floor 
 * of the dungeon or win the game if it is the final floor.
 */
public record ExitRoom() implements Room {

  /**
   * Draws the exit door sprite on the dungeon map.
   *
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the door texture.
   * @throws NullPointerException if g or img is null.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("exit_door"), x, y, size, size, null);
  }

  /**
   * Triggers the transition to the next dungeon floor or the victory state.
   * The logic for floor management is handled by the central game controller.
   *
   * @param jeu the current game instance.
   * @throws NullPointerException if jeu is null.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    jeu.moveToNextFloor();
  }
}