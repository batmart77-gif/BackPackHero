package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.item.*;

/**
 * Implementation of the behavior for the Frog Wizard enemy.
 * The Frog Wizard focuses on inflicting status effects like poison and slow,
 * or placing curses in the hero's backpack.
 */
public class FrogWizardBehavior implements EnemyBehavior {
  private final Random rnd = new Random();
  
  /**
   * Randomly selects an action for the Frog Wizard among poison, slow, or a curse.
   *
   * @return a non-null EnemyAction representing the wizard's intent.
   */
  @Override
  public EnemyAction chooseAction() {
    int rdm = rnd.nextInt(3);
    return switch (rdm) {
      case 0 -> new StatusEffectAction(Effect.POISON, 4);
      case 1 -> new StatusEffectAction(Effect.SLOW, 2);
      default -> {
        var factory = new StuffFactory();
        yield new CurseAction((Curse) factory.create(Stuff.Curse));
      }
    };
  }

  /**
   * Executes the specified action on the target or owner.
   * This method uses pattern matching in a switch to handle different action types.
   *
   * @param action the action to execute.
   * @param owner  the enemy performing the action.
   * @param target the hero targeted by the action.
   */
  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    Objects.requireNonNull(owner);
    Objects.requireNonNull(target);
    switch (action) {
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case AttackAction atk       -> target.recevoirDegats(atk.damage());
      case ProtectAction prot    -> owner.gagnerProtection(prot.amount());
      case CurseAction c         -> throw new IllegalStateException("Géré par le Combat");
    }
  }
}
