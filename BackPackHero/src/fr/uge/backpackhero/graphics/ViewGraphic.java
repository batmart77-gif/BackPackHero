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
  
  // Pour la réorganisation
  private List<ItemInstance> itemsToReorganize = new ArrayList<>();
  private int currentReorganizeIndex = 0;
  
  private static final int TILE_SIZE = 64;
  
  public ViewGraphic(BackPack backPack, StuffFactory factory, Heros heros) {
    Objects.requireNonNull(backPack);
    Objects.requireNonNull(factory);
    Objects.requireNonNull(heros);
    this.backPack = backPack;
    this.factory = factory;
    this.heros = heros;
  }

  public void displayItemFound(ItemInstance instance) {
    this.currentItem = instance;
    this.mode = InteractionMode.ITEM_PLACEMENT;
    System.out.println("Objet trouvé : " + instance.getName());
  }

  public boolean interactBeforePlacement(ItemInstance item) {
    this.currentItem = item;
    this.mode = InteractionMode.ITEM_PLACEMENT;
    
    try {
      UserAction action = actionQueue.take();
      return switch (action.type) {
        case PLACE -> true;
        case QUIT -> false;
        case ROTATE -> {
          item.rotate();
          yield interactBeforePlacement(item);
        }
        case REMOVE -> {
          yield interactBeforePlacement(item);
        }
      };
    } catch (InterruptedException e) {
      return false;
    }
  }

  public void rotateItem(ItemInstance item) {
    item.rotate();
    System.out.println("Rotation à " + item.getRotationAngle() + "°");
  }

  public void attemptPlacement(ItemInstance item) {
    this.mode = InteractionMode.WAITING_POSITION;
  }

  public void processItemRemoval(Position pos) {
    var instance = backPack.getItemAt(pos).orElse(null);
    if (instance == null) {
      System.err.println("Aucun objet à cette position");
      return;
    }

    if (instance.getItem() instanceof Curse) {
      askCurseRemoval(instance);
    } else {
      backPack.removeItem(instance);
      System.out.println("Objet retiré : " + instance.getName());
    }
  }

  public void askCurseRemoval(ItemInstance inst) {
    this.mode = InteractionMode.CURSE_REMOVAL;
    this.currentItem = inst;
  }

  @Override
  public void handleForcedCurse(Heros heros, Curse curse) {
    this.mode = InteractionMode.FORCED_CURSE;
    this.currentItem = new ItemInstance(curse);
    System.out.println("Malédiction lancée par l'ennemi !");
  }

  public void handleLevelUpExpansion(int levelsGained) {
    int totalTiles = levelsGained * 3;
    this.mode = InteractionMode.LEVEL_UP;
    System.out.println("LEVEL UP ! " + totalTiles + " cases à déverrouiller !");
  }

  public void reorganize() {
    this.mode = InteractionMode.REORGANIZE;
    this.itemsToReorganize = backPack.removeAllItems();
    this.currentReorganizeIndex = 0;
    
    if (itemsToReorganize.isEmpty()) {
      System.out.println("Le sac est vide, rien à réorganiser !");
      this.mode = InteractionMode.NONE;
      return;
    }
    
    this.currentItem = itemsToReorganize.get(0);
    System.out.println("Mode réorganisation : replacez " + itemsToReorganize.size() + " objets");
    System.out.println("Objet 1/" + itemsToReorganize.size() + " : " + currentItem.getName());
  }

  public void render(Graphics2D g, ScreenInfo screenInfo) {
    switch (mode) {
      case ITEM_PLACEMENT -> renderItemPlacement(g, screenInfo);
      case CURSE_REMOVAL -> renderCurseRemovalDialog(g, screenInfo);
      case FORCED_CURSE -> renderForcedCurseDialog(g, screenInfo);
      case LEVEL_UP -> renderLevelUpDialog(g, screenInfo);
      case REORGANIZE -> renderReorganizeInterface(g, screenInfo);
      case WAITING_POSITION -> renderWaitingPosition(g, screenInfo);
      case NONE -> {}
    }
  }

  private void renderWaitingPosition(Graphics2D g, ScreenInfo screenInfo) {
    if (currentItem == null) return;
    
    int panelWidth = 400;
    int panelHeight = 150;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 50;

    g.setColor(new Color(0, 0, 0, 200));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 15, 15);
    g.setColor(Color.CYAN);
    g.setStroke(new BasicStroke(2));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 15, 15);

    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.setColor(Color.WHITE);
    g.drawString("Cliquez dans le sac pour placer", x + 70, y + 50);
    g.drawString(currentItem.getName(), x + 100, y + 80);
    
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.drawString("[R] Rotation | [Echap] Annuler", x + 90, y + 120);
  }

  private void renderItemPlacement(Graphics2D g, ScreenInfo screenInfo) {
    if (currentItem == null) return;

    int panelWidth = 400;
    int panelHeight = 300;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 100;

    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);
    g.setColor(new Color(100, 200, 255));
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 24));
    g.setColor(Color.WHITE);
    g.drawString("OBJET TROUVÉ", x + 120, y + 40);

    String fileName = currentItem.getItem().name().replace(" ", "_");
    Image img = this.img.getImage(fileName);
    g.drawImage(img, x + 150, y + 60, 100, 100, null);

    g.setFont(new Font("Arial", Font.PLAIN, 16));
    g.drawString("Nom : " + currentItem.getName(), x + 50, y + 180);
    g.drawString("Taille : " + currentItem.getCurrentShape().size() + " cases", x + 50, y + 210);

    drawButton(g, "[R] Rotation", x + 50, y + 240, 100, 30, Color.CYAN);
    drawButton(g, "[Suppr] Retirer", x + 160, y + 240, 120, 30, Color.ORANGE);
    drawButton(g, "[Entrée] Placer", x + 50, y + 240 + 35, 230, 30, Color.GREEN);
  }

  private void renderCurseRemovalDialog(Graphics2D g, ScreenInfo screenInfo) {
    int panelWidth = 350;
    int panelHeight = 200;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 200;

    g.setColor(new Color(100, 0, 0, 230));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);
    g.setColor(Color.RED);
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 20));
    g.setColor(Color.WHITE);
    g.drawString("⚠ MALÉDICTION ⚠", x + 80, y + 40);

    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.drawString("Retirer cet objet infligera une pénalité !", x + 30, y + 80);

    drawButton(g, "[R] Retirer (-10 PV)", x + 30, y + 120, 140, 30, Color.RED);
    drawButton(g, "[K] Conserver", x + 180, y + 120, 140, 30, Color.GREEN);
  }

  private void renderForcedCurseDialog(Graphics2D g, ScreenInfo screenInfo) {
    int panelWidth = 400;
    int panelHeight = 250;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 150;

    g.setColor(new Color(50, 0, 50, 240));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);
    g.setColor(new Color(200, 0, 200));
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 24));
    g.setColor(Color.MAGENTA);
    g.drawString("💀 MALÉDICTION ENNEMIE 💀", x + 40, y + 40);

    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.setColor(Color.WHITE);
    g.drawString("L'ennemi vous lance une malédiction !", x + 70, y + 80);
    g.drawString("Vous devez choisir :", x + 130, y + 110);

    drawButton(g, "[A] Accepter & Placer", x + 50, y + 150, 140, 30, Color.CYAN);
    g.setFont(new Font("Arial", Font.PLAIN, 11));
    g.drawString("(Place dans le sac)", x + 60, y + 195);

    g.setFont(new Font("Arial", Font.BOLD, 14));
    drawButton(g, "[R] Refuser (-5 PV)", x + 210, y + 150, 140, 30, Color.RED);
    g.setFont(new Font("Arial", Font.PLAIN, 11));
    g.drawString("(Subit des dégâts)", x + 220, y + 195);
  }

  private void renderLevelUpDialog(Graphics2D g, ScreenInfo screenInfo) {
    int panelWidth = 500;
    int panelHeight = 200;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 150;

    g.setColor(new Color(255, 215, 0, 230));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 20, 20);
    g.setColor(Color.YELLOW);
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 32));
    g.setColor(new Color(0, 100, 0));
    g.drawString("⭐ LEVEL UP ! ⭐", x + 120, y + 50);

    g.setFont(new Font("Arial", Font.PLAIN, 18));
    g.setColor(Color.BLACK);
    g.drawString("Vous avez gagné 3 nouvelles cases !", x + 100, y + 100);
    g.drawString("Cliquez sur le sac pour les déverrouiller", x + 70, y + 130);
  }

  private void renderReorganizeInterface(Graphics2D g, ScreenInfo screenInfo) {
    int panelWidth = 400;
    int panelHeight = 180;
    int x = (screenInfo.width() - panelWidth) / 2;
    int y = 50;

    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(x, y, panelWidth, panelHeight, 15, 15);
    g.setColor(Color.CYAN);
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(x, y, panelWidth, panelHeight, 15, 15);

    g.setFont(new Font("Arial", Font.BOLD, 20));
    g.setColor(Color.WHITE);
    g.drawString("MODE RÉORGANISATION", x + 80, y + 40);

    if (currentItem != null) {
      g.setFont(new Font("Arial", Font.PLAIN, 16));
      g.drawString("Objet " + (currentReorganizeIndex + 1) + "/" + itemsToReorganize.size(), x + 140, y + 75);
      g.drawString(currentItem.getName(), x + 80, y + 100);
      g.drawString("Forme : " + currentItem.getCurrentShape(), x + 40, y + 125);
      
      g.setFont(new Font("Arial", Font.PLAIN, 14));
      g.drawString("[R] Rotation | [Clic sac] Placer", x + 80, y + 155);
    }
  }

  private void drawButton(Graphics2D g, String text, int x, int y, int w, int h, Color c) {
    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
    g.fillRoundRect(x, y, w, h, 10, 10);
    g.setColor(c.brighter());
    g.setStroke(new BasicStroke(2));
    g.drawRoundRect(x, y, w, h, 10, 10);
    
    g.setFont(new Font("Arial", Font.BOLD, 12));
    g.setColor(Color.WHITE);
    FontMetrics fm = g.getFontMetrics();
    int textX = x + (w - fm.stringWidth(text)) / 2;
    int textY = y + ((h - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(text, textX, textY);
  }

  public void handleKeyInput(KeyboardEvent ke) {
    if (ke.action() != KeyboardEvent.Action.KEY_PRESSED) return;

    switch (mode) {
      case ITEM_PLACEMENT -> {
        switch (ke.key()) {
          case R -> {
            if (currentItem != null) {
              currentItem.rotate();
              System.out.println("Rotation à " + currentItem.getRotationAngle() + "°");
            }
          }
          case E -> {
            mode = InteractionMode.WAITING_POSITION;
            System.out.println("Cliquez dans le sac pour placer l'objet");
          }
          case D -> actionQueue.offer(new UserAction(ActionType.REMOVE));
          case ESCAPE -> {
            currentItem = null;
            mode = InteractionMode.NONE;
          }
        }
      }
      case WAITING_POSITION, REORGANIZE -> {
        if (ke.key() == KeyboardEvent.Key.R && currentItem != null) {
          currentItem.rotate();
          System.out.println("Rotation à " + currentItem.getRotationAngle() + "°");
        } else if (ke.key() == KeyboardEvent.Key.ESCAPE) {
          if (mode == InteractionMode.REORGANIZE) {
            // Remettre tous les objets dans le sac
            for (ItemInstance item : itemsToReorganize) {
              backPack.getItems().add(item);
            }
            itemsToReorganize.clear();
          }
          currentItem = null;
          mode = InteractionMode.NONE;
        }
      }
      case CURSE_REMOVAL -> {
        switch (ke.key()) {
          case R -> removeCurseWithPenalty();
          case K -> keepCurse();
        }
      }
      case FORCED_CURSE -> {
        switch (ke.key()) {
          case A -> acceptForcedCurse();
          case R -> refuseForcedCurse();
        }
      }
    }
  }

  public void handleMouseClick(int x, int y, int backpackStartX, int backpackStartY) {
    if (mode == InteractionMode.WAITING_POSITION || mode == InteractionMode.REORGANIZE) {
      int col = (x - backpackStartX) / TILE_SIZE;
      int row = (y - backpackStartY) / TILE_SIZE;
      
      if (col >= 0 && col < backPack.getWidth() && row >= 0 && row < backPack.getHeight()) {
        Position pos = new Position(row, col);
        
        if (currentItem != null) {
          System.out.println("Tentative de placement à " + pos);
          System.out.println("Forme actuelle : " + currentItem.getCurrentShape());
          
          boolean placed = backPack.add(currentItem, pos);
          
          if (placed) {
            System.out.println("✓ Objet placé avec succès à " + pos);
            
            if (mode == InteractionMode.REORGANIZE) {
              // Passer à l'objet suivant
              currentReorganizeIndex++;
              if (currentReorganizeIndex < itemsToReorganize.size()) {
                currentItem = itemsToReorganize.get(currentReorganizeIndex);
                System.out.println("Objet suivant : " + currentItem.getName() + 
                                 " (" + (currentReorganizeIndex + 1) + "/" + itemsToReorganize.size() + ")");
              } else {
                // Fin de la réorganisation
                System.out.println("Réorganisation terminée !");
                itemsToReorganize.clear();
                currentItem = null;
                mode = InteractionMode.NONE;
              }
            } else {
              currentItem = null;
              mode = InteractionMode.NONE;
            }
          } else {
            System.err.println("✗ Impossible de placer l'objet ici (cases occupées ou hors limites)");
          }
        }
      }
    } else if (mode == InteractionMode.LEVEL_UP) {
      int col = (x - backpackStartX) / TILE_SIZE;
      int row = (y - backpackStartY) / TILE_SIZE;
      
      if (col >= 0 && col < backPack.getWidth() && row >= 0 && row < backPack.getHeight()) {
        Position pos = new Position(row, col);
        if (backPack.unlockTile(pos)) {
          System.out.println("Case " + pos + " déverrouillée !");
        } else {
          System.err.println("Cette case est déjà déverrouillée ou invalide");
        }
      }
    }
  }
 
  private void removeCurseWithPenalty() {
    if (currentItem != null) {
      backPack.removeItem(currentItem);
      heros.applyCurseRemovalPenalty();
      System.out.println("Malédiction retirée avec pénalité");
      currentItem = null;
      mode = InteractionMode.NONE;
    }
  }

  private void keepCurse() {
    System.out.println("Malédiction conservée");
    currentItem = null;
    mode = InteractionMode.NONE;
  }

  private void acceptForcedCurse() {
    if (currentItem != null) {
      heros.acceptCurseImmediate();
      mode = InteractionMode.WAITING_POSITION;
      System.out.println("Malédiction acceptée, placez-la dans le sac");
    }
  }

  private void refuseForcedCurse() {
    heros.refuseCurseImmediate();
    System.out.println("Malédiction refusée, vous subissez des dégâts");
    currentItem = null;
    mode = InteractionMode.NONE;
  }

  public InteractionMode getMode() {
    return mode;
  }

  public ItemInstance getCurrentItem() {
    return currentItem;
  }

  public enum InteractionMode {
    NONE, ITEM_PLACEMENT, WAITING_POSITION, CURSE_REMOVAL, 
    FORCED_CURSE, LEVEL_UP, REORGANIZE
  }

  private enum ActionType {
    PLACE, QUIT, ROTATE, REMOVE
  }

  private record UserAction(ActionType type) {}
}