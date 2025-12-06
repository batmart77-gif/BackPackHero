package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Une salle contenant un marchand avec un stock d'objets à vendre.
 */
public record MerchantRoom(List<ItemInstance> stock) implements Room {
  
  public MerchantRoom {
    Objects.requireNonNull(stock);
    stock = new java.util.ArrayList<>(stock); 
  }
}