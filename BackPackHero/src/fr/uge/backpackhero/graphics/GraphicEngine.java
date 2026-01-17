package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;
import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.data.HallOfFame;
import fr.uge.backpackhero.data.ScoreEntry;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.*;
import java.util.List;
import java.util.Objects;
import java.awt.*;

/**
 * Main graphical engine for rendering the game world and handling user interactions.
 */
public class GraphicEngine {
  private final Jeu jeu;
  private final ImageLoader img = new ImageLoader();
  private final ViewGraphic viewGraphic;
  private static final int TILE_SIZE = 64;

  private int mouseX, mouseY;
  private int dungeonStartX, dungeonStartY;
  private int backpackStartX, backpackStartY;

  private String messageFlash = "";
  private int messageTimer = 0;
  
  private final HallOfFame hof;

  /**
   * Constructs the engine.
   * @param jeu the game state controller.
   * @param viewGraphic the interaction view manager.
   */
  public GraphicEngine(Jeu jeu, ViewGraphic viewGraphic, HallOfFame hof) {
    this.jeu = Objects.requireNonNull(jeu);
    this.viewGraphic = Objects.requireNonNull(viewGraphic);
    this.hof = Objects.requireNonNull(hof);
  }

  /**
   * Launches the main application loop.
   */
  public void start() {
    Application.run(Color.decode("#0d0d0d"), context -> {
      while (true) {
        processApplicationEvents(context);
        updateState();
        renderFrame(context);
        waitForNextFrame();
      }
    });
  }

  /**
   * Handles the mandatory InterruptedException for Thread.sleep.
   * This is the only 'try' allowed outside of Main due to API constraints.
   */
  private void waitForNextFrame() {
    try {
      Thread.sleep(15);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private void processApplicationEvents(ApplicationContext context) {
    Event event = context.pollEvent();
    if (event instanceof KeyboardEvent kb && kb.action() == KeyboardEvent.Action.KEY_PRESSED) {
      handleKeyboardGlobal(kb, context);
    } else if (event instanceof PointerEvent pe) {
      mouseX = pe.location().x();
      mouseY = pe.location().y();
      if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
        handleMouse(pe, context.getScreenInfo());
      }
    }
  }

  private void handleKeyboardGlobal(KeyboardEvent kb, ApplicationContext context) {
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE && kb.key() == KeyboardEvent.Key.ESCAPE) {
      viewGraphic.handleKeyPress(kb.key());
    } else if (kb.key() == KeyboardEvent.Key.ESCAPE) {
      context.dispose();
      System.exit(0);
    } else {
      handleInput(kb);
    }
  }

  private void updateState() {
    String nouveauMessage = jeu.pollMessage();
    if (nouveauMessage != null) {
      this.messageFlash = nouveauMessage;
      this.messageTimer = 120;
    }
  }

  private void renderFrame(ApplicationContext context) {
    context.renderFrame(g -> {
      ScreenInfo screenInfo = context.getScreenInfo();
      calculatePositions(screenInfo);
      renderBackground(g, screenInfo);
      renderDungeon(g, screenInfo);
      renderBackpack(g, screenInfo);
      renderHUD(g);
      renderFlashMessage(g, screenInfo);
      renderControls(g, screenInfo);
      renderActiveUI(g, screenInfo);
      viewGraphic.render(g, screenInfo);
      renderGhostItem(g);
    });
  }

  private void renderActiveUI(Graphics2D g, ScreenInfo screenInfo) {
    var floor = jeu.getDonjon().getCurrentFloor();
    var currentRoom = floor.getRoom(jeu.getX(), jeu.getY());
    switch (jeu.getMode()) {
      case BOUTIQUE -> {
        if (currentRoom instanceof MerchantRoom merchant) renderMerchantUI(g, merchant);
      }
      case SOIN -> renderHealerUI(g, screenInfo);
      case COMBAT -> renderCombatUI(g, screenInfo);
      case PERDU -> renderGameOver(g, screenInfo);
      case GAGNE -> renderVictory(g, screenInfo);
      default -> {}
    }
  }

  private void calculatePositions(ScreenInfo screenInfo) {
    var floor = jeu.getDonjon().getCurrentFloor();
    var bp = jeu.getHeros().getBackpack();
    dungeonStartX = (screenInfo.width() - (floor.width() * TILE_SIZE)) / 2;
    dungeonStartY = 100;
    backpackStartX = screenInfo.width() - (bp.getWidth() * TILE_SIZE) - 50;
    backpackStartY = 150;
  }

