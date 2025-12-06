package fr.uge.backpackhero;

import java.util.Scanner;
import java.util.List;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.donjon.Floor;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.View;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;

public class MainPhase1 {
  public static void main(String[] args) {
    System.out.println("#######################################");
    System.out.println("#     PROJET BACKPACK HERO - PHASE 1  #");
    System.out.println("#######################################");

    // 1. CRÉATION DU HÉROS
    Heros heros = new Heros();
    
    // 2. REMPLISSAGE DU SAC
    System.out.println(">> Initialisation de l'équipement...");
    StuffFactory factory = new StuffFactory();
    
    try {
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.WoodSword)), 
          new Position(0, 0)
      );

      // 2. Bouclier (RoughBuckler) : Colonnes 1-2, Lignes 0-1 (Carré 2x2)
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.RoughBuckler)), 
          new Position(0, 1)
      );

      // 3. Arme à Distance (CompositeBow) : Colonne 3, Lignes 0-1 (Vertical 2 cases)
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.MouseBow)), 
          new Position(0, 3)
      );

      // 4. Munition (ShortArrow) : Colonne 4, Ligne 0 (1 case)
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.ShortArrow)), 
          new Position(0, 4)
      );

      // 5. Objet Magique (ElectricWand) : Diagonale
      // Forme: (0,1) et (1,0). Si on place l'ancrage en (1,3)...
      // Occupes : (1,4) et (2,3). C'est libre !
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.ElectricWand)), 
          new Position(1, 3)
      );

      // 6. Armure (LeatherCap) : Colonne 1, Ligne 2 (1 case)
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.LeatherCap)), 
          new Position(2, 1)
      );

      // 7. Pierre de Mana (ManaStone) : Colonne 2, Ligne 2 (1 case)
      heros.getBackpack().add(
          new ItemInstance(factory.create(Stuff.ManaStone)), 
          new Position(2, 2)
      );
      
      System.out.println(">> Sac rempli ! Tout est rentré.");

    } catch (Exception e) {
      System.err.println("Erreur de placement : " + e.getMessage());
    }

    // 3. CRÉATION DU MONDE
    Dungeon donjon = DungeonGenerator.createDungeonPhase1();
    Jeu jeu = new Jeu(heros, donjon);

    // 4. BOUCLE DE JEU
    Scanner scanner = new Scanner(System.in);
    
    // Tant que la partie n'est ni Gagnée ni Perdue
    while (jeu.getMode() != Mode.GAGNE && jeu.getMode() != Mode.PERDU) {
      if (jeu.getMode() == Mode.COMBAT) {
        menuCombat(jeu, scanner);
      } else {
        menuExploration(jeu, scanner);
      }
    }
    // 5. FIN DE PARTIE
    scanner.close();
    System.out.println("\n--- FIN DE LA PARTIE ---");
  }

  /**
   * Gère l'affichage et les commandes quand on explore.
   */
  private static void menuExploration(Jeu jeu, Scanner scanner) {
    System.out.println("\n--- EXPLORATION ---");
    
    // On affiche la carte 
    afficherCarte(jeu);
    
    System.out.println("Vous êtes en position : (" + jeu.getX() + ", " + jeu.getY() + ")");
    System.out.println("Commandes : (z) Haut | (s) Bas | (q) Gauche | (d) Droite");
    System.out.print("> ");

    String choix = scanner.next().toLowerCase();
    
    switch (choix) {
      case "z" -> jeu.deplacer(0, -1); // Monter (Y diminue)
      case "s" -> jeu.deplacer(0, 1);  // Descendre (Y augmente)
      case "q" -> jeu.deplacer(-1, 0); // Gauche (X diminue)
      case "d" -> jeu.deplacer(1, 0);  // Droite (X augmente)
      default -> System.out.println("Commande inconnue.");
    }
  }

  /**
   * Gère l'affichage et les commandes quand on se bat.
   */
  private static void menuCombat(Jeu jeu, Scanner scanner) {
    Combat combat = jeu.getCombat();
    Heros heros = jeu.getHeros();

    System.out.println("\n--- COMBAT ---");
    
    // Affichage Stats Héros
    System.out.printf("HÉROS : %d/%d PV | %d Énergie | %d Protection\n", 
        heros.getPv(), heros.getPvMax(), heros.getEnergie(), heros.getProtection());
    
    // Affichage visuel du sac
    System.out.println("\nVOTRE ÉQUIPEMENT :");
    // On appelle la méthode statique de la classe View 
    View.printBackPack(heros.getBackpack());

    // Affichage de la liste textuelle (pour savoir quel numéro taper)
    System.out.println("Liste des actions :");
    List<ItemInstance> objets = heros.getBackpack().getItems();
    

    // Affichage Ennemis
    System.out.println("ENNEMIS :");
    List<Ennemi> enemies = combat.getAliveEnemies();
    for (int i = 0; i < enemies.size(); i++) {
      Ennemi e = enemies.get(i);
      // On affiche l'intention
      System.out.println("  [" + i + "] Rat-Loup (" + e.getPv() + " PV) -> Prévoit : " 
          + e.getActionAnnoncee().description());
    }

    // Affichage Objets utilisables
    System.out.println("VOTRE SAC :");
    List<ItemInstance> items = heros.getBackpack().getItems(); 
    
    for (int i = 0; i < items.size(); i++) {
      ItemInstance inst = items.get(i);
      // On affiche Nom et Coût (le coût est dans l'Item de base)
      System.out.println("  Tap [" + i + "] pour utiliser : " + inst.getName());
    }
    System.out.println("  Tap [f] pour Finir le tour");

    System.out.print("Combat > ");
    String input = scanner.next();

    if (input.equals("f")) {
      System.out.println("--- Fin de votre tour ---");
      combat.startEnemyTurn();
      jeu.updateCombatState();
    } else {
      try {
        int index = Integer.parseInt(input);
        if (index >= 0 && index < items.size()) {
          ItemInstance objetChoisi = items.get(index);
          
          // Pour simplifier la console, on tape toujours le 1er ennemi
          Ennemi cible = enemies.isEmpty() ? null : enemies.get(0);
          
          boolean ok = combat.tryHeroAction(objetChoisi, cible);
          if (!ok) {
            System.out.println("Action impossible (Pas assez d'énergie ?)");
          }
          
          // Vérifier si on a gagné juste après le coup
          jeu.updateCombatState();
          
        } else {
          System.out.println("Numéro invalide.");
        }
      } catch (NumberFormatException e) {
        System.out.println("Commande invalide.");
      }
    }
  }  
  /**
   * Affiche une représentation l'étage.
   */
  private static void afficherCarte(Jeu jeu) {
    Floor etage = jeu.getDonjon().getCurrentFloor();
    int h = etage.height();
    int l = etage.width();
    System.out.println("\n      --- PLAN DE L'ÉTAGE ---");
    // Affichage des numéros de colonne (0  1  2...)
    System.out.print("    "); 
    for (int x = 0; x < l; x++) {
      System.out.printf(" %-2d ", x);
    }
    System.out.println();
    // Bordure supérieure
    System.out.print("    +");
    for (int x = 0; x < l; x++) {
      System.out.print("---+");
    }
    System.out.println();

    // Affichage de la grille
    for (int y = 0; y < h; y++) {
      // Numéro de ligne à gauche
      System.out.printf(" %-2d |", y);
      for (int x = 0; x < l; x++) {       
        // -- DÉTERMINATION DU SYMBOLE --
        String contenu;
        if (x == jeu.getX() && y == jeu.getY()) {
          contenu = " @ "; // Le Héros
        } else {
          fr.uge.backpackhero.donjon.Room salle = etage.getRoom(x, y);
          
          if (salle == null) {
            contenu = "///"; // Mur / Vide
          } else {
            contenu = switch (salle) {
              case fr.uge.backpackhero.donjon.Corridor c -> " . ";
              case fr.uge.backpackhero.donjon.EnemyRoom e -> " E ";
              case fr.uge.backpackhero.donjon.TreasureRoom t -> " T ";
              case fr.uge.backpackhero.donjon.MerchantRoom m -> " M ";
              case fr.uge.backpackhero.donjon.HealerRoom heal -> " H ";
              case fr.uge.backpackhero.donjon.ExitRoom s -> " S ";
            };
          }
        }
        // -- AFFICHAGE CELLULE --
        System.out.print(contenu + "|");
      }
      // Fin de la ligne
      System.out.println();
      // Ligne de séparation horizontale (pour faire une vraie grille)
      System.out.print("    +");
      for (int x = 0; x < l; x++) {
        System.out.print("---+");
      }
      System.out.println();
    } 
    System.out.println("  Légende: @:Vous E:Ennemi T:Trésor S:Sortie ///:Mur");
  }
}