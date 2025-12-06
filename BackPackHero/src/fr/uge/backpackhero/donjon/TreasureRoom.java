package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.item.ItemInstance; // Le type du binôme

/**
 * Une salle contenant un trésor (coffre).
 */
public record TreasureRoom(List<ItemInstance> loot) implements Room {
  
  public TreasureRoom {
    Objects.requireNonNull(loot);
    loot = List.copyOf(loot);
  }
}