package fr.uge.backpackhero.entites;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.combat.EnemyAction;
import fr.uge.backpackhero.combat.EnemyBehavior;

/**
 * Represents an enemy unit in the dungeon.
 * Manages its health, protection, status effects, and AI behavior during combat.
 */
public class Ennemi {
  /** Current health points (HP). */
  private int pv;
  
  /** Maximum health points (HP) for this enemy. */
  private final int pvMax;
  
  /** Current protection (block) amount. Absorbs damage before HP. */
  private int protection;
  
  /** Experience points awarded to the hero upon defeating this enemy. */
  private final int xpReward;
  
  /** The AI behavior module piloting this enemy. */
  private final EnemyBehavior comportement;
  
  /** The action (intent) announced by the enemy for the current turn. */
  private EnemyAction actionAnnoncee;
  
  /** Map storing the current status effects (Effect -> Stack Count). */
  private final Map<Effect, Integer> statusEffects1 = new HashMap<>();

  private final String name;
  /**
   * Constructs a new enemy instance.
   *
   * @param pvMax The maximum HP of this enemy.
   * @param xpReward The experience points gained by the hero upon defeat.
   * @param comportement The AI behavior module.
   * @throws NullPointerException if the behavior is null.
   * @throws IllegalArgumentException if PV max is not positive or XP is negative.
   */
  public Ennemi(String name,int pvMax, int xpReward, EnemyBehavior comportement) {
    Objects.requireNonNull(comportement, "Le comportement ne peut pas être nul");
    this.name = Objects.requireNonNull(name, "Le nom de l'ennemi est requis");
    if (pvMax <= 0) {
      throw new IllegalArgumentException("Les PV max doivent être positifs.");
    }
    if (xpReward < 0) {
      throw new IllegalArgumentException("L'XP ne peut pas être négative");
    }
    this.pvMax = pvMax;
    this.pv = pvMax;
    this.protection = 0;
    this.comportement = comportement;
    this.actionAnnoncee = null;
    this.xpReward = xpReward;
  }

  /**
   * Chooses the next action by delegating to its behavior interface.
   * Called at the start of the hero's turn to announce the enemy's intent.
   *
   * @return The planned {@link EnemyAction}.
   */
  public EnemyAction choisirProchaineAction() {
    this.actionAnnoncee = comportement.chooseAction();
    return actionAnnoncee;
  }
  
  /**
   * Executes the action that was previously announced on the Hero.
   * Called during the enemy's turn.
   *
   * @param heros The hero target.
   */
  public void executerAction(Heros heros) {
    Objects.requireNonNull(heros);
    if (actionAnnoncee == null) return;
    
    comportement.executeAction(actionAnnoncee, this, heros);
    this.actionAnnoncee = null; // The action is consumed
  }

  /**
   * Inflicts damage upon the enemy (called by the Hero's action).
   * Handles {@link Effect#DODGE} charges and protection block.
   *
   * @param montantDegats The amount of damage to inflict.
   * @throws IllegalArgumentException if the damage amount is negative.
   */
  public void recevoirDegats(int montantDegats) {
    if (montantDegats < 0) {
      throw new IllegalArgumentException("On ne peut pas infliger de dégâts négatifs");
    }
    // Handle DODGE
    int dodge = getStatus(Effect.DODGE);
    if (dodge > 0) {
        System.out.println("L'ennemi ESQUIVE l'attaque ! (" + (dodge - 1) + " charges restantes)");
        return; // Damage completely ignored
    }
    
    // Protection absorption
    int degatsAbsorbes = Math.min(montantDegats, this.protection);
    this.protection -= degatsAbsorbes;
    montantDegats -= degatsAbsorbes;
    
    // Apply remaining damage to HP
    if (montantDegats > 0) {
        this.pv = Math.max(0, this.pv - montantDegats);
    }  
  }
  
  /**
   * Increases the protection (block) of this enemy.
   * Called by the enemy's behavior (ProtectAction).
   *
   * @param montant The amount of protection to add.
   * @throws IllegalArgumentException if the amount is negative.
   */
  public void gagnerProtection(int montant) {
    if (montant < 0) {
      throw new IllegalArgumentException("On ne peut pas gagner de protection négative");
    }
    this.protection += montant;
  }

  /**
   * Checks if the enemy's current HP is greater than zero.
   *
   * @return {@code true} if the enemy is alive.
   */
  public boolean estVivant() {
    return this.pv > 0;
  }

  /**
   * Gets the XP reward granted to the hero upon defeating this enemy.
   *
   * @return The XP amount.
   */
  public int getxpReward() {
    return xpReward;
  }
  
  /**
   * Adds X stacks of a specific status effect to the enemy.
   *
   * @param effect The effect to add.
   * @param amount The number of stacks to add.
   */
  public void addStatus(Effect effect, int amount) {
    statusEffects1.merge(effect, amount, Integer::sum);
    System.out.println("✨ L'ennemi gagne : " + effect.getNom() + " (Cumul: " + getStatus(effect) + ")");
  }

  /**
   * Retrieves the current stack count (X value) of a specific status effect.
   *
   * @param effect The effect to check.
   * @return The stack count, or 0 if the effect is absent.
   */
  public int getStatus(Effect effect) {
    return statusEffects1.getOrDefault(effect, 0);
  }

  /**
   * Triggers effects that occur at the START of the enemy's turn.
   * Handles {@link Effect#REGEN} and {@link Effect#BURN}.
   */
  public void triggerStartTurnEffects() {
    // 1. Regeneration
    int regen = getStatus(Effect.REGEN);
    if (regen > 0) {
      System.out.println("L'ennemi se régénère : +" + regen + " PV");
      this.pv = Math.min(this.pv + regen, this.pvMax);
    }
    
    // 2. Burn (Damage at the start of the turn)
    int burn = getStatus(Effect.BURN);
    if (burn > 0) {
      System.out.println("L'ennemi brûle : -" + burn + " PV");
      recevoirDegats(burn);
    }
  }

  /**
   * Triggers effects that occur at the END of the enemy's turn.
   * Handles {@link Effect#POISON} and the general degradation of status stacks.
   */
  public void triggerEndTurnEffects() {
    // 1. Poison (Ignores armor!)
    int poison = getStatus(Effect.POISON);
    if (poison > 0) {
      System.out.println("Le Poison ronge l'ennemi : -" + poison + " PV");
      // Poison bypasses protection
      this.pv = Math.max(0, this.pv - poison);
    }
    
    // 2. Degradation of effects (-1 stack everywhere at the end of the turn)
    statusEffects1.replaceAll((e, v) -> v - 1);
    statusEffects1.values().removeIf(v -> v <= 0);
  }

  /**
   * Gets the current health points (HP) of the enemy.
   *
   * @return Current HP.
   */
  public int getHp() {
    return pv;
  }
  
  /**
   * Clears the announced action, typically after the action has been executed.
   */
  public void clearActionAnnoncee() {
    this.actionAnnoncee = null;
  }
  
  /**
   * Gets the action announced by the enemy for the current turn.
   *
   * @return The announced {@link EnemyAction}.
   */
  public EnemyAction getActionAnnoncee() { return actionAnnoncee; }

  public int getMaxHp() {
    return pvMax;
  }
  
  /**
   * Retourne le nom de l'ennemi (ex: "ratloup"). 
   * Utilisé par GraphicEngine pour charger l'image.
   */
  public String getName() {
    return name;
  }
  
}