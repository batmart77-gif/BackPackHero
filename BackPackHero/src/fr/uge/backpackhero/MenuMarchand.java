package fr.uge.backpackhero;

import java.util.Scanner;
import java.util.List;

import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.item.View;

public class MenuMarchand {

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

  private static void afficherInterfaceGlobale(Heros heros, MerchantRoom shop) {
    System.out.println("\n--- BOUTIQUE --- (Solde: " + heros.getGold() + " Or)");
    System.out.println("VOTRE SAC :");
    View.printBackPack(heros.getBackpack());
    afficherStock(shop.stock());
    afficherActions();
  }

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

  private static void afficherActions() {
    System.out.println("\nACTIONS : [Numéro] Acheter | [V] Vendre | [Q] Quitter");
    System.out.print("> ");
  }

  // --- LOGIQUE PRINCIPALE ---

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