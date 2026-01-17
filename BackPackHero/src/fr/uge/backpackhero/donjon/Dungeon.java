package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents the entire Dungeon structure.
 * Manages the list of floors and the player's progression through them.
 */
public class Dungeon {
  
  private final List<Floor> floors;
  private int currentFloorIndex;
  
  /**
   * Constructs a Dungeon object.
   * Initializes the floor list and sets the starting floor index to 0.
   * To avoid "morts subites", the list is checked for nullity.
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
   * @return true if the transition to the next floor was successful, 
   * false if the current floor was the last floor.
   */
  public boolean moveToNextFloor() {
    if (isFinished()) {
      return false;
    }
    currentFloorIndex++;
    return true;
  }
  
  /**
   * Checks if the player has finished the dungeon floors.
   *
   * @return true if the player is currently on the final floor index, false otherwise.
   */
  public boolean isFinished() {
    return currentFloorIndex == floors.size() - 1;
  }
  
  /**
   * Returns the one-based number of the current floor.
   *
   * @return The current floor number.
   */
  public int getFloorNumber() {
    return currentFloorIndex + 1;
  }
}