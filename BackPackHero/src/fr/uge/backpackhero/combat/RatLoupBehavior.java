package fr.uge.backpackhero.combat;

import java.util.Random;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Implémentation concrète du comportement pour le Rat-Loup.
 * Logique : Choisit aléatoirement entre Attaque et Protection.
 */
public final class RatLoupBehavior implements EnemyBehavior {

  private final int ATTACK_DAMAGE;
  private final int PROTECT_AMOUNT;
  private final Random RND;
  
  public RatLoupBehavior() {
    this.ATTACK_DAMAGE = 5;
    this.PROTECT_AMOUNT = 3;
    this.RND = new Random();
  }
 
  /**
   * Choisit aléatoirement (50/50) entre attaquer et se protéger.
   */
  @Override
  public EnemyAction chooseAction() {
    int rdm = RND.nextInt(10);
    if (rdm < 7) {
      return new AttackAction(ATTACK_DAMAGE);
    } else if (rdm < 9){
      return new ProtectAction(PROTECT_AMOUNT);
    } else {
      var factory = new StuffFactory();
      var item = factory.create(Stuff.Curse);
      return new CurseAction((fr.uge.backpackhero.item.Curse) item);      
    }
  }

  /**
   * Exécute l'action choisie en utilisant le pattern matching.
   */
  @Override
  public void executeAction(EnemyAction action, Ennemi enemy, Heros heros) {
    Objects.requireNonNull(action);
    Objects.requireNonNull(enemy);
    Objects.requireNonNull(heros);
    switch (action) {
      case AttackAction attack ->
        heros.recevoirDegats(attack.damage());
      case ProtectAction protect ->
        enemy.gagnerProtection(protect.amount());
      case CurseAction curseAct ->
        heros.receiveCurse(curseAct.curse());
    }
  }
}