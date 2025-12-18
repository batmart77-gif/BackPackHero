package fr.uge.backpackhero.combat;

/**
 * Represents the different possible states of a combat.
 * Used to determine if the game should continue or stop.
 */
public enum CombatState { 
  /**
   * The combat is still in progress.
   */
  IN_PROGRESS, 
  
  /**
   * The hero has won (all enemies are dead).
   */
  WIN, 
  
  /**
   * The hero has lost (0 HP).
   */
  LOSS 
}