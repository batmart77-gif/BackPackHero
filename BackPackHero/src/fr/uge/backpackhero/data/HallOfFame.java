package fr.uge.backpackhero.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Gère l'enregistrement et l'affichage des 3 meilleurs scores.
 */
public final class HallOfFame {
  private static final Path FAME_FILE = Path.of("hall_of_fame.txt"); // Utilisation de Path

  /**
   * Enregistre un nouveau score et ne conserve que le top 3 dans le fichier.
   * @throws IOException si une erreur survient lors de l'accès au fichier
   */
  public void recordScore(String name, int score) throws IOException {
    Objects.requireNonNull(name);
    var scores = loadScores();
    scores.add(name + ":" + score);
    var top3 = scores.stream()
            .map(line -> line.split(":"))
            .sorted((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])))
            .limit(3)
            .map(parts -> parts[0] + ":" + parts[1])
            .collect(Collectors.toList());
    Files.write(FAME_FILE, top3);
  }

  /**
   * Charge la liste des scores depuis le fichier
   */
  public List<String> loadScores() throws IOException {
    if (!Files.exists(FAME_FILE)) {
      return new ArrayList<>();
    }
    return Files.readAllLines(FAME_FILE);
  }
}

/*
public void onGameOver(Heros heros, String playerName) {
    Objects.requireNonNull(heros);
    int finalScore = heros.calculateScore();
    var hof = new HallOfFame();
    
    try {
        hof.recordScore(playerName, finalScore);
        System.out.println("--- HALL OF FAME (TOP 3) ---");
        hof.loadScores().forEach(line -> System.out.println(line.replace(":", " - ")));
    } catch (IOException e) {
        // Report de l'erreur à l'utilisateur comme suggéré au cours [cite: 416]
        System.err.println("Erreur technique : impossible d'accéder au Hall of Fame.");
    }
}
*/