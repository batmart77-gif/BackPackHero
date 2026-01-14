package fr.uge.backpackhero;

import java.util.Scanner;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.donjon.Floor;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.donjon.Corridor;
import fr.uge.backpackhero.donjon.EnemyRoom;
import fr.uge.backpackhero.donjon.TreasureRoom;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.HealerRoom;
import fr.uge.backpackhero.donjon.ExitRoom;
import fr.uge.backpackhero.donjon.GateRoom;
import fr.uge.backpackhero.donjon.EventRoom;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.View;
import fr.uge.backpackhero.data.HallOfFame;

/**
 * Controller class for the terminal version of the game.
 * Manages game loops, user input, and display updates
 */
public final class TerminalController {
  private final Scanner scanner = new Scanner(System.in);
  private final Heros heros = new Heros();
  private final Jeu jeu;

  /**
   * Initializes the controller, the dungeon, and the hero's equipment.
   * Uses the DungeonGenerator for Phase 3 requirements.
   */
  public TerminalController() {
    Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
    View view = new View(heros.getBackpack(), new StuffFactory(), heros);
    this.jeu = new Jeu(heros, dungeon, view);
    initialiserEquipement();
  }

  /**
   * Provides the hero with basic starting equipment in the backpack.
   */
  private void initialiserEquipement() {
    StuffFactory f = new StuffFactory();
    var backpack = heros.getBackpack();
    backpack.add(new ItemInstance(f.create(Stuff.WoodSword)), new Position(0, 0));
    backpack.add(new ItemInstance(f.create(Stuff.RoughBuckler)), new Position(0, 1));
    System.out.println(">> Preparation complete. Backpack initialized!");
  }

  /**
   * Runs the main game loop until victory or defeat.
   * @throws IOException 
   */
  public void run() throws IOException {
    System.out.println("========================================\n     BACKPACK HERO\n========================================");
    while (jeu.getMode() != Mode.GAGNE && jeu.getMode() != Mode.PERDU) {
      if (jeu.getMode() == Mode.COMBAT) {
        gererCombat();
      } else {
        gererExploration();
      }
    }
    terminerPartie();
  }

  /**
   * Manages the exploration phase by displaying status and processing movement.
   */
  private void gererExploration() {
    afficherInfosExploration();
    System.out.println("Current position: (" + jeu.getY() + "," + jeu.getX() + ")");
    System.out.print("Commands: (z) Up | (s) Down | (q) Left | (d) Right | (i) Inv | (o) Reorg\n> ");
    processExplorationInput(scanner.next().toLowerCase());
  }

  /**
   * Processes the input character for movement or inventory actions.
   * * @param input the user command string.
   */
  private void processExplorationInput(String input) {
    int dx = 0;
    int dy = 0;
    switch (input) {
      case "z" -> dy = -1;
      case "s" -> dy = 1;
      case "q" -> dx = -1;
      case "d" -> dx = 1;
      case "i" -> { jeu.getView().printBackPack(); return; }
      case "o" -> { jeu.getView().reorganize(); return; }
      default -> { System.out.println("Unknown command."); return; }
    }
    executeMovement(dx, dy);
  }

  /**
   * Validates and executes a movement request in the dungeon.
   * * @param dx the horizontal displacement.
   * @param dy the vertical displacement.
   */
  private void executeMovement(int dx, int dy) {
    int tx = jeu.getX() + dx;
    int ty = jeu.getY() + dy;
    Floor floor = jeu.getDonjon().getCurrentFloor();
    if (floor.getRoom(tx, ty) != null) {
      floor.revealRoom(tx, ty);
      jeu.deplacer(dx, dy);
    } else {
      System.out.println("Bong! A wall.");
    }
  }

  /**
   * Manages the combat loop and handles turn transitions.
   */
  private void gererCombat() {
    Combat combat = jeu.getCombat();
    afficherInfosCombat(combat);
    String action = scanner.next();
    if (action.equalsIgnoreCase("f")) {
      combat.startEnemyTurn();
    } else {
      traiterActionCombat(action, combat);
    }
    jeu.updateCombatState();
  }

