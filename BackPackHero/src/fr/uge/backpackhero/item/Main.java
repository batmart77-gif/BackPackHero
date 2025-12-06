package fr.uge.backpackhero.item;

import fr.uge.backpackhero.entites.Heros;

/**
 * Main entry point for testing the backpack and item system.
 */
public class Main {

  public static void main(String[] args) {
    var backPack = new BackPack();
    var stuffFactory = new StuffFactory();
    var heros = new Heros();
    var view = new View(backPack, stuffFactory, heros);
    view.testCurseScenario(3);

  }

}
