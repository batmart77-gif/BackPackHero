package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Represents a gold coin item. Gold occupies one slot in the backpack but
 * cannot be used in combat.
 */
public record Gold() implements Item {

  /**
   * Constructs a Gold item. No parameters are required since gold always occupies
   * a single tile.
   */
  public Gold {
  }

  /**
   * Gold cannot be sold or bought as an item.
   *
   * @return always {@code 0}
   */
  @Override
  public int price() {
    return 0;
  }

  /**
   * @return a short string used for backpack display
   */
  @Override
  public String toString() {
    return "Gld";
  }

  /**
   * Gold has a common rarity.
   *
   * @return {@code Rarity.COMMON}
   */
  @Override
  public Rarity rarity() {
    return Rarity.COMMON;
  }

  /**
   * Gold always occupies exactly one grid position.
   *
   * @return a list containing one position: (0, 0)
   */
  @Override
  public List<Position> pos() {
    return List.of(new Position(0, 0));
  }

  /**
   * @return the fixed name of this item: {@code "Gold"}
   */
  @Override
  public String name() {
    return "Gold";
  }

  /**
   * @return a textual description of this item
   */
  @Override
  public String details() {
    return "A simple gold coin";
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
}
