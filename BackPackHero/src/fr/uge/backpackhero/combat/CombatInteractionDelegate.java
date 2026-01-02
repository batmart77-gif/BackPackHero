package fr.uge.backpackhero.combat;

import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.Curse;

/**
 * Interface allowing the combat system to initiate user interaction
 * without directly depending on the View or Controller classes.
 * This is used to handle complex events like receiving a curse during the enemy's turn.
 */
public interface CombatInteractionDelegate {
    
    /**
     * Handles the complete interaction when an enemy forces a Curse onto the Hero.
     * This typically involves asking the player to accept or refuse the curse.
     *
     * @param heros The Hero affected by the curse.
     * @param curse The Curse object being inflicted.
     */
    void handleForcedCurse(Heros heros, Curse curse);
    
    /**
     * Triggered when the hero levels up to allow the user to choose new tiles.
     * @param levelsGained Number of levels to process.
     */
    void handleLevelUpExpansion(int levelsGained);
}