package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Represents a room containing a treasure chest.
 * Entering this room allows the hero to obtain the items contained within the loot list.
 *
 * @param loot The list of item instances found as loot in this treasure chest.
 */
public record TreasureRoom(List<ItemInstance> loot) implements Room {
  
  /**
   * Compact constructor to validate that the list of loot is not null.
   *
   * @param loot The list of loot items.
   * @throws NullPointerException if the loot list is null.
   */
  public TreasureRoom {
    Objects.requireNonNull(loot);
  }
  
  /**
   * Draws the representative treasure or the first item of the loot on the map.
   *
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the item textures.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    if (!loot.isEmpty()) {
      String itemName = loot.get(0).getItem().name().replace(" ", "_");
      g.drawImage(img.getImage(itemName), x, y, size, size, null);
    }
  }
  
  /**
   * Triggers the loot collection process when the player clicks on the chest.
   * Iterates through the loot and prompts the user for placement in the backpack.
   *
   * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    jeu.notifier("Chest opened!");
    for (var item : this.loot) {
      processLootItem(jeu, item);
    }
  }

  private void processLootItem(Jeu jeu, ItemInstance item) {
    jeu.getView().displayItemFound(item);
    if (jeu.getView().interactBeforePlacement(item)) {
      jeu.getView().attemptPlacement(item);
    }
  }
}