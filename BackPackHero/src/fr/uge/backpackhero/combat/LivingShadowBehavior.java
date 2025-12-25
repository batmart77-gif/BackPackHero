package fr.uge.backpackhero.combat;

import java.util.Objects;
import java.util.Random;
import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.item.*;

public class LivingShadowBehavior implements EnemyBehavior {
  private final Random rnd = new Random();

  @Override
  public EnemyAction chooseAction() {
    if (rnd.nextBoolean()) {
        var factory = new StuffFactory();
        return new CurseAction((Curse) factory.create(Stuff.Curse));
    }
    return new ProtectAction(0);
  }

  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    switch (action) {
      case CurseAction c -> throw new IllegalStateException("Géré par le Combat");
      case ProtectAction p -> { /* Ne fait rien si 0 */ }
      case AttackAction a -> target.recevoirDegats(a.damage());
      case StatusEffectAction s -> target.addEffect(s.effect(), s.stacks());
    }
  }
}