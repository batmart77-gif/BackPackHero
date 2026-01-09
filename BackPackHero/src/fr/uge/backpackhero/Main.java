/*
package fr.uge.backpackhero;


public class Main {
    public static void main(String[] args) {
      TerminalController controller = new TerminalController();
      controller.run();
    }
}
*/


package fr.uge.backpackhero;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.donjon.DungeonGenerator;
import fr.uge.backpackhero.donjon.Dungeon;
import fr.uge.backpackhero.item.View;
import fr.uge.backpackhero.item.StuffFactory;
import fr.uge.backpackhero.item.Item;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import fr.uge.backpackhero.graphics.GraphicEngine;

public class Main {
    public static void main(String[] args) {
        // 1. Initialisation du héros avec 40 PV
        Heros heros = new Heros();
        
        // 2. Génération du donjon
        Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
        
        // 3. Préparation de la fabrique d'objets
        StuffFactory factory = new StuffFactory();
        
        // 4. Création de la vue logique
        View view = new View(heros.getBackpack(), factory, heros);
        
        // 5. Initialisation du modèle logique du jeu
        Jeu jeu = new Jeu(heros, dungeon, view);

        // --- SECTION TEST : AJOUT D'UN OBJET DANS LE SAC ---
        // On récupère le modèle de l'épée
        Item swordTemplate = factory.getItem("Wood Sword");
        
        // On crée l'instance physique pour le sac
        ItemInstance swordInstance = new ItemInstance(swordTemplate);
        
        // On l'ajoute à la position (0,0) du sac
        heros.getBackpack().add(swordInstance, new Position(0, 0));
        // ---------------------------------------------------

        // 6. Lancement de l'interface graphique
        GraphicEngine engine = new GraphicEngine(jeu);
        
        System.out.println("Lancement de Backpack Hero (Version Graphique)...");
        engine.start();
        
    }
}