  private void renderBackground(Graphics2D g, ScreenInfo info) {
    g.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, info.height(), Color.decode("#1a0f00")));
    g.fillRect(0, 0, info.width(), info.height());
  }

  private void renderDungeon(Graphics2D g, ScreenInfo screenInfo) {
    var floor = jeu.getDonjon().getCurrentFloor();
    for (int y = 0; y < floor.height(); y++) {
      for (int x = 0; x < floor.width(); x++) {
        drawDungeonCell(g, x, y);
      }
    }
    drawDungeonHover(g, floor);
  }

  private void drawDungeonCell(Graphics2D g, int x, int y) {
    int px = dungeonStartX + x * TILE_SIZE;
    int py = dungeonStartY + y * TILE_SIZE;
    var room = jeu.getDonjon().getCurrentFloor().getRoom(x, y);
    if (room != null) {
      g.drawImage(img.getImage("tile_floor"), px, py, TILE_SIZE, TILE_SIZE, null);
      renderRoomContent(g, room, px, py);
    } else {
      g.drawImage(img.getImage("wall"), px, py, TILE_SIZE, TILE_SIZE, null);
    }
    if (x == jeu.getX() && y == jeu.getY()) {
      g.drawImage(img.getImage("heros"), px, py, TILE_SIZE, TILE_SIZE, null);
    }
  }

  private void drawDungeonHover(Graphics2D g, fr.uge.backpackhero.donjon.Floor floor) {
    int hc = (mouseX - dungeonStartX) / TILE_SIZE;
    int hr = (mouseY - dungeonStartY) / TILE_SIZE;
    if (hc >= 0 && hc < floor.width() && hr >= 0 && hr < floor.height()) {
      g.setColor(new Color(255, 255, 255, 80));
      g.setStroke(new BasicStroke(3));
      g.drawRect(dungeonStartX + hc * TILE_SIZE, dungeonStartY + hr * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }
  }

  private void renderRoomContent(Graphics2D g, Room room, int px, int py) {
    room.draw(g, px, py, TILE_SIZE, img);
  }

  private void renderBackpack(Graphics2D g, ScreenInfo screenInfo) {
    var bp = jeu.getHeros().getBackpack();
    g.setColor(Color.WHITE);
    g.drawString("BACKPACK", backpackStartX, backpackStartY - 10);
    renderBackpackGrid(g, bp);
    bp.getItems().forEach(item -> {
      if (item.getPos() != null) {
        drawItem(g, item, backpackStartX + item.getPos().column() * TILE_SIZE, 
                 backpackStartY + item.getPos().row() * TILE_SIZE, TILE_SIZE);
      }
    });
    drawBackpackHover(g, bp);
  }

  private void renderBackpackGrid(Graphics2D g, BackPack bp) {
    for (int r = 0; r < bp.getHeight(); r++) {
      for (int c = 0; c < bp.getWidth(); c++) {
        int px = backpackStartX + c * TILE_SIZE;
        int py = backpackStartY + r * TILE_SIZE;
        boolean unlocked = bp.getUnlockedTiles().contains(new Position(r, c));
        g.setColor(unlocked ? new Color(255, 255, 255, 40) : new Color(50, 50, 50, 150));
        g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        g.setColor(new Color(255, 255, 255, 100));
        g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
      }
    }
  }

  private void drawBackpackHover(Graphics2D g, BackPack bp) {
    int hc = (mouseX - backpackStartX) / TILE_SIZE;
    int hr = (mouseY - backpackStartY) / TILE_SIZE;
    if (hc >= 0 && hc < bp.getWidth() && hr >= 0 && hr < bp.getHeight()) {
      g.setColor(new Color(100, 200, 255, 100));
      g.setStroke(new BasicStroke(2));
      g.drawRect(backpackStartX + hc * TILE_SIZE, backpackStartY + hr * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }
  }

  private void renderHUD(Graphics2D g) {
    var h = jeu.getHeros();
    g.setColor(new Color(50, 50, 50));
    g.fillRect(50, 30, 250, 25);
    float ratio = (float) h.getPv() / h.getPvMax();
    g.setColor(ratio > 0.3 ? new Color(46, 204, 113) : new Color(231, 76, 60));
    g.fillRect(50, 30, (int) (250 * ratio), 25);
    g.setColor(Color.WHITE);
    g.drawRect(50, 30, 250, 25);
    g.drawString("HP: " + h.getPv() + " / " + h.getPvMax(), 120, 48);
    g.drawString("Gold: " + h.getGold(), 350, 48);
    g.drawString("Floor: " + jeu.getDonjon().getFloorNumber(), 500, 48);
  }

  private void renderControls(Graphics2D g, ScreenInfo info) {
    int cx = info.width() / 2;
    int y = info.height() - 80;
    g.setColor(new Color(0, 0, 0, 180));
    g.fillRoundRect(cx - 350, y - 10, 700, 70, 15, 15);
    g.setColor(Color.WHITE);
    g.drawString("Move: ZQSD | Inventory: I | Reorganize: O", cx - 300, y + 35);
  }

  private void handleInput(KeyboardEvent kb) {
    if (jeu.getMode() == Mode.PERDU || jeu.getMode() == Mode.GAGNE) return;
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE) {
      viewGraphic.handleKeyPress(kb.key());
      return;
    }
    switch (kb.key()) {
      case Z -> jeu.deplacer(0, -1);
      case S -> jeu.deplacer(0, 1);
      case Q -> jeu.deplacer(-1, 0);
      case D -> jeu.deplacer(1, 0);
      case I -> jeu.getView().printBackPack();
      case O -> viewGraphic.reorganize();
      default -> {}
    }
  }

  private void handleMouse(PointerEvent pe, ScreenInfo info) {
    int mx = pe.location().x();
    int my = pe.location().y();
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE) {
      viewGraphic.handleMouseClick(mx, my, backpackStartX, backpackStartY);
      return;
    }
    handleContextualMouse(mx, my, info);
  }

  private void handleContextualMouse(int mx, int my, ScreenInfo info) {
    switch (jeu.getMode()) {
      case COMBAT -> handleCombatClick(mx, my, info);
      case BOUTIQUE -> gererClicBoutique(mx, my);
      case SOIN -> handleHealerClick(mx, my, info);
      default -> handleDungeonClick(mx, my);
    }
  }

  private void handleDungeonClick(int mx, int my) {
    int col = (mx - dungeonStartX) / TILE_SIZE;
    int row = (my - dungeonStartY) / TILE_SIZE;
    if (Math.abs(col - jeu.getX()) + Math.abs(row - jeu.getY()) <= 1) {
      jeu.deplacer(col - jeu.getX(), row - jeu.getY());
    }
  }

  private void gererClicBoutique(int mx, int my) {
    int sx = 200, sy = 250;
    if (mx >= sx + 150 && mx <= sx + 350 && my >= sy + 310 && my <= sy + 350) {
      jeu.setMode(Mode.EXPLORATION);
    } else if (!detecterAchat(mx, my, sx, sy)) {
      detecterVente(mx, my);
    }
  }

  private boolean detecterAchat(int mx, int my, int sx, int sy) {
    var room = jeu.getDonjon().getCurrentFloor().getRoom(jeu.getX(), jeu.getY());
    if (room instanceof MerchantRoom merchant) {
      for (int i = 0; i < merchant.stock().size(); i++) {
        int ix = sx + 50 + (i * 150), iy = sy + 100;
        if (mx >= ix && mx <= ix + 80 && my >= iy && my <= iy + 80) {
          effectuerAchat(merchant, i);
          return true;
        }
      }
    }
    return false;
  }

  private void effectuerAchat(MerchantRoom merchant, int index) {
    ItemInstance item = merchant.stock().get(index);
    if (jeu.getHeros().payer(item.getItem().price())) {
      merchant.stock().remove(index);
      viewGraphic.displayItemFound(item);
      viewGraphic.attemptPlacement(item);
    } else {
      this.messageFlash = "Not enough gold!";
      this.messageTimer = 100;
    }
  }

  private void detecterVente(int mx, int my) {
    int col = (mx - backpackStartX) / TILE_SIZE;
    int row = (my - backpackStartY) / TILE_SIZE;
    var bp = jeu.getHeros().getBackpack();
    if (col >= 0 && col < bp.getWidth() && row >= 0 && row < bp.getHeight()) {
      bp.getItemAt(new Position(row, col)).ifPresent(this::procederVente);
    }
  }

  private void procederVente(ItemInstance item) {
    jeu.getHeros().getBackpack().removeItem(item);
    jeu.getHeros().gagnerOr(item.getItem().price());
    this.messageFlash = "Item sold!";
    this.messageTimer = 100;
  }

  private void handleHealerClick(int mx, int my, ScreenInfo info) {
    int sx = (info.width() - 300) / 2, sy = 200;
    if (mx >= sx + 50 && mx <= sx + 250 && my >= sy + 100 && my <= sy + 140) {
      triggerHeal(5, 10);
    } else if (mx >= sx + 50 && mx <= sx + 250 && my >= sy + 200 && my <= sy + 240) {
      triggerHeal(15, jeu.getHeros().getPvMax());
    } else if (mx >= sx + 50 && mx <= sx + 250 && my >= sy + 280 && my <= sy + 320) {
      jeu.setMode(Mode.EXPLORATION);
    }
  }

  private void triggerHeal(int cost, int amount) {
    if (jeu.getHeros().getPv() < jeu.getHeros().getPvMax() && jeu.getHeros().payer(cost)) {
      jeu.getHeros().soigner(amount);
      this.messageFlash = "Healed!";
    } else {
      this.messageFlash = "Not enough gold!";
    }
    this.messageTimer = 100;
  }

  private void handleCombatClick(int mx, int my, ScreenInfo info) {
    var combat = jeu.getCombat();
    if (combat == null) return;
    int bx = (info.width() - 200) / 2;
    if (mx >= bx && mx <= bx + 200 && my >= 520 && my <= 560) {
      executerFinDeTour(combat);
    } else if (!detecterActionSacCombat(mx, my)) {
      detecterClicEnnemi(mx, my, info);
    }
  }

  private void executerFinDeTour(fr.uge.backpackhero.combat.Combat combat) {
    this.messageFlash = "Enemy turn...";
    this.messageTimer = 60;
    combat.startEnemyTurn();
    jeu.getHeros().debuterTourCombat();
    jeu.getHeros().rafraichirMana();
    jeu.updateCombatState();
  }

  private boolean detecterActionSacCombat(int mx, int my) {
    int col = (mx - backpackStartX) / TILE_SIZE;
    int row = (my - backpackStartY) / TILE_SIZE;
    var bp = jeu.getHeros().getBackpack();
    if (col >= 0 && col < bp.getWidth() && row >= 0 && row < bp.getHeight()) {
      var itemOpt = bp.getItemAt(new Position(row, col));
      if (itemOpt.isPresent()) {
        tenterActionCombat(itemOpt.get());
        return true;
      }
    }
    return false;
  }

  private void tenterActionCombat(ItemInstance item) {
    var combat = jeu.getCombat();
    var enemies = combat.getAliveEnemies();
    if (enemies.isEmpty()) return;
    var target = enemies.get(0);
    int oldHp = target.getHp();
    if (combat.tryHeroAction(item, target)) {
      this.messageFlash = "Damage: " + (oldHp - target.getHp());
      processCombatVictory(combat);
    } else {
      this.messageFlash = "No Energy/Mana!";
    }
    this.messageTimer = 80;
  }

  private void processCombatVictory(fr.uge.backpackhero.combat.Combat combat) {
    if (combat.getState() == fr.uge.backpackhero.combat.CombatState.WIN) {
      this.messageFlash = "VICTORY!";
      List<ItemInstance> loot = combat.finishCombat();
      jeu.setMode(Mode.EXPLORATION);
      if (!loot.isEmpty()) viewGraphic.displayItemFound(loot.get(0));
    }
  }

  private void detecterClicEnnemi(int mx, int my, ScreenInfo info) {
    var list = jeu.getCombat().getAliveEnemies();
    int spacing = info.width() / (list.size() + 1);
    for (int i = 0; i < list.size(); i++) {
      int x = (i + 1) * spacing - 64;
      if (mx >= x && mx <= x + 128 && my >= 220 && my <= 420) {
        this.messageFlash = "Target: " + list.get(i).getName();
        this.messageTimer = 60;
      }
    }
  }

  private void renderGameOver(Graphics2D g, ScreenInfo screenInfo) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, screenInfo.width(), screenInfo.height());
    
    g.setColor(Color.RED);
    g.setFont(new Font("Serif", Font.BOLD, 48));
    String text = "VOUS ÊTES MORT";
    int x = (screenInfo.width() - g.getFontMetrics().stringWidth(text)) / 2;
    g.drawString(text, x, 200);
    
    renderHallOfFame(g, screenInfo, hof);
  }

  private void renderVictory(Graphics2D g, ScreenInfo screenInfo) {
    g.setColor(new Color(0, 100, 0, 200));
    g.fillRect(0, 0, screenInfo.width(), screenInfo.height());
    
    g.setColor(Color.YELLOW);
    g.setFont(new Font("Serif", Font.BOLD, 48));
    String text = "VICTOIRE !";
    int x = (screenInfo.width() - g.getFontMetrics().stringWidth(text)) / 2;
    g.drawString(text, x, 200);
    
    renderHallOfFame(g, screenInfo, hof);
  }

  private void renderCombatUI(Graphics2D g, ScreenInfo info) {
    var combat = jeu.getCombat();
    if (combat == null) return;
    g.setColor(new Color(0, 0, 0, 180));
    g.fillRect(0, 0, info.width(), 600);
    renderEnemies(g, combat.getAliveEnemies(), info);
    drawButton(g, "End Turn", (info.width() - 200) / 2, 520, 200, 40, Color.RED);
  }

  private void renderEnemies(Graphics2D g, List<Ennemi> ennemis, ScreenInfo info) {
    int spacing = info.width() / (ennemis.size() + 1);
    for (int i = 0; i < ennemis.size(); i++) {
      drawEnemyCombat(g, ennemis.get(i), (i + 1) * spacing - 64, 250);
    }
  }

  private void drawEnemyCombat(Graphics2D g, Ennemi e, int x, int y) {
    g.drawImage(img.getImage(e.getName()), x, y, 128, 128, null);
    renderEnemyHealthBar(g, e, x, y - 30);
    renderEnemyIntent(g, e, x, y);
    renderEnemyStatus(g, e, x, y);
    if (mouseX >= x && mouseX <= x + 128 && mouseY >= y - 30 && mouseY <= y + 170) {
      drawAttackButton(g, x + 14, y + 45);
    }
  }

  private void renderEnemyStatus(Graphics2D g, Ennemi e, int x, int y) {
    int offset = 0;
    for (fr.uge.backpackhero.combat.Effect effect : fr.uge.backpackhero.combat.Effect.values()) {
      int stacks = e.getStatus(effect);
      if (stacks > 0) {
        g.setColor(Color.CYAN);
        g.drawString(effect.getNom() + " : " + stacks, x + 135, y + 20 + offset);
        offset += 15;
      }
    }
  }

  private void renderEnemyIntent(Graphics2D g, Ennemi e, int x, int y) {
    var action = e.getActionAnnoncee();
    if (action != null) {
      g.setColor(Color.YELLOW);
      g.drawString("Intent: " + action.description(), x, y - 45);
    }
  }

  private void renderHealerUI(Graphics2D g, ScreenInfo info) {
    int sx = (info.width() - 300) / 2, sy = 200;
    g.setColor(new Color(20, 20, 20, 240));
    g.fillRoundRect(sx, sy, 300, 350, 15, 15);
    g.setColor(Color.WHITE);
    g.drawString("Healer", sx + 120, sy + 50);
    drawButton(g, "Heal (5 Gold)", sx + 50, sy + 100, 200, 40, Color.GREEN);
    drawButton(g, "Full (15 Gold)", sx + 50, sy + 200, 200, 40, Color.CYAN);
    drawButton(g, "Leave", sx + 50, sy + 280, 200, 40, Color.GRAY);
  }

  private void renderMerchantUI(Graphics2D g, MerchantRoom room) {
    int sx = 200, sy = 250;
    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(sx, sy, 500, 380, 20, 20);
    g.setColor(Color.WHITE);
    g.drawString("Merchant Shop", sx + 150, sy + 50);
    for (int i = 0; i < room.stock().size(); i++) {
      renderStoreItem(g, room.stock().get(i), sx + 50 + (i * 150), sy + 100);
    }
    drawButton(g, "Leave", sx + 150, sy + 310, 200, 40, Color.GRAY);
  }

  private void renderStoreItem(Graphics2D g, ItemInstance item, int x, int y) {
    g.drawImage(img.getImage(item.getItem().name().replace(" ", "_")), x, y, 80, 80, null);
    g.setColor(Color.YELLOW);
    g.drawString(item.getItem().price() + " Gold", x + 10, y + 110);
  }

  private void renderGhostItem(Graphics2D g) {
    ItemInstance item = viewGraphic.getCurrentItem();
    boolean active = viewGraphic.getMode() == ViewGraphic.InteractionMode.WAITING_POSITION 
                  || viewGraphic.getMode() == ViewGraphic.InteractionMode.REORGANIZE;
    if (item != null && active) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
      int col = (mouseX - backpackStartX) / TILE_SIZE;
      int row = (mouseY - backpackStartY) / TILE_SIZE;
      int dx = (col >= 0 && col < jeu.getHeros().getBackpack().getWidth()) ? 
                backpackStartX + col * TILE_SIZE : mouseX - 32;
      int dy = (row >= 0 && row < jeu.getHeros().getBackpack().getHeight()) ? 
                backpackStartY + row * TILE_SIZE : mouseY - 32;
      drawItem(g, item, dx, dy, TILE_SIZE);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
  }

  private void drawItem(Graphics2D g, ItemInstance item, int px, int py, int cellSize) {
    String name = item.getItem().name().replace(" ", "_");
    Image image = img.getImage(name);
    if (image == null) return;
    var shape = item.getItem().shapeAtRotation(0);
    int bw = shape.stream().mapToInt(Position::column).max().getAsInt() + 1;
    int bh = shape.stream().mapToInt(Position::row).max().getAsInt() + 1;
    var transform = g.getTransform();
    g.translate(px, py);
    applyRotationLogic(g, item, cellSize, bw, bh);
    g.drawImage(image, 0, 0, bw * cellSize, bh * cellSize, null);
    g.setTransform(transform);
  }

  private void applyRotationLogic(Graphics2D g, ItemInstance item, int size, int bw, int bh) {
    int angle = item.getRotationAngle();
    if (angle != 0) {
      g.rotate(Math.toRadians(angle), size / 2.0, size / 2.0);
      if (angle == 90)  g.translate(0, -bh * size + size);
      if (angle == 180) g.translate(-bw * size + size, -bh * size + size);
      if (angle == 270) g.translate(-bw * size + size, 0);
    }
  }

  public ViewGraphic getViewGraphic() {
    return viewGraphic;
  }

  private void renderFlashMessage(Graphics2D g, ScreenInfo info) {
    if (messageTimer > 0) {
      g.setFont(new Font("Arial", Font.BOLD, 22));
      g.setColor(new Color(255, 50, 50, Math.min(255, messageTimer * 5)));
      int x = (info.width() - g.getFontMetrics().stringWidth(messageFlash)) / 2;
      g.drawString(messageFlash, x, 90);
      messageTimer--;
    }
  }

  private void drawButton(Graphics2D g, String t, int x, int y, int w, int h, Color c) {
    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
    g.fillRect(x, y, w, h);
    g.setColor(c);
    g.drawRect(x, y, w, h);
    g.setColor(Color.WHITE);
    g.drawString(t, x + 20, y + 25);
  }

  private void drawAttackButton(Graphics2D g, int x, int y) {
    g.setColor(new Color(255, 0, 0, 200));
    g.fillRoundRect(x, y, 100, 30, 10, 10);
    g.setColor(Color.WHITE);
    g.drawString("ATTACK", x + 20, y + 20);
  }

  private void renderEnemyHealthBar(Graphics2D g, Ennemi e, int x, int y) {
    g.setColor(Color.GRAY);
    g.fillRect(x, y, 128, 10);
    g.setColor(Color.RED);
    g.fillRect(x, y, (int) (128 * ((float) e.getHp() / e.getMaxHp())), 10);
    g.setColor(Color.WHITE);
    g.drawRect(x, y, 128, 10);
  }
  
  /**
   * Renders the top scores from the Hall of Fame onto the screen.
   * @param g the graphics context.
   * @param info screen dimensions and info.
   * @param hof the persistent high score manager.
   */
  /**
   * Renders the top scores on the screen.
   */
  private void renderHallOfFame(Graphics2D g, ScreenInfo info, HallOfFame hof) {
    int startX = info.width() / 2 - 150;
    int startY = 300;
    
    g.setFont(new Font("Arial", Font.BOLD, 24));
    g.setColor(Color.ORANGE);
    g.drawString("--- HALL OF FAME ---", startX, startY);
    
    List<ScoreEntry> entries = getSafeScores(hof);
    for (int i = 0; i < entries.size(); i++) {
      renderScoreLine(g, entries.get(i), startX, startY + 40 + (i * 30), i + 1);
    }
  }

  private void renderScoreLine(Graphics2D g, ScoreEntry e, int x, int y, int rank) {
    g.setFont(new Font("Arial", Font.PLAIN, 18));
    g.setColor(Color.WHITE);
    String line = rank + ". " + e.playerName() + " : " + e.score() + " pts";
    g.drawString(line, x, y);
  }

  private List<ScoreEntry> getSafeScores(HallOfFame hof) {
    try {
      return hof.loadScores();
    } catch (java.io.IOException e) {
      return List.of(); 
    }
  }
}