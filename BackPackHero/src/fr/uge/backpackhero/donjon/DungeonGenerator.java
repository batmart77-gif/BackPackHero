package fr.uge.backpackhero.donjon;

import java.util.List;

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

  private DungeonGenerator() {}

  /**
   * Crée le donjon complet (3 étages).
   */
  public static Dungeon createDungeonPhase1() {
    List<Floor> floors = List.of(
      createFloor("Niveau 1 ", 15),
      createFloor("Niveau 2 ", 25),
      createFloor("Niveau 3 ", 40)
    );
    return new Dungeon(floors);
  }

  /**
   * Crée un étage rectangulaire avec un chemin prédéfini.
   */
  private static Floor createFloor(String name, int difficultyHp) {
    Room[][] map = new Room[5][11];

    // Ligne 0 : Le chemin principal
    map[0][0] = new Corridor();
    map[0][1] = new EnemyRoom(createEnemies(difficultyHp));
    map[0][2] = new TreasureRoom(createLoot()); // Butin généré via Factory
    map[0][3] = new EnemyRoom(createEnemies(difficultyHp + 5));
    map[0][4] = new MerchantRoom();
    map[0][5] = new HealerRoom();
    map[0][6] = new EnemyRoom(createEnemies(difficultyHp + 10));
    map[0][7] = new TreasureRoom(createLoot()); // Autre butin
    map[0][8] = new ExitRoom();

    System.out.println("Génération : " + name + " prêt.");
    return new Floor(map);
  }


  private static List<Ennemi> createEnemies(int hp) {
    EnemyBehavior behavior = new RatLoupBehavior();
    return List.of(new Ennemi(hp, behavior));
  }

  /**
   * Crée le butin des coffres 
   * Retourne une liste d'ItemInstance.
   */
  private static List<ItemInstance> createLoot() {
    StuffFactory factory = new StuffFactory();
    return List.of(
      new ItemInstance(factory.create(Stuff.ManaStone)),
      new ItemInstance(factory.create(Stuff.WoodSword))
    );
  }
}