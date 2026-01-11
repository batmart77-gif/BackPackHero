package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.graphics.ImageManager;

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
  
  public String getSpriteName() {
    // On récupère le premier ennemi et on demande son nom
    // Assurez-vous que votre classe Ennemi a une méthode getName() ou nom()
    return enemies.get(0).getName(); 
  }
  
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageManager img) {
      String spriteName = enemies.get(0).getName(); 
      g.drawImage(img.getImage(spriteName), x, y, size, size, null);
  }
  
  @Override
  public void onClick(Jeu jeu) {
      if (this.enemies().stream().anyMatch(e -> e.estVivant())) {
          // On prépare le combat et on change de mode
          jeu.lancerCombat(this.enemies()); 
          jeu.setMode(Mode.COMBAT);
      }
  }
  
}