package fr.uge.backpackhero.donjon;

import java.util.Objects;

/**
 * Représente un étage complet du donjon.
 * C'est une grille (tableau 2D) de salles.
 */
public record Floor(Room[][] map) {
  
  public Floor {
    Objects.requireNonNull(map);
    if (map.length == 0 || map[0].length == 0) {
      throw new IllegalArgumentException("La carte ne peut pas être vide.");
    }
  }
  
  /**
   * Récupère la salle à une coordonnée donnée (x, y).
   * Renvoie null si les coordonnées sont hors de la carte.
   */
  public Room getRoom(int x, int y) {
    if (y < 0 || y >= map.length || x < 0 || x >= map[0].length) {
        return null; // Hors de la carte
    }
    return map[y][x];
  }
  
  // Pour la vue (Hauteur = nombre de lignes)
  public int height() { 
    return map.length; 
  }
  
  // Pour la vue (Largeur = nombre de colonnes)
  public int width() {
    return map[0].length;
  }
}