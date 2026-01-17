package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents an arrow that can be fired by a ranged weapon. Arrows deal damage
 * when used and occupy a specific shape inside the backpack inventory.
 *
 * @param name   The name of the arrow.
 * @param pos    The list of relative positions describing the shape of the item
 *               in the backpack
 * @param rarity The rarity level of the arrow.
 * @param stats  The amount of damage the arrow deals.
 * @param price  The buying and selling price of the armor.
 */
public record Arrow(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {

  /**
   * Compact constructor with validation.
   *
   * @throws NullPointerException     if name, rarity, or pos is null.
   * @throws IllegalArgumentException if stats or price is negative.
   */
  public Arrow {
    Objects.requireNonNull(name);
    Objects.requireNonNull(pos);
    Objects.requireNonNull(rarity);

    if (stats < 0 || price < 0) {
      throw new IllegalArgumentException("stats and price cant be negative");
    }
  }

  /**
   * Returns a human-readable description of the arrow.
   *
   * @return A string describing the arrow and its gameplay effect.
   */
  public String details() {
    return "Arrow " + name + ", " + rarity.toString() + ", deals " + stats
        + " damage on use, can be sold or bought to a merchant for " + price;
  }

  /**
   * Returns a compact identifier for display purposes.
   *
   * @return A short code representing this arrow.
   *
   * @throws IllegalArgumentException if no short form is defined for this arrow
   *                                  name.
   */
  @Override
  public String toString() {
    return switch (name) {
    case "Short Arrow" -> "SA";
    default -> throw new IllegalArgumentException("Unknown arrow type: " + name);
    };
  }

  /**
   * Defines the active usage of the item during combat.
   *
   * @param heros    the hero attempting to use the item.
   * @param target   the enemy targeted by the item's effect.
   * @param backpack the backpack containing the item, used for layout-based
   *                 bonuses.
   * @param instance the specific instance of the item being used.
   * @return false
   * @throws NullPointerException if any of the provided arguments are
   *                              {@code null}.
   */
  @Override
  public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance self) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(target);
    Objects.requireNonNull(backpack);
    Objects.requireNonNull(self);
    return false;
  }

}
