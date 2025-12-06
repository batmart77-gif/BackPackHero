package fr.uge.backpackhero.combat;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Interface définissant l'intelligence (le comportement) d'un ennemi.
 */
public interface EnemyBehavior {
  
  /**
   * L'ennemi réfléchit et choisit sa prochaine action.
   * @return L'objet action (le message) qui sera annoncé.
   */
  EnemyAction chooseAction();
  
  /**
   * Exécute l'action qui avait été annoncée précédemment.
   *
   * @param action L'action à exécuter.
   * @param owner L'ennemi qui exécute l'action 
   * @param target La cible de l'action
   */
  void executeAction(EnemyAction action, Ennemi owner, Heros target);
}