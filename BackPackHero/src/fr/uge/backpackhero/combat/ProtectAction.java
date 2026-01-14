package fr.uge.backpackhero.combat;

/**
 * Represents the intention to gain protection (block).
 * Stores the amount of protection gained by the enemy.
 *
 * @param amount The quantity of protection (block) gained.
 */
public record ProtectAction(int amount) implements EnemyAction {
  
  /**
   * Compact constructor to validate that the protection amount is not negative.
   *
   * @param amount The quantity of protection.
   * @throws IllegalArgumentException if the amount is negative.
   */
  public ProtectAction {
    if (amount < 0) {
      throw new IllegalArgumentException("Le montant (amount) ne peut pas être négatif");
    }
  }
  
  /**
   * Provides a human-readable description of the protection action for the interface.
   *
   * @return a descriptive string including the protection value.
   */
  @Override
  public String description() {
    return "Protect (" + amount + ")";
  }
}