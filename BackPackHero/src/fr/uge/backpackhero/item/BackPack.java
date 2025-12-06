package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Représente le sac à dos du héros
 */
public class BackPack {
	
	/**
     * Map principale: associe chaque {@code ItemInstance} à la liste des
     * {@code Position} ABSOLUES qu'il occupe dans la grille.
     */
	private final HashMap<ItemInstance, List<Position>> backpack;
	
	private final int row;
	private final int column;
	
	private int goldQuantity;
	
	/**
     * La grille représentant les cases du sac à dos. Chaque case contient
     * l'ItemInstance qui l'occupe, ou {@code null} si elle est libre.
     */
	private final ItemInstance[][] grid;

	public BackPack(int row, int column) {
		this.backpack = new HashMap<>();
		this.grid = new ItemInstance[row][column];
		this.row = row;
		this.column = column;
		this.goldQuantity = 0;
	}

	public BackPack() {
		this(3, 5);
	}
	
	
	/**
     * Incrémente la quantité d'or du héros.
     * @param amount La quantité d'or à ajouter (doit être >= 0).
     * @throws IllegalArgumentException si la quantité est négative.
     */
    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("La quantité d'or à ajouter ne peut pas être négative.");
        }
        this.goldQuantity += amount;
    }
	
    /**
     * Tente de dépenser une certaine quantité d'or.
     * @param amount La quantité d'or à dépenser (doit être > 0).
     * @return true si la dépense a réussi, false sinon (fonds insuffisants).
     * @throws IllegalArgumentException si la quantité est négative ou nulle.
     */
    public boolean spendGold(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("La quantité d'or à dépenser doit être positive.");
        }
        
        if (this.goldQuantity >= amount) {
            this.goldQuantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Renvoie la quantité totale d'or possédée par le héros.
     * @return La quantité d'or actuelle.
     */
    public int getGoldQuantity() {
        return this.goldQuantity;
    }
    
	/**
     * Vérifie si l'espace est suffisant et libre pour placer l'objet à la position
     * d'ancrage spécifiée, en tenant compte de la forme actuelle (rotation) de l'objet
     * @param itemInstance L'ItemInstance à vérifier
     * @param startPos La Position (coin supérieur gauche) de placement souhaitée
     * @return {@code true} si l'espace est libre, {@code false} sinon (collision ou dépassement)
     * @throws NullPointerException si un argument est {@code null}
     */
	private boolean checkIfEnoughSpace(ItemInstance itemInstance, Position startPos) {
		Objects.requireNonNull(itemInstance);
		Objects.requireNonNull(startPos);
		var listPos = itemInstance.getCurrentShape();
		for (var elmt : listPos) {
			var newRow = startPos.row() + elmt.row();
			var newCol = startPos.column() + elmt.column();
			if (!isInside(elmt)) {
				return false;
			}
			if (this.grid[newRow][newCol] != null) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isInside(Position pos) {
	    return pos.row() >= 0 && pos.row() < this.row &&
	           pos.column() >= 0 && pos.column() < this.column;
	}
	
	/**
     * Place effectivement l'ItemInstance dans la grille et enregistre ses positions
     * Cette méthode est privée et suppose que les vérifications d'espace ont déjà été faites
     * @param itemInstance L'ItemInstance à placer
     * @param startPos La Position (ancrage) où le placer
     * @throws NullPointerException si un argument est {@code null}
     */
	private void placeItem(ItemInstance itemInstance, Position startPos) {
		Objects.requireNonNull(itemInstance);
		Objects.requireNonNull(startPos);
		var absolutePositions = new ArrayList<Position>();
		var shape = itemInstance.getCurrentShape();
		for (var elmt : shape) {
			var newRow = startPos.row() + elmt.row();
			var newCol = startPos.column() + elmt.column();
			var absolutePos = new Position(newRow, newCol);
	        absolutePositions.add(absolutePos);
			this.grid[newRow][newCol] = itemInstance;
		}
		switch(itemInstance.getItem()) {
		case Curse c -> {
			System.out.println("Curse placed successfully. Now the curse will be removed.");
			removeItem(itemInstance);
			}
		default -> this.backpack.put(itemInstance, absolutePositions);
		}
	};

	/**
     * Ajoute un {@code ItemInstance} au sac à dos à la position de départ spécifiée
     * @param itemInstance L'ItemInstance à ajouter
     * @param startPos La Position (ancrage) où le joueur souhaite placer l'Item
     * @return {@code true} si l'Item a été ajouté, {@code false} si le placement est invalide
     * @throws NullPointerException si un argument est {@code null}
     */
	public boolean add(ItemInstance itemInstance, Position startPos) {
		Objects.requireNonNull(itemInstance);
		Objects.requireNonNull(startPos);
		var item = itemInstance.getItem();
		switch(item) {
		case Curse c -> {return addCurse(itemInstance, startPos);}
		default -> {
						if (!checkIfEnoughSpace(itemInstance, startPos)) {
							return false;
						}
						placeItem(itemInstance, startPos);
						return true;
					}
		}
	}
	
	private boolean addCurse(ItemInstance curse, Position startPos) {
		Objects.requireNonNull(curse);
		Objects.requireNonNull(startPos);
		var shape = curse.getCurrentShape();
		for (var relative : shape) {
            var target = new Position(
                startPos.row() + relative.row(),
                startPos.column() + relative.column()
            );
            if (!isInside(target)) {
                return false;
            }
            // S'il y a un item, on le supprime définitivement
            var overlapping = getItemAt(target).orElse(null);
            if (overlapping != null) {
                removeItem(overlapping);
            }
        }
        placeItem(curse, startPos);
        return true;
    }

	/**
     * Supprime un {@code ItemInstance} du sac à dos et libère toutes les cases qu'il occupait
     * @param itemInstance L'ItemInstance à retirer
     * @return {@code true} si l'Item a été retiré, {@code false} s'il n'était pas dans le sac
     * @throws NullPointerException si l'argument est {@code null}
     */
	public boolean removeItem(ItemInstance itemInstance) {
		Objects.requireNonNull(itemInstance);
		var startPos = this.backpack.remove(itemInstance);
		if (startPos == null) {
			return false;
		}
		for (var elmt : startPos) {
			var newRow = elmt.row();
			var newCol = elmt.column();
			if (newRow >= 0 && newRow < row && newCol >= 0 && newCol < column) {
				if (this.grid[newRow][newCol] == itemInstance) {
					this.grid[newRow][newCol] = null;
				}
			}
		}
		return true;
	}

	/**
     * Retourne l'{@code ItemInstance} présent à une position donnée du sac à dos
     * @param pos La Position à vérifier
     * @return Un {@code Optional} contenant l'ItemInstance s'il y en a un, ou {@code Optional.empty()} sinon
     * @throws NullPointerException si la position est {@code null}
     */
	public Optional<ItemInstance> getItemAt(Position pos) {
		Objects.requireNonNull(pos);
		if (pos.row() >= 0 && pos.row() < row && pos.column() >= 0 && pos.column() < column) {
			return Optional.ofNullable(grid[pos.row()][pos.column()]);
		}
		return Optional.empty();
	}
	
	public java.util.List<ItemInstance> getItems() {
	  return new java.util.ArrayList<>(this.backpack.keySet());
	}

}
