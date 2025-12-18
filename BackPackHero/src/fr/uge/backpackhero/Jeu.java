package fr.uge.backpackhero;

import java.util.Objects;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.Floor;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.donjon.Corridor;
import fr.uge.backpackhero.donjon.EnemyRoom;
import fr.uge.backpackhero.donjon.TreasureRoom;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.HealerRoom;
import fr.uge.backpackhero.donjon.ExitRoom;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.combat.CombatState;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.View;

/**
 * The main class for the game logic (Model).
 * It manages the hero, the dungeon, the current game state, and the player's position.
 */
public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final View view; 
  private Mode modeActuel;
  private Combat combatEnCours; // Rempli uniquement si Mode.COMBAT
  private int posX; // Position Colonne
  private int posY; // Position Ligne
  
  /**
   * Creates a new Game instance.
   * Initializes the position to (0,0) and the mode to EXPLORATION.
   * * @param heros The hero player.
   * @param donjon The dungeon to explore.
   * @param view The view component (used for combat).
   * @throws NullPointerException if any argument is null.
   */
  public Jeu(Heros heros, Dungeon donjon, View view) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    this.view = Objects.requireNonNull(view);
    this.modeActuel = Mode.EXPLORATION;
    this.posX = 0;
    this.posY = 0;
  }

  /**
   * Tries to move the hero by a given offset.
   * * @param dx The change in X coordinate (-1 for left, +1 for right).
   * @param dy The change in Y coordinate (-1 for up, +1 for down).
   */
  public void deplacer(int dx, int dy) {
    if (modeActuel != Mode.EXPLORATION) {
      System.out.println("Impossible de bouger maintenant !");
      return;
    }
    int newX = posX + dx;
    int newY = posY + dy;
    Floor currentFloor = donjon.getCurrentFloor();
    Room targetRoom = currentFloor.getRoom(newX, newY);
    if (targetRoom == null) {
      System.out.println("Bong ! Un mur.");
      return;
    }
    this.posX = newX;
    this.posY = newY;
    analyserSalle(targetRoom);
  }

  /**
   * Checks the type of the current room and triggers the appropriate action.
   * * @param room The room the hero has entered.
   */
  private void analyserSalle(Room room) {
    // Utilisation du pattern matching (Java 17+)
    switch (room) {
      case Corridor c -> 
        System.out.println("Un couloir");
      case EnemyRoom e -> handleEnemyRoom(e);
      case TreasureRoom t -> handleTreasureRoom(t);
      case MerchantRoom m -> System.out.println("Un marchand vous observe");    
      case HealerRoom h -> System.out.println("Un autel de soin");
      case ExitRoom x -> handleExitRoom();
    }
  }

  /**
   * Handles the logic when entering a room with enemies.
   * Starts a combat if enemies are alive.
   * * @param e The enemy room.
   */
  private void handleEnemyRoom(EnemyRoom e) {
    // 1. Vérification des vivants
    boolean ennemisVivants = false;
    for (var ennemi : e.enemies()) {
      if (ennemi.estVivant()) {
        ennemisVivants = true;
        break;
      }
    }
    // 2. Action
    if (ennemisVivants) {
      System.out.println("ATTENTION ! Des ennemis vous barrent la route !");
      this.modeActuel = Mode.COMBAT;
      this.combatEnCours = new Combat(heros, e.enemies(), view);
    } else {
      System.out.println("Des cadavres de rats gisent sur le sol. Vous passez.");
    }
  }

  /**
   * Handles the logic when entering a treasure room.
   * Displays the content of the chest.
   * * @param t The treasure room.
   */
  private void handleTreasureRoom(TreasureRoom t) {
    System.out.println("Un coffre au trésor !");
    for (ItemInstance item : t.loot()) {
      System.out.println("   - Vous voyez : " + item.getName());
    }
  }

  /**
   * Handles the logic when entering an exit room.
   * Triggers the transition to the next floor.
   */
  private void handleExitRoom() {
    System.out.println("Vous activez la porte...");
    tenterSortie();
  }

  /**
   * Tries to move to the next floor.
   * If successful, resets the hero's position. If it was the last floor, the player wins.
   */
  private void tenterSortie() {
    boolean succes = donjon.moveToNextFloor();
    if (succes) {
      System.out.println("Vous descendez les escaliers vers l'étage suivant !");
      Floor newFloor = donjon.getCurrentFloor();
      this.posX = newFloor.startX();
      this.posY = newFloor.startY();
    } else {
      System.out.println("VICTOIRE ! Vous sortez du donjon à l'air libre !");
      this.modeActuel = Mode.GAGNE;
    }
  }

  /**
   * Updates the game state based on the current combat result.
   * Should be called after each combat turn.
   */
  public void updateCombatState() {
    if (combatEnCours == null) return;
    CombatState state = combatEnCours.getState();   
    if (state == CombatState.LOSS) {
      System.out.println("Vous êtes mort...");
      this.modeActuel = Mode.PERDU;
    } 
    else if (state == CombatState.WIN) {
      System.out.println("Victoire ! Les ennemis sont vaincus.");
      this.combatEnCours = null;
      this.modeActuel = Mode.EXPLORATION;
    }
  }
  
  /**
   * Gets the dungeon.
   * * @return The dungeon.
   */
  public Dungeon getDonjon() {
    return donjon;
  }

  /**
   * Gets the current game mode.
   * * @return The current mode.
   */
  public Mode getMode() { return modeActuel; }
  
  /**
   * Gets the current combat instance.
   * * @return The current combat, or null if not in combat.
   */
  public Combat getCombat() { return combatEnCours; }
  
  /**
   * Gets the hero.
   * * @return The hero.
   */
  public Heros getHeros() { return heros; }
  
  /**
   * Gets the X position of the hero.
   * * @return The X coordinate.
   */
  public int getX() { return posX; }
  
  /**
   * Gets the Y position of the hero.
   * * @return The Y coordinate.
   */
  public int getY() { return posY; }
}