package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * Represents the hero's backpack grid containing placed items.
 */
public class BackPack {
  
	/**
     * Main map: associates each {@code ItemInstance} with the list
     * of ABSOLUTE {@code Position} it occupies in the grid.
     */
  private final HashMap<ItemInstance, List<Position>> backpack;
  
  private final int row;
  private int column;
  private int goldQuantity;
  
  /**
   * The grid representing the backpack tiles.
   * Each tile contains the occupying {@code ItemInstance}, or {@code null} if empty.
   */
  private ItemInstance[][] grid;

  /**
   * Creates a backpack of the given size.
   *
   * @param row    number of rows
   * @param column number of columns
   */
  public BackPack(int row, int column) {
    this.backpack = new HashMap<>();
    this.grid = new ItemInstance[row][column];
    this.row = row;
    this.column = column;
    this.goldQuantity = 0;
  }

  /**
   * Creates the default starting backpack (3 rows × 5 columns).
   */
  public BackPack() {
    this(3, 5);
  }
  
  /**
   * Adds gold to the hero's wallet.
   *
   * @param amount non-negative gold amount
   * @throws IllegalArgumentException if {@code amount < 0}
   */
    public void addGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Gold amount cannot be negative.");
        }
        this.goldQuantity += amount;
    }
  
    /**
     * Attempts to spend gold.
     *
     * @param amount positive amount of gold to spend
     * @return {@code true} if the hero had enough gold, {@code false} otherwise
     * @throws IllegalArgumentException if {@code amount <= 0}
     */
    public boolean spendGold(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Gold to spend must be positive.");
        }
        
        if (this.goldQuantity >= amount) {
            this.goldQuantity -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * @return the current gold amount.
     */
    public int getGoldQuantity() {
        return this.goldQuantity;
    }
    
    /**
     * Checks whether there is enough free space to place the item at the given anchor position.
     *
     * @param itemInstance the item instance to check
     * @param startPos     the anchor position
     * @return {@code true} if the placement is possible, {@code false} otherwise
     * @throws NullPointerException if arguments are {@code null}
     */
  private boolean checkIfEnoughSpace(ItemInstance itemInstance, Position startPos) {
    Objects.requireNonNull(itemInstance);
    Objects.requireNonNull(startPos);
    var listPos = itemInstance.getCurrentShape();
    for (var elmt : listPos) {
      var newRow = startPos.row() + elmt.row();
      var newCol = startPos.column() + elmt.column();
      if (!isInside(new Position(newRow, newCol))) {
    	  System.out.println("Placement failed: outside backpack bounds.");
          return false;
      }
      if (this.grid[newRow][newCol] != null) {
    	  System.out.println("Placement failed: tile already occupied.");
        return false;
      }
    }
    return true;
  }
  
  public boolean isInside(Position pos) {
      return pos.row() >= 0 && pos.row() < this.row &&
             pos.column() >= 0 && pos.column() < this.column;
  }
  
  /**
   * Actually places the item in the grid after validation.
   *
   * @param itemInstance item to place
   * @param startPos     anchor position
   * @throws NullPointerException if arguments are {@code null}
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
    this.backpack.put(itemInstance, absolutePositions);
  }

  /**
   * Adds an {@code ItemInstance} to the backpack at the given position.
   *
   * @param itemInstance the instance to add
   * @param startPos     anchor position
   * @return {@code true} if successfully added, {@code false} otherwise
   */
  /*public boolean add(ItemInstance itemInstance, Position startPos) {
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
  }*/
  
  public boolean add(ItemInstance itemInstance, Position startPos) {
     Objects.requireNonNull(itemInstance);
     Objects.requireNonNull(startPos);
     var item = itemInstance.getItem();
     switch(item) {
         case Curse c -> {
             boolean added = addCurse(itemInstance, startPos);
             if (added) itemInstance.setPos(startPos); // Mémorise la position
             return added;
         }
         default -> {
             if (!checkIfEnoughSpace(itemInstance, startPos)) {
                 return false;
             }
             placeItem(itemInstance, startPos);
             itemInstance.setPos(startPos); 
             return true;
         }
     }
  }

  /**
   * Special placement logic for curses:
   * they overwrite existing items.
   */
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
            var overlapping = getItemAt(target).orElse(null);
            if (overlapping != null) {
                removeItem(overlapping);
            }
        }
        placeItem(curse, startPos);
        return true;
    }

  /**
   * Removes an {@code ItemInstance} from the backpack and clears its tiles.
   *
   * @param itemInstance item to remove
   * @return {@code true} if it was removed, {@code false} otherwise
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
   * Returns the item present at the given position.
   *
   * @param pos position in the grid
   * @return an Optional containing the item instance or empty if none
   */
  public Optional<ItemInstance> getItemAt(Position pos) {
    Objects.requireNonNull(pos);
    if (pos.row() >= 0 && pos.row() < row && pos.column() >= 0 && pos.column() < column) {
      return Optional.ofNullable(grid[pos.row()][pos.column()]);
    }
    return Optional.empty();
  }
  
  /**
   * @return all items currently in the backpack.
   */
  public List<ItemInstance> getItems() {
    return new ArrayList<>(this.backpack.keySet());
  }
  
  /**
   * Expands the backpack by adding columns.
   *
   * @param extraColumns number of additional columns
   */
  public void expand(int extraColumns) {
      if (extraColumns <= 0) return;
      int newColumnCount = this.column + extraColumns;
      ItemInstance[][] newGrid = new ItemInstance[this.row][newColumnCount];
      // Recopie des objets existants
      for (int i = 0; i < this.row; i++) {
          System.arraycopy(this.grid[i], 0, newGrid[i], 0, this.column);
      }
      // Mise à jour
      this.grid = newGrid;
      this.column = newColumnCount;    
      System.out.println("The backpack expanded! New size: " + this.row + "x" + this.column + ")");
  }

  public int getRows() {
    return row;
  }

  public int getColumns() {
    return column;
  }
  
  /**
   * Compte le nombre de pierres de mana présentes dans le sac.
   * Cette méthode respecte la règle : 1 pierre = 1 case.
   */
  public int countManaStones() {
    return backpack.keySet().stream()
            .map(ItemInstance::getItem)
            .filter(Item::isManaStone)
            .mapToInt(item -> 1)
            .sum();
  }
  /**
   * Vérifie si l'instance donnée touche un objet répondant au critère.
   * Deux objets sont adjacents si au moins une de leurs cases est côte à côte (distance de 1).
   */
  public boolean hasAdjacentItem(ItemInstance self, java.util.function.Predicate<Item> criteria) {
      var myPositions = backpack.get(self); // List<Position>
      
      return backpack.entrySet().stream()
          .filter(entry -> !entry.getKey().equals(self)) // Ne pas se comparer à soi-même
          .filter(entry -> criteria.test(entry.getKey().getItem()))
          .anyMatch(entry -> areAdjacent(myPositions, entry.getValue()));
  }

  private boolean areAdjacent(List<Position> posA, List<Position> posB) {
      for (var pA : posA) {
          for (var pB : posB) {
              // Distance de Manhattan = 1 (Haut, Bas, Gauche, Droite)
              int dist = Math.abs(pA.row() - pB.row()) + Math.abs(pA.column() - pB.column());
              if (dist == 1) return true;
          }
      }
      return false;
  }

  public int getWidth() {
    return column;
  }
  
  public int getHeight() {
    return row;
  }
}
