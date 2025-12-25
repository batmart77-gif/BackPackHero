package fr.uge.backpackhero.combat;

import java.util.Objects;

/**
 * Représente l'intention d'appliquer un effet de statut (Poison, Lenteur, etc.).
 */
public record StatusEffectAction(Effect effect, int stacks) implements EnemyAction {
  public StatusEffectAction {
    Objects.requireNonNull(effect);
    if (stacks <= 0) throw new IllegalArgumentException("Stacks must be positive");
  }

  @Override
  public String description() {
    return effect.getNom() + " (" + stacks + ")";
  }
}