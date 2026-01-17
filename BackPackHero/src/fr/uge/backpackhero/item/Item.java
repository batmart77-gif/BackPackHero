package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.List;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a generic item that can be placed in the hero's backpack and
 * optionally used in combat. All items define their shape, rarity, price, and
 * detailed description.
 */
public sealed interface Item permits RangeWeapon, Arrow, MeleeWeapon, Armor, Shield, ManaStone, MagicItem, Gold, Curse {
  /**
   * Returns the price of this item when bought or sold.
   *
   * @return the item's price (must be non-negative)
   */

  public abstract int price();

  /**
   * Returns a short string representation used for compact display (e.g., in the
   * backpack grid).
   *
   * @return a short identifier string
   */
  public abstract String toString();

  /**
   * Returns the relative grid positions occupied by this item. The origin (0,0)
   * corresponds to the top-left pivot point.
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
   * @param hero   the hero using the item
   * @param target the targeted enemy (may be {@code null} for defensive items)
   * @return {@code true} if the item was successfully used, {@code false}
   *         otherwise
   */
  // boolean use(Heros heros, Ennemi target);

  /**
   * Indicates whether this item can be rotated in the backpack.
   *
   * @return {@code true} if the item can rotate, {@code false} otherwise
   */
  default boolean rotatable() {
    return true;
  }

  /**
   * Calculates the rotated shape of the item according to the rotation angle.
   *
   * @param originalShape the original shape of the item
   * @param angle         the rotation angle in degrees
   * @return a list of positions representing the rotated shape
   */
  default List<Position> shapeAtRotation(int rotation) {
    var shape = pos();
    int times = (rotation % 360) / 90;

    for (int i = 0; i < times; i++) {
      shape = rotate90(shape);
    }
    return shape;
  }

  /**
   * Rotates the shape 90 degrees clockwise and normalizes it to start at (0,0).
   *
   * @param shape the original shape
   * @return the rotated and normalized shape
   */
  private List<Position> rotate90(List<Position> shape) {
    var rotated = new ArrayList<Position>();
    for (var p : shape) {
      rotated.add(new Position(p.column(), -p.row()));
    }

    int minR = rotated.stream().mapToInt(Position::row).min().orElse(0);
    int minC = rotated.stream().mapToInt(Position::column).min().orElse(0);

    return rotated.stream().map(p -> new Position(p.row() - minR, p.column() - minC)).toList();
  }

  /**
   * Indicates whether this item is classified as a mana stone. Default
   * implementation returns {@code false}. Specific item types should override
   * this to return {@code true}.
   *
   * @return {@code true} if the item is a mana stone; {@code false} otherwise.
   */
  default boolean isManaStone() {
    return false;
  }

  /**
   * Executes the item's unique effect during a combat turn. This method defines
   * how an item interacts with the hero, the target enemy, and the backpack's
   * layout (for adjacency or positional bonuses).
   *
   * @param heros    the hero character initiating the item's use.
   * @param target   the enemy character currently targeted by the hero.
   * @param backpack the backpack instance containing the item, used to calculate
   *                 bonuses.
   * @param instance the specific instance of the item being triggered.
   * @return {@code true} if the item was successfully activated; {@code false} if
   *         requirements were not met.
   */
  boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance instance);

  /**
   * Indicates whether this item is classified as a piece of armor. By default,
   * this returns {@code false}. Armor items should override this to return
   * {@code true} to enable passive protection calculations during combat.
   *
   * @return {@code true} if the item is armor; {@code false} otherwise.
   */
  default boolean isArmor() {
    return false;
  }
}
