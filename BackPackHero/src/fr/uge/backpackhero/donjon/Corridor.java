package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;
import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents an empty corridor room in the dungeon.
 * Corridors are neutral spaces that allow the hero to move freely between rooms.
 * This class is implemented as a record to ensure immutability.
 */
public record Corridor() implements Room {

  /**
   * {@inheritDoc}
   * Corridors are always traversable by default.
   * * @return true as corridors never block movement.
   */
  @Override
  public boolean isTraversable() {
    return true;
  }

  /**
   * {@inheritDoc}
   * Renders the basic floor tile image at the specified coordinates.
   * * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the tile texture.
   * @throws NullPointerException if any parameter is null.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("tile_floor"), x, y, size, size, null);
  }

  /**
   * {@inheritDoc}
   * Defines the behavior when a corridor is clicked.
   * For corridors, no specific action is triggered.
   * * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
  }
}