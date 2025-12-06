package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.uge.backpackhero.combat.EnemyBehavior;
import fr.uge.backpackhero.combat.RatLoupBehavior;
import fr.uge.backpackhero.entites.Ennemi;

import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Classe utilitaire pour générer le donjon fixe de la Phase 1.
 */
public final class DungeonGenerator {
  private static final Random rdm = new Random();

  private DungeonGenerator() {
    
  }

  /**
   * Crée le donjon complet (3 étages).
   */
  public static Dungeon createDungeonPhase1() {
    List<Floor> floors = List.of(
      createFloor("Niveau 1 ", 15, 0),
      createFloor("Niveau 2 ", 30, 1),
      createFloor("Niveau 3 ", 50, 2)
    );
    return new Dungeon(floors);
  }
  
  /**
   * Crée un étage rectangulaire avec un chemin prédéfini.
   */
  private static Floor createFloor(String name, int difficultyHp, int row) {
    Room[][] map = new Room[5][11];

    // Ligne 0 : Le chemin principal
    map[row][0] = new Corridor();
    map[row][1] = new EnemyRoom(createRandomEnemies(difficultyHp));
    map[row][2] = new TreasureRoom(createRandomLoot());
    map[row][3] = new EnemyRoom(createRandomEnemies(difficultyHp + 5));
    map[row][4] = new MerchantRoom(createShopStock());
    map[row][5] = new HealerRoom();
    map[row][6] = new EnemyRoom(createRandomEnemies(difficultyHp + 10));
    map[row][7] = new TreasureRoom(createRandomLoot());    
    map[row][8] = new ExitRoom();
    return new Floor(map, 0, row);
  }
  
  private static List<Ennemi> createRandomEnemies(int difficultyBase) {
    EnemyBehavior behavior = new RatLoupBehavior();
    List<Ennemi> enemies = new ArrayList<>();
    
    // 50% de chance : 1 Gros Ennemi OU 2 Petits Ennemis
    if (rdm.nextBoolean()) {
      // Cas A : 1 Gros Ennemi
      // PV = difficulté +/- 3
      int hp = difficultyBase + (rdm.nextInt(7) - 3);
      hp = Math.max(5, hp); // Sécurité minimum 5 PV
      int xp = hp / 2;      // XP calculée selon les PV    
      enemies.add(new Ennemi(hp, xp, behavior));
    } else {
      // Cas B : 2 Petits Ennemis
      int hp1 = (difficultyBase / 2) + (rdm.nextInt(3) - 1);
      int hp2 = (difficultyBase / 2) + (rdm.nextInt(3) - 1);     
      hp1 = Math.max(1, hp1);
      hp2 = Math.max(1, hp2);     
      enemies.add(new Ennemi(hp1, hp1 / 2, behavior));
      enemies.add(new Ennemi(hp2, hp2 / 2, behavior));
    }
    return enemies;
  }

  /**
   * Crée un butin aléatoire en piochant dans la liste Stuff.
   */
  private static List<ItemInstance> createRandomLoot() {
    StuffFactory factory = new StuffFactory();
    List<ItemInstance> loot = new ArrayList<>();   
    // On décide combien d'objets (1 ou 2)
    int nbItems = 1 + rdm.nextInt(2);   
    // On récupère tous les objets possibles
    Stuff[] allStuff = Stuff.values();   
    for (int i = 0; i < nbItems; i++) {
      // Tirage au sort d'un objet
      int randomIndex = rdm.nextInt(allStuff.length);
      Stuff randomStuff = allStuff[randomIndex];  
      loot.add(new ItemInstance(factory.create(randomStuff)));
    }    
    return loot;
  }
  
  /**
   * Crée le stock du marchand (3 objets aléatoires).
   */
  private static List<ItemInstance> createShopStock() {
    StuffFactory factory = new StuffFactory();
    List<ItemInstance> stock = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      Stuff[] allStuff = Stuff.values();
      Stuff randomStuff = allStuff[rdm.nextInt(allStuff.length)];
      if (randomStuff != Stuff.Curse) {
        stock.add(new ItemInstance(factory.create(randomStuff)));
      } else {
        // On recommence le tirage
        i--; 
      }
    }
    return stock;
  }
}