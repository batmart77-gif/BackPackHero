package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.graphics.ImageLoader;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Represents a room containing a merchant with a stock of items available for sale.
 * Interaction with this room triggers the shopping menu where the hero can buy items.
 *
 * @param stock The list of items currently offered for sale by the merchant.
 */
public record MerchantRoom(List<ItemInstance> stock) implements Room {
  
  /**
   * Compact constructor to validate the stock and ensure mutability.
   * A new ArrayList is created to allow item removal during purchase.
   *
   * @param stock The initial list of items for the merchant.
   * @throws NullPointerException if the stock list is null.
   */
  public MerchantRoom {
    Objects.requireNonNull(stock);
    stock = new ArrayList<>(stock);
  }
  
  /**
   * Draws the merchant sprite on the dungeon map.
   *
   * @param g the graphics context used for drawing.
   * @param x the x-coordinate on the screen.
   * @param y the y-coordinate on the screen.
   * @param size the size of the tile in pixels.
   * @param img the loader providing the merchant texture.
   */
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(img);
    g.drawImage(img.getImage("merchant"), x, y, size, size, null);
  }
  
  /**
   * Switches the game mode to the shop interface when clicked.
   *
   * @param jeu the current game instance.
   */
  @Override
  public void onClick(Jeu jeu) {
    Objects.requireNonNull(jeu);
    jeu.setMode(Mode.BOUTIQUE);
  }
}