package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.item.*;

/**
 * Defines the behavior for the Living Shadow enemy, a Phase 3 antagonist.
 * This enemy specializes in obstructing the hero by inflicting curses 
 * or performing defensive maneuvers.
 */
public class LivingShadowBehavior implements EnemyBehavior {
  private final Random rnd = new Random();

  /**
   * Selects an action for the Living Shadow. 
   * It has a 50% chance to inflict a curse and a 50% chance to defend.
   *
   * @return a non-null EnemyAction representing the shadow's intent.
   */
  @Override
  public EnemyAction chooseAction() {
    if (rnd.nextBoolean()) {
        var factory = new StuffFactory();
        return new CurseAction((Curse) factory.create(Stuff.Curse));
    }
    return new ProtectAction(0);
  }

  /**
   * Executes the chosen action using pattern matching for polymorphism.
   * Ensures all arguments are valid before application.
   *
   * @param action the action to be performed.
   * @param owner  the enemy instance owning this behavior.
   * @param target the hero targeted by the action.
   */
  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    Objects.requireNonNull(owner);
    Objects.requireNonNull(target);
    switch (action) {
      case CurseAction c -> throw new IllegalStateException("Géré par le Combat");
      case ProtectAction p -> owner.gagnerProtection(p.amount());
      case AttackAction a -> target.recevoirDegats(a.damage());
      case StatusEffectAction s -> target.addEffect(s.effect(), s.stacks());
    }
  }
}