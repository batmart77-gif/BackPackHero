package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.item.*;

public class FrogWizardBehavior implements EnemyBehavior {
  private final Random rnd = new Random();

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

  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    switch (action) {
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case AttackAction atk       -> target.recevoirDegats(atk.damage());
      case ProtectAction prot    -> owner.gagnerProtection(prot.amount());
      case CurseAction c         -> throw new IllegalStateException("Géré par le Combat");
    }
  }
}
