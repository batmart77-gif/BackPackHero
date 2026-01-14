package fr.uge.backpackhero.entites;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.item.Armor;
import fr.uge.backpackhero.item.BackPack;
import fr.uge.backpackhero.item.Item;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Main character class managing health, level progression, inventory, 
 * gold transactions, and status effect mechanics including curse penalties.
 */
public final class Heros {
  private int hp;
  private int maxHp;
  private int energy;
  private final int maxEnergy;
  private int protection;
  private int currentXp;
  private int currentLevel;
  private int xpToNextLevel;
  private final BackPack backpack;
  private final Map<Effect, Integer> statusEffects = new HashMap<>();
  
  private int cursePenaltyDuration = 0;
  private int hpMaxPenalty = 0;
  private int currentCurseRefusalDamage = 0;
  private int mana;
  private int maxMana;

  /**
   * Initializes the hero with starting stats (40 HP, 3 Energy).
   */
  public Heros() {
    this.maxHp = 40;
    this.hp = maxHp;
    this.maxEnergy = 3;
    this.energy = maxEnergy;
    this.currentLevel = 1;
    this.xpToNextLevel = 10;
    this.backpack = new BackPack(); 
  }

  /**
   * Applies a temporary Max HP penalty (20%) when a curse is removed.
   * Lasts for 2 combats.
   */
  public void applyCurseRemovalPenalty() {
    this.hpMaxPenalty = (int) (this.maxHp * 0.20);
    this.maxHp -= this.hpMaxPenalty;
    this.hp = Math.min(this.hp, this.maxHp);
    this.cursePenaltyDuration = 2;
  }

  /**
   * Triggers effects like regeneration or burn at the start of the turn.
   */
  public void triggerStartTurnEffects() {
    int regen = getStatus(Effect.REGEN);
    if (regen > 0) {
      soigner(regen);
    }
    recevoirDegats(getStatus(Effect.BURN));
  }

  /**
   * Resets energy and calculates passive protection from armor.
   */
  public void debuterTourCombat() {
    this.energy = maxEnergy;
    this.protection = 0;
    for (var instance : backpack.getItems()) {
      if (instance.getItem() instanceof Armor armor) {
        this.protection += armor.stats();
      }
    }
  }

  /**
   * Processes damage intake, handling protection and Dodge charges.
   * @param damage raw damage value.
   */
  public void recevoirDegats(int damage) {
    if (damage < 0) throw new IllegalArgumentException("Damage cannot be negative");
    if (getStatus(Effect.DODGE) > 0) return;
    
    int absorbed = Math.min(damage, this.protection);
    this.protection -= absorbed;
    this.hp = Math.max(0, this.hp - (damage - absorbed));
  }

  /**
   * Handles level-up and XP progression.
   * @param amount XP gained.
   * @return number of levels gained.
   */
  public int gainXp(int amount) {
    if (amount < 0) throw new IllegalArgumentException("XP cannot be negative");
    int levels = 0;
    this.currentXp += amount;
    while (this.currentXp >= this.xpToNextLevel) {
      this.currentXp -= this.xpToNextLevel;
      this.currentLevel++;
      this.xpToNextLevel = this.xpToNextLevel * 3 / 2;
      levels++;
    }
    return levels;
  }

  /**
   * Decrements curse penalty duration and restores Max HP if duration ends.
   */
  public void decrementCursePenaltyDuration() {
    if (cursePenaltyDuration > 0) {
      cursePenaltyDuration--;
      if (cursePenaltyDuration == 0) {
        this.maxHp += hpMaxPenalty;
        this.hpMaxPenalty = 0;
      }
    }
  }

  /**
   * Processes end-of-turn status effects like Poison.
   */
  public void triggerEndTurnEffects() {
    this.hp = Math.max(0, this.hp - getStatus(Effect.POISON));
    statusEffects.replaceAll((e, v) -> v - 1);
    statusEffects.values().removeIf(v -> v <= 0);
  }

  /**
   * Calculates total final score (Max HP + inventory value).
   * @return final score.
   */
  public int calculateFinalScore() {
    int itemsValue = backpack.getItems().stream()
        .mapToInt(i -> i.getItem().price()).sum();
    return this.maxHp + itemsValue;
  }

  // --- Getters & Small Helpers ---
  public int getPv() { return hp; }
  public int getPvMax() { return maxHp; }
  public int getLevel() { return currentLevel; }
  public int getGold() { return backpack.getGoldQuantity(); }
  public int getEnergie() { return energy; }
  public int getProtection() { return protection; }
  public int getMana() { return mana; }
  public boolean estVivant() { return hp > 0; }
  public BackPack getBackpack() { return backpack; }

  public void acceptCurseImmediate() { this.currentCurseRefusalDamage = 0; }
  public void refuseCurseImmediate() { 
    this.currentCurseRefusalDamage++; 
    recevoirDegats(currentCurseRefusalDamage); 
  }
  public void gagnerOr(int amount) { backpack.addGold(amount); }
  public boolean payer(int amount) {
    if (amount >= 0 && getGold() >= amount) { backpack.spendGold(amount); return true; }
    return false;
  }
  public void addEffect(Effect effect, int amount) {
    Objects.requireNonNull(effect);
    statusEffects.merge(effect, amount, Integer::sum);
  }
  public int getStatus(Effect effect) { return statusEffects.getOrDefault(effect, 0); }
  public void soigner(int val) { this.hp = Math.min(maxHp, hp + val); }
  public void rafraichirMana() { this.mana = backpack.countManaStones(); }
  public void ajouterProtection(int val) { this.protection += val; }
  public boolean depenserEnergie(int cost) {
    if (energy >= cost) { this.energy -= cost; return true; }
    return false;
  }
}