package fr.uge.backpackhero.entites;

import java.util.Objects;

import fr.uge.backpackhero.combat.EnemyAction;
import fr.uge.backpackhero.combat.EnemyBehavior;

public class Ennemi {
  private int pv;
  private final int pvMax;
  private int protection;
  
  private final EnemyBehavior comportement;
  
  private EnemyAction actionAnnoncee;

  /**
   * Construit un nouvel ennemi.
   * @param pvMax Les PV maximum de cet ennemi.
   * @param comportement L'intelligence artificielle qui va piloter cet ennemi.
   */
  public Ennemi(int pvMax, EnemyBehavior comportement) {
    Objects.requireNonNull(comportement, "Le comportement ne peut pas être nul");
    if (pvMax <= 0) {
      throw new IllegalArgumentException("Les PV max doivent être positifs.");
    }
    this.pvMax = pvMax;
    this.pv = pvMax;
    this.protection = 0;
    this.comportement = comportement;
    this.actionAnnoncee = null;
  }

  /**
   * Choisit la prochaine action en "demandant" à son comportement.
   * Appelé au début du tour du héros.
   */
  public void choisirProchaineAction() {
    this.actionAnnoncee = comportement.chooseAction();
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
    if (montantDegats < 0) { // Pré-condition [cite: 2737]
      throw new IllegalArgumentException("On ne peut pas infliger de dégâts négatifs");
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
    if (montant < 0) { // Pré-condition [cite: 2737]
      throw new IllegalArgumentException("On ne peut pas gagner de protection négative");
    }
    this.protection += montant;
  }

  public boolean estVivant() {
    return this.pv > 0;
  }

  // --- Getters (pour la Vue) ---
  public int getPv() { return pv; }
  public int getPvMax() { return pvMax; }
  public int getProtection() { return protection; }
  public EnemyAction getActionAnnoncee() { return actionAnnoncee; }

}
