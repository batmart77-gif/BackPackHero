package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Représente le Donjon entier.
 * Gère la liste des étages et la progression du joueur.
 */
public class Dungeon {
  
  private final List<Floor> floors;
  private int currentFloorIndex;
  
  public Dungeon(List<Floor> floors) {
    Objects.requireNonNull(floors);
    this.floors = new ArrayList<>(floors); 
    this.currentFloorIndex = 0;
  }
  
  /**
   * Récupère l'étage sur lequel le héros se trouve actuellement.
   */
  public Floor getCurrentFloor() {
    return floors.get(currentFloorIndex);
  }
  
  /**
   * Fait passer le joueur à l'étage suivant.
   * @return true si le changement a réussi, false si c'était le dernier étage.
   */
  public boolean moveToNextFloor() {
      if (currentFloorIndex < floors.size() - 1) {
          currentFloorIndex++;
          return true;
      }
      return false; 
  }
  
  /**
   * Vérifie si le joueur a fini le donjon (franchi la sortie du dernier étage).
   */
  public boolean isFinished() {
      return currentFloorIndex == floors.size() - 1;
  }
}