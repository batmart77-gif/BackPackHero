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
import fr.uge.backpackhero.graphics.GraphicController;

public class Main {
    public static void main(String[] args) {
        Heros heros = new Heros();
        Dungeon dungeon = DungeonGenerator.createDungeonPhase3();
        View view = new View(heros.getBackpack(), new StuffFactory(), heros);
        Jeu jeu = new Jeu(heros, dungeon, view);
        GraphicController graphicController = new GraphicController(jeu);
        System.out.println("Lancement de Backpack Hero (Version Graphique)...");
        graphicController.run();
    }
}