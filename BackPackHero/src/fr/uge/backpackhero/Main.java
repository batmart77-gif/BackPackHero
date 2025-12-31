package fr.uge.backpackhero;

/**
 * Point d'entrée unique du projet.
 */
public class Main {
    public static void main(String[] args) {
      // Lancement du contrôleur terminal amélioré
      TerminalController controller = new TerminalController();
      controller.run();
    }
}