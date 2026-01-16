package fr.uge.backpackhero;

import java.util.List;
import java.util.Objects;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.combat.CombatState;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.graphics.ViewGraphic; // On utilise la version graphique

public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final ViewGraphic view; // Type mis à jour
  private Mode modeActuel;
  private Combat combatEnCours;
  private int posX;
  private int posY;

  /**
   * Ce constructeur accepte maintenant les 4 paramètres envoyés par le Main.
   */
  public Jeu(Heros heros, Dungeon donjon, ViewGraphic vg1, ViewGraphic vg2) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    // On initialise 'view' avec l'un des deux ViewGraphic reçus
    this.view = Objects.requireNonNull(vg1);
    this.modeActuel = Mode.EXPLORATION;
    this.posX = donjon.getCurrentFloor().startX();
    this.posY = donjon.getCurrentFloor().startY();
  }

  public void deplacer(int dx, int dy) {
    if (modeActuel != Mode.EXPLORATION) return;
    int newX = posX + dx;
    int newY = posY + dy;
    Room targetRoom = donjon.getCurrentFloor().getRoom(newX, newY);
    if (targetRoom != null) {
      this.posX = newX;
      this.posY = newY;
      targetRoom.onClick(this);
    }
  }

  public void updateCombatState() {
    if (combatEnCours == null) return;
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

  public void lancerCombat(List<fr.uge.backpackhero.entites.Ennemi> enemies) {
    Objects.requireNonNull(enemies);
    // Comme view est un ViewGraphic, Java accepte de l'utiliser comme Delegate
    this.combatEnCours = new Combat(heros, enemies, view); 
    this.modeActuel = Mode.COMBAT;
  }

  public void moveToNextFloor() {
    if (donjon.moveToNextFloor()) {
      this.posX = donjon.getCurrentFloor().startX();
      this.posY = donjon.getCurrentFloor().startY();
    } else {
      this.modeActuel = Mode.GAGNE;
    }
  }

  public void setMode(Mode mode) { this.modeActuel = Objects.requireNonNull(mode); }
  public Mode getMode() { return modeActuel; }
  public Combat getCombat() { return combatEnCours; }
  public Dungeon getDonjon() { return donjon; }
  public int getX() { return posX; }
  public int getY() { return posY; }
  public Heros getHeros() { return heros; }
  public ViewGraphic getView() { return view; }
}