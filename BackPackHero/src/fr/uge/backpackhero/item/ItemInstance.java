package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Represents an instance of an Item in the backpack.
 * Manages variable states such as rotation.
 */
public class ItemInstance {

    private final Item baseItem;
    private int rotationAngle = 0;

    /**
     * Creates a new instance of an Item.
     *
     * @param item the base item
     * @throws NullPointerException if the item is null
     */
    public ItemInstance(Item item) {
        Objects.requireNonNull(item);
        this.baseItem = item;
    }
    
    /**
     * Rotates the item by 90 degrees clockwise.
     *
     * @throws IllegalStateException if the item cannot be rotated
     */
    public void rotate() {
      if (!baseItem.rotatable()) {
        throw new IllegalStateException("You can't rotate a curse !");
      }
        this.rotationAngle = (this.rotationAngle + 90) % 360;
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
     * Returns the current shape of the item based on its rotation.
     *
     * @return a list of positions representing the rotated shape
     */
    public List<Position> getCurrentShape() {
        return calculateRotatedShape(this.baseItem.pos(), this.rotationAngle);
    }
    
    /**
     * Returns the name of the base item.
     *
     * @return the item's name
     */
    public String getName() {
        return this.baseItem.name();
    }
    
    @Override
    public String toString() {
        return this.baseItem.toString();
    }

    /**
     * Calculates the rotated shape of the item according to the rotation angle.
     *
     * @param originalShape the original shape of the item
     * @param angle the rotation angle in degrees
     * @return a list of positions representing the rotated shape
     */
    private List<Position> calculateRotatedShape(List<Position> originalShape, int angle) {
      Objects.requireNonNull(originalShape);
        var currentShape = originalShape;
        var rotationCount = angle / 90;
        for (int i = 0; i < rotationCount; i++) {
            currentShape = rotateShape90(currentShape);
        }
        return currentShape;
    }

    /**
     * Rotates the shape 90 degrees clockwise and normalizes it to start at (0,0).
     *
     * @param shape the original shape
     * @return the rotated and normalized shape
     */
    private List<Position> rotateShape90(List<Position> shape) {
      Objects.requireNonNull(shape);
        var list = new ArrayList<Position>();
        for (var elmt : shape) {
            var newR = elmt.column();
            var newC = -elmt.row();
            list.add(new Position(newR, newC));
        }
        var minR = list.stream().mapToInt(Position::row).min().orElse(0);
        var minC = list.stream().mapToInt(Position::column).min().orElse(0);
        var normalized = new ArrayList<Position>();
        for (var elmt : list) {
            normalized.add(new Position(elmt.row() - minR, elmt.column() - minC));
        }
        this.rotationAngle = 0; // Reset angle after normalization
        return normalized;
    }
    
    /**
     * Returns the base item.
     *
     * @return the base item
     */
    public Item getItem() {
      return this.baseItem;
    } 
}