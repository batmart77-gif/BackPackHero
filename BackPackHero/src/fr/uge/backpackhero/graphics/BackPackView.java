package fr.uge.backpackhero.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Optional;

import fr.uge.backpackhero.item.BackPack;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;

/**
 * Handles the graphical representation of the backpack (the inventory grid).
 * It draws the grid cells and the items contained within them using AWT Graphics.
 */
public class BackPackView {
  
  /** The backpack data model containing the items. */
  private final BackPack backpack;
  
  /** The size of a single grid cell in pixels. */
  private final int cellSize; 
  
  /**
   * Creates a new view for the specified backpack.
   *
   * @param backpack The backpack model to display.
   * @param cellSize The width and height of one cell in pixels.
   */
  public BackPackView(BackPack backpack, int cellSize) {
    this.backpack = backpack;
    this.cellSize = cellSize;
  }
  
  /**
   * Draws the entire backpack grid at the specified coordinates.
   *
   * @param graphics The graphics context used for drawing.
   * @param startX   The x-coordinate of the top-left corner of the grid.
   * @param startY   The y-coordinate of the top-left corner of the grid.
   */
  public void draw(Graphics2D graphics, int startX, int startY) {
    int rows = backpack.getRows();
    int cols = backpack.getColumns();

    // Draw the grid and cells
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < cols; c++) {
        int x = startX + c * cellSize;
        int y = startY + r * cellSize;
        
        // Background color (dark gray)
        graphics.setColor(new Color(60, 60, 60)); 
        graphics.fillRect(x, y, cellSize, cellSize);
        
        // Border color (darker gray)
        graphics.setColor(new Color(30, 30, 30)); 
        graphics.drawRect(x, y, cellSize, cellSize);
        
        // Draw the item content if present
        drawItemContent(graphics, r, c, x, y);
      }
    }
    
    // Draw the main border of the backpack (white)
    graphics.setColor(Color.WHITE);
    graphics.drawRect(startX, startY, cols * cellSize, rows * cellSize);
  }
  
  /**
   * Draws the content of a specific cell if an item occupies it.
   *
   * @param graphics The graphics context.
   * @param r        The row index in the backpack grid.
   * @param c        The column index in the backpack grid.
   * @param x        The x-coordinate on screen.
   * @param y        The y-coordinate on screen.
   */
  private void drawItemContent(Graphics2D graphics, int r, int c, int x, int y) {
    Optional<ItemInstance> optionalItem = backpack.getItemAt(new Position(r, c));
    
    if (optionalItem.isPresent()) {
      ItemInstance item = optionalItem.get();
      
      // Generate a unique color based on the item's hashcode
      Color itemColor = Color.getHSBColor(item.hashCode() % 255 / 255f, 0.7f, 0.9f);
      graphics.setColor(itemColor);
      
      int padding = 3;
      graphics.fillRect(x + padding, y + padding, cellSize - 2 * padding, cellSize - 2 * padding);
      
      graphics.setColor(Color.BLACK);
      // Display the first letter of the item name
      String letter = item.getName().isEmpty() ? "?" : item.getName().substring(0, 1);
      graphics.drawString(letter, x + cellSize / 3, y + 2 * cellSize / 3); 
    }
  }
}