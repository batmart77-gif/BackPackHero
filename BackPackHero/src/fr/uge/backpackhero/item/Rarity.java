package fr.uge.backpackhero.item;

/**
 * Represents the rarity level of an item, influencing its value, power, and
 * appearance frequency in the game.
 *
 * The levels range from standard (COMMON) to extremely rare (LEGENDARY),
 * including a special category for negative items (CURSE).
 */
public enum Rarity {
  /** Standard items, frequently encountered. */
  COMMON,

  /** Items slightly less common, often with moderate stats. */
  UNCOMMON,

  /** Items that are difficult to find, possessing valuable stats. */
  RARE,

  /** Extremely powerful and scarce items. */
  LEGENDARY,

  /**
   * * Special rarity reserved for Malice or negative effects. These items are
   * often unwanted and carry penalties.
   */
  CURSE
}
