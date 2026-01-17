package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a mana stone that can be used in combat. Without a mana stone, a
 * magic item can't be used.
 * 
 * @param name   The name of the mana stone.
 * @param pos    The list of relative positions describing the shape of the item
 *               in the backpack.
 * @param rarity The rarity level of the mana stone.
 * @param stats  The amount of mana the mana stone provides to the magic item.
 * @param price  The buying and selling price of the mana stone.
 */
public record ManaStone(String name, List<Position> pos, Rarity rarity, int stats, int price) implements Item {

  /**
   * Compact constructor with validation.
   *
   * @throws NullPointerException     if name, rarity, or pos is null.
   * @throws IllegalArgumentException if stats or price is negative.
   */
  public ManaStone {
    Objects.requireNonNull(pos);
    Objects.requireNonNull(name);
    Objects.requireNonNull(rarity);

    if (stats < 0 || price < 0) {
      throw new IllegalArgumentException("stats and price cant be negative");
    }
  }

  /**
   * Returns a human-readable description of the mana stone.
   *
   * @return A string describing the armor and its gameplay effect.
   */
  public String details() {
    return "Mana Stone " + name + ", gives " + stats + " mana, can be sold or bought to a merchant for " + price;
  }

  /**
   * Returns a compact identifier for display purposes.
   *
   * @return A short code representing this mana stone.
   *
   * @throws IllegalArgumentException if no short form is defined for this mana
   *                                  stone name.
   */
  @Override
  public String toString() {
    return switch (name) {
    case "Mana Stone" -> "MS";
    default -> throw new IllegalArgumentException("Unknown magic item type: " + name);
    };
  }

  /**
   * Implements the usage contract for this item during combat.
   * <p>
   * This method performs strict null checks on all required game objects but does
   * not trigger any specific effect, serving as a placeholder for items that may
   * only have passive properties or are not yet implemented.
   * </p>
   *
   * @param heros    the hero instance using the item.
   * @param target   the enemy targeted by the hero.
   * @param backpack the backpack grid containing the item.
   * @param self     the specific instance of the item being triggered.
   * @return {@code false} as no active effect is performed by this
   *         implementation.
   * @throws NullPointerException if any of the provided arguments are
   *                              {@code null}.
   */
  @Override
  public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance self) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(backpack);
    Objects.requireNonNull(self);
    Objects.requireNonNull(heros);
    return false;
  }

  @Override
  public boolean isManaStone() {
    return true;
  }
}
