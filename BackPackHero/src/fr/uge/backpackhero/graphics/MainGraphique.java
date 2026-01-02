package fr.uge.backpackhero.graphics;

import java.awt.Color;

import com.github.forax.zen.Application;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.View;
import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.graphics.GameView;
import fr.uge.backpackhero.graphics.BackPackView;

/**
 * Main entry point for launching the game in graphical mode using the Zen library.
 * This class sets up the game state and manages the main loop (events and rendering).
 */
public class MainGraphique {

  /**
   * Initializes the game model, sets up the Zen application, and starts the game loop.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    // --- 1. SETUP DU JEU ---
    Heros heros = new Heros();
    StuffFactory factory = new StuffFactory();
    
    // Initializing the backpack with starter items
    try {
      heros.getBackpack().add(new ItemInstance(factory.create(Stuff.WoodSword)), new Position(0, 0));
      heros.getBackpack().add(new ItemInstance(factory.create(Stuff.RoughBuckler)), new Position(0, 1));
      heros.getBackpack().add(new ItemInstance(factory.create(Stuff.ManaStone)), new Position(0, 3));
    } catch (Exception e) {}

    Dungeon donjon = DungeonGenerator.createDungeonPhase3();
    
    // Creating a placeholder console view to satisfy the Jeu constructor
    View viewConsole = new View(heros.getBackpack(), factory, heros);
    Jeu jeu = new Jeu(heros, donjon, viewConsole);

    // Graphical view of the backpack
    BackPackView backpackView = new BackPackView(heros.getBackpack(), 40);

    // --- 2. LANCEMENT ZEN ---
    Application.run(Color.BLACK, context -> {
      
      while (true) {
        // A. EVENT HANDLING: Poll for keyboard or pointer events
        Event event = context.pollOrWaitEvent(10);
        
        if (event != null) {
          switch (event) {
            case KeyboardEvent ke -> {
              if (ke.action() == KeyboardEvent.Action.KEY_PRESSED) {
                gererClavier(jeu, ke.key(), context);
              }
            }
            case PointerEvent pe -> {
              // Mouse clicks (to be implemented later)
            }
          }
        }

        // B. RENDERING
        GameView.draw(context, jeu, backpackView);
      }
    });
  }

  /**
   * Processes keyboard input based on the current game mode (Exploration or Combat).
   *
   * @param jeu     The game model.
   * @param key     The key pressed by the user.
   * @param context The Zen application context (used for disposal/quitting).
   */
  private static void gererClavier(Jeu jeu, KeyboardEvent.Key key, com.github.forax.zen.ApplicationContext context) {
    // Quitting the application
    if (key == KeyboardEvent.Key.Q) {
      context.dispose();
      return; 
    }

    // EXPLORATION MODE (Movement)
    if (jeu.getMode() == Mode.EXPLORATION) {
      switch (key) {
        case UP -> jeu.deplacer(0, -1);
        case DOWN -> jeu.deplacer(0, 1);
        case LEFT -> jeu.deplacer(-1, 0);
        case RIGHT -> jeu.deplacer(1, 0);
      }
    }
    // COMBAT MODE (Actions)
    else if (jeu.getMode() == Mode.COMBAT) {
      Combat combat = jeu.getCombat();
      if (combat == null) return;

      // Finish turn action
      if (key == KeyboardEvent.Key.F) {
        combat.startEnemyTurn();
        jeu.updateCombatState();
      }
      // Item usage (A/Z/E/R mapped to 0/1/2/3 index)
      else {
        // We use A/Z/E/R as quick keys for the first few items (Index 0, 1, 2, 3)
        int index = -1;
        if (key == KeyboardEvent.Key.A) index = 0;
        else if (key == KeyboardEvent.Key.Z) index = 1;
        else if (key == KeyboardEvent.Key.E) index = 2;
        else if (key == KeyboardEvent.Key.R) index = 3;

        if (index != -1) {
          var items = jeu.getHeros().getBackpack().getItems();
          if (index < items.size()) {
            var item = items.get(index);
            // Auto-target the first alive enemy
            var cible = combat.getAliveEnemies().isEmpty() ? null : combat.getAliveEnemies().get(0);
            combat.tryHeroAction(item, cible);
            jeu.updateCombatState();
          }
        }
      }
    }
  }
}