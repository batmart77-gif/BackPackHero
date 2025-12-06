package fr.uge.backpackhero.combat;

/**
 * Représente l'intention de se protéger.
 * Stocke le montant de protection gagné.
 *
 * @param amount La quantité de protection (amount = montant).
 */
public record ProtectAction(int amount) implements EnemyAction {
  
  public ProtectAction {
    if (amount < 0) {
      throw new IllegalArgumentException("Le montant (amount) ne peut pas être négatif");
    }
  }
  
  @Override
  public String description() {
    return "Protect (" + amount + ")";
  }
}