package fr.uge.backpackhero;

/**
 * Represents the different possible states (modes) of the game.
 * This enum is used by the Game and GraphicEngine to switch between different
 * game logic and user interfaces.
 */
public enum Mode {
  /**
   * The player is navigating the dungeon map and moving between rooms.
   */
  EXPLORATION, 

  /**
   * The player is engaged in turn-based combat with one or more enemies.
   */
  COMBAT, 

  /**
   * The game is over because the hero's health points reached zero.
   */
  PERDU, 

  /**
   * The hero has successfully finished the dungeon floors.
   */
  GAGNE,
  
  /**
   * The hero is currently interacting with a merchant in a shop room.
   */
  BOUTIQUE,
  
  /**
   * The hero is currently interacting with a healer to restore health.
   */
  SOIN
}