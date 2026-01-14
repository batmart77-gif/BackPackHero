package fr.uge.backpackhero.entites;

import fr.uge.backpackhero.item.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.item.Armor;
import fr.uge.backpackhero.item.BackPack;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.Item;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * The player character class. Manages stats, inventory, combat actions, status effects, and level progression.
 */
public final class Heros {
  /** Current health points (HP). */
  private int hp;
  /** Maximum health points (HP). */
  private int maxHp;
  /** Current energy available for actions. */
  private int energy;
  /** Maximum energy available per turn. */
  private final int maxEnergy;
  /** Temporary protection (block) applied during the current turn. */
  private int protection;
  /** Current experience points accumulated. */
  private int currentXp;
  /** Current hero level. */
  private int currentLevel;
  /** XP required to reach the next level. */
  private int xpToNextLevel;
  /** The hero's inventory. */
  private final BackPack backpack;
  /** Counter for consecutive curse refusals, used to determine penalty damage. */
  private int curseRefusalCount = 0;
  /** Map storing the current status effects (Effect -> Stack Count). */
  private final Map<Effect, Integer> statusEffects = new HashMap<>();
  
  /** Rate (20%) at which maximum HP is reduced when a curse is removed. */
  private final double HP_MAX_PENALTY_RATE = 0.20;
  /** Duration (in combats remaining) of the curse removal penalty. */
  private int cursePenaltyDuration = 0;
  /** The actual amount Max HP was reduced by due to the penalty. */
  private int hpMaxPenalty = 0;
  /** Damage inflicted when refusing a curse, increases per refusal. */
  private int currentCurseRefusalDamage = 0;

  private int mana;
  private int maxMana;
  
  /**
   * Constructs a new Hero with base starting statistics (40 HP, 3 Energy, Lvl 1).
   */
  public Heros() {
    this.maxHp = 40;
    this.hp = maxHp;
    this.maxEnergy = 3;
    this.energy = maxEnergy;
    this.protection = 0;
    
    this.currentXp = 0;
    this.currentLevel = 1;
    this.xpToNextLevel = 10;
    // Initializes the default backpack
    this.backpack = new BackPack(); 
  }

  /**
   * Resets energy and protection at the beginning of the combat turn.
   * Also calculates and applies passive protection from {@link Armor} items in the backpack.
   */
  public void debuterTourCombat() {
    this.energy = maxEnergy;
    this.protection = 0;
    
    // Apply passive protection from Armor items
    for (var item : backpack.getItems()) {
        if (item.getItem() instanceof Armor armor) {
            this.protection += armor.stats(); 
        }
    }
  }

  /**
   * Inflicts damage on the hero.
   * Handles damage absorption from Protection and the evasion logic from the Dodge status.
   *
   * @param damage The amount of damage to inflict.
   * @throws IllegalArgumentException if the damage amount is negative.
   */
  public void recevoirDegats(int damage) {
    if (damage < 0) throw new IllegalArgumentException("Dégâts négatifs interdits");
    int dodge = getStatus(Effect.DODGE);
    if (dodge > 0) {
        System.out.println("ESQUIVE ! (" + (dodge - 1) + " charges restantes)");
        return;
    }
    int absorbed = Math.min(damage, this.protection);
    this.protection -= absorbed;
    damage -= absorbed;
    if (damage > 0) {
      this.hp = Math.max(0, this.hp - damage);
    }
  }

  /**
   * Adds protection (block) to the hero.
   *
   * @param amount The amount of protection to add.
   * @throws IllegalArgumentException if the amount is negative.
   */
  public void ajouterProtection(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Protection négative interdite");
    this.protection += amount;
  }

  /**
   * Heals the hero, restoring HP up to the maximum HP cap.
   *
   * @param amount The amount of HP to restore.
   * @throws IllegalArgumentException if the amount is negative.
   */
  public void soigner(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Soin négatif interdit");
    this.hp = Math.min(this.hp + amount, maxHp);
  }

  /**
   * Tries to spend energy.
   *
   * @param cost The energy cost.
   * @return {@code true} if the energy was spent successfully, {@code false} if insufficient.
   * @throws IllegalArgumentException if the cost is negative.
   */
  public boolean depenserEnergie(int cost) {
    if (cost < 0) throw new IllegalArgumentException("Coût négatif interdit");     
    if (this.energy >= cost) {
      this.energy -= cost;
      return true;
    }
    return false;
  }

  /**
   * Checks if the hero is alive (HP > 0).
   *
   * @return {@code true} if the hero is alive.
   */
  public boolean estVivant() {
    return this.hp > 0;
  }
  
  /**
   * Gets the hero's backpack.
   *
   * @return The {@link BackPack} instance.
   */
  public BackPack getBackpack() {
    return backpack;
  }
  
