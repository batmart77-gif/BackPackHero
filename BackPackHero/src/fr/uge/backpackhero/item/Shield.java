package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a defensive item that provides protection during combat. Shields
 * add a block value to the Hero's defense at a certain energy cost.
 *
 * @param name   The name of the shield.
 * @param pos    The list of relative positions describing the shape of the item
 *               in the backpack.
 * @param rarity The rarity level of the shield.
 * @param stats  The base protection value the shield adds on use.
 * @param cost   The energy cost required to use the shield in combat.
 * @param price  The buying and selling price of the shield.
 */
public record Shield(String name, List<Position> pos, Rarity rarity, int stats, int cost, int price) implements Item {

  /**
   * Compact constructor with validation.
   *
   * @throws NullPointerException  if name, rarity, or pos is null.
   * @throws IllegalStateException if cost, stats, or price is negative.
   */
  public Shield {
    Objects.requireNonNull(name);
    Objects.requireNonNull(rarity);
    Objects.requireNonNull(pos);

    if (cost < 0 || stats < 0 || price < 0) {
      throw new IllegalStateException("cost,stats and price must be positive or null");
    }
  }

  /**
   * Returns a human-readable description of the shield.
   *
   * @return A string describing the shield and its protective effect.
   */
  public String details() {
    return "Shield " + name + ", " + rarity.toString() + ", adds " + stats + " protection on use, needs " + cost
        + " to be used, can be sold or bought to a merchant for " + price;
  }

  /**
   * Returns a compact identifier for display purposes.
   *
   * @return A short code representing this shield.
   * @throws IllegalArgumentException if no short form is defined for this shield
   *                                  name.
   */
  @Override
  public String toString() {
    return switch (name) {
    case "Rough Buckler" -> "RB";
    default -> throw new IllegalArgumentException("Je connais pas ca c'est quoi ? ");
    };
  }

  @Override
  public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance self) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(backpack);
    Objects.requireNonNull(self);
    Objects.requireNonNull(heros);
    if (heros.depenserEnergie(cost)) {
      heros.ajouterProtection(stats);
      return true;
    }
    return false;
  }

  @Override
  public boolean isCurse() {
    return false;
  }
}
