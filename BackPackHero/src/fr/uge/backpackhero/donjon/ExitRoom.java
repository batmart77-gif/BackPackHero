package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageManager;

/**
 * Represents the room that contains the exit door.
 * Entering this room allows the hero to pass to the next floor of the dungeon (or win the game if it is the last floor).
 */
public record ExitRoom() implements Room {
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageManager img) {
      g.drawImage(img.getImage("exit_door"), x, y, size, size, null);
  }
  
  @Override
  public void onClick(Jeu jeu) {
      if (jeu.getDonjon().moveToNextFloor()) {
          System.out.println("Étage suivant !");
          // On réinitialise la position du héros
          // jeu.resetPosition(); // Il faudra créer cette méthode dans Jeu
      } else {
          // jeu.gagner(); // Il faudra mettre le mode à GAGNE
      }
  }
}