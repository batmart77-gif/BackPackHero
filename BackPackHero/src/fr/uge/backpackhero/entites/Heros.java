package fr.uge.backpackhero.entites;

import fr.uge.backpackhero.item.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.item.Armor;
import fr.uge.backpackhero.item.BackPack;
import fr.uge.backpackhero.item.Curse;
import fr.uge.backpackhero.item.ItemInstance;

public final class Heros {
  
  private int hp;
  private  int maxHp;
  private int energy;
  private final int maxEnergy;
  private int protection;
  private int currentXp;
  private int currentLevel;
  private int xpToNextLevel;
  private final BackPack backpack;
  private int curseRefusalCount = 0;
  private final Map<Effect, Integer> statusEffects = new HashMap<>();
  
  private final double HP_MAX_PENALTY_RATE = 0.20;
  private int cursePenaltyDuration = 0;
  private int hpMaxPenalty = 0;
  private int currentCurseRefusalDamage = 0;

  public Heros() {
    this.maxHp = 40;
    this.hp = maxHp;
    this.maxEnergy = 3;
    this.energy = maxEnergy;
    this.protection = 0;
    
    this.currentXp = 0;
    this.currentLevel = 1;
    this.xpToNextLevel = 10;
    // On initialise un sac vide par défaut (3x5)
    this.backpack = new BackPack(); 
  }

  /**
   * Réinitialise l'énergie et la protection au début du tour.
   */
  public void debuterTourCombat() {
    this.energy = maxEnergy;
    this.protection = 0;
    
    // On ajoute la protection passive des Armures présentes dans le sac
    for (var item : backpack.getItems()) {
        if (item.getItem() instanceof Armor armor) {
            this.protection += armor.stats(); 
        }
    }
  }

  public void recevoirDegats(int damage) {
    if (damage < 0) throw new IllegalArgumentException("Dégâts négatifs interdits");
    
    // Gestion ESQUIVE (DODGE)
    // "Peut prévenir les dommages subis jusqu'à X fois"
    int dodge = getStatus(Effect.DODGE);
    if (dodge > 0) {
        System.out.println("ESQUIVE ! (" + (dodge - 1) + " charges restantes)");
        return; // 0 Dégâts pris
    }
    
    int absorbed = Math.min(damage, this.protection);
    this.protection -= absorbed;
    damage -= absorbed;

    if (damage > 0) {
      this.hp = Math.max(0, this.hp - damage);
    }
  }

  public void ajouterProtection(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Protection négative interdite");
    this.protection += amount;
  }

  public void soigner(int amount) {
    if (amount < 0) throw new IllegalArgumentException("Soin négatif interdit");
    this.hp = Math.min(this.hp + amount, maxHp);
  }

  /**
   * Tente de dépenser de l'énergie.
   * @return true si l'énergie a été dépensée, false si insuffisant.
   */
  public boolean depenserEnergie(int cost) {
    if (cost < 0) throw new IllegalArgumentException("Coût négatif interdit");    
    if (this.energy >= cost) {
      this.energy -= cost;
      return true;
    }
    return false;
  }

  public boolean estVivant() {
    return this.hp > 0;
  }
  
  // Accès au sac pour l'interface graphique ou la gestion d'inventaire
  public BackPack getBackpack() {
    return backpack;
  }
  
  @Override
  public String toString() {
    return "Héros (" + hp + "/" + maxHp + " PV)";
  }
  
  public void gainXp(int amount) {
    if (amount < 0) throw new IllegalArgumentException("XP négative");
    this.currentXp += amount;    
    while (this.currentXp >= this.xpToNextLevel) {
      levelUp();
    }
  }
  private void levelUp() {
    this.currentXp -= this.xpToNextLevel;
    this.currentLevel++;
    this.xpToNextLevel = this.xpToNextLevel * 3 / 2;
    System.out.println("NIVEAU SUPÉRIEUR ! Vous êtes niveau " + currentLevel + " !");
    backpack.expand(1);
  }

  

  private void appliquerPenaliteRefus() {
    curseRefusalCount++;
    System.out.println("Vous subissez " + curseRefusalCount + " dégâts de pénalité.");   
    this.recevoirDegats(curseRefusalCount);
  }
  
  /** Ajoute X cumuls d'un effet */
  public void addStatus(Effect effect, int amount) {
    statusEffects.merge(effect, amount, Integer::sum);
    System.out.println("Effet appliqué : " + effect.getNom() + " (Cumul: " + getStatus(effect) + ")");
  }

  /** Récupère la valeur X d'un effet (0 si absent) */
  private int getStatus(Effect effect) {
    return statusEffects.getOrDefault(effect, 0);
  }


