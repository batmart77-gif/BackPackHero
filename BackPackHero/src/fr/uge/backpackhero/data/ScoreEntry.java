package fr.uge.backpackhero.data;

import java.util.Objects;

/**
 * Représente une entrée dans le Hall of Fame.
 */
public record ScoreEntry(String playerName, int score) implements Comparable<ScoreEntry> {
  public ScoreEntry {
    Objects.requireNonNull(playerName);
  }

  @Override
  public int compareTo(ScoreEntry other) {
    return Integer.compare(other.score, this.score);
  }
    
  @Override
  public String toString() {
    return playerName + " : " + score + " points";
  }
}