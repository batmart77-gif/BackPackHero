package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;

/**
 * Une salle contenant des ennemis.
 * Déclenche un combat quand on entre.
 */
public record EnemyRoom(List<Ennemi> enemies) implements Room {
  
  public EnemyRoom {
    Objects.requireNonNull(enemies);
    if (enemies.isEmpty()) {
      throw new IllegalArgumentException("Une salle d'ennemi ne peut pas être vide");
    }
  }
}