package fr.uge.backpackhero.donjon;

/**
 * Represents the room that contains the exit door.
 * Entering this room allows the hero to pass to the next floor of the dungeon (or win the game if it is the last floor).
 */
public record ExitRoom() implements Room {}