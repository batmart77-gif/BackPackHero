package fr.uge.backpackhero;

import java.util.*;
import java.io.IOException;
import fr.uge.backpackhero.entites.*;
import fr.uge.backpackhero.donjon.*;
import fr.uge.backpackhero.combat.*;
import fr.uge.backpackhero.item.*;
import fr.uge.backpackhero.data.HallOfFame;

public class TerminalController {
    private final Scanner scanner = new Scanner(System.in);
    private final Heros heros = new Heros();
    private final Jeu jeu;

    public TerminalController() {
        Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
        View view = new View(heros.getBackpack(), new StuffFactory(), heros);
        this.jeu = new Jeu(heros, dungeon, view);
        initialiserEquipement();
    }

    private void initialiserEquipement() {
        StuffFactory f = new StuffFactory();
        heros.getBackpack().add(new ItemInstance(f.create(Stuff.WoodSword)), new Position(0, 0));
        heros.getBackpack().add(new ItemInstance(f.create(Stuff.RoughBuckler)), new Position(0, 1));
        System.out.println(">> Préparation de votre équipement...\n>> Sac rempli avec succès !");
    }

    public void run() {
        System.out.println("========================================\n     BACKPACK HERO  \n========================================");
        while (jeu.getMode() != Mode.GAGNE && jeu.getMode() != Mode.PERDU) {
            if (jeu.getMode() == Mode.COMBAT) gererCombat();
            else gererExploration();
        }
        terminerPartie();
    }

    private void gererExploration() {
        afficherInfosExploration();
        System.out.println("Position actuelle : (" + jeu.getY() + "," + jeu.getX() + ")");
        System.out.print("Commandes : (z) Haut | (s) Bas | (q) Gauche | (d) Droite | (i) Inventaire | (o) réorganiser le sac\n> ");
        String input = scanner.next().toLowerCase();
        int dx = 0, dy = 0;
        switch (input) {
            case "z" -> dy = -1; case "s" -> dy = 1; case "q" -> dx = -1; case "d" -> dx = 1;
            case "i" -> { jeu.getView().printBackPack(); return; }
            case "o" -> { jeu.getView().reorganize(); return; }
            default -> { System.out.println("❓ Commande inconnue."); return; }
        }
        int tx = jeu.getX() + dx, ty = jeu.getY() + dy;
        Floor floor = jeu.getDonjon().getCurrentFloor();
        if (floor.getRoom(tx, ty) != null) {
            floor.revealRoom(tx, ty);
            jeu.deplacer(dx, dy);
        } else {
            System.out.println("Bong ! Un mur.");
        }
    }

    private void afficherInfosExploration() {
        System.out.println("\nPV : " + heros.getPv() + "/" + heros.getPvMax() + 
                           " | Mana : " + heros.getMana() + 
                           " | Or : " + heros.getGold());
        afficherCarte();
    }

    private void gererCombat() {
      Combat combat = jeu.getCombat();
      System.out.println("\n--- COMBAT ---");
      System.out.printf("HÉROS : %d/%d PV | %d Énergie | %d Protection | Niv %d\n", 
          heros.getPv(), heros.getPvMax(), heros.getEnergie(), heros.getProtection(), heros.getLevel());
      combat.getAliveEnemies().forEach(e -> 
          System.out.println("  Ennemi (" + e.getHp() + " PV) -> Prévoit : " + e.getActionAnnoncee().description()));
      jeu.getView().printBackPack(); 
      List<ItemInstance> objets = heros.getBackpack().getItems();
      System.out.println("Liste des actions :");
      if (objets.isEmpty()) {
          System.out.println("  (Sac vide !)");
      } else {
          for (int i = 0; i < objets.size(); i++) {
              System.out.println("  Tap [" + i + "] pour utiliser : " + objets.get(i).getName());
          }
      }
      System.out.println("  [f] Fin de tour");
      System.out.print("Action > ");
      String action = scanner.next();
      if (action.equalsIgnoreCase("f")) {
          combat.startEnemyTurn();
      } else {
          traiterActionCombat(action, combat);
      }    
      jeu.updateCombatState();
    }

    private void traiterActionCombat(String input, Combat combat) {
      try {
          int idx = Integer.parseInt(input);
          List<ItemInstance> items = heros.getBackpack().getItems();
          if (idx >= 0 && idx < items.size()) {
              // On récupère le résultat de l'action
              boolean succes = combat.tryHeroAction(items.get(idx), combat.getAliveEnemies().get(0));
              if (succes) {
                  System.out.println("Action réussie : " + items.get(idx).getName());
              } else {
                  System.out.println("Action impossible (Énergie/Mana insuffisant ou déjà utilisé)");
              }
          }
      } catch (Exception e) { 
          System.out.println("Entrée invalide."); 
      }
    }

    private void afficherCarte() {
        Floor floor = jeu.getDonjon().getCurrentFloor();
        System.out.println("\n--- EXPLORATION (Étage " + jeu.getDonjon().getFloorNumber() + ") ---\n      --- PLAN ---");
        System.out.print("    ");
        for (int x = 0; x < floor.width(); x++) System.out.printf(" %-2d ", x);
        System.out.println();
        
        for (int y = 0; y < floor.height(); y++) {
            System.out.print("    +"); for(int x=0; x<floor.width(); x++) System.out.print("---+");
            System.out.printf("\n %d  |", y);
            for (int x = 0; x < floor.width(); x++) {
                System.out.print(getSymbol(x, y, floor) + "|");
            }
            System.out.println();
        }
        System.out.println("    +"); for(int x=0; x<floor.width(); x++) System.out.print("---+");
        System.out.println("\n  Légende: @:Moi E:Ennemi T:Trésor M:Marchand H:Soin S:Sortie !:Event #:Grille");
    }

    private String getSymbol(int x, int y, Floor floor) {
        if (x == jeu.getX() && y == jeu.getY()) return " @ ";
        //if (!floor.isExplored(x, y)) return "///"; // Phase 3 : Brouillard de guerre
        Room r = floor.getRoom(x, y);
        if (r == null) return "///"; // Mur réel
        return switch(r) {
            case Corridor c -> " . ";
            case EnemyRoom e -> " E ";
            case TreasureRoom t -> " T ";
            case MerchantRoom m -> " M ";
            case HealerRoom h -> " H ";
            case ExitRoom s -> " S ";
            case GateRoom g -> " # ";
            case EventRoom v -> " ! ";
            default -> "///";
        };
    }

    private void terminerPartie() {
        int finalScore = heros.calculateFinalScore();
        System.out.println("\n=== FIN DE PARTIE ===\nScore : " + finalScore);
        HallOfFame hof = new HallOfFame();
        try {
            hof.saveScore("Joueur", finalScore);
            hof.display();
        } catch (IOException e) { System.err.println("Erreur Hall of Fame."); }
    }
}