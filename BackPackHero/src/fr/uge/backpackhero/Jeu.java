package fr.uge.backpackhero;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.combat.CombatState;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.View;

/**
 * Main model class representing the game state. 
 * Manages the hero, the dungeon navigation, and transitions between game modes.
 */
public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final View view;
  private Mode modeActuel;
  private Combat combatEnCours;
  private int posX;
  private int posY;

  /**
   * Initializes the game with a hero and a dungeon.
   *
   * @param heros  the non-null hero player.
   * @param donjon the non-null dungeon to explore.
   * @param view   the non-null view for item interactions.
   */
  public Jeu(Heros heros, Dungeon donjon, View view) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    this.view = Objects.requireNonNull(view);
    this.modeActuel = Mode.EXPLORATION;
    this.posX = donjon.getCurrentFloor().startX();
    this.posY = donjon.getCurrentFloor().startY();
  }

  /**
   * Moves the hero in the current floor if the destination is valid.
   * Triggers the room's effect via polymorphism.
   *
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
   * Updates the combat state, handling victory rewards or game over.
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

  private void handleVictoryRewards(List<ItemInstance> rewards) {
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
   * Transitions the game to the next floor if available, or marks victory.
   */
  public void moveToNextFloor() {
    if (donjon.moveToNextFloor()) {
      this.posX = donjon.getCurrentFloor().startX();
      this.posY = donjon.getCurrentFloor().startY();
    } else {
      this.modeActuel = Mode.GAGNE;
    }
  }

  /**
   * Sets the current game mode.
   *
   * @param mode the non-null game mode.
   */
  public void setMode(Mode mode) {
    this.modeActuel = Objects.requireNonNull(mode);
  }

  /**
   * Initializes a combat session with the provided enemies.
   *
   * @param enemies the non-null list of enemies.
   */
  public void lancerCombat(List<fr.uge.backpackhero.entites.Ennemi> enemies) {
    Objects.requireNonNull(enemies);
    this.combatEnCours = new Combat(heros, enemies, view);
    this.modeActuel = Mode.COMBAT;
  }

  // --- Getters ---

  public Mode getMode() { return modeActuel; }
  public Combat getCombat() { return combatEnCours; }
  public Dungeon getDonjon() { return donjon; }
  public int getX() { return posX; }
  public int getY() { return posY; }
  public Heros getHeros() { return heros; }
  public View getView() { return view; }
}