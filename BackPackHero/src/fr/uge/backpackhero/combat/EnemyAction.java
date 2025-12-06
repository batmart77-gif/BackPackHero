package fr.uge.backpackhero.combat;

/**
 * Interface scellée représentant une action ("message") qu'un ennemi annonce.
 */
public sealed interface EnemyAction permits AttackAction, ProtectAction, CurseAction {
  
  /**
   * Renvoie une description textuelle de l'action pour l'afficher au joueur.
   * @return Le texte (ex: "Attaque (5)").
   */
  String description();
}