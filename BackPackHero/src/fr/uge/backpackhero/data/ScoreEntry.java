package fr.uge.backpackhero.data;

import java.util.Objects;

/**
 * Represents a single entry in the Hall of Fame.
 * This record stores the player's name and their final calculated score.
 * It implements Comparable to allow easy sorting for high scores.
 *
 * @param playerName the non-null name of the player.
 * @param score      the final score achieved.
 */
public record ScoreEntry(String playerName, int score) implements Comparable<ScoreEntry> {
  
  /**
   * Compact constructor that ensures the player name is not null.
   *
   * @param playerName the name of the player.
   * @param score      the points achieved.
   * @throws NullPointerException if playerName is null.
   */
  public ScoreEntry {
    Objects.requireNonNull(playerName);
  }

  /**
   * Compares this entry with another for descending sort order (highest score first).
   *
   * @param other the other score entry to compare to.
   * @return a negative integer, zero, or a positive integer as this score 
   * is greater than, equal to, or less than the specified score.
   */
  @Override
  public int compareTo(ScoreEntry other) {
    Objects.requireNonNull(other);
    // Descending order: highest scores appear first in the list
    return Integer.compare(other.score, this.score);
  }
    
  /**
   * Provides a formatted string for display in the Hall of Fame.
   *
   * @return a string in the format "Player : Score points".
   */
  @Override
  public String toString() {
    return playerName + " : " + score + " points";
  }
}