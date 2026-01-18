package fr.uge.backpackhero.data;

import java.io.IOException;
import java.io.UncheckedIOException;
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
   * Records a new score and updates the top 3 rankings.
   *
   * @param entry the non-null score entry to record.
   * @throws IOException if the score file cannot be written.
   */
  public void recordScore(ScoreEntry entry) {
    Objects.requireNonNull(entry);
    try {
      List<ScoreEntry> allScores = loadScores();
      allScores.add(entry);      
      List<String> top3 = allScores.stream()
          .sorted()
          .limit(3)
          .map(e -> e.playerName() + ":" + e.score())
          .collect(Collectors.toList());

      Files.write(FAME_FILE, top3);
      
    } catch (IOException e) {
        throw new UncheckedIOException(e); 
    }
  }
  

  /**
   * Loads the current list of scores as ScoreEntry objects.
   *
   * @return a list of ScoreEntry objects.
   * @throws IOException if the file cannot be read.
   */
  public List<ScoreEntry> loadScores() throws IOException {
    if (!Files.exists(FAME_FILE)) {
      return new ArrayList<>();
    }
    return Files.readAllLines(FAME_FILE).stream()
        .map(line -> line.split(":"))
        .filter(parts -> parts.length == 2)
        .map(parts -> new ScoreEntry(parts[0], Integer.parseInt(parts[1])))
        .collect(Collectors.toCollection(ArrayList::new));
  }
}