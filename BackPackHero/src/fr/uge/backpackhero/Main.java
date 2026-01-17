package fr.uge.backpackhero;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.DungeonGenerator;

import java.io.IOException;
import java.util.List;

import fr.uge.backpackhero.data.HallOfFame;
import fr.uge.backpackhero.data.ScoreEntry;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.Item;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.graphics.GraphicEngine;
import fr.uge.backpackhero.graphics.ViewGraphic;

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
    Heros heros = new Heros();
    Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
    StuffFactory factory = new StuffFactory();    
    ViewGraphic viewGraphic = new ViewGraphic(heros.getBackpack(), factory, heros);
    Jeu jeu = new Jeu(heros, dungeon, viewGraphic, viewGraphic);
    setupInitialInventory(heros, factory);
    HallOfFame hof = new HallOfFame();
    GraphicEngine engine = new GraphicEngine(jeu, viewGraphic, hof);
    if (jeu.getMode() == Mode.GAGNE || jeu.getMode() == Mode.PERDU) {
      try {
        int finalScore = heros.calculateFinalScore();
        hof.recordScore(new ScoreEntry("Player1", finalScore)); 
      } catch (IOException e) {
      }
    }

    engine.start(); 
  }

  /**
   * Adds basic starting equipment to the hero's backpack for testing.
   *
   * @param heros   the hero receiving the items.
   * @param factory the factory used to create items.
   */
  private static void setupInitialInventory(Heros heros, StuffFactory factory) {
    Item sword
    = factory.getItem("Wood Sword");
    if (sword != null) {
      heros.getBackpack().add(new ItemInstance(sword), new Position(0, 0));
    }
    Item shield = factory.getItem("Rough Buckler");
    if (shield != null) {
      heros.getBackpack().add(new ItemInstance(shield), new Position(0, 1));
    }
  }
}

