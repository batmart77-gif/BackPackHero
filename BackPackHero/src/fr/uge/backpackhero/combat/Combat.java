package fr.uge.backpackhero.combat;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.StuffFactory;

/**
 * Manages turn-based combat logic between the hero and enemies.
 * It coordinates actions, status effects, and rewards.
 */
public final class Combat {
  private final Heros heros;
  private final List<Ennemi> enemies;
  private final CombatInteractionDelegate delegate;
  private final Random random = new Random();
  private boolean isHeroTurn;

  /**
   * Initializes a combat session.
   *
   * @param heros       the hero participant.
   * @param listEnemies the list of enemies.
   * @param delegate    the delegate for UI/Forced interactions.
   * @throws NullPointerException if any argument is null.
   */
  public Combat(Heros heros, List<Ennemi> listEnemies, CombatInteractionDelegate delegate) {
    this.heros = Objects.requireNonNull(heros);
    Objects.requireNonNull(listEnemies);
    this.delegate = Objects.requireNonNull(delegate);
    if (listEnemies.isEmpty()) {
      throw new IllegalArgumentException("Combat requires at least one enemy");
    }
    this.enemies = new ArrayList<>(listEnemies);
    startHeroTurn();
  }

  /**
   * Starts the hero's turn by resetting energy and planning enemy actions.
   */
  public void startHeroTurn() {
    this.isHeroTurn = true;
    heros.triggerStartTurnEffects();
    if (!heros.estVivant()) {
      return;
    }
    heros.debuterTourCombat();
    heros.rafraichirMana();
    enemies.stream().filter(Ennemi::estVivant).forEach(Ennemi::choisirProchaineAction);
  }

  /**
   * Executes an action from an item.
   *
   * @param instance the item being used.
   * @param target   the targeted enemy (can be null).
   * @return true if the action was performed.
   */
  public boolean tryHeroAction(ItemInstance instance, Ennemi target) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(instance);
    if (!isHeroTurn || !heros.estVivant()) {
      return false;
    }
    boolean success = instance.getItem().use(heros, target, heros.getBackpack(), instance);
    if (success && target != null && !target.estVivant()) {
      handleEnemyDefeat(target);
    }
    return success;
  }

  private void handleEnemyDefeat(Ennemi target) {
    int levels = heros.gainXp(target.getxpReward());
    if (levels > 0) {
      delegate.handleLevelUpExpansion(levels);
    }
    enemies.remove(target);
  }

  /**
   * Processes enemy actions and ends the turn.
   */
  public void startEnemyTurn() {
    if (!isHeroTurn) {
      return;
    }
    this.isHeroTurn = false;
    heros.triggerEndTurnEffects();
    if (heros.estVivant()) {
      applyAllEnemiesActions();
      checkEndOfTurnTransition();
    }
  }

  private void applyAllEnemiesActions() {
    for (var enemy : enemies) {
      if (enemy.estVivant() && heros.estVivant()) {
        EnemyAction action = enemy.getActionAnnoncee();
        switch (action) {
          case CurseAction c -> delegate.handleForcedCurse(heros, c.curse());
          default -> enemy.executerAction(heros);
        }
        enemy.triggerStartTurnEffects();
      }
    }
  }

  private void checkEndOfTurnTransition() {
    enemies.removeIf(e -> !e.estVivant());
    if (!enemies.isEmpty() && heros.estVivant()) {
      startHeroTurn();
    }
  }

  /**
   * Cleans up combat state and generates rewards if victorious.
   *
   * @return the list of reward items.
   */
  public List<ItemInstance> finishCombat() {
    heros.decrementCursePenaltyDuration();
    if (getState() == CombatState.WIN) {
      int levels = heros.gainXp(10);
      if (levels > 0) {
        delegate.handleLevelUpExpansion(levels);
      }
      return generateRewards();
    }
    return List.of();
  }

  private List<ItemInstance> generateRewards() {
    var factory = new StuffFactory();
    var rewards = new ArrayList<ItemInstance>();
    int count = 2 + random.nextInt(2);
    for (int i = 0; i < count; i++) {
      rewards.add(new ItemInstance(factory.randomItem()));
    }
    return rewards;
  }

  /**
   * Evaluates the current combat status.
   *
   * @return the CombatState.
   */
  public CombatState getState() {
    if (!heros.estVivant()) {
      return CombatState.LOSS;
    }
    return enemies.isEmpty() ? CombatState.WIN : CombatState.IN_PROGRESS;
  }

  public List<Ennemi> getAliveEnemies() {
    return List.copyOf(enemies);
  }

  public boolean isHeroTurn() {
    return isHeroTurn;
  }
}