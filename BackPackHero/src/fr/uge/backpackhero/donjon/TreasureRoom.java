package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
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
}