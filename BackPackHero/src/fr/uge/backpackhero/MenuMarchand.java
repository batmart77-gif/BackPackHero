package fr.uge.backpackhero;

import java.util.Scanner;
import java.util.List;

import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.View;

/**
 * Handles the interaction logic between the player (Hero) and the Merchant in a console-based interface.
 * This class manages buying and selling items within a specific MerchantRoom.
 */
public class MenuMarchand {

  /**
   * Opens the merchant interaction menu.
   * This method starts the main loop for the shop interface, allowing the player to buy, sell, or leave.
   *
   * @param heros   The hero interacting with the merchant.
   * @param shop    The merchant room containing the stock of items.
   * @param scanner The scanner used to read user input.
   */
  public static void ouvrir(Heros heros, MerchantRoom shop, Scanner scanner) {
    boolean shopping = true;
    System.out.println("Des affaires en or pour un héros en or !");
    
    while (shopping) {
      afficherInterfaceGlobale(heros, shop);
      String input = scanner.next().toUpperCase();
      shopping = traiterCommandePrincipale(input, heros, shop, scanner);
    }
    System.out.println("Au revoir !");
  }

  // --- AFFICHAGE ---

  /**
   * Displays the global interface of the shop, including the hero's gold, backpack, stock, and actions.
   *
   * @param heros The hero whose information is displayed.
   * @param shop  The shop whose stock is displayed.
   */
  private static void afficherInterfaceGlobale(Heros heros, MerchantRoom shop) {
    System.out.println("\n--- BOUTIQUE --- (Solde: " + heros.getGold() + " Or)");
    System.out.println("VOTRE SAC :");
    View.printBackPack(heros.getBackpack());
    afficherStock(shop.stock());
    afficherActions();
  }

  /**
   * Displays the list of items available for purchase in the shop.
   *
   * @param stock The list of items currently in stock.
   */
  private static void afficherStock(List<ItemInstance> stock) {
    System.out.println("OBJETS :");
    if (stock.isEmpty()) {
      System.out.println("  (Rupture de stock !)");
      return;
    }
    for (int i = 0; i < stock.size(); i++) {
      ItemInstance item = stock.get(i);
      System.out.println("  [" + i + "] " + item.getName() + " (" + item.getItem().price() + " Or)");
    }
  }

  /**
   * Displays the available actions for the player in the main menu.
   */
  private static void afficherActions() {
    System.out.println("\nACTIONS : [Numéro] Acheter | [V] Vendre | [Q] Quitter");
    System.out.print("> ");
  }

  // --- LOGIQUE PRINCIPALE ---

  /**
   * Processes the main command entered by the user.
   *
   * @param input   The user input string.
   * @param heros   The hero.
   * @param shop    The merchant room.
   * @param sc      The scanner for further input if needed.
   * @return {@code true} if the shopping session should continue, {@code false} if the user wants to quit.
   */
  private static boolean traiterCommandePrincipale(String input, Heros heros, MerchantRoom shop, Scanner sc) {
    if (input.equals("Q")) return false;
    
    if (input.equals("V")) {
      menuVente(heros, sc);
      return true;
    }
    
    try {
      int index = Integer.parseInt(input);
      verifierEtLancerAchat(heros, shop, index, sc);
    } catch (NumberFormatException e) {
      System.out.println("Commande invalide.");
    }
    return true;
  }

  // --- LOGIQUE ACHAT ---

  /**
   * Verifies if the purchase is possible (valid index, enough gold) and proceeds to the next step.
   *
   * @param heros The hero attempting to buy.
   * @param shop  The shop.
   * @param index The index of the item to buy.
   * @param sc    The scanner for input.
   */
  private static void verifierEtLancerAchat(Heros heros, MerchantRoom shop, int index, Scanner sc) {
    if (index < 0 || index >= shop.stock().size()) {
      System.out.println("Article inconnu.");
      return;
    }
    ItemInstance item = shop.stock().get(index);
    if (heros.getGold() < item.getItem().price()) {
      System.out.println("Pas assez d'or !");
      return;
    }
    finaliserAchat(heros, shop, item, index, sc);
  }

  /**
   * Finalizes the purchase by asking for placement coordinates and deducting gold.
   *
   * @param heros The hero buying the item.
   * @param shop  The shop.
   * @param item  The item being bought.
   * @param index The index of the item in the shop's stock.
   * @param sc    The scanner for coordinates input.
   */
  private static void finaliserAchat(Heros heros, MerchantRoom shop, ItemInstance item, int index, Scanner sc) {
    System.out.println("Placer " + item.getName() + " : Ligne Colonne ? > ");
    try {
      int r = sc.nextInt();
      int c = sc.nextInt();
      if (heros.getBackpack().add(item, new Position(r, c))) {
        heros.payer(item.getItem().price());
        shop.stock().remove(index);
        System.out.println("Achat confirmé !");
      } else {
        System.out.println("Pas de place ici.");
      }
    } catch (Exception e) {
      System.out.println("Erreur de saisie.");
      sc.nextLine(); // Vider buffer
    }
  }

  // --- LOGIQUE VENTE ---

  /**
   * Displays and handles the selling menu.
   *
   * @param heros   The hero wanting to sell items.
   * @param scanner The scanner for user input.
   */
  private static void menuVente(Heros heros, Scanner scanner) {
    afficherListeVente(heros);
    String input = scanner.next();
    
    if (input.equalsIgnoreCase("X")) return;

    try {
      int idx = Integer.parseInt(input);
      effectuerVente(heros, idx);
    } catch (Exception e) { 
      System.out.println("Erreur."); 
    }
  }

  /**
   * Displays the list of items in the hero's backpack that can be sold.
   *
   * @param heros The hero.
   */
  private static void afficherListeVente(Heros heros) {
    System.out.println("\n-- VENTE -- (X pour Annuler)");
    View.printBackPack(heros.getBackpack()); 
    var items = heros.getBackpack().getItems();
    for (int i = 0; i < items.size(); i++) {
      int prixVente = items.get(i).getItem().price() / 2;
      System.out.println("  [" + i + "] " + items.get(i).getName() + " (+" + prixVente + " Or)");
    }
    System.out.print("> ");
  }

  /**
   * Executes the sale of a specific item from the backpack.
   *
   * @param heros The hero selling the item.
   * @param idx   The index of the item in the backpack list.
   */
  private static void effectuerVente(Heros heros, int idx) {
    var items = heros.getBackpack().getItems();
    if (idx >= 0 && idx < items.size()) {
      ItemInstance toSell = items.get(idx);
      int gain = toSell.getItem().price() / 2;
      heros.getBackpack().removeItem(toSell);
      heros.gagnerOr(gain);
      System.out.println("Vendu !");
    } else {
      System.out.println("Numéro invalide.");
    }
  }
}