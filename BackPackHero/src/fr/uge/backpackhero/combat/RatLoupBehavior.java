package fr.uge.backpackhero.combat;

import java.util.Random;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Defines the behavior for rat-loup enemies.
 * Rat-loups can attack, defend, or inflict curses with specific probabilities.
 */
public final class RatLoupBehavior implements EnemyBehavior {
  private final int minAtk;
  private final int maxAtk;
  private final int minProt;
  private final int maxProt;
  private final Random rnd = new Random();

  /**
   * Constructs a behavior with specific range values for combat actions.
   *
   * @param minAtk  minimum damage value.
   * @param maxAtk  maximum damage value.
   * @param minProt minimum protection value.
   * @param maxProt maximum protection value.
   * @throws IllegalArgumentException if min values are greater than max values.
   */
  public RatLoupBehavior(int minAtk, int maxAtk, int minProt, int maxProt) {
    if (minAtk > maxAtk || minProt > maxProt) {
      throw new IllegalArgumentException("min values cannot exceed max values");
    }
    this.minAtk = minAtk;
    this.maxAtk = maxAtk;
    this.minProt = minProt;
    this.maxProt = maxProt;
  }

  /**
   * Randomly selects an action among attack (70%), protection (20%), or curse (10%).
   *
   * @return a non-null EnemyAction.
   */
  @Override
  public EnemyAction chooseAction() {
    int rdm = rnd.nextInt(10);
    if (rdm < 7) {
      int damage = minAtk + rnd.nextInt(maxAtk - minAtk + 1);
      return new AttackAction(damage);
    } else if (rdm < 9) {
      int prot = minProt + rnd.nextInt(maxProt - minProt + 1);
      return new ProtectAction(prot);
    }
    var factory = new StuffFactory();
    return new CurseAction((Curse) factory.create(Stuff.Curse));
  }

  /**
   * Executes the chosen action using pattern matching in a switch statement.
   *
   * @param action the action to execute.
   * @param owner  the enemy using the action.
   * @param target the hero targeted by the action.
   */
  @Override
  public void executeAction(EnemyAction action, Ennemi owner, Heros target) {
    Objects.requireNonNull(action);
    Objects.requireNonNull(owner);
    Objects.requireNonNull(target);

    switch (action) {
      case AttackAction atk -> target.recevoirDegats(atk.damage());
      case ProtectAction prot -> owner.gagnerProtection(prot.amount());
      case StatusEffectAction sea -> target.addEffect(sea.effect(), sea.stacks());
      case CurseAction c -> throw new IllegalStateException("Curses are handled by the combat loop");
    }
  }
}