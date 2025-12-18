package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Represents a room containing a merchant with a stock of items available for sale.
 * Interaction with this room triggers the buying and selling menu.
 *
 * @param stock The mutable list of items currently offered for sale by the merchant.
 */
public record MerchantRoom(List<ItemInstance> stock) implements Room {
  
  /**
   * Compact constructor to validate the stock list and create a mutable copy
   * to enable buying (removing items from the merchant's stock).
   *
   * @param stock The initial list of items for the merchant.
   * @throws NullPointerException if the stock list is null.
   */
  public MerchantRoom {
    Objects.requireNonNull(stock);
    stock = new java.util.ArrayList<>(stock); 
  }
}