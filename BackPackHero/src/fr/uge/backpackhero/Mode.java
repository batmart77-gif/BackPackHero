package fr.uge.backpackhero;

/**
 * Represents the different possible states (modes) of the game.
 * This enum is used to switch between exploration, combat, or end-game screens.
 */
public enum Mode {
  /**
   * The player is exploring the dungeon map (moving between rooms).
   */
  EXPLORATION, 

  /**
   * The player is currently fighting enemies.
   */
  COMBAT, 

  /**
   * The game is over, the player has lost (0 HP).
   */
  PERDU, 

  /**
   * The game is won, the player has finished the dungeon.
   */
  GAGNE 
}