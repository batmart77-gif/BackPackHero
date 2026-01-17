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
 * Manages graphical interactions related to the backpack, such as item
 * placement, reorganization, level-up expansions, and curse handling.
 */
public class ViewGraphic implements CombatInteractionDelegate {
  private final BackPack backPack;
  private final Heros heros;
  private ItemInstance currentItem = null;
  private InteractionMode mode = InteractionMode.NONE;
  private List<ItemInstance> itemsToReorganize = new ArrayList<>();
  private int currentReorganizeIndex = 0;
  private static final int TILE_SIZE = 64;
  private int tilesLeftToUnlock = 0;

  /**
   * Constructs the view with the required game components.
   * 
   * @param backPack the hero's backpack.
   * @param factory  the factory for item generation.
   * @param heros    the hero instance.
   */
  public ViewGraphic(BackPack backPack, Heros heros) {
    this.backPack = Objects.requireNonNull(backPack);
  
    this.heros = Objects.requireNonNull(heros);
  }

  /**
   * Switches the interface to reorganization mode by removing all items from the
   * bag.
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
   * 
   * @param key the key pressed by the user.
   */
  public void handleKeyPress(KeyboardEvent.Key key) {
    Objects.requireNonNull(key);
    switch (mode) {
    case ITEM_PLACEMENT -> handlePlacementKeys(key);
    case FORCED_CURSE -> handleCurseKeys(key);
    case WAITING_POSITION, REORGANIZE -> handlePositioningKeys(key);
    case LEVEL_UP -> {}
    default -> {
    }
    }
  }

  private void handlePlacementKeys(KeyboardEvent.Key key) {
    if (key == KeyboardEvent.Key.A) {
      this.mode = InteractionMode.WAITING_POSITION;
    } else if (key == KeyboardEvent.Key.R && currentItem != null) {
      currentItem.rotate();
    } else if (key == KeyboardEvent.Key.ESCAPE) {
      abandonnerItem(); // Cette méthode doit être modifiée
    }
  }

  private void handleCurseKeys(KeyboardEvent.Key key) {
    if (key == KeyboardEvent.Key.A) {
      heros.acceptCurseImmediate();
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
   * 
   * @param x   mouse x-coordinate.
   * @param y   mouse y-coordinate.
   * @param bpX backpack UI x-start.
   * @param bpY backpack UI y-start.
   */
  public void handleMouseClick(int mx, int my, int startX, int startY) {
    int col = (mx - startX) / TILE_SIZE;
    int row = (my - startY) / TILE_SIZE;
    if (mx < startX)
      col = (mx - startX - TILE_SIZE + 1) / TILE_SIZE;
    if (my < startY)
      row = (my - startY - TILE_SIZE + 1) / TILE_SIZE;

    switch (mode) {
    case LEVEL_UP -> processLevelUp(row, col);
    case WAITING_POSITION, REORGANIZE -> processItemPlacement(row, col); // Utilise la méthode dédiée
    default -> {
    }
    }
  }

  private void processItemPlacement(int row, int col) {
    Position pos = new Position(row, col);
    if (currentItem != null && backPack.add(currentItem, pos)) {
      if (mode == InteractionMode.REORGANIZE) {
        handleNextReorganize();
      } else {
        // APRES avoir placé l'objet, on vérifie s'il reste des cases à débloquer
        checkPendingLevelUp();
      }
    }
  }

  private void checkPendingLevelUp() {
    if (tilesLeftToUnlock > 0) {
      this.mode = InteractionMode.LEVEL_UP;
    } else {
      this.mode = InteractionMode.NONE;
    }
  }

  private void processLevelUp(int row, int col) {
    Position pos = new Position(row, col);
    // On tente de débloquer la case
    if (backPack.unlockTile(pos)) {
      tilesLeftToUnlock--;
      if (tilesLeftToUnlock <= 0) {
        this.mode = InteractionMode.NONE;
      }
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
    heros.refuseCurseImmediate();
    this.currentItem = null;
    checkPendingLevelUp(); 
}

  /**
   * Renders the current interaction UI overlay.
   * 
   * @param g          graphics context.
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
    default -> {
    }
    }
  }

  private void renderPlacementUI(Graphics2D g, ScreenInfo info, String prefix) {
    int x = (info.width() - 400) / 2;
    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(x, 100, 400, 200, 20, 20);
    g.setColor(Color.WHITE);
    String name = (currentItem != null) ? currentItem.getName() : "";
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.drawString(prefix + name, x + 50, 150);
    g.setFont(new Font("Arial", Font.PLAIN, 16));
    if (mode == InteractionMode.FORCED_CURSE) {
        g.setColor(Color.ORANGE);
        g.drawString("[A] Accept Curse", x + 50, 200);
        g.setColor(Color.RED);
        g.drawString("[R] Refuse (Take Damage!)", x + 50, 240);
    } else {
        g.setColor(Color.WHITE);
        g.drawString("[A] Accept | [R] Rotate", x + 50, 200);
        g.drawString("[ESC] Discard", x + 50, 240);
    }
}

  private void renderInstruction(Graphics2D g, ScreenInfo info, String text) {
    int x = (info.width() - 450) / 2;
    g.setColor(new Color(0, 0, 0, 180));
    g.fillRoundRect(x, 20, 450, 60, 10, 10);
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 18));

    if (mode == InteractionMode.LEVEL_UP) {
      text = "LEVEL UP! Choose " + tilesLeftToUnlock + " more tiles";
      g.setColor(Color.YELLOW);
    }

    g.drawString(text, x + 30, 55);
  }

  private void abandonnerItem() {
    this.currentItem = null;
    this.itemsToReorganize.clear();
    checkPendingLevelUp();
  }

  // --- Delegate & Getters ---
  @Override
  public void handleForcedCurse(Heros h, Curse c) {
    this.mode = InteractionMode.FORCED_CURSE;
    this.currentItem = new ItemInstance(c);
  }

  @Override
  public void handleLevelUpExpansion(int levels) {
    this.tilesLeftToUnlock += levels * 3;
    if (this.mode == InteractionMode.NONE) {
      this.mode = InteractionMode.LEVEL_UP;
    }
  }

  public boolean interactBeforePlacement(ItemInstance item) {
    Objects.requireNonNull(item);
    this.currentItem = item;
    this.mode = InteractionMode.ITEM_PLACEMENT;
    return true;
  }

  public void displayItemFound(ItemInstance instance) {
    Objects.requireNonNull(instance);
    this.currentItem = instance;
    this.mode = InteractionMode.ITEM_PLACEMENT;
  }

  public void attemptPlacement(ItemInstance item) {
    Objects.requireNonNull(item);
    this.mode = InteractionMode.WAITING_POSITION;
  }

  public ItemInstance getCurrentItem() {
    return currentItem;
  }

  public InteractionMode getMode() {
    return mode;
  }

  public void printBackPack() {
    System.out.println(backPack.toString());
  }

  /** Interaction modes for the UI. */
  public enum InteractionMode {
    NONE, ITEM_PLACEMENT, WAITING_POSITION, FORCED_CURSE, LEVEL_UP, REORGANIZE
  }
}