  /**
   * Displays the combat interface, including hero stats and enemy intentions.
   * * @param combat the current non-null combat session.
   */
  private void afficherInfosCombat(Combat combat) {
    System.out.println("\n--- COMBAT ---");
    System.out.printf("HERO: %d/%d HP | %d Energy | %d Def | Lv %d\n", 
        heros.getPv(), heros.getPvMax(), heros.getEnergie(), heros.getProtection(), heros.getLevel());
    combat.getAliveEnemies().forEach(e -> 
        System.out.println("  Enemy (" + e.getHp() + " HP) -> Intends: " + e.getActionAnnoncee().description()));
    jeu.getView().printBackPack();
    List<ItemInstance> items = heros.getBackpack().getItems();
    for (int i = 0; i < items.size(); i++) {
      System.out.println("  [" + i + "] Use: " + items.get(i).getName());
    }
    System.out.print("  [f] End turn\nAction > ");
  }

  /**
   * Processes a combat action input (item index) and tries to execute it.
   * * @param input the numeric string input.
   * @param combat the current combat session.
   */
  private void traiterActionCombat(String input, Combat combat) {
    if (!input.matches("\\d+")) {
      System.out.println("Invalid numeric input.");
      return;
    }
    int idx = Integer.parseInt(input);
    List<ItemInstance> items = heros.getBackpack().getItems();
    if (idx >= 0 && idx < items.size() && !combat.getAliveEnemies().isEmpty()) {
      boolean success = combat.tryHeroAction(items.get(idx), combat.getAliveEnemies().get(0));
      System.out.println(success ? "Action success!" : "Action failed (Energy/Mana).");
    }
  }

  /**
   * Displays the hero's exploration stats and the floor map.
   */
  private void afficherInfosExploration() {
    System.out.println("\nHP: " + heros.getPv() + "/" + heros.getPvMax() + 
        " | Mana: " + heros.getMana() + " | Gold: " + heros.getGold());
    afficherCarte();
  }

  /**
   * Renders the current dungeon floor map in the terminal
   */
  private void afficherCarte() {
    Floor floor = jeu.getDonjon().getCurrentFloor();
    System.out.println("\n--- MAP (Floor " + jeu.getDonjon().getFloorNumber() + ") ---");
    for (int y = 0; y < floor.height(); y++) {
      System.out.print("  |");
      for (int x = 0; x < floor.width(); x++) {
        System.out.print(getSymbol(x, y, floor) + "|");
      }
      System.out.println();
    }
    System.out.println("  Legend: @:Me E:Enemy T:Treasure M:Merchant H:Healer S:Exit #:Gate");
  }

  /**
   * Returns a character representing the room type or hero position.
   * * @param x the column coordinate.
   * @param y the row coordinate.
   * @param floor the current dungeon floor.
   * @return a 3-character string representing the map tile.
   */
  private String getSymbol(int x, int y, Floor floor) {
    if (x == jeu.getX() && y == jeu.getY()) return " @ ";
    Room r = floor.getRoom(x, y);
    if (r == null) return "###";
    return switch(r) {
      case Corridor c -> " . ";
      case EnemyRoom e -> " E ";
      case TreasureRoom t -> " T ";
      case MerchantRoom m -> " M ";
      case HealerRoom h -> " H ";
      case ExitRoom s -> " S ";
      case GateRoom g -> " # ";
      case EventRoom v -> " ! ";
      default -> "###";
    };
  }

  /**
   * Ends the game, calculates the final score, and displays the Hall of Fame.
   * @throws IOException 
   */
  private void terminerPartie() throws IOException {
    int finalScore = heros.calculateFinalScore();
    System.out.println("\n=== GAME OVER ===\nFinal Score: " + finalScore);
    HallOfFame hof = new HallOfFame();
    hof.display();
  }
}