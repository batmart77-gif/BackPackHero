package fr.uge.backpackhero;

import java.util.Objects;
import java.util.Scanner;
import fr.uge.backpackhero.entites.Heros;

/**
 * Handles the interaction logic between the hero and the Healer.
 * Provides healing services in exchange for gold pieces as defined in the rules.
 */
public class MenuGuerisseur {
  private static final int LIGHT_HEAL_PRICE = 5;
  private static final int FULL_HEAL_PRICE = 15;
  private static final int LIGHT_HEAL_AMOUNT = 10;

  /**
   * Opens the healer interaction menu and starts the input loop.
   *
   * @param heros   the non-null hero interacting with the healer.
   * @param scanner the non-null scanner for user input.
   * @throws NullPointerException if heros or scanner is null.
   */
  public static void ouvrir(Heros heros, Scanner scanner) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(scanner);
    
    System.out.println("Welcome to the healer's sanctuary.");
    boolean interacting = true;
    while (interacting) {
      displayStatus(heros);
      String input = scanner.next().toUpperCase();
      interacting = processChoice(input, heros);
    }
  }

  private static void displayStatus(Heros heros) {
    System.out.println("\n--- HEALER ---");
    System.out.println("Health: " + heros.getPv() + "/" + heros.getPvMax());
    System.out.println("Gold: " + heros.getGold());
    System.out.println("CHOICES:");
    System.out.println("  [1] Light Heal (+" + LIGHT_HEAL_AMOUNT + " HP) - Cost: " + LIGHT_HEAL_PRICE);
    System.out.println("  [2] Full Heal (Max HP) - Cost: " + FULL_HEAL_PRICE);
    System.out.println("  [Q] Quit");
    System.out.print("> ");
  }

  private static boolean processChoice(String input, Heros heros) {
    switch (input) {
      case "Q" -> {
        System.out.println("\"Take care of yourself...\"");
        return false;
      }
      case "1" -> performHeal(heros, LIGHT_HEAL_AMOUNT, LIGHT_HEAL_PRICE, "A soft light heals you.");
      case "2" -> performHeal(heros, heros.getPvMax(), FULL_HEAL_PRICE, "You are fully restored!");
      default -> System.out.println("Unknown command.");
    }
    return true;
  }

  private static void performHeal(Heros heros, int amount, int price, String message) {
    if (heros.getPv() >= heros.getPvMax()) {
      System.out.println("You are already at full health!");
      return;
    }
    
    if (heros.payer(price)) {
      heros.soigner(amount);
      System.out.println(message);
    } else {
      System.out.println("You don't have enough gold.");
    }
  }
}