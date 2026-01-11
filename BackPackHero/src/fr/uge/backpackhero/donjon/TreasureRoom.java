package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageManager;
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
  
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageManager img) {
      if (loot != null && !loot.isEmpty()) {
          String itemName = loot.get(0).getItem().name().replace(" ", "_");
          g.drawImage(img.getImage(itemName), x, y, size, size, null);
      }
  }
  
  @Override
  public void onClick(Jeu jeu) {
      System.out.println("Coffre trouvé !");
      for (var item : this.loot()) { // Utilise loot() ou items() selon ton record
          System.out.println("Contenu : " + item.getName());
          jeu.getView().displayItemFound(item);
          if (jeu.getView().interactBeforePlacement(item)) {
              jeu.getView().attemptPlacement(item);
          }
      }
  }
}