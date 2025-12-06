package fr.uge.backpackhero.combat;

/**
 * Représente l'intention d'attaquer.
 * Stocke le montant des dégâts prévus.
 *
 * @param damage Le nombre de points de dégâts (damage = dégâts).
 */
public record AttackAction(int damage) implements EnemyAction {
  
  public AttackAction {
    if (damage < 0) {
      throw new IllegalArgumentException("Les dégâts (damage) ne peuvent pas être négatifs");
    }
  }
  
  @Override
  public String description() {
      return "Attack (" + damage + ")";
  }
}