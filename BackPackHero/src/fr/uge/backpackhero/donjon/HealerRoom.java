package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents a room containing a healer.
 * Interacting with this room allows the hero to access healing services 
 * in exchange for gold pieces.
 */
public record HealerRoom() implements Room {

  /**
   * Draws the healer sprite on the dungeon map.
   *
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the healer texture.
   * @throws NullPointerException if g or img is null.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("healer"), x, y, size, size, null);
  }

  /**
   * Transitions the game state to the healing menu when the player clicks the room.
   *
   * @param jeu the current game instance.
   * @throws NullPointerException if jeu is null.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    jeu.setMode(Mode.SOIN);
  }
}