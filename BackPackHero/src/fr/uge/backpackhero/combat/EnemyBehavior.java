package fr.uge.backpackhero.combat;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Interface defining the intelligence (behavior) of an enemy.
 * Classes implementing this interface define how a specific enemy type chooses and executes its actions.
 */
public interface EnemyBehavior {
  
  /**
   * The enemy thinks and chooses its next action based on its specific logic.
   * This action is typically announced to the player before execution.
   *
   * @return The action object (the message) that will be announced.
   */
  EnemyAction chooseAction();
  
  /**
   * Executes the action that had been previously announced.
   *
   * @param action The action to execute.
   * @param owner  The enemy that executes the action.
   * @param target The target of the action (usually the Hero).
   */
  void executeAction(EnemyAction action, Ennemi owner, Heros target);
}