package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a curse item. A curse usually occupies space in the backpack but
 * provides no benefit and cannot be meaningfully used in combat.
 */
public record Curse(List<Position> pos) implements Item {

  /**
   * Creates a new curse with the given relative shape positions.
   *
   * @param pos the list of relative positions that define the shape of the curse
   * @throws NullPointerException if {@code pos} is null
   */
  public Curse {
    Objects.requireNonNull(pos);
  }

  /**
   * Curses have no monetary value.
   *
   * @return always {@code 0}
   */
  @Override
  public int price() {
    return 0;
  }

  /**
   * @return the fixed name of this item: {@code "Curse"}
   */
  @Override
  public String name() {
    return "Curse";
  }

  /**
   * @return a textual description of the curse
   */
  @Override
  public String details() {
    return "Nothing special about a curse... But you will know despair";
  }

  /**
   * @return the rarity type {@code Rarity.CURSE}
   */
  @Override
  public Rarity rarity() {
    return Rarity.CURSE;
  }

  /**
   * Implements the active usage logic for this item during a combat turn. This
   * specific implementation acts as a placeholder and performs no action, always
   * returning {@code false}.
   *
   * @param heros    the hero character using the item.
   * @param target   the enemy character targeted by the item's action.
   * @param backpack the backpack instance where the item is stored.
   * @param self     the specific instance of this item being used.
   * @return {@code true} if an action was performed; {@code false} otherwise.
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

  /**
   * Short string representation used for display in the backpack grid.
   *
   * @return {@code "C"}
   */
  @Override
  public String toString() {
    return "C";
  }

}
