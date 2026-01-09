package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;

import fr.uge.backpackhero.graphics.ImageManager;

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
  
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageManager img) {
      g.drawImage(img.getImage("cadenas"), x, y, size, size, null);
  }
}