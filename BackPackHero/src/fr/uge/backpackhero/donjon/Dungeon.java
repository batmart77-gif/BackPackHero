package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the entire Dungeon structure.
 * Manages the list of floors and the player's progression through them.
 */
public class Dungeon {
  
  /** The list of all floors comprising the dungeon. */
  private final List<Floor> floors;
  
  /** The zero-based index of the floor the hero is currently on. */
  private int currentFloorIndex;
  
  /**
   * Constructs a Dungeon object.
   * Initializes the floor list and sets the starting floor index to 0.
   *
   * @param floors The list of floors that make up the dungeon.
   * @throws NullPointerException if the floors list is null.
   */
  public Dungeon(List<Floor> floors) {
    Objects.requireNonNull(floors);
    this.floors = new ArrayList<>(floors); 
    this.currentFloorIndex = 0;
  }
  
  /**
   * Retrieves the floor the hero is currently on.
   *
   * @return The current {@link Floor} object.
   */
  public Floor getCurrentFloor() {
    return floors.get(currentFloorIndex);
  }
  
  /**
   * Moves the player to the next floor in the sequence.
   *
   * @return {@code true} if the transition to the next floor was successful, 
   * {@code false} if the current floor was the last floor.
   */
  public boolean moveToNextFloor() {
    if (currentFloorIndex < floors.size() - 1) {
      currentFloorIndex++;
      return true;
    }
    return false; 
  }
  
  /**
   * Checks if the player has finished the dungeon (i.e., reached the exit of the last floor).
   *
   * @return {@code true} if the player is currently on the final floor index, {@code false} otherwise.
   */
  public boolean isFinished() {
    // Note: This checks if the hero is ON the last floor, not necessarily that they have exited it.
    return currentFloorIndex == floors.size() - 1;
  }
  
  /**
   * Returns the one-based number of the current floor (e.g., 1, 2, 3).
   *
   * @return The current floor number.
   */
  public int getFloorNumber() {
    // currentFloorIndex starts at 0, so we add 1 for display purposes
    return currentFloorIndex + 1;
  }
}