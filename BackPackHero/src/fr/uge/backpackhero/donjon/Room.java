package fr.uge.backpackhero.donjon;

/**
 * Interface scellée représentant une case (une salle) de la carte.
 * C'est le type générique pour tout ce qui compose un étage.
 */
public sealed interface Room permits 
    Corridor, EnemyRoom, TreasureRoom, MerchantRoom, HealerRoom, ExitRoom {
}