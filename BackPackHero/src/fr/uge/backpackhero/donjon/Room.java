package fr.uge.backpackhero.donjon;

/**
 * A sealed interface representing a cell or room on the dungeon map.
 * This is the generic type for all elements that compose a floor.
 * Only the types listed in the {@code permits} clause can implement this interface.
 */
public sealed interface Room permits 
    Corridor, EnemyRoom, TreasureRoom, MerchantRoom, HealerRoom, ExitRoom, EventRoom, GateRoom {
  
  /**
   * Indique si le héros peut traverser cette salle pour atteindre une autre destination.
   * Selon le sujet : "en ne passant que par des couloirs".
   */
  default boolean isTraversable() {
    return false;
  }
}