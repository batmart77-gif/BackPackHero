package fr.uge.backpackhero.donjon;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Ennemi;

/**
 * Represents a room containing enemies.
 * This type of room triggers combat when the hero enters it.
 */
public record EnemyRoom(List<Ennemi> enemies) implements Room {
  
  /**
   * Compact constructor to validate that the list of enemies is not null or empty.
   *
   * @param enemies The list of enemies present in this room.
   * @throws NullPointerException if the enemies list is null.
   * @throws IllegalArgumentException if the enemies list is empty.
   */
  public EnemyRoom {
    Objects.requireNonNull(enemies);
    if (enemies.isEmpty()) {
      throw new IllegalArgumentException("Une salle d'ennemi ne peut pas être vide");
    }
  }
}