package fr.uge.backpackhero.donjon;

import java.util.Objects;

/**
 * Représente une grille bloquant l'accès à une autre salle.
 * Nécessite une clé pour être déverrouillée
 */
public record GateRoom(Room hiddenRoom) implements Room {
  public GateRoom {
    Objects.requireNonNull(hiddenRoom);
  }

  /**
   * Une grille n'est pas traversable tant qu'elle n'est pas déverrouillée.
   */
  @Override
  public boolean isTraversable() {
    return false; 
  }
}