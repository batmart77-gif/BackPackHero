package fr.uge.backpackhero;

import java.util.Scanner;
import fr.uge.backpackhero.entites.Heros;

public class MenuGuerisseur {
  private static final int PRIX_SOIN_LEGER = 5;
  private static final int PRIX_SOIN_TOTAL = 15;

  public static void ouvrir(Heros heros, Scanner scanner) {
    boolean chezLeMedecin = true;
    System.out.println("Bienvenue chez le guerisseur.");
    
    while (chezLeMedecin) {
      afficherMenu(heros);
      String input = scanner.next().toUpperCase();
      chezLeMedecin = traiterChoix(input, heros);
    }
  }

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