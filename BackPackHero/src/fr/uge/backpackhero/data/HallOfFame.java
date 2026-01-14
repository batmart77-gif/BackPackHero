package fr.uge.backpackhero.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manages the high scores for the game.
 * Stores and displays the top 3 best performances in a persistent file.
 */
public final class HallOfFame {
  private static final Path FAME_FILE = Path.of("hall_of_fame.txt");

  /**
   * Records a new player score and updates the top 3 rankings.
   *
   * @param name  the non-null name of the player.
   * @param score the numerical score achieved.
   * @throws IOException if the score file cannot be written.
   * @throws NullPointerException if name is null.
   */
  public void recordScore(String name, int score) throws IOException {
    Objects.requireNonNull(name);
    List<String> scores = loadScores();
    scores.add(name + ":" + score);
    
    List<String> top3 = scores.stream()
        .map(line -> line.split(":"))
        .filter(parts -> parts.length == 2)
        .sorted((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])))
        .limit(3)
        .map(parts -> parts[0] + ":" + parts[1])
        .collect(Collectors.toList());

    Files.write(FAME_FILE, top3);
  }

  /**
   * Loads the current list of scores from the data file.
   *
   * @return a non-null list of raw score strings.
   * @throws IOException if the file exists but cannot be read.
   */
  public List<String> loadScores() throws IOException {
    if (!Files.exists(FAME_FILE)) {
      return new ArrayList<>();
    }
    return Files.readAllLines(FAME_FILE);
  }

  /**
   * Prints the Hall of Fame top scores to the standard output.
   *
   * @throws IOException if the scores cannot be retrieved.
   */
  public void display() throws IOException {
    System.out.println("\n--- HALL OF FAME (TOP 3) ---");
    List<String> scores = loadScores();
    if (scores.isEmpty()) {
      System.out.println("No scores recorded yet.");
    } else {
      scores.forEach(l -> System.out.println(l.replace(":", " - ") + " pts"));
    }
  }
}