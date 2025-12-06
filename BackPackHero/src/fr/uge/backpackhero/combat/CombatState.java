package fr.uge.backpackhero.combat;

/**
 * Représente les différents états possibles d'un combat.
 * Utilisé pour savoir si le jeu doit continuer ou s'arrêter.
 */
public enum CombatState { 
  /** Le combat est toujours en cours. */
  IN_PROGRESS, 
  
  /** Le héros a gagné (tous les ennemis sont morts). */
  WIN, 
  
  /** Le héros a perdu (0 PV). */
  LOSS 
}

