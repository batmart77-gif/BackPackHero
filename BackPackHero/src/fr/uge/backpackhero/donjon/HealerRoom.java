package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents a room containing a healer.
 * Entering this room allows the hero to access healing services in exchange for gold.
 */
public record HealerRoom() implements Room {
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
      g.drawImage(img.getImage("healer"), x, y, size, size, null);
  }
  
  @Override
  public void onClick(Jeu jeu) {
      jeu.setMode(Mode.SOIN);
  }
  
}