package fr.uge.backpackhero.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.github.forax.zen.*;
import fr.uge.backpackhero.combat.CombatInteractionDelegate;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.item.*;

/**
 * Manages graphical interactions related to the backpack, such as item placement,
 * reorganization, level-up expansions, and curse handling.
 */
public class ViewGraphic implements CombatInteractionDelegate {
  private final BackPack backPack;
  private final Heros heros;
  private ItemInstance currentItem = null;
  private InteractionMode mode = InteractionMode.NONE;
  private List<ItemInstance> itemsToReorganize = new ArrayList<>();
  private int currentReorganizeIndex = 0;
  private static final int TILE_SIZE = 64;

  /**
   * Constructs the view with the required game components.
   * @param backPack the hero's backpack.
   * @param factory the factory for item generation.
   * @param heros the hero instance.
   */
  public ViewGraphic(BackPack backPack, StuffFactory factory, Heros heros) {
    this.backPack = Objects.requireNonNull(backPack);
    this.heros = Objects.requireNonNull(heros);
  }

  /**
   * Switches the interface to reorganization mode by removing all items from the bag.
   */
  public void reorganize() {
    this.mode = InteractionMode.REORGANIZE;
    this.itemsToReorganize = backPack.removeAllItems();
    this.currentReorganizeIndex = 0;
    if (itemsToReorganize.isEmpty()) {
      this.mode = InteractionMode.NONE;
    } else {
      this.currentItem = itemsToReorganize.get(0);
    }
  }

  /**
   * Processes keyboard input based on the current interaction mode.
   * @param key the key pressed by the user.
   */
  public void handleKeyPress(KeyboardEvent.Key key) {
    Objects.requireNonNull(key);
    switch (mode) {
      case ITEM_PLACEMENT -> handlePlacementKeys(key);
      case FORCED_CURSE -> handleCurseKeys(key);
      case WAITING_POSITION, REORGANIZE -> handlePositioningKeys(key);
      default -> { }
    }
  }

  private void handlePlacementKeys(KeyboardEvent.Key key) {
    if (key == KeyboardEvent.Key.A) {
      this.mode = InteractionMode.WAITING_POSITION;
    } else if (key == KeyboardEvent.Key.R && currentItem != null) {
      currentItem.rotate();
    } else if (key == KeyboardEvent.Key.ESCAPE) {
      abandonnerItem();
    }
  }

  private void handleCurseKeys(KeyboardEvent.Key key) {
    if (key == KeyboardEvent.Key.A) {
      this.mode = InteractionMode.WAITING_POSITION;
    } else if (key == KeyboardEvent.Key.R) {
      refuseForcedCurse();
    }
  }

  private void handlePositioningKeys(KeyboardEvent.Key key) {
    if (key == KeyboardEvent.Key.R && currentItem != null) {
      currentItem.rotate();
    } else if (key == KeyboardEvent.Key.ESCAPE) {
      this.mode = InteractionMode.NONE;
      this.currentItem = null;
    }
  }

  /**
   * Processes mouse clicks to place items or unlock tiles in the backpack.
   * @param x mouse x-coordinate.
   * @param y mouse y-coordinate.
   * @param bpX backpack UI x-start.
   * @param bpY backpack UI y-start.
   */
  public void handleMouseClick(int x, int y, int bpX, int bpY) {
    int col = (x - bpX) / TILE_SIZE;
    int row = (y - bpY) / TILE_SIZE;
    if (mode == InteractionMode.WAITING_POSITION || mode == InteractionMode.REORGANIZE) {
      processItemPlacement(row, col);
    } else if (mode == InteractionMode.LEVEL_UP) {
      processLevelUp(row, col);
    }
  }

  private void processItemPlacement(int row, int col) {
    Position pos = new Position(row, col);
    if (currentItem != null && backPack.add(currentItem, pos)) {
      if (mode == InteractionMode.REORGANIZE) {
        handleNextReorganize();
      } else {
        this.mode = InteractionMode.NONE;
        this.currentItem = null;
      }
    }
  }

  private void processLevelUp(int row, int col) {
    Position pos = new Position(row, col);
    if (row >= 0 && row < backPack.getHeight() && col >= 0 && col < backPack.getWidth()) {
      backPack.unlockTile(pos);
      this.mode = InteractionMode.NONE;
    }
  }

  private void handleNextReorganize() {
    currentReorganizeIndex++;
    if (currentReorganizeIndex < itemsToReorganize.size()) {
      currentItem = itemsToReorganize.get(currentReorganizeIndex);
    } else {
      this.mode = InteractionMode.NONE;
      this.currentItem = null;
    }
  }

  private void refuseForcedCurse() {
    heros.takeDamage(5);
    this.mode = InteractionMode.NONE;
    this.currentItem = null;
  }

  /**
   * Renders the current interaction UI overlay.
   * @param g graphics context.
   * @param screenInfo screen dimensions.
   */
  public void render(Graphics2D g, ScreenInfo screenInfo) {
    Objects.requireNonNull(g);
    Objects.requireNonNull(screenInfo);
    switch (mode) {
      case ITEM_PLACEMENT -> renderPlacementUI(g, screenInfo, "ITEM FOUND: ");
      case FORCED_CURSE -> renderPlacementUI(g, screenInfo, "CURSE: ");
      case WAITING_POSITION -> renderInstruction(g, screenInfo, "Click in backpack to place");
      case LEVEL_UP -> renderInstruction(g, screenInfo, "LEVEL UP! Click a tile to unlock");
      case REORGANIZE -> renderInstruction(g, screenInfo, "Reorganizing items...");
      default -> { }
    }
  }

  private void renderPlacementUI(Graphics2D g, ScreenInfo info, String prefix) {
    int x = (info.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(x, 100, 400, 200, 20, 20);
    g.setColor(Color.WHITE);
    String name = (currentItem != null) ? currentItem.getName() : "";
    g.drawString(prefix + name, x + 50, 150);
    g.drawString("[A] Accept | [R] Rotate | [ESC] Discard", x + 50, 220);
  }

  private void renderInstruction(Graphics2D g, ScreenInfo info, String text) {
    int x = (info.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 150));
    g.fillRoundRect(x, 20, 400, 60, 10, 10);
    g.setColor(Color.WHITE);
    g.drawString(text, x + 50, 55);
  }

  private void abandonnerItem() {
    this.mode = InteractionMode.NONE;
    this.currentItem = null;
    this.itemsToReorganize.clear();
  }

  // --- Delegate & Getters ---
  @Override public void handleForcedCurse(Heros h, Curse c) { 
    this.mode = InteractionMode.FORCED_CURSE; 
    this.currentItem = new ItemInstance(c); 
  }
  @Override public void handleLevelUpExpansion(int lv) { this.mode = InteractionMode.LEVEL_UP; }

  public boolean interactBeforePlacement(ItemInstance item) {
    this.currentItem = item;
    this.mode = InteractionMode.ITEM_PLACEMENT;
    return true;
  }

  public void displayItemFound(ItemInstance instance) { 
    this.currentItem = instance; 
    this.mode = InteractionMode.ITEM_PLACEMENT; 
  }

  public void attemptPlacement(ItemInstance item) { this.mode = InteractionMode.WAITING_POSITION; }
  public ItemInstance getCurrentItem() { return currentItem; }
  public InteractionMode getMode() { return mode; }
  public void printBackPack() { System.out.println(backPack.toString()); }

  /** Interaction modes for the UI. */
  public enum InteractionMode { NONE, ITEM_PLACEMENT, WAITING_POSITION, FORCED_CURSE, LEVEL_UP, REORGANIZE }
}