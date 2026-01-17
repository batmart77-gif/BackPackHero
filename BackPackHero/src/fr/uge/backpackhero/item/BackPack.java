package fr.uge.backpackhero.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Represents the hero's backpack grid containing placed items.
 */
public class BackPack {

  /** Stores which tiles are unlocked and available for items. */
  private final Set<Position> unlockedTiles;

  /** Maps absolute positions to the ItemInstance occupying them. */
  private final Map<Position, ItemInstance> grid;

  /**
   * Main map: associates each {@code ItemInstance} with the list of ABSOLUTE
   * {@code Position} it occupies in the grid.
   */
  private final HashMap<ItemInstance, List<Position>> backpack;

  /**
   * The current amount of gold currency held by the player. This value is used
   * for transactions with merchants and healers.
   */
  private int goldQuantity;

  /**
   * Creates a backpack with a default unlocked area of 3x5. The internal grid is
   * larger to allow for custom expansion.
   */
  public BackPack() {
    this.backpack = new HashMap<>();
    this.grid = new HashMap<>();
    this.unlockedTiles = new HashSet<>();
    this.goldQuantity = 0;
    for (int r = 0; r < 3; r++) {
      for (int c = 0; c < 5; c++) {
        unlockedTiles.add(new Position(r, c));
      }
    }
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

  private boolean hasAdjacentUnlockedTile(Position pos) {
    return unlockedTiles.stream()
        .anyMatch(unlocked -> Math.abs(unlocked.row() - pos.row()) + Math.abs(unlocked.column() - pos.column()) == 1);
  }

  /**
   * Unlocks a specific tile in the backpack.
   *
   * @param pos The position to unlock.
   * @return {@code true} if the tile was successfully unlocked, {@code false} if
   *         already unlocked or out of bounds.
   */
  public boolean unlockTile(Position pos) {
    Objects.requireNonNull(pos);
    if (unlockedTiles.contains(pos)) {
      return false;
    }

    // Optional: check adjacency to prevent "floating" tiles
    if (!hasAdjacentUnlockedTile(pos)) {
      System.out.println("You can only unlock a tile adjacent to your current backpack!");
      return false;
    }

    return unlockedTiles.add(pos);
  }

  /**
   * Checks if a tile is available for placement (inside bounds and unlocked).
   *
   * @param pos The position to check.
   * @return {@code true} if the tile is usable.
   */
  public boolean isAvailable(Position pos) {
    return unlockedTiles.contains(pos) && !grid.containsKey(pos);
  }

  /**
   * Checks whether there is enough free space to place the item at the given
   * anchor position.
   *
   * @param itemInstance the item instance to check
   * @param startPos     the anchor position
   * @return {@code true} if the placement is possible, {@code false} otherwise
   * @throws NullPointerException if arguments are {@code null}
   */
  private boolean checkIfEnoughSpace(ItemInstance instance, Position startPos) {
    for (var relative : instance.getCurrentShape()) {
      Position absPos = new Position(startPos.row() + relative.row(), startPos.column() + relative.column());

      if (!isAvailable(absPos)) {
        return false;
      }
    }
    return true;
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

    for (var relative : itemInstance.getCurrentShape()) {
      Position absPos = new Position(startPos.row() + relative.row(), startPos.column() + relative.column());

      this.grid.put(absPos, itemInstance);
      absolutePositions.add(absPos);
    }

    this.backpack.put(itemInstance, absolutePositions);
  }

  /**
   * Attempts to place an item at the specified anchor position.
   *
   * @param itemInstance The item to place.
   * @param startPos     The anchor position (top-left of the item).
   * @return {@code true} if successful.
   */
  public boolean add(ItemInstance itemInstance, Position startPos) {
    Objects.requireNonNull(itemInstance);
    Objects.requireNonNull(startPos);
    var item = itemInstance.getItem();
    switch (item) {
    case Curse c -> {
      boolean added = addCurse(itemInstance, startPos);
      if (added)
        itemInstance.setPos(startPos);
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
   * Special logic for Curses: they destroy any existing items they overlap with.
   */
  private boolean addCurse(ItemInstance curse, Position startPos) {
    var absolutePositions = new ArrayList<Position>();
    for (var relative : curse.getCurrentShape()) {
      Position absPos = new Position(startPos.row() + relative.row(), startPos.column() + relative.column());
      if (!unlockedTiles.contains(absPos))
        return false;
      absolutePositions.add(absPos);
    }
    // Remove overlapping items
    for (var pos : absolutePositions) {
      ItemInstance overlapping = grid.get(pos);
      if (overlapping != null)
        removeItem(overlapping);
    }
    // Place curse
    for (var pos : absolutePositions) {
      grid.put(pos, curse);
    }
    backpack.put(curse, absolutePositions);
    return true;
  }

  /**
   * Removes an {@code ItemInstance} from the backpack and clears its tiles.
   *
   * @param itemInstance item to remove
   * @return {@code true} if it was removed, {@code false} otherwise
   */
  public boolean removeItem(ItemInstance instance) {
    Objects.requireNonNull(instance);
    List<Position> positions = backpack.remove(instance);
    if (positions == null)
      return false;

    for (var pos : positions) {
      grid.remove(pos);
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
    return Optional.ofNullable(grid.get(pos));
  }

  /**
   * @return all items currently in the backpack.
   */
  public List<ItemInstance> getItems() {
    return new ArrayList<>(this.backpack.keySet());
  }

  /**
   * Returns a view of all grid positions currently unlocked in the backpack.
   *
   * @return an unmodifiable {@code Set} of {@link Position} objects representing
   *         the available space in the inventory.
   */
  public Set<Position> getUnlockedTiles() {
    return Collections.unmodifiableSet(unlockedTiles);
  }

  /**
   * Counts the total number of mana stones currently stored in the backpack.
   *
   * @return the total count of items identified as mana stones.
   */
  public int countManaStones() {
    return backpack.keySet().stream().map(ItemInstance::getItem).filter(Item::isManaStone).mapToInt(item -> 1).sum();
  }

  /**
   * Determines if there is another item in the backpack that satisfies a given
   * condition and is physically adjacent to the specified item.
   *
   * @param self     the item instance used as the reference point for the
   *                 adjacency check.
   * @param criteria a {@link java.util.function.Predicate} used to define the
   *                 characteristics of the items being searched for.
   * @return {@code true} if at least one item matching the criteria is adjacent
   *         to {@code self}; {@code false} otherwise.
   * @throws NullPointerException if {@code self} or {@code criteria} is
   *                              {@code null}.
   */
  public boolean hasAdjacentItem(ItemInstance self, java.util.function.Predicate<Item> criteria) {
    Objects.requireNonNull(self);
    Objects.requireNonNull(criteria);
    var myPositions = backpack.get(self);

    return backpack.entrySet().stream().filter(entry -> !entry.getKey().equals(self))
        .filter(entry -> criteria.test(entry.getKey().getItem()))
        .anyMatch(entry -> areAdjacent(myPositions, entry.getValue()));
  }

  /**
   * Searches for and returns an instance of an item that satisfies the given
   * criteria and is physically adjacent to the specified item.
   *
   * @param self     the reference item instance used to check for neighbors.
   * @param criteria a {@link java.util.function.Predicate} defining the required
   *                 properties of the neighbor to be found.
   * @return an {@link java.util.Optional} containing the first adjacent
   *         {@code ItemInstance} that matches the criteria, or an empty
   *         {@code Optional} if no such item exists or if the reference item is
   *         not in the backpack.
   * @throws NullPointerException if {@code self} or {@code criteria} is
   *                              {@code null}.
   */
  public Optional<ItemInstance> getAdjacentItemInstance(ItemInstance self,
      java.util.function.Predicate<Item> criteria) {
    Objects.requireNonNull(self);
    Objects.requireNonNull(criteria);
    var myPositions = backpack.get(self);
    if (myPositions == null)
      return Optional.empty();

    return backpack.entrySet().stream().filter(entry -> !entry.getKey().equals(self))
        .filter(entry -> criteria.test(entry.getKey().getItem()))
        .filter(entry -> areAdjacent(myPositions, entry.getValue())).map(Map.Entry::getKey).findFirst();
  }

  private boolean areAdjacent(List<Position> posA, List<Position> posB) {
    for (var pA : posA) {
      for (var pB : posB) {
        int dist = Math.abs(pA.row() - pB.row()) + Math.abs(pA.column() - pB.column());
        if (dist == 1)
          return true;
      }
    }
    return false;
  }

  /**
   * Removes all items from the backpack and resets their internal positions. This
   * effectively clears both the grid occupancy and the item tracking map.
   *
   * @return a {@link java.util.List} containing all {@link ItemInstance} objects
   *         that were previously stored in the backpack.
   */
  public List<ItemInstance> removeAllItems() {
    List<ItemInstance> itemsToReplace = new ArrayList<>(this.backpack.keySet());

    this.grid.clear();
    this.backpack.clear();

    for (ItemInstance instance : itemsToReplace) {
      instance.setPos(null);
    }

    return itemsToReplace;
  }

  /**
   * Calculates the current width of the backpack grid. The width is determined by
   * finding the highest column index among all unlocked tiles and adding one.
   *
   * @return the total number of columns spanning the unlocked area of the
   *         backpack, or 0 if no tiles are unlocked.
   */
  public int getWidth() {
    return unlockedTiles.stream().mapToInt(Position::column).max().orElse(0) + 1;
  }

  /**
   * Calculates the current height of the backpack grid. The height is determined
   * by finding the highest row index among all unlocked tiles and adding one.
   *
   * @return the total number of rows spanning the unlocked area of the backpack,
   *         or 0 if no tiles are unlocked.
   */
  public int getHeight() {
    return unlockedTiles.stream().mapToInt(Position::row).max().orElse(0) + 1;
  }

  /**
   * Retrieves the grid positions occupied by a specific item instance.
   *
   * @param instance the item instance for which to find occupied positions.
   * @return a {@link java.util.List} containing the {@link Position} objects
   *         occupied by the item; returns an empty list if the item is not in the
   *         backpack.
   * @throws NullPointerException if {@code instance} is {@code null}.
   */
  public List<Position> getPositions(ItemInstance instance) {
    Objects.requireNonNull(instance);
    return List.copyOf(backpack.getOrDefault(instance, List.of()));
  }

}
