package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents a random encounter room where the content is revealed only upon entry.
 * This room triggers a random effect (bonus or penalty) when the hero interacts with it.
 *
 * @param description A brief text description of the room encounter.
 */
public record EventRoom(String description) implements Room {
  private static final Random RDM = new Random();

  /**
   * Compact constructor to ensure the description is not null.
   * @param description The text description of the event.
   */
  public EventRoom {
    Objects.requireNonNull(description);
  }

  /**
   * Triggers a randomized effect on the hero, such as gaining gold or taking damage.
   * This method ensures game logic is updated and the player is notified via the UI.
   * @param jeu The current game instance. Must not be null.
   */
  public void triggerEffect(Jeu jeu) {
    Objects.requireNonNull(jeu);
    int chance = RDM.nextInt(3);
    switch (chance) {
      case 0 -> {
        jeu.getHeros().gagnerOr(10);
        jeu.notifier("You found a bag of gold! (+10 Gold)");
      }
      case 1 -> {
        jeu.getHeros().takeDamage(5);
        jeu.notifier("Ouch! A trap was triggered! (-5 HP)");
      }
      default -> {
        jeu.getHeros().soigner(5);
        jeu.notifier("A magical fountain restores your health. (+5 HP)");
      }
    }
  }

  /**
   * Draws the event icon on the dungeon map.
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the tile texture.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("event"), x, y, size, size, null);
  }

  /**
   * Triggers the room's random effect through the game controller when clicked.
   * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    this.triggerEffect(jeu);
  }
}