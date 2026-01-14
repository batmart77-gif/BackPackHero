package fr.uge.backpackhero;

import java.util.Objects;
import java.util.Scanner;
import java.util.List;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.View;

/**
 * Handles the console-based interaction between the hero and the merchant.
 * Manages the logic for buying and selling items according to the hero's gold and backpack space.
 */
public final class MenuMarchand {

  private MenuMarchand() { }

  /**
   * Opens the merchant interaction menu and manages the shopping loop.
   *
   * @param heros   the non-null hero player.
   * @param shop    the non-null merchant room.
   * @param scanner the non-null scanner for user input.
   * @throws NullPointerException if any argument is null.
   */
  public static void ouvrir(Heros heros, MerchantRoom shop, Scanner scanner) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(shop);
    Objects.requireNonNull(scanner);

    System.out.println("Gold deals for a gold hero!");
    boolean shopping = true;
    while (shopping) {
      afficherInterfaceGlobale(heros, shop);
      String input = scanner.next().toUpperCase();
      shopping = traiterCommandePrincipale(input, heros, shop, scanner);
    }
    System.out.println("Goodbye!");
  }

  private static void afficherInterfaceGlobale(Heros heros, MerchantRoom shop) {
    System.out.println("\n--- SHOP --- (Balance: " + heros.getGold() + " Gold)");
    System.out.println("YOUR BACKPACK:");
    new View(heros.getBackpack(), new StuffFactory(), heros).printBackPack();
    afficherStock(shop.stock());
    System.out.println("\nACTIONS: [Number] Buy | [V] Sell | [Q] Quit");
    System.out.print("> ");
  }

  private static void afficherStock(List<ItemInstance> stock) {
    System.out.println("ITEMS FOR SALE:");
    if (stock.isEmpty()) {
      System.out.println("  (Out of stock!)");
      return;
    }
    for (int i = 0; i < stock.size(); i++) {
      ItemInstance item = stock.get(i);
      System.out.println("  [" + i + "] " + item.getName() + " (" + item.getItem().price() + " Gold)");
    }
  }

  private static boolean traiterCommandePrincipale(String input, Heros heros, MerchantRoom shop, Scanner sc) {
    switch (input) {
      case "Q" -> { return false; }
      case "V" -> menuVente(heros, sc);
      default -> {
        if (input.matches("\\d+")) {
          verifierEtLancerAchat(heros, shop, Integer.parseInt(input), sc);
        } else {
          System.out.println("Invalid command.");
        }
      }
    }
    return true;
  }

  private static void verifierEtLancerAchat(Heros heros, MerchantRoom shop, int index, Scanner sc) {
    if (index < 0 || index >= shop.stock().size()) {
      System.out.println("Unknown item.");
      return;
    }
    ItemInstance item = shop.stock().get(index);
    int price = item.getItem().price();
    if (heros.getGold() < price) {
      System.out.println("Not enough gold!");
      return;
    }
    executePurchase(heros, shop, item, index, sc);
  }

  private static void executePurchase(Heros heros, MerchantRoom shop, ItemInstance item, int idx, Scanner sc) {
    Position pos = lirePosition(sc, heros);
    if (pos != null && heros.getBackpack().add(item, pos)) {
      heros.payer(item.getItem().price());
      shop.stock().remove(idx);
      System.out.println("Purchase successful!");
    } else {
      System.out.println("Transaction failed (Invalid position or full).");
    }
  }

  private static void menuVente(Heros heros, Scanner scanner) {
    new View(heros.getBackpack(), new StuffFactory(), heros).printBackPack();
    var items = heros.getBackpack().getItems();
    System.out.println("\n-- SELL -- (X to Cancel)");
    for (int i = 0; i < items.size(); i++) {
      int sellPrice = items.get(i).getItem().price() / 2;
      System.out.println("  [" + i + "] " + items.get(i).getName() + " (+" + sellPrice + " Gold)");
    }
    System.out.print("> ");
    String input = scanner.next();
    if (input.matches("\\d+")) {
      effectuerVente(heros, Integer.parseInt(input));
    }
  }

  private static void effectuerVente(Heros heros, int idx) {
    var items = heros.getBackpack().getItems();
    if (idx >= 0 && idx < items.size()) {
      ItemInstance toSell = items.get(idx);
      heros.getBackpack().removeItem(toSell);
      heros.gagnerOr(toSell.getItem().price() / 2);
      System.out.println("Sold!");
    }
  }

  private static Position lirePosition(Scanner sc, Heros heros) {
    System.out.println("Enter position (row [space] col):");
    if (!sc.hasNextInt()) {
      sc.next(); 
      return null;
    }
    int row = sc.nextInt();
    int col = sc.hasNextInt() ? sc.nextInt() : -1;
    Position pos = new Position(row, col);
    return heros.getBackpack().isAvailable(pos) ? pos : null;
  }
}