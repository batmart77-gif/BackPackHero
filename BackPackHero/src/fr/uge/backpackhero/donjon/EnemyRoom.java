package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents a room containing enemies.
 * This type of room triggers combat when the hero enters it and interacts with it.
 *
 * @param enemies The list of enemies present in this room.
 */
public record EnemyRoom(List<Ennemi> enemies) implements Room {
  
  /**
   * Compact constructor to validate that the list of enemies is valid.
   * Ensures the list is neither null nor empty to prevent runtime errors.
   *
   * @param enemies The list of enemies present in this room.
   * @throws NullPointerException if the enemies list is null.
   * @throws IllegalArgumentException if the enemies list is empty.
   */
  public EnemyRoom {
    Objects.requireNonNull(enemies);
    if (enemies.isEmpty()) {
      throw new IllegalArgumentException("An enemy room cannot be empty");
    }
  }

  /**
   * Retrieves the name of the sprite associated with the first enemy in the room.
   *
   * @return the name of the enemy as a String for image loading.
   */
  public String getSpriteName() {
    return enemies.get(0).getName();
  }

  /**
   * Draws the representative enemy sprite on the dungeon map.
   *
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the enemy texture.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    String spriteName = getSpriteName();
    g.drawImage(img.getImage(spriteName), x, y, size, size, null);
  }

  /**
   * Triggers a combat session if at least one enemy in the room is alive.
   * Transitions the game state to combat mode.
   *
   * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    if (this.enemies().stream().anyMatch(Ennemi::estVivant)) {
      jeu.notifier("Combat starts!");
      jeu.lancerCombat(this.enemies());
      jeu.setMode(Mode.COMBAT);
    }
  }
}