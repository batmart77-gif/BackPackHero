package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a ranged weapon used for attacking enemies from a distance.
 * Requires arrows placed adjacently in the backpack to be used.
 *
 * @param name   The name of the ranged weapon.
 * @param pos    The list of relative positions describing the shape of the item
 *               in the backpack.
 * @param rarity The rarity level of the ranged weapon.
 * @param cost   The energy cost required to use the weapon in combat.
 * @param price  The buying and selling price of the ranged weapon.
 */
public record RangeWeapon(String name, List<Position> pos, Rarity rarity, int cost, int price) implements Item {

  /**
   * Compact constructor with validation.
   *
   * @throws NullPointerException     if name, pos, or rarity is null.
   * @throws IllegalArgumentException if cost or price is negative.
   */
  public RangeWeapon {
    Objects.requireNonNull(name);
    Objects.requireNonNull(pos);
    Objects.requireNonNull(rarity);

    if (cost < 0 || price < 0) {
      throw new IllegalArgumentException("cost and price cant be negative");
    }
  }

  /**
   * Returns a human-readable description of the ranged weapon.
   */
  public String details() {
    return "Range Weapon " + name + ", " + rarity
        + " needs " + cost + " to be used, can be sold or bought to a merchant for " + price;
  }

  /**
   * Returns a compact identifier for display purposes.
   */
  @Override
  public String toString() {
    return switch (name) {
      case "Composite Bow" -> "CB";
      case "Mouse Bow" -> "MB";
      default -> throw new IllegalArgumentException("Unknown ranged weapon type: " + name);
    };
  }

  @Override
  public boolean use(Heros heros, Ennemi target, BackPack backpack, ItemInstance instance) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(backpack);
    Objects.requireNonNull(instance);

    if (target == null || !target.estVivant()) {
      return false;
    }

    if (heros.depenserEnergie(cost)) {
      // Recherche d'une flèche adjacente
      var arrowOpt = backpack.getAdjacentItemInstance(instance, item -> item instanceof Arrow);
      if (arrowOpt.isPresent()) {
        ItemInstance arrowInst = arrowOpt.get();
        Arrow arrow = (Arrow) arrowInst.getItem();

        System.out.println("L'arc tire une flèche !");
        target.recevoirDegats(arrow.stats());

        // Consommation de la flèche
        backpack.removeItem(arrowInst);
        return true;
      }
    }

    System.out.println("Pas de flèche adjacente pour l'arc !");
    return false;
  }
}