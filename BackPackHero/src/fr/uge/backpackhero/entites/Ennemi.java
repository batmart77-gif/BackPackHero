package fr.uge.backpackhero.entites;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.uge.backpackhero.combat.Effect;
import fr.uge.backpackhero.combat.EnemyAction;
import fr.uge.backpackhero.combat.EnemyBehavior;

public class Ennemi {
  private int pv;
  private final int pvMax;
  private int protection;
  private final int xpReward;
  
  private final EnemyBehavior comportement;
  private EnemyAction actionAnnoncee;
  private final Map<Effect, Integer> statusEffects1 = new HashMap<>();

  /**
   * Construit un nouvel ennemi.
   * @param pvMax Les PV maximum de cet ennemi.
   * @param comportement L'intelligence artificielle qui va piloter cet ennemi.
   */
  public Ennemi(int pvMax, int xpReward, EnemyBehavior comportement) {
    Objects.requireNonNull(comportement, "Le comportement ne peut pas être nul");
    if (pvMax <= 0) {
      throw new IllegalArgumentException("Les PV max doivent être positifs.");
    }
    if (xpReward < 0) {
      throw new IllegalArgumentException("L'XP ne peut pas être négative");
    }
    this.pvMax = pvMax;
    this.pv = pvMax;
    this.protection = 0;
    this.comportement = comportement;
    this.actionAnnoncee = null;
    this.xpReward = xpReward;
  }

  /**
   * Choisit la prochaine action en "demandant" à son comportement.
   * Appelé au début du tour du héros.
   */
  public EnemyAction choisirProchaineAction() {
    this.actionAnnoncee = comportement.chooseAction();
    return actionAnnoncee;
  }
  
  /**
   * Exécute l'action annoncée en "demandant" à son comportement
   */
  public void executerAction(Heros heros) {
    Objects.requireNonNull(heros);
    if (actionAnnoncee == null) return;
    
    comportement.executeAction(actionAnnoncee, this, heros);
    this.actionAnnoncee = null; // L'action est consommée
  }

  /**
   * Inflige des dégâts à l'ennemi (appelé par le Héros)
   */
  
  public void recevoirDegats(int montantDegats) {
    if (montantDegats < 0) {
      throw new IllegalArgumentException("On ne peut pas infliger de dégâts négatifs");
    }
    // Gestion ESQUIVE (DODGE)
    int dodge = getStatus(Effect.DODGE);
    if (dodge > 0) {
        System.out.println("L'ennemi ESQUIVE l'attaque ! (" + (dodge - 1) + " charges restantes)");
        return; 
    }
    int degatsAbsorbes = Math.min(montantDegats, this.protection);
    this.protection -= degatsAbsorbes;
    montantDegats -= degatsAbsorbes;
    if (montantDegats > 0) {
        this.pv = Math.max(0, this.pv - montantDegats);
    }  
  }
  
  /**
   * Méthode appelée par le Comportement
   * pour augmenter la protection de cet ennemi.
   * @param montant Le montant de protection à ajouter.
   */
  public void gagnerProtection(int montant) {
    if (montant < 0) {
      throw new IllegalArgumentException("On ne peut pas gagner de protection négative");
    }
    this.protection += montant;
  }

  public boolean estVivant() {
    return this.pv > 0;
  }

  public int getxpReward() {
    return xpReward;
  }
  
//Map : Effet -> Nombre de cumuls (X)
 private final Map<Effect, Integer> statusEffects = new HashMap<>();

 /** Ajoute X cumuls d'un effet */
 public void addStatus(Effect effect, int amount) {
   statusEffects1.merge(effect, amount, Integer::sum);
   System.out.println("✨ L'ennemi gagne : " + effect.getNom() + " (Cumul: " + getStatus(effect) + ")");
 }

 /** Récupère la valeur X d'un effet (0 si absent) */
 public int getStatus(Effect effect) {
   return statusEffects1.getOrDefault(effect, 0);
 }

 /**
  * Appelé au DÉBUT du tour de l'ennemi.
  * Gère : Régénération, Brûlure.
  */
 public void triggerStartTurnEffects() {
   // 1. Régénération
   int regen = getStatus(Effect.REGEN);
   if (regen > 0) {
     System.out.println("L'ennemi se régénère : +" + regen + " PV");
       // On soigne l'ennemi (maxHp cap)
     this.pv = Math.min(this.pv + regen, this.pvMax);
   }
   
   // 2. Brûlure (Dégâts au début du tour)
   int burn = getStatus(Effect.BURN);
   if (burn > 0) {
     System.out.println("L'ennemi brûle : -" + burn + " PV");
     recevoirDegats(burn);
   }
 }

 /**
  * Appelé à la FIN du tour de l'ennemi.
  * Gère : Poison, Diminution des cumuls.
  */
  public void triggerEndTurnEffects() {
    // 1. Poison (Ignore l'armure !)
    int poison = getStatus(Effect.POISON);
    if (poison > 0) {
      System.out.println("Le Poison ronge l'ennemi : -" + poison + " PV");
      this.pv = Math.max(0, this.pv - poison);
    }
    // 2. Dégradation des effets (-1 partout à la fin du tour)
    statusEffects1.replaceAll((e, v) -> v - 1);
    statusEffects1.values().removeIf(v -> v <= 0);
  }

  public int getHp() {
    return pv;
  }
  public void clearActionAnnoncee() {
    this.actionAnnoncee = null;
  }
  public EnemyAction getActionAnnoncee() { return actionAnnoncee; }
}