  @Override
  public String toString() {
    return "Héros (" + hp + "/" + maxHp + " PV)";
  }
  
  /**
   * Grants experience points to the hero.
   * Triggers a level up if the XP threshold is met.
   *
   * @param amount The amount of XP gained.
   * @throws IllegalArgumentException if the amount is negative.
   */
  public int gainXp(int amount) {
    if (amount < 0) throw new IllegalArgumentException("XP négative");
    int levelsGained = 0;
    this.currentXp += amount;
    while (this.currentXp >= this.xpToNextLevel) {
      levelUp();
      levelsGained++;
    }
    return levelsGained;
  }

  /**
   * Handles the level up logic: increases level, calculates new XP threshold, and expands the backpack.
   */
  private void levelUp() {
    this.currentXp -= this.xpToNextLevel;
    this.currentLevel++;
    this.xpToNextLevel = this.xpToNextLevel * 3 / 2; // Increases XP required by 50%
    System.out.println("LEVEL UP! You are now level " + currentLevel + " !");
  }

  /**
   * Applies the damage penalty for immediately refusing a curse.
   * The damage amount increases with each consecutive refusal.
   */
  private void appliquerPenaliteRefus() {
    curseRefusalCount++;
    System.out.println("Vous subissez " + curseRefusalCount + " dégâts de pénalité.");    
    this.recevoirDegats(curseRefusalCount);
  }
  
  /**
   * Adds X stacks of a specific status effect to the hero.
   *
   * @param effect The status effect to add.
   * @param amount The number of stacks to add.
   */
  public void addEffect(Effect effect, int amount) {
    Objects.requireNonNull(effect);
    statusEffects.merge(effect, amount, Integer::sum);
    System.out.println("Effet appliqué : " + effect.getNom() + " (Cumul: " + getStatus(effect) + ")");
  }

   
  /**
   * Retrieves the current stack count (X value) of a specific status effect.
   *
   * @param effect The status effect to check.
   * @return The stack count, or 0 if the effect is absent.
   */
  private int getStatus(Effect effect) {
    return statusEffects.getOrDefault(effect, 0);
  }

  /**
   * Calculates the actual damage output of an item based on status effects (Rage/Weak).
   *
   * @param baseDamage The base damage stat of the item.
   * @return The final damage output.
   */
  public int calculateDamageOutput(int baseDamage) {
    int bonus = getStatus(Effect.RAGE);
    int malus = getStatus(Effect.WEAK);
    int total = Math.max(0, baseDamage + bonus - malus);        
    if (bonus > 0) System.out.println("  (Bonus Rage +" + bonus + ")");
    if (malus > 0) System.out.println("  (Malus Faiblesse -" + malus + ")");
    return total;
  }

  /**
   * Calculates the actual block output of an item based on status effects (Haste/Slow).
   *
   * @param baseBlock The base block stat of the item.
   * @return The final block output.
   */
  public int calculateBlockOutput(int baseBlock) {
    int bonus = getStatus(Effect.HASTE);
    int malus = getStatus(Effect.SLOW);
    int total = Math.max(0, baseBlock + bonus - malus);
    if (bonus > 0) System.out.println("  (Bonus Hâte +" + bonus + ")");
    if (malus > 0) System.out.println("  (Malus Lenteur -" + malus + ")");
    return total;
  }

  // --- TURN MANAGEMENT (START / END) ---

  /**
   * Triggers effects that occur at the START of the hero's turn.
   * Handles {@link Effect#REGEN} and {@link Effect#BURN}.
   */
  public void triggerStartTurnEffects() {
    // 1. Regeneration
    int regen = getStatus(Effect.REGEN);
    if (regen > 0) {
      System.out.println("Régénération : +" + regen + " PV");
      soigner(regen);
    }
    // 2. Burn (Damage at the start of the turn)
    int burn = getStatus(Effect.BURN);
    if (burn > 0) {
      System.out.println("Brûlure : -" + burn + " PV");
      recevoirDegats(burn);
    }
  }

  /**
   * Triggers effects that occur at the END of the hero's turn.
   * Handles {@link Effect#POISON} and the general degradation of status stacks.
   */
  public void triggerEndTurnEffects() {
    // 1. Gestion du Poison (Dégâts directs aux PV)
    int poison = getStatus(Effect.POISON);
    if (poison > 0) {
        System.out.println("Le Poison ronge le héros : -" + poison + " PV");
        this.hp = Math.max(0, this.hp - poison);
    }
    
    // 2. Dégradation naturelle : -1 stack pour tous les effets à chaque fin de tour
    statusEffects.replaceAll((e, v) -> v - 1);
    statusEffects.values().removeIf(v -> v <= 0);
  }

