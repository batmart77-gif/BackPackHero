package fr.uge.backpackhero.combat;

/**
 * Represents the different status effects that can be applied to an entity (Hero or Enemy).
 * Each effect has a stack count (X) that determines its intensity or duration.
 * Stacks generally decrease by 1 at the end of each turn.
 */
public enum Effect {
  /**
   * Haste: Increases the block amount provided by items by X.
   */
  HASTE("Hâte"),

  /**
   * Rage: Increases the damage dealt by weapons by X.
   */
  RAGE("Rage"),

  /**
   * Slow: Decreases the block amount provided by items by X.
   * Can also represent a penalty (e.g., reduced energy) in some contexts.
   */
  SLOW("Lenteur"),

  /**
   * Weak: Reduces the damage dealt by weapons by X.
   */
  WEAK("Faiblesse"),

  /**
   * Poison: Deals X damage at the end of the turn.
   * These damages ignore armor (block) and dodge.
   */
  POISON("Poison"),

  /**
   * Burn: Deals X damage at the start of the turn.
   */
  BURN("Brûlure"),

  /**
   * Dodge: Prevents incoming damage X times.
   * Consumes one stack per hit avoided.
   */
  DODGE("Esquive"),

  /**
   * Regeneration: Restores X health points at the start of the turn.
   */
  REGEN("Régénération");

  /**
   * The display name of the effect in French.
   */
  private final String nom;

  /**
   * Constructs an Effect with its display name.
   *
   * @param nom The French name of the effect.
   */
  Effect(String nom) {
    this.nom = nom;
  }

  /**
   * Returns the display name of the effect.
   *
   * @return The name of the effect (e.g., "Poison").
   */
  public String getNom() {
    return nom;
  }
}