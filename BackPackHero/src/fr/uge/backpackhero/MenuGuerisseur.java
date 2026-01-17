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
    
    boolean interacting = true;
    while (interacting) {
      String input = scanner.next().toUpperCase();
      interacting = processChoice(input, heros);
    }
  }

  private static boolean processChoice(String input, Heros heros) {
    switch (input) {
      case "1" -> performHeal(heros, LIGHT_HEAL_AMOUNT, LIGHT_HEAL_PRICE);
      case "2" -> performHeal(heros, heros.getPvMax(), FULL_HEAL_PRICE);
      default -> System.out.println("Unknown command.");
    }
    return true;
  }

  private static void performHeal(Heros heros, int amount, int price) {
    if (heros.payer(price)) {
      heros.soigner(amount);
    }
  }
}