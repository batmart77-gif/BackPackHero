package fr.uge.backpackhero.combat;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;

/**
 * Classe de gestion d'un combat.
 * Gère le tour par tour et les conditions de victoire/défaite.
 */
public final class Combat {
  
  private final Heros heros;
  private final List<Ennemi> enemies;
  private boolean isHeroTurn;
  private final CombatInteractionDelegate delegate;


  public Combat(Heros heros, List<Ennemi> listEnemies, CombatInteractionDelegate delegate) {
    Objects.requireNonNull(heros);
    Objects.requireNonNull(listEnemies);
    Objects.requireNonNull(delegate);
    
    if (listEnemies.isEmpty()) {
      throw new IllegalArgumentException("Pas d'ennemis dans le combat");
    }
    
    this.heros = heros;
    this.enemies = new ArrayList<>(listEnemies); 
    this.delegate = delegate;

    // Le combat commence par le tour du héros
    startHeroTurn();
  }
  


  /**
   * Prépare et démarre le tour du héros.
   * Réinitialise l'énergie et demande aux ennemis d'annoncer leurs intentions.
   */
  public void startHeroTurn() {
    this.isHeroTurn = true;
    
    // 1. Effets de début de tour (Regen, Burn)
    heros.triggerStartTurnEffects();
    if (!heros.estVivant()) return;

    // 2. Fin des effets du tour précédent pour les ennemis (Poison, Dégradation)
    // C'est logique : le tour de l'ennemi vient de finir techniquement.
    for (Ennemi e : enemies) {
        if (e.estVivant()) e.triggerEndTurnEffects();
    }
    heros.debuterTourCombat();
    
    // Les ennemis choisissent leur action pour le tour à venir
    for (Ennemi enemy : enemies) {
      if (enemy.estVivant()) {
        enemy.choisirProchaineAction();
      }
    }
  }

  /**
   * Tente d'exécuter une action du joueur (utiliser un objet du sac).
   *
   * @param instance L'instance de l'objet dans le sac (ItemInstance).
   * @param target La cible (Ennemi).
   * @return true si l'action a réussi, false sinon.
   */
  public boolean tryHeroAction(ItemInstance instance, Ennemi target) {
    Objects.requireNonNull(instance);
    
    if (!isHeroTurn) {
      System.out.println("Ce n'est pas votre tour !");
      return false;
    }
    if (!heros.estVivant()) {
      return false;
    }

    //On récupère l'Item de base et on l'utilise.
    // Cela suppose que l'interface Item a une méthode: use(Heros, Ennemi)
    boolean success = instance.getItem().use(heros, target);

    // Si l'action a réussi et a tué un ennemi, on le retire de la liste
    if (success && target != null && !target.estVivant()) {
      heros.gainXp(target.getxpReward());
      enemies.remove(target);
    }
    
    return success;
  }

  /**
   * Termine le tour du héros et lance le tour des ennemis.
   */
  public void startEnemyTurn() {
    if (!isHeroTurn) return;
    this.isHeroTurn = false;
    heros.triggerEndTurnEffects(); 
    if (!heros.estVivant()) return;
    for (Ennemi enemy : enemies) {
      if (enemy.estVivant()) {
        EnemyAction action = enemy.getActionAnnoncee();
        if (action instanceof CurseAction curseAct) {
              delegate.handleForcedCurse(heros, curseAct.curse()); 
              enemy.clearActionAnnoncee();
          } else {
              enemy.executerAction(heros); 
          }
        enemy.triggerStartTurnEffects();
      }
    }
    if (!heros.estVivant()) return; 
    enemies.removeIf(e -> !e.estVivant());
    if (!enemies.isEmpty()) {
      startHeroTurn();
    } else {
      endCombat();
    }
  }
  
  /**
   * Gère la fin du combat, quel que soit le résultat.
   * Inclut la décrémentation des pénalités temporaires.
   */
  public void endCombat() {
    heros.decrementCursePenaltyDuration();
    if (getState() == CombatState.WIN) {
      heros.gainXp(10);
    }
      
  }
  
  /**
   * Récupère l'état actuel du combat (En cours, Victoire, Défaite).
   * @return L'enum CombatState.
   */
  public CombatState getState() {
    if (!heros.estVivant()) return CombatState.LOSS;
    if (enemies.isEmpty()) return CombatState.WIN;
    return CombatState.IN_PROGRESS;
  }
  
  
  public Heros getHero() { 
    return heros; 
  }
  
  public List<Ennemi> getAliveEnemies() { 
    return List.copyOf(enemies); 
  }
  
  public boolean isHeroTurn() { 
    return isHeroTurn; 
  }
  
  
}