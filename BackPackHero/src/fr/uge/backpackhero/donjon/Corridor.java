package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.graphics.ImageLoader;

/**
 * Represents an empty corridor room in the dungeon.
 * Nothing happens here, and the hero can safely pass through this room.
 */
public record Corridor() implements Room {
  @Override
  public boolean isTraversable() {
    return true;
  }
  
  @Override
  public void draw(Graphics2D g, int x, int y, int size, ImageLoader img) {
    g.drawImage(img.getImage("tile_floor"), x, y, size, size, null);
  }
  
  @Override
  public void onClick(Jeu jeu) {
  }
}