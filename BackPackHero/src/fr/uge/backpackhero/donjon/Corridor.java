package fr.uge.backpackhero.donjon;

/**
 * Represents an empty corridor room in the dungeon.
 * Nothing happens here, and the hero can safely pass through this room.
 */
public record Corridor() implements Room {
  @Override
  public boolean isTraversable() {
    return true;
  }
}