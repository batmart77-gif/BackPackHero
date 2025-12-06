package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Représente une instance d'un Item dans le sac à dos
 * Gère les états variables comme la rotation
 */
public class ItemInstance {

    private final Item baseItem;
    private int rotationAngle = 0;

    public ItemInstance(Item item) {
        Objects.requireNonNull(item);
        this.baseItem = item;
    }
    
    /**
     * Met à jour l'angle de rotation de l'item
     */
    public void rotate() {
    	if (!baseItem.rotatable()) {
    		throw new IllegalStateException("You can't rotate a curse !");
    	}
        this.rotationAngle = (this.rotationAngle + 90) % 360;
    }

    /**
     * Méthode accesseur
     * @return l'angle de rotation
     */
    public int getRotationAngle() {
        return this.rotationAngle;
    }
    
    /**
     * Modifie la forme de l'item en le tournant
     * @return une liste contenant les nouvelles positions de l'item
     */
    public List<Position> getCurrentShape() {
        return calculateRotatedShape(this.baseItem.pos(), this.rotationAngle);
    }
    
    /**
     * Méthode accesseur
     * @return le nom de l'item
     */
    public String getName() {
        return this.baseItem.name();
    }
    
    @Override
    public String toString() {
        return this.baseItem.toString();
    }

    /**
     * Calcule la nouvelle forme de l'item
     * @param originalShape
     * @param angle
     * @return une liste contenant les nouvelles positions de l'item
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
     * Tourne l'item a 90 degré dans le sens des aiguilles d'une montre
     * @param shape
     * @return une liste contenant les nouvelles positions de l'item
     */
    private List<Position> rotateShape90(List<Position> shape) {
    	Objects.requireNonNull(shape);
        var list = new ArrayList<Position>();
        for (var elmt : shape) {
            var newR = elmt.column();
            var newC = -elmt.row();
            list.add(new Position(newR, newC));
        }
        var minR = list.stream()
        		.mapToInt(Position::row)
        		.min()
        		.orElse(0);
        var minC = list.stream()
        		.mapToInt(Position::column)
        		.min()
        		.orElse(0);
        var normalized = new ArrayList<Position>();
        for (var elmt : list) {
            normalized.add(new Position(elmt.row() - minR, elmt.column() - minC));
        }
        return normalized;
    }
    
    public Item getItem() {
      return this.baseItem;
    } 
}