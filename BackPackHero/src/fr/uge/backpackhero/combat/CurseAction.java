package fr.uge.backpackhero.combat;

import java.util.Objects;

import fr.uge.backpackhero.item.Curse;

/**
 * Represents the intention to inflict a curse.
 * Stores the specific curse object that the enemy wants to give to the hero.
 *
 * @param curse The curse object to be inflicted.
 */
public record CurseAction(Curse curse) implements EnemyAction {
  
  /**
   * Compact constructor to validate that the curse is not null.
   * * @param curse The curse object.
   */
  public CurseAction {  
    Objects.requireNonNull(curse);
  }
  
  @Override
  public String description() {
    return "Malediction ";
  }
}