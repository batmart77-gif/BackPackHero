package fr.uge.backpackhero.item;

import fr.uge.backpackhero.entites.Heros;

/**
 * Common interface for both Terminal and Graphical views.
 */
public interface BackpackView {
  void displayItemFound(ItemInstance item);

  boolean interactBeforePlacement(ItemInstance item);

  void attemptPlacement(ItemInstance item);

  void printBackPack();

  void handleForcedCurse(Heros heros, Curse curse);
}