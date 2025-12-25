package fr.uge.backpackhero.combat;

import java.util.Random;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

public final class RatLoupBehavior implements EnemyBehavior {
  private final int minAtk;
  private final int maxAtk;
  private final int minProt;
  private final int maxProt;
  private final Random rnd = new Random();

  /**
   * Constructeur flexible pour gérer les différentes variantes de rats.
   */
  public RatLoupBehavior(int minAtk, int maxAtk, int minProt, int maxProt) {
    this.minAtk = minAtk;
    this.maxAtk = maxAtk;
    this.minProt = minProt;
    this.maxProt = maxProt;
  }

  @Override
  public EnemyAction chooseAction() {
    int rdm = rnd.nextInt(10);
    if (rdm < 7) { // 70% Attaque
      int damage = minAtk + rnd.nextInt(maxAtk - minAtk + 1);
      return new AttackAction(damage);
    } else if (rdm < 9) { // 20% Protection
      int prot = minProt + rnd.nextInt(maxProt - minProt + 1);
      return new ProtectAction(prot);
    } else { // 10% Malédiction
      var factory = new StuffFactory();
      // On suppose que la factory est mise à jour pour éviter le cast manuel
      return new CurseAction((Curse) factory.create(Stuff.Curse));
    }
  }

  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    // Utilisation du switch avec pattern matching (sans instanceof ni casting)
    switch (action) {
      case AttackAction atk       -> target.recevoirDegats(atk.damage());
      case ProtectAction prot    -> owner.gagnerProtection(prot.amount());
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case CurseAction c         -> throw new IllegalStateException("Géré par le Combat");
    }
  }
}