package fr.uge.backpackhero.combat;

import java.util.Objects;

/**
 * Represents the intention of an enemy to apply a status effect.
 * This record stores the type of effect and the number of stacks to be applied to the hero.
 *
 * @param effect the non-null type of status effect.
 * @param stacks the positive number of stacks for the effect.
 */
public record StatusEffectAction(Effect effect, int stacks) implements EnemyAction {

  /**
   * Compact constructor that ensures the status effect data is valid.
   *
   * @param effect the status effect type.
   * @param stacks the intensity/stacks of the effect.
   * @throws NullPointerException if the effect is null.
   * @throws IllegalArgumentException if stacks is not strictly positive.
   */
  public StatusEffectAction {
    Objects.requireNonNull(effect);
    if (stacks <= 0) {
      throw new IllegalArgumentException("stacks must be positive");
    }
  }

  /**
   * Provides a human-readable description of the status effect action for the UI.
   *
   * @return a descriptive string including the effect name and stacks.
   */
  @Override
  public String description() {
    return effect.getNom() + " (" + stacks + ")";
  }
}