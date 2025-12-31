package fr.uge.backpackhero.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class HallOfFame {
    // Utilisation de java.nio.file.Path conformément au cours
    private static final Path FAME_FILE = Path.of("hall_of_fame.txt");

    /**
     * Enregistre un nouveau score et ne garde que les 3 meilleurs.
     */
    public void saveScore(String name, int score) throws IOException {
        Objects.requireNonNull(name);
        List<String> lines = loadRawLines();
        lines.add(name + ":" + score);
        
        // Tri décroissant sur le score et limitation au Top 3
        List<String> top3 = lines.stream()
                .map(line -> line.split(":"))
                .filter(parts -> parts.length == 2)
                .sorted((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])))
                .limit(3)
                .map(parts -> parts[0] + ":" + parts[1])
                .collect(Collectors.toList());

        // Écriture NIO
        Files.write(FAME_FILE, top3);
    }

    private List<String> loadRawLines() throws IOException {
        if (!Files.exists(FAME_FILE)) return new ArrayList<>();
        return Files.readAllLines(FAME_FILE);
    }

    /**
     * Affiche le Hall of Fame dans la console.
     */
    public void display() {
        try {
            System.out.println("\n--- HALL OF FAME (TOP 3) ---");
            List<String> scores = loadRawLines();
            if (scores.isEmpty()) System.out.println("Aucun score enregistré.");
            else scores.forEach(s -> System.out.println(s.replace(":", " - ") + " pts"));
        } catch (IOException e) {
            System.err.println("Erreur technique lors de la lecture du Hall of Fame.");
        }
    }
}


/*
package fr.uge.backpackhero.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public final class HallOfFame {
  private static final Path FAME_FILE = Path.of("hall_of_fame.txt"); // Utilisation de Path

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

 
  public List<String> loadScores() throws IOException {
    if (!Files.exists(FAME_FILE)) {
      return new ArrayList<>();
    }
    return Files.readAllLines(FAME_FILE);
  }
  
  
  public void display() {
      try {
          System.out.println("\n--- HALL OF FAME (TOP 3) ---");
          loadScores().forEach(l -> System.out.println(l.replace(":", " - ")));
      } catch (IOException e) {
          System.err.println("Impossible de lire le Hall of Fame."); 
      }
  }
}
*/
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