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
    boolean success = instance.getItem().utiliser(heros, target);

    // Si l'action a réussi et a tué un ennemi, on le retire de la liste
    if (success && target != null && !target.estVivant()) {
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
    // Chaque ennemi exécute l'action qu'il avait annoncée
    for (Ennemi enemy : enemies) {
      if (enemy.estVivant()) {
    	  EnemyAction action = enemy.chooseAction();
    	  if (action instanceof CurseAction curseAct) {
              // Si c'est une Malédiction, on appelle le délégué (la View) pour gérer l'interaction.
              // Cette méthode va bloquer l'exécution jusqu'à ce que l'utilisateur choisisse.
              delegate.handleForcedCurse(heros, curseAct.curse()); 
              
              // Une fois l'interaction terminée, le Héros a subi les dégâts/a placé l'item.
              // On ne demande plus à RatLoupBehavior d'exécuter l'action !
          } else {
              // Si ce n'est pas une Malédiction, exécution normale de l'action
              enemy.executerAction(action, heros); 
          }
      }
    }
    // Vérification de défaite immédiate
    if (!heros.estVivant()) return; 
    // Nettoyage des ennemis morts
    enemies.removeIf(e -> !e.estVivant());
    // Si le combat n'est pas fini, on redonne la main au héros
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
      // 1. Décrémentation de la pénalité de Malédiction
      heros.decrementCursePenaltyDuration();
      // 2. Logique de récompenses (Exemple: gain d'XP pour chaque ennemi vaincu)
      if (getState() == CombatState.WIN) {
          heros.gainXp(10);
      }
      
      // 3. Autres nettoyages...
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
  
  
  public Heros getHero() { return heros; }
  
  public List<Ennemi> getAliveEnemies() { return List.copyOf(enemies); }
  
  public boolean isHeroTurn() { return isHeroTurn; }
}