  /** * Calcule les dégâts RÉELS infligés par une arme (Rage / Faiblesse).
   * @param baseDamage Les dégâts de l'arme (stats).
   */
  public int calculateDamageOutput(int baseDamage) {
    int bonus = getStatus(Effect.RAGE);
    int malus = getStatus(Effect.WEAK);
    int total = Math.max(0, baseDamage + bonus - malus);      
    if (bonus > 0) System.out.println("  (Bonus Rage +" + bonus + ")");
    if (malus > 0) System.out.println("  (Malus Faiblesse -" + malus + ")");
    return total;
  }

  /** * Calcule le blocage RÉEL fourni par un bouclier (Hâte / Lenteur).
   * @param baseBlock Le blocage du bouclier (stats).
   */
  public int calculateBlockOutput(int baseBlock) {
    int bonus = getStatus(Effect.HASTE);
    int malus = getStatus(Effect.SLOW);
    int total = Math.max(0, baseBlock + bonus - malus);
    if (bonus > 0) System.out.println("  (Bonus Hâte +" + bonus + ")");
    if (malus > 0) System.out.println("  (Malus Lenteur -" + malus + ")");
    return total;
  }

  // --- GESTION DU TOUR (DÉBUT / FIN) ---

  public void triggerStartTurnEffects() {
    // 1. Régénération
    int regen = getStatus(Effect.REGEN);
    if (regen > 0) {
      System.out.println("Régénération : +" + regen + " PV");
      soigner(regen);
    }
    // 2. Brûlure (Dégâts directs sur l'armure puis PV)
    int burn = getStatus(Effect.BURN);
    if (burn > 0) {
      System.out.println("Brûlure : -" + burn + " PV");
      recevoirDegats(burn);
    }
  }

  public void triggerEndTurnEffects() {
      // 1. Poison (Ignore l'armure !)
      int poison = getStatus(Effect.POISON);
      if (poison > 0) {
          System.out.println("Poison : -" + poison + " PV (Ignore l'armure)");
          this.hp = Math.max(0, this.hp - poison); // Tape direct dans les PV
      }
      // 2. Dégradation des effets (-1 partout)
      // On utilise removeIf pour nettoyer ce qui tombe à 0
      statusEffects.replaceAll((e, v) -> v - 1);
      statusEffects.values().removeIf(v -> v <= 0);
  }

  public int getGold() {
    return backpack.getGoldQuantity();
  }

  public void gagnerOr(int montant) {
    backpack.addGold(montant);
    System.out.println("+" + montant + " Or");
  }

  /**
   * Tente de payer un montant.
   * @return true si le paiement a réussi, false sinon.
   */
  public boolean payer(int montant) {
    if (backpack.spendGold(montant)) {
      System.out.println("-" + montant + " Or");
      return true;
    }
    return false;
  }

  public int getPv() {
    return hp;
  }

  public int getPvMax() {
    return maxHp;
  }

  public int getLevel() {
    return currentLevel;
  }

  public int getProtection() {
    return protection;
  }

  public int getEnergie() {
    return energy;
  }
  
  /**
   * Conséquence du Refus Immédiat de la Malédiction
   * Le Héros subit des dégâts croissants et le compteur de dégâts augmente.
   */
  public void refuseCurseImmediate() {
      this.currentCurseRefusalDamage++; // Le dégât augmente à chaque refus
      int damage = this.currentCurseRefusalDamage;
      this.recevoirDegats(damage);
      System.out.println("Refusé ! Vous subissez " + damage + " dégâts de pénalité.");
  }
  
  /**
   * Conséquence de l'Acceptation Immédiate
   * Ne fait rien d'autre que l'ajout dans le sac (qui sera fait par la View).
   * On conserve cette méthode pour être explicite.
   */
  public void acceptCurseImmediate() {
      // Si l'on accepte, le compteur de refus est réinitialisé, car le héros a cédé
      this.currentCurseRefusalDamage = 0;
  }
     
  /**
   * Applique la pénalité pour les 2 combats suivants.
   */
  public void applyCurseRemovalPenalty() {
  // 1. Calculer la pénalité
      this.hpMaxPenalty = (int) (this.maxHp * HP_MAX_PENALTY_RATE);
      
      // 2. Appliquer la réduction des HP Max
      this.maxHp = this.maxHp - this.hpMaxPenalty;
      
      // 3. Assurer que les HP actuels ne dépassent pas le nouveau maximum
      this.hp = Math.min(this.hp, this.maxHp);
    this.cursePenaltyDuration = 2;
      System.out.println("Pénalité de Malédiction appliquée. Défense réduite jusqu'à la fin du prochain combat !");
  }

  /**
   * Décrémente la durée de la pénalité.
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
}