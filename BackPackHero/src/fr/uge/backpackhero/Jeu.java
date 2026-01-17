package fr.uge.backpackhero;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.combat.CombatState;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.graphics.ViewGraphic;

/**
 * Main controller for the game logic.
 * Manages the state transitions between exploration, combat, and menus.
 */
public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final ViewGraphic view;
  private Mode modeActuel;
  private Combat combatEnCours;
  private int posX;
  private int posY;
  private String messageAEnregistrer = null;

  /**
   * Constructs the game instance and sets the initial position.
   * @param heros the player character.
   * @param donjon the dungeon structure containing levels.
   * @param vg1 the primary graphical view handler.
   * @param vg2 secondary view handler (reserved for future use).
   */
  public Jeu(Heros heros, Dungeon donjon, ViewGraphic vg1, ViewGraphic vg2) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    this.view = Objects.requireNonNull(vg1);
    this.modeActuel = Mode.EXPLORATION;
    this.posX = donjon.getCurrentFloor().startX();
    this.posY = donjon.getCurrentFloor().startY();
  }

  /**
   * Attempts to move the hero by a given offset and triggers room interaction.
   * @param dx horizontal displacement.
   * @param dy vertical displacement.
   */
  public void deplacer(int dx, int dy) {
    if (modeActuel != Mode.EXPLORATION) {
      return;
    }
    int newX = posX + dx;
    int newY = posY + dy;
    Room targetRoom = donjon.getCurrentFloor().getRoom(newX, newY);
    if (targetRoom != null) {
      this.posX = newX;
      this.posY = newY;
      targetRoom.onClick(this);
    }
  }

  /**
   * Checks the ongoing combat state and handles transitions to win or loss.
   */
  public void updateCombatState() {
    if (combatEnCours == null) {
      return;
    }
    CombatState state = combatEnCours.getState();
    if (state == CombatState.WIN) {
      handleVictoryRewards(combatEnCours.finishCombat());
    } else if (state == CombatState.LOSS) {
      this.modeActuel = Mode.PERDU;
    }
  }

  /**
   * Distributes rewards found after a combat victory.
   * @param rewards the list of items gained.
   */
  private void handleVictoryRewards(List<ItemInstance> rewards) {
    Objects.requireNonNull(rewards);
    rewards.forEach(r -> {
      view.displayItemFound(r);
      if (view.interactBeforePlacement(r)) {
        view.attemptPlacement(r);
      }
    });
    this.combatEnCours = null;
    this.modeActuel = Mode.EXPLORATION;
  }

  /**
   * Initializes a combat session against a list of enemies.
   * @param enemies the list of adversaries in the room.
   */
  public void lancerCombat(List<fr.uge.backpackhero.entites.Ennemi> enemies) {
    Objects.requireNonNull(enemies);
    this.combatEnCours = new Combat(heros, enemies, view);
    this.modeActuel = Mode.COMBAT;
  }

  /**
   * Manages transition to the next dungeon floor or sets the game to victory mode.
   */
  public void moveToNextFloor() {
    if (donjon.moveToNextFloor()) {
      this.posX = donjon.getCurrentFloor().startX();
      this.posY = donjon.getCurrentFloor().startY();
      this.notifier("Direction Etage " + donjon.getFloorNumber() + "!");
    } else {
      this.modeActuel = Mode.GAGNE;
    }
  }

  /**
   * Records a notification message to be displayed by the graphical engine.
   * @param message the text to display.
   */
  public void notifier(String message) {
    this.messageAEnregistrer = Objects.requireNonNull(message);
  }

  /**
   * Retrieves and clears the pending notification message.
   * @return the last notification message or null if empty.
   */
  public String pollMessage() {
    String msg = messageAEnregistrer;
    messageAEnregistrer = null;
    return msg;
  }

  // --- Getters and Setters ---

  public void setMode(Mode mode) {
    this.modeActuel = Objects.requireNonNull(mode);
  }

  public Mode getMode() { return modeActuel; }
  public Combat getCombat() { return combatEnCours; }
  public Dungeon getDonjon() { return donjon; }
  public int getX() { return posX; }
  public int getY() { return posY; }
  public Heros getHeros() { return heros; }
  public ViewGraphic getView() { return view; }
}