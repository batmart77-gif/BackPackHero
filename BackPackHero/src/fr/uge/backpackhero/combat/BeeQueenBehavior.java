package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;

public class BeeQueenBehavior implements EnemyBehavior {
  private final Random rnd = new Random();

  @Override
  public EnemyAction chooseAction() {
    int rdm = rnd.nextInt(10);
    if (rdm < 7) {
      return new AttackAction(15);
    } else {
      return new StatusEffectAction(Effect.POISON, 1);
    }
  }

  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    switch (action) {
      case AttackAction atk       -> target.recevoirDegats(atk.damage());
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case ProtectAction prot    -> owner.gagnerProtection(prot.amount());
      case CurseAction c         -> throw new IllegalStateException("Géré par le Combat");
    }
  }
}