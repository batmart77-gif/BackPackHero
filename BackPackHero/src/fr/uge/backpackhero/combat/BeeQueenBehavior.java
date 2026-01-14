package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;

/**
 * Defines the specific behavior for the Bee Queen enemy.
 * The Bee Queen has a high probability of attacking and a lower probability 
 * of inflicting poison status effects.
 */
public class BeeQueenBehavior implements EnemyBehavior {
  private final Random rnd = new Random();

  /**
   * Randomly chooses an action for the Bee Queen.
   * There is a 70% chance of a standard attack and 30% for a poison effect.
   *
   * @return The selected EnemyAction.
   */
  @Override
  public EnemyAction chooseAction() {
    int rdm = rnd.nextInt(10);
    if (rdm < 7) {
      return new AttackAction(15);
    } else {
      return new StatusEffectAction(Effect.POISON, 1);
    }
  }

  /**
   * Executes the provided action using polymorphism via a type-switch.
   *
   * @param action The action to be executed.
   * @param owner  The enemy instance performing the action.
   * @param target The hero being attacked or affected.
   */
  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    Objects.requireNonNull(owner);
    Objects.requireNonNull(target);
    switch (action) {
      case AttackAction atk       -> target.recevoirDegats(atk.damage());
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case ProtectAction prot    -> owner.gagnerProtection(prot.amount());
      case CurseAction c         -> throw new IllegalStateException("Géré par le Combat");
    }
  }
}