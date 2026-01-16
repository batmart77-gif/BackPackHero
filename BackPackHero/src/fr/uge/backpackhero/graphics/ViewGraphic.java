package fr.uge.backpackhero.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.forax.zen.*;

import fr.uge.backpackhero.combat.CombatInteractionDelegate;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.*;

public class ViewGraphic implements CombatInteractionDelegate {
  private final BackPack backPack;
  private final StuffFactory factory;
  private final Heros heros;
  private final ImageLoader img = new ImageLoader();
  
  private ItemInstance currentItem = null;
  private InteractionMode mode = InteractionMode.NONE;
  private final BlockingQueue<UserAction> actionQueue = new LinkedBlockingQueue<>();
  
  private List<ItemInstance> itemsToReorganize = new ArrayList<>();
  private int currentReorganizeIndex = 0;
  private static final int TILE_SIZE = 64;

  public ViewGraphic(BackPack backPack, StuffFactory factory, Heros heros) {
    this.backPack = Objects.requireNonNull(backPack);
    this.factory = Objects.requireNonNull(factory);
    this.heros = Objects.requireNonNull(heros);
  }

  /**
   * Retourne l'objet en cours (utilisé par GraphicEngine).
   */
  public ItemInstance getCurrentItem() {
    return currentItem;
  }

  /**
   * Affiche le sac dans la console (utilisé par GraphicEngine).
   */
  public void printBackPack() {
    System.out.println(backPack.toString());
  }

  /**
   * Gère les touches du clavier. Correction : on utilise 'A' pour valider.
   */
  public void handleKeyInput(KeyboardEvent ke) {
    if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) return;
    KeyboardEvent.Key key = ke.key();

