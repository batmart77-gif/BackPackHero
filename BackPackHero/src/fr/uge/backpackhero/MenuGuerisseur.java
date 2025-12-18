package fr.uge.backpackhero;

import java.util.Scanner;
import fr.uge.backpackhero.entites.Heros;

/**
 * Handles the interaction logic between the player (Hero) and the Healer in a console-based interface.
 * This class manages the healing services offered in a HealerRoom.
 */
public class MenuGuerisseur {
  private static final int PRIX_SOIN_LEGER = 5;
  private static final int PRIX_SOIN_TOTAL = 15;

  /**
   * Opens the healer interaction menu.
   * This method starts the main loop for the healer interface, allowing the player to choose a healing option or leave.
   *
   * @param heros   The hero interacting with the healer.
   * @param scanner The scanner used to read user input.
   */
  public static void ouvrir(Heros heros, Scanner scanner) {
    boolean chezLeMedecin = true;
    System.out.println("Bienvenue chez le guerisseur.");
    while (chezLeMedecin) {
      afficherMenu(heros);
      String input = scanner.next().toUpperCase();
      chezLeMedecin = traiterChoix(input, heros);
    }
  }

  /**
   * Displays the healer menu, including the hero's current health, gold, and available healing options with their prices.
   *
   * @param heros The hero whose stats are displayed.
   */
  private static void afficherMenu(Heros heros) {
    System.out.println("\n--- GUERISSEUR ---");
    System.out.println("Santé : " + heros.getPv() + "/" + heros.getPvMax());
    System.out.println("Solde : " + heros.getGold() + " Or");
    System.out.println("CHOIX :");
    System.out.println("  [1] Soin léger (+10 PV) - Coût : " + PRIX_SOIN_LEGER + " Or");
    System.out.println("  [2] Soin complet (Max PV) - Coût : " + PRIX_SOIN_TOTAL + " Or");
    System.out.println("  [Q] Quitter");
    System.out.print("> ");
  }

  /**
   * Processes the user's choice in the healer menu.
   *
   * @param input The user input string.
   * @param heros The hero.
   * @return {@code true} if the interaction should continue, {@code false} if the user wants to quit.
   */
  private static boolean traiterChoix(String input, Heros heros) {
    if (input.equals("Q")) {
      System.out.println("\"Prenez soin de vous...\"");
      return false;
    }
    if (input.equals("1")) {
      traiterSoinLeger(heros);
    } else if (input.equals("2")) {
      traiterSoinComplet(heros);
    } else {
      System.out.println("Commande inconnue.");
    }
    return true;
  }

  /**
   * Handles the logic for the "Light Heal" option.
   * Checks if the hero is already full health or has enough gold, then applies the heal.
   *
   * @param heros The hero attempting to heal.
   */
  private static void traiterSoinLeger(Heros heros) {
    if (heros.getPv() >= heros.getPvMax()) {
      System.out.println("Vous êtes déjà en pleine forme.");
      return;
    }
    if (heros.payer(PRIX_SOIN_LEGER)) {
      heros.soigner(10);
      System.out.println("Une douce lumière vous soigne (+10 PV).");
    } else {
      System.out.println("Vous n'avez pas assez d'argent.");
    }
  }

  /**
   * Handles the logic for the "Full Heal" option.
   * Checks if the hero is already full health or has enough gold, then restores health to maximum.
   *
   * @param heros The hero attempting to heal.
   */
  private static void traiterSoinComplet(Heros heros) {
    if (heros.getPv() >= heros.getPvMax()) {
      System.out.println("Inutile, vous pétez la forme !");
      return;
    }
    if (heros.payer(PRIX_SOIN_TOTAL)) {
      int manque = heros.getPvMax() - heros.getPv();
      heros.soigner(manque);
      System.out.println("Vous êtes totalement rétabli !");
    } else {
      System.out.println("Revenez quand vous serez riche.");
    }
  }
}