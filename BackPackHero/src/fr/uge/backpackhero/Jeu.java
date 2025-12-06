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

public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final View view; 
  private Mode modeActuel;
  private Combat combatEnCours; // Rempli uniquement si Mode.COMBAT
  private int posX; // Position Colonne
  private int posY; // Position Ligne
  
  public Jeu(Heros heros, Dungeon donjon, View view) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    this.view = Objects.requireNonNull(view);
    this.modeActuel = Mode.EXPLORATION;
    this.posX = 0;
    this.posY = 0;
  }

  /**
   * Tente de déplacer le héros.
   * @param dx Changement en X (-1 gauche, +1 droite)
   * @param dy Changement en Y (-1 haut, +1 bas)
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
   * Regarde le type de salle et agit en conséquence.
   */
  private void analyserSalle(Room room) {
    switch (room) {
      case Corridor c -> System.out.println("Un couloir");
      case EnemyRoom e -> handleEnemyRoom(e);
      case TreasureRoom t -> handleTreasureRoom(t);
      case MerchantRoom m -> System.out.println("Un marchand vous observe");    
      case HealerRoom h -> System.out.println("Un autel de soin");
      case ExitRoom x -> handleExitRoom();
    }
  }

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

  private void handleTreasureRoom(TreasureRoom t) {
    System.out.println("Un coffre au trésor !");
    for (ItemInstance item : t.loot()) {
      System.out.println("   - Vous voyez : " + item.getName());
    }
  }
  

  private void handleExitRoom() {
    System.out.println("Vous activez la porte...");
    tenterSortie();
  }

  /**
   * Logique de changement d'étage.
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
   * À appeler après chaque tour de combat pour voir si c'est fini.
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
  
  public Dungeon getDonjon() {
    return donjon;
  }

  public Mode getMode() { return modeActuel; }
  public Combat getCombat() { return combatEnCours; }
  public Heros getHeros() { return heros; }
  public int getX() { return posX; }
  public int getY() { return posY; }
}