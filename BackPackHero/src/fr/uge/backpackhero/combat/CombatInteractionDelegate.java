package fr.uge.backpackhero.combat;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;

/**
 * Interface permettant au système de combat d'initier une interaction utilisateur
 * sans dépendre directement de la classe View.
 */
public interface CombatInteractionDelegate {
    
    /**
     * Gère l'interaction complète lorsque l'ennemi force une Malédiction sur le Héros.
     * @param heros Le Héros affecté.
     * @param curse L'objet Malédiction concerné.
     */
    void handleForcedCurse(Heros heros, Curse curse);
}