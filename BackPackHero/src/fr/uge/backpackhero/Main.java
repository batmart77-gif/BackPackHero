
//package fr.uge.backpackhero;
//
//
//public class Main {
//    public static void main(String[] args) {
//      TerminalController controller = new TerminalController();
//      controller.run();
//    }
//}


package fr.uge.backpackhero;

import java.util.Objects;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.item.View;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.Item;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.graphics.GraphicEngine;

/**
 * Entry point for the Backpack Hero application.
 * This class orchestrates the initialization of the hero, the dungeon, 
 * the game model, and launches the graphical interface.
 */
public final class Main {

  /**
   * Main method to start the game.
   * * @param args command line arguments.
   * @throws NullPointerException if args is null.
   */
  public static void main(String[] args) {
    Objects.requireNonNull(args);

    Heros heros = new Heros();
    Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
    StuffFactory factory = new StuffFactory();
    View view = new View(heros.getBackpack(), factory, heros);
    
    Jeu jeu = new Jeu(heros, dungeon, view);
    setupInitialInventory(heros, factory);

    System.out.println("Launching Backpack Hero (Graphical Version)...");
    new GraphicEngine(jeu).start();
  }

  /**
   * Adds basic starting equipment to the hero's backpack for testing.
   *
   * @param heros   the hero receiving the items.
   * @param factory the factory used to create items.
   */
  private static void setupInitialInventory(Heros heros, StuffFactory factory) {
    Item sword = factory.getItem("Wood Sword");
    if (sword != null) {
      heros.getBackpack().add(new ItemInstance(sword), new Position(0, 0));
    }
  }
}

