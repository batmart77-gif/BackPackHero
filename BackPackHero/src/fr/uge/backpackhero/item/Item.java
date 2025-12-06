package fr.uge.backpackhero.item;

import java.util.List;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a generic item that can be placed in the hero's backpack
 * and optionally used in combat. All items define their shape, rarity,
 * price, and detailed description.
 */
public sealed interface Item permits RangeWeapon, Arrow, MeleeWeapon, Armor, Shield, ManaStone, MagicItem, Gold, Curse {
  /**
   * Returns the price of this item when bought or sold.
   *
   * @return the item's price (must be non-negative)
   */
  public abstract int price();
  
  /**
   * Returns a short string representation used for compact display
   * (e.g., in the backpack grid).
   *
   * @return a short identifier string
   */
  public abstract String toString();
  
  /**
   * Returns the relative grid positions occupied by this item.
   * The origin (0,0) corresponds to the top-left pivot point.
   *
   * @return a list of relative positions describing the item’s shape
   */
  public abstract List<Position> pos();
  
  /**
   * Returns the full display name of this item.
   *
   * @return the name of the item
   */
  public abstract String name();
  
  /**
   * Returns a textual description of this item, explaining its behavior.
   *
   * @return a description string
   */
  public abstract String details();
   
  /**
   * Returns the rarity of this item.
   *
   * @return the item rarity
   */
  public abstract Rarity rarity();
  
  
  /**
   * Attempts to use this item during combat.
   *
   * @param hero  the hero using the item
   * @param target the targeted enemy (may be {@code null} for defensive items)
   * @return {@code true} if the item was successfully used, {@code false} otherwise
   */
  boolean use(Heros heros, Ennemi target);
  
  /**
   * Indicates whether this item can be rotated in the backpack.
   *
   * @return {@code true} if the item can rotate, {@code false} otherwise
   */
  default boolean rotatable() {
      return true;
  }
}


