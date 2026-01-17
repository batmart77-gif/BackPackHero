package fr.uge.backpackhero.donjon;

import java.util.*;
import fr.uge.backpackhero.combat.*;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Utility class responsible for generating randomized dungeon structures.
 * It manages the creation of floors, room placement, and content generation.
 */
public final class DungeonGenerator {
  private static final Random RDM = new Random();
  private static final int MAP_ROWS = 5;
  private static final int MAP_COLS = 11;
  private static final int POSITIONS_COUNT = 20;

  private DungeonGenerator() { }

  /**
   * Creates a complete dungeon consisting of 3 floors with increasing difficulty.
   * @return A new Dungeon instance.
   */
  public static Dungeon createDungeonPhase3() {
    List<Floor> floors = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      floors.add(generateRandomFloor(15 + (i * 15)));
    }
    return new Dungeon(floors);
  }

  /**
   * Generates a single floor with connected rooms based on difficulty.
   * @param difficulty The difficulty level to determine enemy scaling.
   * @return A randomized Floor object.
   */
  public static Floor generateRandomFloor(int difficulty) {
    Room[][] map = new Room[MAP_ROWS][MAP_COLS];
    List<PositionInDungeon> positions = generateConnectedPositions(POSITIONS_COUNT);
    Collections.shuffle(positions);
    placeSpecialRooms(map, positions, difficulty);
    for (var pos : positions) {
      if (map[pos.row()][pos.col()] == null) {
        map[pos.row()][pos.col()] = new Corridor();
      }
    }
    return new Floor(map, positions.get(0).col(), positions.get(0).row());
  }

  /**
   * Creates a connected path of coordinates using a growth algorithm.
   * @param count The number of rooms to generate.
   * @return A list of unique connected positions.
   */
  private static List<PositionInDungeon> generateConnectedPositions(int count) {
    List<PositionInDungeon> path = new ArrayList<>();
    path.add(new PositionInDungeon(RDM.nextInt(MAP_ROWS), RDM.nextInt(MAP_COLS)));
    while (path.size() < count) {
      var current = path.get(RDM.nextInt(path.size()));
      var next = getRandomNeighbor(current);
      if (isInside(next) && !path.contains(next)) {
        path.add(next);
      }
    }
    return path;
  }

  /**
   * Places mandatory special rooms (Exit, Merchant, Healer, etc.) at specific positions.
   * @param map The room grid.
   * @param pos The available connected positions.
   * @param diff The difficulty for enemy generation.
   */
  private static void placeSpecialRooms(Room[][] map, List<PositionInDungeon> pos, int diff) {
    Objects.requireNonNull(map);
    Objects.requireNonNull(pos);
    map[pos.get(1).row()][pos.get(1).col()] = new ExitRoom();
    map[pos.get(2).row()][pos.get(2).col()] = new EnemyRoom(createRandomEnemies(diff));
    map[pos.get(3).row()][pos.get(3).col()] = new EnemyRoom(createRandomEnemies(diff + 5));
    map[pos.get(4).row()][pos.get(4).col()] = new EnemyRoom(createRandomEnemies(diff + 10));
    map[pos.get(5).row()][pos.get(5).col()] = new MerchantRoom(createShopStock());
    map[pos.get(6).row()][pos.get(6).col()] = new HealerRoom();
    map[pos.get(7).row()][pos.get(7).col()] = new TreasureRoom(createRandomLoot());
    map[pos.get(8).row()][pos.get(8).col()] = new TreasureRoom(createRandomLoot());
    map[pos.get(9).row()][pos.get(9).col()] = new EventRoom("Surprise");
    map[pos.get(10).row()][pos.get(10).col()] = new GateRoom(new TreasureRoom(createRandomLoot()));
  }

  private static PositionInDungeon getRandomNeighbor(PositionInDungeon p) {
    int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
    int[] d = dirs[RDM.nextInt(4)];
    return new PositionInDungeon(p.row() + d[0], p.col() + d[1]);
  }

  private static boolean isInside(PositionInDungeon p) {
    return p.row() >= 0 && p.row() < MAP_ROWS && p.col() >= 0 && p.col() < MAP_COLS;
  }

  private static Ennemi generateRandomEnemyType() {
    return switch (RDM.nextInt(5)) {
      case 0 -> new Ennemi("ratloup", 15, 6, new RatLoupBehavior(2, 4, 2, 2));
      case 1 -> new Ennemi("ratloup", 20, 6, new RatLoupBehavior(3, 5, 3, 5));
      case 2 -> new Ennemi("frogwizard", 25, 8, new FrogWizardBehavior());
      case 3 -> new Ennemi("Living_Shadow", 30, 25, new LivingShadowBehavior());
      default -> new Ennemi("beequeen", 40, 20, new BeeQueenBehavior());
    };
  }

  private static List<Ennemi> createRandomEnemies(int difficultyBase) {
    List<Ennemi> enemies = new ArrayList<>();
    int count = (difficultyBase > 40 && RDM.nextBoolean()) ? 2 : 1;
    for (int i = 0; i < count; i++) {
      enemies.add(generateRandomEnemyType());
    }
    if (difficultyBase > 50 && enemies.size() == 1) {
      return List.of(new Ennemi("beequeen", 40, 20, new BeeQueenBehavior()));
    }
    return enemies;
  }

  private static List<ItemInstance> createRandomLoot() {
    StuffFactory factory = new StuffFactory();
    List<ItemInstance> loot = new ArrayList<>();
    int nbItems = 1 + RDM.nextInt(2);
    Stuff[] allStuff = Stuff.values();
    for (int i = 0; i < nbItems; i++) {
      Stuff randomStuff = allStuff[RDM.nextInt(allStuff.length)];
      loot.add(new ItemInstance(factory.create(randomStuff)));
    }
    return loot;
  }

  private static List<ItemInstance> createShopStock() {
    StuffFactory factory = new StuffFactory();
    List<ItemInstance> stock = new ArrayList<>();
    Stuff[] allStuff = Stuff.values();
    while (stock.size() < 3) {
      Stuff randomStuff = allStuff[RDM.nextInt(allStuff.length)];
      if (randomStuff != Stuff.Curse) {
        stock.add(new ItemInstance(factory.create(randomStuff)));
      }
    }
    return stock;
  }
}