  /**
   * Gets the amount of gold the hero currently possesses.
   *
   * @return The current gold amount.
   */
  public int getGold() {
    return backpack.getGoldQuantity();
  }

  /**
   * Adds gold to the hero's possession (stored in the backpack).
   *
   * @param montant The amount of gold to add.
   */
  public void gagnerOr(int montant) {
    backpack.addGold(montant);
    System.out.println("+" + montant + " Or");
  }

  /**
   * Tries to pay a required amount of gold.
   *
   * @param montant The amount to pay.
   * @return {@code true} if the payment was successful, {@code false} otherwise (insufficient funds).
   */
  public boolean payer(int montant) {
    if (backpack.spendGold(montant)) {
      System.out.println("-" + montant + " Or");
      return true;
    }
    return false;
  }

  /**
   * Gets the current health points.
   *
   * @return Current HP.
   */
  public int getPv() {
    return hp;
  }

  /**
   * Gets the maximum health points.
   *
   * @return Max HP.
   */
  public int getPvMax() {
    return maxHp;
  }

  /**
   * Gets the current level.
   *
   * @return Current level.
   */
  public int getLevel() {
    return currentLevel;
  }

  /**
   * Gets the current protection (block) amount.
   *
   * @return Current protection.
   */
  public int getProtection() {
    return protection;
  }

  /**
   * Gets the current energy available.
   *
   * @return Current energy.
   */
  public int getEnergie() {
    return energy;
  }
  
  /**
   * Applies the self-damage penalty for refusing a forced curse immediately.
   * The damage is cumulative (increases with each consecutive refusal).
   */
  public void refuseCurseImmediate() {
    this.currentCurseRefusalDamage++; 
    int damage = this.currentCurseRefusalDamage;
    this.recevoirDegats(damage);
    System.out.println("Refusé ! Vous subissez " + damage + " dégâts de pénalité.");
  }
  
  /**
   * Handles the consequence of immediately accepting a forced curse.
   * Resets the consecutive refusal damage counter.
   */
  public void acceptCurseImmediate() {
    // If the curse is accepted, the refusal counter is reset.
    this.currentCurseRefusalDamage = 0;
  }
      
  /**
   * Applies the temporary Max HP penalty when a curse is removed by other means (e.g., in town).
   * Sets the penalty duration (2 combats).
   */
  public void applyCurseRemovalPenalty() {
    // 1. Calculate the Max HP reduction amount (20%)
    this.hpMaxPenalty = (int) (this.maxHp * HP_MAX_PENALTY_RATE);
      
    // 2. Apply the Max HP reduction
    this.maxHp = this.maxHp - this.hpMaxPenalty;
      
      // 3. Ensure current HP doesn't exceed the new maximum
    this.hp = Math.min(this.hp, this.maxHp);
      
    this.cursePenaltyDuration = 2; // Penalty lasts for 2 combats
    System.out.println("Pénalité de Malédiction appliquée. Défense réduite jusqu'à la fin du prochain combat !");
  }

  /**
   * Decrements the duration of the curse removal penalty.
   * Restores Max HP when the duration reaches zero.
   */
  public void decrementCursePenaltyDuration() {
    if (this.cursePenaltyDuration > 0) {
      this.cursePenaltyDuration--;
      if (this.cursePenaltyDuration == 0) {
        this.maxHp = this.maxHp + this.hpMaxPenalty;
        this.hpMaxPenalty = 0;
        System.out.println("La pénalité de Malédiction a été levée.");
      } else {
        System.out.println("La pénalité de Malédiction persiste. Durée restante: " + this.cursePenaltyDuration + " combat(s).");
      }
    }
  }
  
  /**
   * Recalcule le mana maximum en comptant les pierres de mana dans le sac
   * et réinitialise le mana actuel.
   */
  public void rafraichirMana() {
    // On demande au sac de compter les pierres (logique déléguée)
    this.maxMana = backpack.countManaStones(); 
    this.mana = maxMana;
  }

  /**
   * Tente de dépenser du mana pour un objet magique.
   * @param cost Coût en mana.
   * @return true si le mana a été débité.
   */
  public boolean depenserMana(int cost) {
    if (cost < 0) throw new IllegalArgumentException("Coût négatif");
    if (this.mana >= cost) {
      this.mana -= cost;
      return true;
    }
    return false;
  }

  public int getMana() { return mana; }
  
  /**
   * Calcule le score final du héros selon la recette de la Phase 3.
   * @return le score total calculé.
   */
  public int calculateFinalScore() {
    int inventoryValue = backpack.getItems().stream()
              .map(ItemInstance::getItem)
              .mapToInt(Item::price)
              .sum();
    return this.maxHp + inventoryValue;
  }
}