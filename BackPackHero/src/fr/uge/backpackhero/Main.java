package fr.uge.backpackhero;

import java.util.Scanner;
import java.util.List;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.donjon.HealerRoom;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.item.BackPack;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.View;

public class Main {

  public static void main(String[] args) {
    System.out.println("========================================");
    System.out.println("     BACKPACK HERO  ️");
    System.out.println("========================================");
    System.out.println("TEEEEEEEEST");
    System.out.println("TEEEEEEEEST2");
    Heros heros = new Heros();
    System.out.println(">> Préparation de votre équipement...");
    StuffFactory factory = new StuffFactory();
    remplirSacADos(heros, factory);
    Dungeon donjon = DungeonGenerator.createDungeonPhase1();
    View view = new View(heros.getBackpack(), factory, heros);
    Jeu jeu = new Jeu(heros, donjon, view); 
    Scanner scanner = new Scanner(System.in); 
    lancerBoucleJeu(jeu, scanner);
    finDePartie(jeu, heros);
    scanner.close();
  }

  // --- SOUS-METHODES INITIALISATION ---

  private static void remplirSacADos(Heros heros, StuffFactory factory) {
    try {
      ItemInstance epee = new ItemInstance(factory.create(Stuff.WoodSword));
      ItemInstance bouclier = new ItemInstance(factory.create(Stuff.RoughBuckler));
      BackPack backpack = heros.getBackpack();
      backpack.add(epee, new Position(0, 0));
      backpack.add(bouclier, new Position(0, 1));
      // backpack.add(potion, new Position(2, 1)); // Optionnel selon ta taille de sac
      
      System.out.println(">> Sac rempli avec succès !");
    } catch (Exception e) {
      System.out.println("Petit souci d'inventaire : " + e.getMessage());
    }
  }

  private static void lancerBoucleJeu(Jeu jeu, Scanner scanner) {
    while (jeu.getMode() != Mode.GAGNE && jeu.getMode() != Mode.PERDU) {
      Room currentRoom = jeu.getDonjon().getCurrentFloor().getRoom(jeu.getX(), jeu.getY());
      if (jeu.getMode() == Mode.COMBAT) {
        gererCombatConsole(jeu, scanner);
      } else {
        aiguillerSalle(jeu, currentRoom, scanner);
      }
    }
  }

  private static void aiguillerSalle(Jeu jeu, Room room, Scanner scanner) {
    switch (room) {
      case MerchantRoom shop -> 
          MenuMarchand.ouvrir(jeu.getHeros(), shop, scanner);
      case HealerRoom healer -> 
          MenuGuerisseur.ouvrir(jeu.getHeros(), scanner);
      default -> { }
    }
    gererExplorationConsole(jeu, scanner);
  }

  private static void finDePartie(Jeu jeu, Heros heros) {
    System.out.println("\n========================================");
    if (jeu.getMode() == Mode.GAGNE) {
      System.out.println("VICTOIRE ! Niveau final : " + heros.getLevel());
      System.out.println("Or accumulé : " + heros.getGold());
    } else {
      System.out.println("GAME OVER ! Le donjon a eu raison de vous.");
    }
  }

  private static void gererExplorationConsole(Jeu jeu, Scanner scanner) {
    afficherInfosExploration(jeu);
    String input = scanner.next().toLowerCase();
    traiterCommandeExploration(jeu, input, scanner);
  }

  private static void afficherInfosExploration(Jeu jeu) {
    int numEtage = jeu.getDonjon().getFloorNumber();
    System.out.println("\n--- EXPLORATION (Étage " + numEtage + ") ---");
    afficherCarte(jeu);
    System.out.println("Position : (" + jeu.getX() + ", " + jeu.getY() + ")");
    System.out.println("Commandes : (z) Haut | (s) Bas | (q) Gauche | (d) Droite | (i) Inventaire");
    System.out.print("> ");
  }

  private static void traiterCommandeExploration(Jeu jeu, String input, Scanner scanner) {
    int dx = 0, dy = 0;
    switch (input) {
      case "z" -> dy = -1;
      case "s" -> dy = 1;
      case "q" -> dx = -1;
      case "d" -> dx = 1;
      case "i" -> {
        gererMenuInventaire(jeu.getHeros(), scanner);
        return;
      }
      default -> System.out.println("❓ Commande inconnue.");
    }
    if (dx != 0 || dy != 0) jeu.deplacer(dx, dy);
  }

  // --- SOUS-METHODES COMBAT ---

  private static void gererCombatConsole(Jeu jeu, Scanner scanner) {
    Combat combat = jeu.getCombat();
    Heros heros = jeu.getHeros();

    jeu.updateCombatState();
    if (jeu.getMode() != Mode.COMBAT) return;

    afficherInfosCombat(heros, combat);
    
    System.out.print("Action > ");
    String input = scanner.next();
    traiterActionCombat(input, combat, heros, jeu);
  }