    switch (mode) {
      case ITEM_PLACEMENT -> {
        // On utilise 'A' au lieu de 'ENTER' pour éviter les erreurs de compilation
        if (key == KeyboardEvent.Key.A) actionQueue.offer(new UserAction(ActionType.PLACE));
        else if (key == KeyboardEvent.Key.R) actionQueue.offer(new UserAction(ActionType.ROTATE));
        else if (key == KeyboardEvent.Key.ESCAPE) actionQueue.offer(new UserAction(ActionType.QUIT));
      }
      case FORCED_CURSE -> {
        if (key == KeyboardEvent.Key.A) acceptForcedCurse();
        else if (key == KeyboardEvent.Key.R) refuseForcedCurse();
      }
      case WAITING_POSITION, REORGANIZE -> {
        if (key == KeyboardEvent.Key.R && currentItem != null) currentItem.rotate();
        else if (key == KeyboardEvent.Key.ESCAPE) mode = InteractionMode.NONE;
      }
      default -> {}
    }
  }

  public boolean interactBeforePlacement(ItemInstance item) {
    this.currentItem = item;
    this.mode = InteractionMode.ITEM_PLACEMENT;
     return true;
  }

  @Override public void handleForcedCurse(Heros h, Curse c) { this.mode = InteractionMode.FORCED_CURSE; this.currentItem = new ItemInstance(c); }
  @Override public void handleLevelUpExpansion(int lv) { this.mode = InteractionMode.LEVEL_UP; }

  public void handleMouseClick(int x, int y, int bpX, int bpY) {
    int col = (x - bpX) / TILE_SIZE;
    int row = (y - bpY) / TILE_SIZE;
    if (mode == InteractionMode.WAITING_POSITION || mode == InteractionMode.REORGANIZE) {
      if (col >= 0 && col < backPack.getWidth() && row >= 0 && row < backPack.getHeight()) {
        if (currentItem != null && backPack.add(currentItem, new Position(row, col))) {
          if (mode == InteractionMode.REORGANIZE) handleNextReorganize();
          else { mode = InteractionMode.NONE; currentItem = null; }
        }
      }
    } else if (mode == InteractionMode.LEVEL_UP) {
      if (col >= 0 && col < backPack.getWidth() && row >= 0 && row < backPack.getHeight()) {
        backPack.unlockTile(new Position(row, col));
      }
    }
  }

  private void handleNextReorganize() {
    currentReorganizeIndex++;
    if (currentReorganizeIndex < itemsToReorganize.size()) {
      currentItem = itemsToReorganize.get(currentReorganizeIndex);
    } else { mode = InteractionMode.NONE; currentItem = null; }
  }

  private void acceptForcedCurse() { this.mode = InteractionMode.WAITING_POSITION; }
  private void refuseForcedCurse() { heros.takeDamage(5); this.mode = InteractionMode.NONE; this.currentItem = null; }

  public void render(Graphics2D g, ScreenInfo screenInfo) {
    switch (mode) {
      case ITEM_PLACEMENT -> renderItemPlacement(g, screenInfo);
      case FORCED_CURSE -> renderForcedCurseDialog(g, screenInfo);
      case WAITING_POSITION -> renderWaitingPosition(g, screenInfo);
      case LEVEL_UP -> renderLevelUpDialog(g, screenInfo);
      case REORGANIZE -> renderReorganizeInterface(g, screenInfo);
      default -> {}
    }
  }

  private void renderForcedCurseDialog(Graphics2D g, ScreenInfo screenInfo) {
    int x = (screenInfo.width() - 400) / 2;
    g.setColor(new Color(50, 0, 50, 240)); g.fillRoundRect(x, 150, 400, 200, 20, 20);
    g.setColor(Color.WHITE); g.drawString("💀 MALÉDICTION ENNEMIE 💀", x + 80, 200);
    g.drawString("Touches : [A] Accepter | [R] Refuser", x + 80, 240);
  }

  private void renderItemPlacement(Graphics2D g, ScreenInfo screenInfo) {
    int x = (screenInfo.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 220)); g.fillRoundRect(x, 100, 400, 200, 20, 20);
    g.setColor(Color.WHITE); g.drawString("OBJET : " + (currentItem != null ? currentItem.getName() : ""), x + 80, 150);
    g.drawString("[A] Valider | [R] Tourner", x + 80, 220);
  }

  private void renderWaitingPosition(Graphics2D g, ScreenInfo screenInfo) {
    int x = (screenInfo.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 150)); g.fillRoundRect(x, 20, 400, 60, 10, 10);
    g.setColor(Color.WHITE); g.drawString("Cliquez dans le sac pour poser l'objet", x + 80, 55);
  }

  private void renderLevelUpDialog(Graphics2D g, ScreenInfo screenInfo) {
    int x = (screenInfo.width() - 400) / 2;
    g.setColor(new Color(255, 215, 0, 200)); g.fillRoundRect(x, 150, 400, 100, 20, 20);
    g.setColor(Color.BLACK); g.drawString("⭐ LEVEL UP ! Cliquez sur le sac ⭐", x + 80, 205);
  }

  private void renderReorganizeInterface(Graphics2D g, ScreenInfo screenInfo) {
    int x = (screenInfo.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 200)); g.fillRoundRect(x, 50, 400, 100, 15, 15);
    if (currentItem != null) { g.setColor(Color.WHITE); g.drawString("Réorganiser : " + currentItem.getName(), x + 50, 100); }
  }

  public void reorganize() {
    this.mode = InteractionMode.REORGANIZE;
    this.itemsToReorganize = backPack.removeAllItems();
    this.currentReorganizeIndex = 0;
    if (itemsToReorganize.isEmpty()) this.mode = InteractionMode.NONE;
    else this.currentItem = itemsToReorganize.get(0);
  }

  public void attemptPlacement(ItemInstance item) { this.mode = InteractionMode.WAITING_POSITION; }
  public void displayItemFound(ItemInstance instance) { this.currentItem = instance; this.mode = InteractionMode.ITEM_PLACEMENT; }
  public InteractionMode getMode() { return mode; }

  public enum InteractionMode { NONE, ITEM_PLACEMENT, WAITING_POSITION, FORCED_CURSE, LEVEL_UP, REORGANIZE, CURSE_REMOVAL }
  private enum ActionType { PLACE, QUIT, ROTATE, REMOVE }
  private record UserAction(ActionType type) {}
  
  private void abandonnerItem() {
    this.mode = InteractionMode.NONE;
    this.currentItem = null;
    // Si on était en train de réorganiser, on arrête tout
    this.itemsToReorganize.clear();
}
  
  /**
   * MÉTHODE CORRIGÉE : Correspond maintenant à l'appel de GraphicEngine.
   * @param key La touche pressée récupérée depuis KeyboardEvent.key()
   */
  public void handleKeyPress(KeyboardEvent.Key key) {
    // On ne vérifie plus l'action car le GraphicEngine s'en occupe déjà
    switch (mode) {
      case ITEM_PLACEMENT -> {
        // Rappel : On utilise 'A' pour valider et éviter les bugs de touche ENTER
        if (key == KeyboardEvent.Key.A) this.mode = InteractionMode.WAITING_POSITION;
        else if (key == KeyboardEvent.Key.R) if (currentItem != null) currentItem.rotate();
        else if (key == KeyboardEvent.Key.ESCAPE) {abandonnerItem();}
      }
      case FORCED_CURSE -> {
        if (key == KeyboardEvent.Key.A) acceptForcedCurse();
        else if (key == KeyboardEvent.Key.R) refuseForcedCurse();
      }
      case WAITING_POSITION, REORGANIZE -> {
        if (key == KeyboardEvent.Key.R && currentItem != null) currentItem.rotate();
        else if (key == KeyboardEvent.Key.ESCAPE) { mode = InteractionMode.NONE; this.currentItem = null;}
      }
      default -> {}
    }
  }
}