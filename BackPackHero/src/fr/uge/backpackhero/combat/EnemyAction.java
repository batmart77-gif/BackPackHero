package fr.uge.backpackhero.combat;

/**
 * A sealed interface representing a planned action ("message") that an enemy announces
 * before executing it.
 * This is used to display the enemy's intent to the player during the hero's turn.
 */
public sealed interface EnemyAction permits AttackAction, ProtectAction, CurseAction {
  
  /**
   * Returns a textual description of the action to display to the player.
   *
   * @return The text describing the action (e.g., "Attack (5)" or "Malediction").
   */
  String description();
}