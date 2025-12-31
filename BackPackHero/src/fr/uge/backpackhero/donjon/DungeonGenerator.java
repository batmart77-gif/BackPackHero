package fr.uge.backpackhero.donjon;

import java.util.*;
import fr.uge.backpackhero.combat.*;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Stuff;
import fr.uge.backpackhero.item.StuffFactory;


public final class DungeonGenerator {
  private static final Random RDM = new Random();

  private DungeonGenerator() { }

  /**
   * Crée le donjon final composé de 3 étages
   * La difficulté augmente à chaque étage (15, 30, 45)
   */
  public static Dungeon createDungeonPhase3() {
    List<Floor> floors = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      floors.add(generateRandomFloor(15 + (i * 15)));
    }
    return new Dungeon(floors);
  }

  /**
   * Génère un étage de taille 5x11.
   * Utilise une approche par "croissance" pour garantir que toutes les salles sont reliées.
   */
  public static Floor generateRandomFloor(int difficulty) {
    // Initialisation de la grille vide (toutes les cases sont 'null')
    Room[][] map = new Room[5][11];
    List<PositionInDungeon> positions = generateConnectedPositions(20);
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
   * Algorithme de génération de chemin connexe.
   * On part d'un point et on ajoute des voisins au hasard jusqu'à atteindre 'count'.
   */
  private static List<PositionInDungeon> generateConnectedPositions(int count) {
    List<PositionInDungeon> path = new ArrayList<>();
    path.add(new PositionInDungeon(RDM.nextInt(5), RDM.nextInt(11)));
    while (path.size() < count) {
      var current = path.get(RDM.nextInt(path.size()));
      var next = getRandomNeighbor(current);
      // Si le voisin est dans la grille et n'est pas déjà dans le chemin, on l'ajoute
      if (isInside(next) && !path.contains(next)) {
        path.add(next);
      }
    }
    return path;
  }

  /**
   * Assigne les types de salles obligatoires aux positions sélectionnées.
   */
  private static void placeSpecialRooms(Room[][] map, List<PositionInDungeon> pos, int diff) {
    // On prend les positions 1 à 10 de la liste mélangée pour placer les éléments fixes :
    map[pos.get(1).row()][pos.get(1).col()] = new ExitRoom(); // La sortie de l'étage
    
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

  /**
   * Retourne un voisin adjacent (Haut, Bas, Gauche, Droite) de manière aléatoire.
   */
  private static PositionInDungeon getRandomNeighbor(PositionInDungeon p) {
    int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}}; // Les 4 directions possibles
    int[] d = dirs[RDM.nextInt(4)];
    return new PositionInDungeon(p.row() + d[0], p.col() + d[1]);
  }

  /**
   * Vérifie que la position ne sort pas de la grille 5x11 demandée.
   */
  private static boolean isInside(PositionInDungeon p) {
    return p.row() >= 0 && p.row() < 5 && p.col() >= 0 && p.col() < 11;
  }
  
  /*
  private static Ennemi generateRandomEnemyType() {
    int type = RDM.nextInt(5);
    return switch (type) {
        case 0 -> new Ennemi(32, 6, new RatLoupBehavior(7, 9, 14, 14)); // Petit Rat-loup
        case 1 -> new Ennemi(45, 6, new RatLoupBehavior(7, 9, 13, 16)); // Ratwolf
        case 2 -> new Ennemi(45, 8, new FrogWizardBehavior());         // Sorcier-grenouille
        case 3 -> new Ennemi(50, 25, new LivingShadowBehavior());      // Ombre vivante
        default -> new Ennemi(74, 20, new BeeQueenBehavior());         // Reine des abeilles
    };
  }
  */
  private static Ennemi generateRandomEnemyType() {
    int type = RDM.nextInt(5);
    return switch (type) {
        // PV baissés de 32 -> 15 
        case 0 -> new Ennemi(15, 6, new RatLoupBehavior(2, 4, 2, 2)); 
        // PV baissés de 45 -> 20
        case 1 -> new Ennemi(20, 6, new RatLoupBehavior(3, 5, 3, 5)); 
        // PV baissés de 45 -> 25
        case 2 -> new Ennemi(25, 8, new FrogWizardBehavior());         
        // PV baissés de 50 -> 30
        case 3 -> new Ennemi(30, 25, new LivingShadowBehavior());      
        // PV baissés de 74 -> 40
        default -> new Ennemi(40, 20, new BeeQueenBehavior());         
    };
}
  /*
  private static List<Ennemi> createRandomEnemies(int difficultyBase) {
    List<Ennemi> enemies = new ArrayList<>();
    int count = (difficultyBase > 5 && RDM.nextBoolean()) ? 2 : 1; 
    
    for (int i = 0; i < count; i++) {
        enemies.add(generateRandomEnemyType());
    }
    
    if (difficultyBase > 10 && enemies.size() == 1) {
        return List.of(new Ennemi(74, 20, new BeeQueenBehavior()));
    }
    return enemies;
  }*/
  
  private static List<Ennemi> createRandomEnemies(int difficultyBase) {
    List<Ennemi> enemies = new ArrayList<>();
    // On ne met 2 ennemis que si la difficulté est très haute (> 40 au lieu de 5)
    int count = (difficultyBase > 40 && RDM.nextBoolean()) ? 2 : 1; 
    
    for (int i = 0; i < count; i++) {
        enemies.add(generateRandomEnemyType());
    }
    
    // On ne force la Reine (Boss) que si diff > 50 (au lieu de 10)
    if (difficultyBase > 50 && enemies.size() == 1) {
        return List.of(new Ennemi(40, 20, new BeeQueenBehavior()));
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
    int nbItems = 1 + RDM.nextInt(2);    
    // Get all possible items
    Stuff[] allStuff = Stuff.values();    
    for (int i = 0; i < nbItems; i++) {
      // Randomly select an item
      int randomIndex = RDM.nextInt(allStuff.length);
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
      Stuff randomStuff = allStuff[RDM.nextInt(allStuff.length)];
      if (randomStuff != Stuff.Curse) {
        stock.add(new ItemInstance(factory.create(randomStuff)));
      } else {
        i--; 
      }
    }
    return stock;
  }
}