  private static void afficherInfosCombat(Heros heros, Combat combat) {
    System.out.println("\n--- COMBAT ---");
    System.out.printf("HÉROS : %d/%d PV | %d Energie | %d Protection\n", 
        heros.getPv(), heros.getPvMax(), heros.getEnergie(), heros.getProtection());
    System.out.println("ENNEMIS :");
    for (int i = 0; i < combat.getAliveEnemies().size(); i++) {
      Ennemi e = combat.getAliveEnemies().get(i);
      System.out.println("  [" + i + "] Ennemi (" + e.getHp() + " PV) -> " 
          + e.choisirProchaineAction().description());
    }
    System.out.println("VOTRE SAC :"); 
    View.printBackPack(heros.getBackpack()); 
    List<ItemInstance> objets = heros.getBackpack().getItems();
    if (objets.isEmpty()) System.out.println("  (Sac vide !)");
    else {
        for (int i = 0; i < objets.size(); i++) 
            System.out.println("  Tap [" + i + "] : " + objets.get(i).getName());
    }
    System.out.println("  [f] Fin de tour");
  }

  private static void traiterActionCombat(String input, Combat combat, Heros heros, Jeu jeu) {
    if (input.equalsIgnoreCase("f")) {
      System.out.println("--- Fin de votre tour ---");
      combat.startEnemyTurn();   
      jeu.updateCombatState();
    } else {
      try {
        int index = Integer.parseInt(input);
        executerActionItem(index, combat, heros, jeu);
      } catch (NumberFormatException e) {
        System.out.println("Commande invalide.");
      }
    }
  }

  private static void executerActionItem(int index, Combat combat, Heros heros, Jeu jeu) {
    List<ItemInstance> objets = heros.getBackpack().getItems();
    if (index >= 0 && index < objets.size()) {
      ItemInstance item = objets.get(index);        
      Ennemi cible = combat.getAliveEnemies().isEmpty() ? null : combat.getAliveEnemies().get(0);
      
      if (!combat.tryHeroAction(item, cible)) {
          System.out.println("Action impossible");
      }          
      jeu.updateCombatState();
    } else {
      System.out.println("Numéro invalide.");
    }
  }

  // --- SOUS-METHODES INVENTAIRE ---

  private static void gererMenuInventaire(Heros heros, Scanner scanner) {
    afficherMenuInventaire(heros);
    
    String in = scanner.next();
    if (in.equalsIgnoreCase("X")) return;
    
    try {
      int idx = Integer.parseInt(in);
      jeterObjet(idx, heros);
    } catch (Exception e) { System.out.println("Erreur."); }
  }

  private static void afficherMenuInventaire(Heros heros) {
    System.out.println("\n--- SAC À DOS ---");
    System.out.println("Or : " + heros.getGold());
    var items = heros.getBackpack().getItems();
    for (int i = 0; i < items.size(); i++) {
      System.out.println(" [" + i + "] " + items.get(i).getName());
    }
    System.out.println("Tapez le numéro pour JETER un objet.");
    System.out.println("Tapez 'X' pour retour.");
    System.out.print("> ");
  }

  private static void jeterObjet(int idx, Heros heros) {
    var items = heros.getBackpack().getItems();
    if (idx >= 0 && idx < items.size()) {
      ItemInstance toThrow = items.get(idx);
      heros.getBackpack().removeItem(toThrow);
      System.out.println("Objet jeté.");
    }
  }

  // --- SOUS-METHODES CARTE ---

  private static void afficherCarte(Jeu jeu) {
    var etage = jeu.getDonjon().getCurrentFloor();
    System.out.println("\n      --- PLAN ---");
    afficherEnteteCarte(etage.width());
    
    for (int y = 0; y < etage.height(); y++) {
      System.out.printf(" %-2d |", y);
      afficherLigneCarte(y, etage.width(), jeu);
      afficherSeparateurHorizontal(etage.width());
    }
    System.out.println("  Légende: @:Moi E:Ennemi T:Trésor M:Marchand H:Soin S:Sortie");
  }

  private static void afficherEnteteCarte(int width) {
    System.out.print("    ");
    for (int x = 0; x < width; x++) System.out.printf(" %-2d ", x);
    System.out.println();
    afficherSeparateurHorizontal(width);
  }

  private static void afficherSeparateurHorizontal(int width) {
    System.out.print("    +");
    for (int x = 0; x < width; x++) System.out.print("---+");
    System.out.println();
  }

  private static void afficherLigneCarte(int y, int width, Jeu jeu) {
    var etage = jeu.getDonjon().getCurrentFloor();
    for (int x = 0; x < width; x++) {
      String contenu = getSymboleCase(x, y, jeu, etage);
      System.out.print(contenu + "|");
    }
    System.out.println();
  }

  private static String getSymboleCase(int x, int y, Jeu jeu, fr.uge.backpackhero.donjon.Floor etage) {
    if (x == jeu.getX() && y == jeu.getY()) return " @ ";
    
    var salle = etage.getRoom(x, y);
    if (salle == null) return "///";
    
    return switch (salle) {
      case fr.uge.backpackhero.donjon.Corridor c -> " . ";
      case fr.uge.backpackhero.donjon.EnemyRoom e -> " E ";
      case fr.uge.backpackhero.donjon.TreasureRoom t -> " T ";
      case fr.uge.backpackhero.donjon.MerchantRoom m -> " M ";
      case fr.uge.backpackhero.donjon.HealerRoom heal -> " H ";
      case fr.uge.backpackhero.donjon.ExitRoom s -> " S ";
    };
  }
}