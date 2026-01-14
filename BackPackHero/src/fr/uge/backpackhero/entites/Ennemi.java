package fr.uge.backpackhero.entites;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.combat.EnemyAction;
import fr.uge.backpackhero.combat.EnemyBehavior;

/**
 * Represents an enemy unit in the dungeon.
 * Manages statistics, status effects, and delegates combat logic to its behavior.
 */
public final class Ennemi {
  private final String name;
  private final int pvMax;
  private final int xpReward;
  private final EnemyBehavior behavior;
  private final Map<Effect, Integer> statusEffects = new HashMap<>();
  private int pv;
  private int protection;
  private EnemyAction announcedAction;

  /**
   * Constructs a new enemy with specified statistics and behavior.
   *
   * @param name      the non-null name of the enemy.
   * @param pvMax     the maximum health points (must be positive).
   * @param xpReward  the experience points awarded upon defeat.
   * @param behavior  the non-null AI behavior module.
   * @throws NullPointerException if name or behavior is null.
   */
  public Ennemi(String name, int pvMax, int xpReward, EnemyBehavior behavior) {
    this.name = Objects.requireNonNull(name);
    this.behavior = Objects.requireNonNull(behavior);
    if (pvMax <= 0 || xpReward < 0) {
      throw new IllegalArgumentException("Invalid health or XP reward");
    }
    this.pvMax = pvMax;
    this.pv = pvMax;
    this.xpReward = xpReward;
  }

  /**
   * Plans the next action for the current turn.
   *
   * @return the selected EnemyAction intent.
   */
  public EnemyAction choisirProchaineAction() {
    this.announcedAction = behavior.chooseAction();
    return announcedAction;
  }
  
  /**
   * Executes the announced action on the specified hero.
   *
   * @param heros the targeted hero.
   */
  public void executerAction(Heros heros) {
    Objects.requireNonNull(heros);
    if (announcedAction != null) {
      behavior.executeAction(announcedAction, this, heros);
      this.announcedAction = null;
    }
  }

  /**
   * Processes damage intake, considering protection and dodge effects.
   *
   * @param amount the damage amount to receive.
   */
  public void recevoirDegats(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Damage cannot be negative");
    }
    if (getStatus(Effect.DODGE) > 0) {
      return;
    }
    int absorbed = Math.min(amount, this.protection);
    this.protection -= absorbed;
    this.pv = Math.max(0, this.pv - (amount - absorbed));
  }
  
  /**
   * Adds protection points to the enemy.
   *
   * @param amount the quantity of protection to add.
   */
  public void gagnerProtection(int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Protection cannot be negative");
    }
    this.protection += amount;
  }

  /**
   * Adds stacks of a status effect to the enemy.
   *
   * @param effect the non-null status effect type.
   * @param stacks the number of stacks to add.
   */
  public void addStatus(Effect effect, int stacks) {
    Objects.requireNonNull(effect);
    statusEffects.merge(effect, stacks, Integer::sum);
  }

  /**
   * Retrieves the current stack count of a status effect.
   *
   * @param effect the non-null effect to check.
   * @return the stack count, or 0 if absent.
   */
  public int getStatus(Effect effect) {
    Objects.requireNonNull(effect);
    return statusEffects.getOrDefault(effect, 0);
  }

  /**
   * Triggers regeneration and burn effects at the start of the turn.
   */
  public void triggerStartTurnEffects() {
    int regen = getStatus(Effect.REGEN);
    if (regen > 0) {
      this.pv = Math.min(this.pv + regen, this.pvMax);
    }
    recevoirDegats(getStatus(Effect.BURN));
  }

  /**
   * Triggers poison and decreases status stacks at the end of the turn.
   */
  public void triggerEndTurnEffects() {
    int poison = getStatus(Effect.POISON);
    this.pv = Math.max(0, this.pv - poison);
    statusEffects.replaceAll((e, v) -> v - 1);
    statusEffects.values().removeIf(v -> v <= 0);
  }

  /** @return true if health points are above zero. */
  public boolean estVivant() { return pv > 0; }
  public int getHp() { return pv; }
  public int getMaxHp() { return pvMax; }
  public int getxpReward() { return xpReward; }
  public String getName() { return name; }
  public EnemyAction getActionAnnoncee() { return announcedAction; }
  public int getProtection() { return protection; }
}