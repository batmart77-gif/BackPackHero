package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

/**
 * Represents an instance of an Item in the backpack. Manages variable states
 * such as rotation.
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

  /**
   * Sets the anchor position of this item instance within the backpack. This
   * represents the top-left coordinate of the item's shape on the grid.
   *
   * @param pos the new {@link Position} to assign to this item; can be
   *            {@code null} if the item is removed from the grid.
   */
  public void setPos(Position pos) {
    this.pos = pos;
  }

  /**
   * Retrieves the current anchor position of this item instance in the backpack
   * grid. This position typically represents the top-left corner of the item's
   * occupied area.
   *
   * @return the {@link Position} of the item, or {@code null} if the item is not
   *         currently placed on the grid.
   */
  public Position getPos() {
    return pos;
  }

  /**
   * Calculates the width of the item based on its current shape and rotation. The
   * width is defined as the horizontal span between the leftmost and rightmost
   * columns occupied by the item's shape.
   *
   * @return the number of columns the item occupies; 0 if the shape is empty.
   */
  public int width() {
    var shape = getCurrentShape();
    if (shape.isEmpty())
      return 0;
    int maxCol = shape.stream().mapToInt(Position::column).max().getAsInt();
    int minCol = shape.stream().mapToInt(Position::column).min().getAsInt();
    return maxCol - minCol + 1;
  }

  /**
   * Calculates the height of the item based on its current shape and rotation.
   * The height is defined as the vertical span between the highest and lowest
   * rows occupied by the item's shape.
   *
   * @return the number of rows the item occupies; 0 if the shape is empty.
   */
  public int height() {
    var shape = getCurrentShape();
    if (shape.isEmpty())
      return 0;
    int maxRow = shape.stream().mapToInt(Position::row).max().getAsInt();
    int minRow = shape.stream().mapToInt(Position::row).min().getAsInt();
    return maxRow - minRow + 1;
  }

}