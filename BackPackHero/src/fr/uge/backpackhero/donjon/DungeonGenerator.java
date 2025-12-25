package fr.uge.backpackhero.donjon;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.uge.backpackhero.combat.BeeQueenBehavior;
import fr.uge.backpackhero.combat.EnemyBehavior;
import fr.uge.backpackhero.combat.FrogWizardBehavior;
import fr.uge.backpackhero.combat.LivingShadowBehavior;
import fr.uge.backpackhero.combat.RatLoupBehavior;
import fr.uge.backpackhero.entites.Ennemi;

import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Utility class responsible for generating the fixed dungeon structure used in Phase 1
 * (and providing randomized elements for Phase 2 combat encounters).
 */
public final class DungeonGenerator {
  /** Random number generator used for loot and enemy statistics. */
  private static final Random rdm = new Random();

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private DungeonGenerator() {
    
  }

  /**
   * Creates the complete dungeon structure consisting of 3 floors.
   *
   * @return The initialized {@link Dungeon} object.
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
   * Creates a rectangular floor with a predefined linear path and fixed room types.
   *
   * @param name           The name of the floor (e.g., "Niveau 1").
   * @param difficultyHp   Base HP/difficulty level for enemies on this floor.
   * @param row            The row index where the main corridor is placed.
   * @return The initialized {@link Floor} object.
   */
  private static Floor createFloor(String name, int difficultyHp, int row) {
    Room[][] map = new Room[5][11];

    // Ligne 0 : The main path rooms are placed on the specified row
    map[row][0] = new Corridor();
    map[row][1] = new EnemyRoom(createRandomEnemies(difficultyHp));
    map[row][2] = new TreasureRoom(createRandomLoot());
    map[row][3] = new EnemyRoom(createRandomEnemies(difficultyHp + 5));
    map[row][4] = new MerchantRoom(createShopStock());
    map[row][5] = new HealerRoom();
    map[row][6] = new EnemyRoom(createRandomEnemies(difficultyHp + 10));
    map[row][7] = new TreasureRoom(createRandomLoot());    
    map[row][8] = new ExitRoom();
    
    // The starting position (0, row) is passed to the Floor constructor
    return new Floor(map, 0, row);
  }
  
  
  private static Ennemi generateRandomEnemyType() {
    int type = rdm.nextInt(5);
    return switch (type) {
        case 0 -> new Ennemi(32, 6, new RatLoupBehavior(7, 9, 14, 14)); // Petit Rat-loup
        case 1 -> new Ennemi(45, 6, new RatLoupBehavior(7, 9, 13, 16)); // Ratwolf
        case 2 -> new Ennemi(45, 8, new FrogWizardBehavior());         // Sorcier-grenouille
        case 3 -> new Ennemi(50, 25, new LivingShadowBehavior());      // Ombre vivante
        default -> new Ennemi(74, 20, new BeeQueenBehavior());         // Reine des abeilles
    };
  }
  
  private static List<Ennemi> createRandomEnemies(int difficultyBase) {
    List<Ennemi> enemies = new ArrayList<>();
    int count = (difficultyBase > 5 && rdm.nextBoolean()) ? 2 : 1; 
    
    for (int i = 0; i < count; i++) {
        enemies.add(generateRandomEnemyType());
    }
    
    if (difficultyBase > 10 && enemies.size() == 1) {
        return List.of(new Ennemi(74, 20, new BeeQueenBehavior()));
    }
    return enemies;
}

  /**
   * Creates a random selection of loot items (1 or 2 items) by picking from the {@link Stuff} enum.
   *
   * @return A list of {@link ItemInstance} representing the loot.
   */
  private static List<ItemInstance> createRandomLoot() {
    StuffFactory factory = new StuffFactory();
    List<ItemInstance> loot = new ArrayList<>();    
    // Decide number of items (1 or 2)
    int nbItems = 1 + rdm.nextInt(2);    
    // Get all possible items
    Stuff[] allStuff = Stuff.values();    
    for (int i = 0; i < nbItems; i++) {
      // Randomly select an item
      int randomIndex = rdm.nextInt(allStuff.length);
      Stuff randomStuff = allStuff[randomIndex];      
      loot.add(new ItemInstance(factory.create(randomStuff)));
    }    
    return loot;
  }
  
  /**
   * Creates a stock list for a merchant, generating 3 random items while ensuring no {@link Stuff#Curse} items are included.
   *
   * @return A list of {@link ItemInstance} representing the merchant's stock.
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
        // Reroll the item if it's a Curse
        i--; 
      }
    }
    return stock;
  }
}