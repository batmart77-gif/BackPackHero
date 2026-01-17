package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a magical item that can be used in combat. Deals damage to a
 * target at a certain energy cost and can be bought or sold.
 * 
 * @param name   The name of the magic item.
 * @param pos    The list of relative positions describing the shape of the item
 *               in the backpack.
 * @param rarity The rarity level of the magic item.
 * @param stats  The amount of damage the magic item deals on use.
 * @param price  The buying and selling price of the magic item.
 */
public record MagicItem(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price)
    implements Item {

  /**
   * Compact constructor with validation.
   *
   * @throws NullPointerException     if name, rarity, or pos is null.
   * @throws IllegalArgumentException if cost, stats or price is negative.
   */
  public MagicItem {
    Objects.requireNonNull(name);
    Objects.requireNonNull(rarity);
    Objects.requireNonNull(pos);

    if (cost < 0 || stats < 0 || price < 0) {
      throw new IllegalStateException("cost, stats and price must be positive or null");
    }
  }

  /**
   * Returns a human-readable description of the magic item.
   *
   * @return A string describing the armor and its gameplay effect.
   */
  public String details() {
    return "Magic Item " + name + ", " + rarity.toString() + ", deals " + stats + " damage on use, needs " + cost
        + " to be used, can be sold or bought to a merchant for " + price;
  }

  /**
   * Returns a compact identifier for display purposes.
   *
   * @return A short code representing this magic item.
   *
   * @throws IllegalArgumentException if no short form is defined for this magic
   *                                  item name.
   */
  @Override
  public String toString() {
    return switch (name) {
    case "Electric Wand" -> "EW";
    default -> throw new IllegalArgumentException("Unknown magic item type: " + name);
    };
  }

  /**
   * Triggers the magical item's offensive ability by consuming an adjacent Mana
   * Stone.
   * <p>
   * The logic proceeds as follows:
   * <ol>
   * <li>Validates that the target exists and is alive.</li>
   * <li>Searches the backpack for any adjacent item instance that qualifies as a
   * {@code ManaStone}.</li>
   * <li>If a stone is found, the target receives damage equal to the item's
   * {@code stats}, and the consumed {@code ManaStone} is permanently removed from
   * the backpack.</li>
   * </ol>
   * </p>
   *
   * @param heros    the hero using the item.
   * @param target   the enemy to be damaged.
   * @param backpack the inventory containing both this item and potential mana
   *                 stones.
   * @param instance the specific instance of this item being activated.
   * @return {@code true} if a mana stone was successfully found and consumed to
   *         deal damage; {@code false} if the target is invalid or no mana stone
   *         is adjacent.
   * @throws NullPointerException if {@code heros}, {@code target},
   *                              {@code backpack}, or {@code instance} is
   *                              {@code null}.
   */
  @Override
  public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance instance) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(target);
    Objects.requireNonNull(backpack);
    Objects.requireNonNull(instance);
    if (!target.estVivant())
      return false;
    var manaOpt = backpack.getAdjacentItemInstance(instance, item -> item instanceof ManaStone);
    if (manaOpt.isPresent()) {
      ItemInstance manaInst = manaOpt.get();
      target.recevoirDegats(stats);
      backpack.removeItem(manaInst);
      return true;
    }
    return false;
  }
}
