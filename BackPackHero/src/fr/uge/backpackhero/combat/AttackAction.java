package fr.uge.backpackhero.combat;

/**
 * Represents the intention to attack.
 * Stores the amount of damage planned by the enemy.
 *
 * @param damage The amount of damage points.
 */
public record AttackAction(int damage) implements EnemyAction {
  
  /**
   * Compact constructor to validate the damage value.
   *
   * @param damage The amount of damage.
   * @throws IllegalArgumentException if the damage is negative.
   */
  public AttackAction {
    if (damage < 0) {
      throw new IllegalArgumentException("Damage cannot be negative");
    }
  }
  
  @Override
  public String description() {
      return "Attack (" + damage + ")";
  }
}