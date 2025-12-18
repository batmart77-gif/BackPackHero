package fr.uge.backpackhero.combat;

import java.util.Random;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Concrete implementation of the behavior interface for the Rat-Wolf enemy type.
 * Logic: Randomly chooses between Attack (70%), Protection (20%), and Curse (10%).
 */
public final class RatLoupBehavior implements EnemyBehavior {

  /** Fixed attack damage amount for this enemy type. */
  private final int ATTACK_DAMAGE;
  
  /** Fixed protection (block) amount gained by this enemy type. */
  private final int PROTECT_AMOUNT;
  
  /** Random number generator used for probabilistic choices. */
  private final Random RND;
  
  /**
   * Initializes the fixed damage and protection values for the Rat-Wolf.
   */
  public RatLoupBehavior() {
    this.ATTACK_DAMAGE = 5;
    this.PROTECT_AMOUNT = 3;
    this.RND = new Random();
  }
  
  /**
   * Randomly selects the next action (Attack, Protect, or Curse) based on predefined probabilities.
   * Logic split: 0-6 (Attack), 7-8 (Protect), 9 (Curse).
   * * @return The planned action object.
   */
  @Override
  public EnemyAction chooseAction() {
    int rdm = RND.nextInt(10);
    if (rdm < 7) { // 70% chance
      return new AttackAction(ATTACK_DAMAGE);
    } else if (rdm < 9){ // 20% chance
      return new ProtectAction(PROTECT_AMOUNT);
    } else { // 10% chance
      var factory = new StuffFactory();
      var item = factory.create(Stuff.Curse);
      return new CurseAction((fr.uge.backpackhero.item.Curse) item);      
    }
  }

  /**
   * Executes the action that was previously announced on the target Hero.
   * This method uses pattern matching to handle the specific action type.
   * * @param action The action to execute (Attack or Protect).
   * @param enemy  The Rat-Wolf executing the action.
   * @param heros  The Hero target.
   * @throws NullPointerException if any argument is null.
   * @throws IllegalStateException if a CurseAction is received, as it should be handled by the Combat controller.
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
      throw new IllegalStateException("CurseAction must be handled by Combat/Delegate, not by EnemyBehavior");
    }
  }
}