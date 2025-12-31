package fr.uge.backpackhero;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.donjon.*;
import fr.uge.backpackhero.combat.*;
import fr.uge.backpackhero.item.*;

public final class Jeu {
  private final Heros heros;
  private final Dungeon donjon;
  private final View view; 
  private Mode modeActuel;
  private Combat combatEnCours;
  private int posX, posY;

  public Jeu(Heros heros, Dungeon donjon, View view) {
    this.heros = Objects.requireNonNull(heros);
    this.donjon = Objects.requireNonNull(donjon);
    this.view = Objects.requireNonNull(view);
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
      analyserSalle(targetRoom);
    }
  }

  private void analyserSalle(Room room) {
    switch (room) {
      case Corridor c -> System.out.println("Simple couloir.");
      case EnemyRoom e -> handleEnemyRoom(e);
      case TreasureRoom t -> handleTreasureRoom(t);
      case MerchantRoom m -> MenuMarchand.ouvrir(heros, m, new Scanner(System.in));
      case HealerRoom h -> MenuGuerisseur.ouvrir(heros, new Scanner(System.in));
      case ExitRoom x -> handleExitRoom();
      case EventRoom v -> v.triggerEffect(heros); // Phase 3 : Effet aléatoire
      case GateRoom g -> handleGateRoom(g); // Phase 3 : Demande une clé
    }
  }

  private void handleGateRoom(GateRoom g) {
      System.out.println("Une grille bloque le passage ! Il faut sacrifier une clé.");
      // Logique simplifiée : le héros doit posséder un objet nommé "Key"
      boolean aCle = heros.getBackpack().getItems().stream()
                          .anyMatch(i -> i.getName().equalsIgnoreCase("Key"));
      if (aCle) {
          System.out.println("Grille déverrouillée !");
          analyserSalle(g.hiddenRoom()); // On accède à ce qu'il y avait derrière
      } else {
          System.out.println("Vous n'avez pas de clé...");
      }
  }

  private void handleEnemyRoom(EnemyRoom e) {
    if (e.enemies().stream().anyMatch(Ennemi::estVivant)) {
      this.modeActuel = Mode.COMBAT;
      this.combatEnCours = new Combat(heros, e.enemies(), view);
    }
  }

  private void handleTreasureRoom(TreasureRoom t) {
    System.out.println("Coffre trouvé !");
    for (ItemInstance item : t.loot()) {
      System.out.println("Contenu : " + item.getName());
      view.displayItemFound(item);
      if (view.interactBeforePlacement(item)) view.attemptPlacement(item);
    }
  }

  private void handleExitRoom() {
    if (donjon.moveToNextFloor()) {
      System.out.println("Étage suivant !");
      this.posX = donjon.getCurrentFloor().startX();
      this.posY = donjon.getCurrentFloor().startY();
    } else {
      this.modeActuel = Mode.GAGNE;
    }
  }

  public void updateCombatState() {
    if (combatEnCours == null) return;
    if (combatEnCours.getState() == CombatState.WIN) {
        // Récupération automatique du butin post-combat (Phase 2/3)
        List<ItemInstance> rewards = combatEnCours.finishCombat();
        rewards.forEach(r -> {
            view.displayItemFound(r);
            if(view.interactBeforePlacement(r)) view.attemptPlacement(r);
        });
        this.combatEnCours = null;
        this.modeActuel = Mode.EXPLORATION;
    } else if (combatEnCours.getState() == CombatState.LOSS) {
        this.modeActuel = Mode.PERDU;
    }
  }
  
  // Getters
  public Mode getMode() { return modeActuel; }
  public Combat getCombat() { return combatEnCours; }
  public Dungeon getDonjon() { return donjon; }
  public int getX() { return posX; }
  public int getY() { return posY; }

  public Heros getHeros() {
    return heros;
  }
  
  /**
   * Permet au contrôleur d'accéder à la vue
   * @return l'instance de View utilisée par le jeu.
   */
  public View getView() {
    return view;
  }
}