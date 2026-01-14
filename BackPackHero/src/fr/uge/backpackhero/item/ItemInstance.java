package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;


/**
 * Represents an instance of an Item in the backpack.
 * Manages variable states such as rotation.
 */
public class ItemInstance {

    private final Item item;
    private int rotationAngle;
    private Position pos;

    /**
     * Creates a new instance of an Item.
     *
     * @param item the base item
     * @throws NullPointerException if the item is null
     */
    public ItemInstance(Item item) {
        Objects.requireNonNull(item);
        this.item = item;
    }
    
    /**
     * Rotates the item by 90 degrees clockwise.
     *
     * @throws IllegalStateException if the item cannot be rotated
     */
    public void rotate() {
      if (!item.rotatable()) {
        throw new IllegalStateException("You can't rotate a curse !");
      }
        this.rotationAngle = (this.rotationAngle + 90) % 360;
    }
    
    /**
     * Returns the current shape of the item based on its rotation.
     *
     * @return a list of positions representing the rotated shape
     */
    public List<Position> getCurrentShape() {
        return item.shapeAtRotation(this.rotationAngle);
    }
    
    /**
     * Returns the name of the base item.
     *
     * @return the item's name
     */
    public String getName() {
        return this.item.name();
    }
    
    /**
     * Returns the current rotation angle of the item.
     *
     * @return the rotation angle in degrees
     */
    public int getRotationAngle() {
        return this.rotationAngle;
    }
    
    /**
     * Returns the base item.
     *
     * @return the base item
     */
    public Item getItem() {
      return this.item;
    } 
    
    @Override
    public String toString() {
        return this.item.toString();
    }
    
    public void setPos(Position pos) {
      this.pos = pos;
    }
  
    public Position getPos() {
      return pos;
    }
    
    public int width() {
      var shape = getCurrentShape();
      if (shape.isEmpty()) return 0;
      int maxCol = shape.stream().mapToInt(Position::column).max().getAsInt();
      int minCol = shape.stream().mapToInt(Position::column).min().getAsInt();
      return maxCol - minCol + 1;
  }

  public int height() {
      var shape = getCurrentShape();
      if (shape.isEmpty()) return 0;
      int maxRow = shape.stream().mapToInt(Position::row).max().getAsInt();
      int minRow = shape.stream().mapToInt(Position::row).min().getAsInt();
      return maxRow - minRow + 1;
  }
    
}