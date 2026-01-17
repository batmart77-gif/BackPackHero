package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;
import java.util.Optional;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Represents a gate blocking access to another room.
 * It requires a "Key" item in the hero's backpack to be unlocked.
 * * @param hiddenRoom The room located behind the gate.
 */
public record GateRoom(Room hiddenRoom) implements Room {
  
  /**
   * Compact constructor to ensure the hidden room is not null.
   * @param hiddenRoom the room behind the gate.
   */
  public GateRoom {
    Objects.requireNonNull(hiddenRoom);
  }

  /**
   * Indicates if the hero can pass through this room.
   * @return false because the gate blocks movement until unlocked.
   */
  @Override
  public boolean isTraversable() {
    return false;
  }

  /**
   * Draws the gate's lock icon on the dungeon map.
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the lock texture.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("cadenas"), x, y, size, size, null);
  }

  /**
   * Attempts to unlock the gate when the player clicks on it.
   * Checks for a "Key" in the inventory and triggers the hidden room if found.
   * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    Optional<ItemInstance> keyOpt = jeu.getHeros().getBackpack().getItems().stream()
        .filter(i -> i.getName().equalsIgnoreCase("Key"))
        .findFirst();

    if (keyOpt.isPresent()) {
      unlockAndTrigger(jeu, keyOpt.get());
    } else {
      jeu.notifier("This gate requires a key!");
    }
  }

  private void unlockAndTrigger(Jeu jeu, ItemInstance key) {
    jeu.getHeros().getBackpack().removeItem(key);
    jeu.notifier("Gate unlocked!");
    this.hiddenRoom().onClick(jeu);
  }
}