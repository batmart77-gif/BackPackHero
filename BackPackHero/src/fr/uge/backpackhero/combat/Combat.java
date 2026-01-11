package fr.uge.backpackhero.combat;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Manages the combat logic between the hero and a group of enemies. It handles
 * the turn-based system, status effects, and victory/defeat conditions.
 */
public final class Combat {

  /** The hero involved in the combat. */
  private final Heros heros;

  /** The list of enemies currently alive in the combat. */
  private final List<Ennemi> enemies;

  /** Flag indicating if it is currently the hero's turn. */
  private boolean isHeroTurn;

  /** Delegate used to handle special interactions like Curses during combat. */
  private final CombatInteractionDelegate delegate;

  /**
   * Creates a new Combat instance.
   *
   * @param heros       The hero player.
   * @param listEnemies The list of enemies to fight.
   * @param delegate    The delegate to handle UI or specific interactions (e.g.,
   *                    Curses).
   * @throws NullPointerException     if any argument is null.
   * @throws IllegalArgumentException if the list of enemies is empty.
   */
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
    // Immediately start the first turn
    startHeroTurn();
  }

  /**
   * Prepares and starts the hero's turn. It triggers start-of-turn effects,
   * resets energy, and makes enemies plan their next move.
   */
  public void startHeroTurn() {
    this.isHeroTurn = true;
    heros.triggerStartTurnEffects();
    if (!heros.estVivant())
      return;

    // Recharge de l'énergie ET du Mana
    heros.debuterTourCombat();
    heros.rafraichirMana();

    for (Ennemi enemy : enemies) {
      if (enemy.estVivant())
        enemy.choisirProchaineAction();
    }
  }

  /**
   * Tries to execute a player action using an item from the backpack.
   *
   * @param instance The item instance from the backpack to use.
   * @param target   The target enemy (can be null for non-targeted items).
   * @return {@code true} if the action was successful, {@code false} otherwise.
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
    boolean success = instance.getItem().use(heros, target, heros.getBackpack(), instance);
    // If the action killed an enemy, grant XP and remove it
    if (success && target != null && !target.estVivant()) {
      int levels = heros.gainXp(target.getxpReward());
      System.out.println("hihi");
      if (levels > 0) {
        delegate.handleLevelUpExpansion(levels);
      }
      enemies.remove(target);
    }
    return success;
  }

  /**
   * Ends the hero's turn and executes the enemies' turn. Enemies perform their
   * planned actions or inflict curses.
   */
  public void startEnemyTurn() {
    if (!isHeroTurn)
      return;
    this.isHeroTurn = false;
    // 1. Hero suffers end-of-turn effects (e.g., Poison)
    heros.triggerEndTurnEffects();
    if (!heros.estVivant())
      return;
    // 2. Enemies act
    for (Ennemi enemy : enemies) {
      if (enemy.estVivant()) {
        EnemyAction action = enemy.getActionAnnoncee();
        switch (action) {
        case CurseAction curseAct -> delegate.handleForcedCurse(heros, curseAct.curse());
        default -> enemy.executerAction(heros);
        }
        enemy.triggerStartTurnEffects();
      }
    }
    if (!heros.estVivant())
      return;
    enemies.removeIf(e -> !e.estVivant());
    if (!enemies.isEmpty()) {
      startHeroTurn();
    } else {
      endCombat();
    }
  }

  /**
   * Handles the end of the combat session. Removes temporary penalties (like SLOW
   * from curses) and grants bonus XP on victory.
   */
  public void endCombat() {
    heros.decrementCursePenaltyDuration();
    int levels = heros.gainXp(10); // Victory bonus
    System.out.println("coucou toi");
    if (levels > 0) {
      delegate.handleLevelUpExpansion(levels);
    }
  }

  /**
   * Returns the current state of the combat.
   *
   * @return {@code LOSS} if hero is dead, {@code WIN} if enemies are empty,
   *         {@code IN_PROGRESS} otherwise.
   */
  public CombatState getState() {
    if (!heros.estVivant())
      return CombatState.LOSS;
    if (enemies.isEmpty())
      return CombatState.WIN;
    return CombatState.IN_PROGRESS;
  }

  // --- Getters ---

  /**
   * Gets the hero involved in this combat.
   * 
   * @return The hero.
   */
  public Heros getHero() {
    return heros;
  }

  /**
   * Gets a list of currently alive enemies.
   * 
   * @return An unmodifiable list of enemies.
   */
  public List<Ennemi> getAliveEnemies() {
    return List.copyOf(enemies);
  }

  /**
   * Checks if it is the hero's turn.
   * 
   * @return {@code true} if it is the hero's turn.
   */
  public boolean isHeroTurn() {
    return isHeroTurn;
  }

  /**
   * Génère une liste de 2 à 3 objets aléatoires pour le héros. Respecte les
   * probabilités de rareté définies dans StuffFactory.
   */
  private List<ItemInstance> generateRewards() {
    var factory = new StuffFactory();
    var rewards = new ArrayList<ItemInstance>();
    int count = 2 + new java.util.Random().nextInt(2); // 2 ou 3 items

    for (int i = 0; i < count; i++) {
      rewards.add(new ItemInstance(factory.randomItem()));
    }
    return rewards;
  }

  /**
   * Gère la fin du combat.
   * 
   * @return La liste des trésors gagnés si le héros a gagné
   */
  public List<ItemInstance> finishCombat() {
    heros.decrementCursePenaltyDuration();
    if (getState() == CombatState.WIN) {
      heros.gainXp(10); // Bonus XP de victoire
      return generateRewards();
    }
    return List.of();
  }
  
  
}