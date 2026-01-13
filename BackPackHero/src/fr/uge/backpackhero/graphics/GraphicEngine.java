package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.*;
import java.util.List;

import java.awt.*;

public class GraphicEngine {
  private final Jeu jeu;
  private final ImageLoader img = new ImageLoader();
  private final ViewGraphic viewGraphic;
  private static final int TILE_SIZE = 64;
  
  private ItemInstance selectedItem = null;
  private int mouseX, mouseY;
  
  // Coordonnées calculées pour le donjon et le sac
  private int dungeonStartX, dungeonStartY;
  private int backpackStartX, backpackStartY;

  public GraphicEngine(Jeu jeu) {
    this.jeu = jeu;
    this.viewGraphic = new ViewGraphic(
      jeu.getHeros().getBackpack(), 
      new StuffFactory(), 
      jeu.getHeros()
    );
  }

  public void start() {
    Application.run(Color.decode("#0d0d0d"), context -> {
      while (true) {
        Event event = context.pollEvent();

        if (event instanceof KeyboardEvent kb && kb.action() == KeyboardEvent.Action.KEY_PRESSED) {
          // Gérer ESCAPE en priorité
          if (kb.key() == KeyboardEvent.Key.ESCAPE) {
            context.dispose();
            return;
          }
          handleInput(kb, context);
          viewGraphic.handleKeyInput(kb);
        }

        if (event instanceof PointerEvent pe) {
          mouseX = pe.location().x();
          mouseY = pe.location().y();

          if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
            handleMouse(pe, context.getScreenInfo());
          }
        }

        context.renderFrame(g -> {
          var screenInfo = context.getScreenInfo();
          renderBackground(g, screenInfo);
          calculatePositions(screenInfo);
          renderDungeon(g, screenInfo);
          renderBackpack(g, screenInfo);
          renderHUD(g);
          renderControls(g, screenInfo);

          var floor = jeu.getDonjon().getCurrentFloor();
          var currentRoom = floor.getRoom(jeu.getX(), jeu.getY());

          switch (jeu.getMode()) {
            case BOUTIQUE -> {
              if (currentRoom instanceof fr.uge.backpackhero.donjon.MerchantRoom merchant) {
                renderMerchantUI(g, merchant);
              }
            }
            case SOIN -> renderHealerUI(g, screenInfo);
            case COMBAT -> renderCombatUI(g, screenInfo);
            case PERDU -> renderGameOver(g, screenInfo);
            case GAGNE -> renderVictory(g, screenInfo);
            default -> {}
          }

          viewGraphic.render(g, screenInfo);

          // Afficher l'objet en cours de placement
          ItemInstance itemToShow = viewGraphic.getCurrentItem();
          if (itemToShow != null && (viewGraphic.getMode() == ViewGraphic.InteractionMode.WAITING_POSITION 
              || viewGraphic.getMode() == ViewGraphic.InteractionMode.REORGANIZE)) {
            String fileName = itemToShow.getItem().name().replace(" ", "_");
            Image ghost = img.getImage(fileName);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g.drawImage(ghost, mouseX - 32, mouseY - 32, TILE_SIZE, TILE_SIZE, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
          }
        });

        try {
          Thread.sleep(15);
        } catch (InterruptedException e) {
          return;
        }
      }
    });
  }

  private void calculatePositions(ScreenInfo screenInfo) {
    var floor = jeu.getDonjon().getCurrentFloor();
    var bp = jeu.getHeros().getBackpack();
    
    int dungeonWidth = floor.width() * TILE_SIZE;
    dungeonStartX = (screenInfo.width() - dungeonWidth) / 2;
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
        int px = dungeonStartX + x * TILE_SIZE;
        int py = dungeonStartY + y * TILE_SIZE;
        var room = floor.getRoom(x, y);

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
    }

    int hoverCol = (mouseX - dungeonStartX) / TILE_SIZE;
    int hoverRow = (mouseY - dungeonStartY) / TILE_SIZE;

    if (hoverCol >= 0 && hoverCol < floor.width() && hoverRow >= 0 && hoverRow < floor.height()) {
      g.setColor(new Color(255, 255, 255, 80));
      g.setStroke(new BasicStroke(3));
      g.drawRect(dungeonStartX + hoverCol * TILE_SIZE, dungeonStartY + hoverRow * TILE_SIZE, 
                 TILE_SIZE, TILE_SIZE);
    }
  }

  private void renderRoomContent(Graphics2D g, Room room, int px, int py) {
    room.draw(g, px, py, TILE_SIZE, img);
  }

  private void renderBackpack(Graphics2D g, ScreenInfo screenInfo) {
    var bp = jeu.getHeros().getBackpack();

    g.setFont(new Font("Serif", Font.BOLD, 20));
    g.setColor(Color.WHITE);
    g.drawString("SAC À DOS", backpackStartX, backpackStartY - 10);

    g.setColor(new Color(255, 255, 255, 40));
    for (int r = 0; r < bp.getHeight(); r++) {
      for (int c = 0; c < bp.getWidth(); c++) {
        int px = backpackStartX + c * TILE_SIZE;
        int py = backpackStartY + r * TILE_SIZE;
        
        Position pos = new Position(r, c);
        if (bp.getUnlockedTiles().contains(pos)) {
          g.setColor(new Color(255, 255, 255, 40));
          g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
          g.setColor(new Color(255, 255, 255, 100));
          g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
        } else {
          g.setColor(new Color(50, 50, 50, 150));
          g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
          g.setColor(new Color(100, 100, 100, 100));
          g.drawRect(px, py, TILE_SIZE, TILE_SIZE);
          
          g.setFont(new Font("Arial", Font.BOLD, 24));
          g.setColor(new Color(150, 150, 150, 180));
          g.drawString("#", px + 22, py + 42);
        }
      }
    }

    for (ItemInstance item : bp.getItems()) {
      Position p = item.getPos();
      if (p != null) {
        String nameForFile = item.getItem().name().replace(" ", "_");
        Image objImg = img.getImage(nameForFile);
        if (objImg != null) {
          g.drawImage(objImg, backpackStartX + p.column() * TILE_SIZE, 
                     backpackStartY + p.row() * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }
      }
    }
    
    int hoverCol = (mouseX - backpackStartX) / TILE_SIZE;
    int hoverRow = (mouseY - backpackStartY) / TILE_SIZE;
    
    if (hoverCol >= 0 && hoverCol < bp.getWidth() && hoverRow >= 0 && hoverRow < bp.getHeight()) {
      g.setColor(new Color(100, 200, 255, 100));
      g.setStroke(new BasicStroke(2));
      g.drawRect(backpackStartX + hoverCol * TILE_SIZE, backpackStartY + hoverRow * TILE_SIZE, 
                 TILE_SIZE, TILE_SIZE);
    }
  }

  private void renderHUD(Graphics2D g) {
    int hp = jeu.getHeros().getPv();
    int hpMax = jeu.getHeros().getPvMax();
    int barWidth = 250;

    g.setColor(new Color(50, 50, 50));
    g.fillRect(50, 30, barWidth, 25);

    float ratio = (float) hp / hpMax;
    g.setColor(ratio > 0.3 ? new Color(46, 204, 113) : new Color(231, 76, 60));
    g.fillRect(50, 30, (int) (barWidth * ratio), 25);

    g.setColor(Color.WHITE);
    g.drawRect(50, 30, barWidth, 25);
    g.setFont(new Font("Arial", Font.BOLD, 14));
    g.drawString("HP: " + hp + " / " + hpMax, 120, 48);

    g.drawString("Gold: " + jeu.getHeros().getGold(), 350, 48);
    g.drawString("Niveau: " + jeu.getHeros().getLevel(), 500, 48);
    
    if (jeu.getMode() == Mode.COMBAT) {
      g.drawString("Énergie: " + jeu.getHeros().getEnergie(), 650, 48);
      g.drawString("Mana: " + jeu.getHeros().getMana(), 800, 48);
    }
  }

  private void renderControls(Graphics2D g, ScreenInfo screenInfo) {
    int y = screenInfo.height() - 80;
    int centerX = screenInfo.width() / 2;

    g.setColor(new Color(0, 0, 0, 180));
    g.fillRoundRect(centerX - 350, y - 10, 700, 70, 15, 15);
    g.setColor(new Color(255, 255, 255, 100));
    g.setStroke(new BasicStroke(2));
    g.drawRoundRect(centerX - 350, y - 10, 700, 70, 15, 15);

    g.setFont(new Font("Arial", Font.BOLD, 16));
    g.setColor(Color.YELLOW);
    g.drawString("COMMANDES", centerX - 50, y + 5);

    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.setColor(Color.WHITE);
    g.drawString("Déplacement: Z Q S D", centerX - 300, y + 30);
    g.drawString("Inventaire: I", centerX - 80, y + 30);
    g.drawString("Réorganiser: O", centerX + 60, y + 30);
    g.drawString("Quitter: ESC", centerX + 220, y + 30);
  }

  private void handleInput(KeyboardEvent kb, ApplicationContext context) {
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE) {
      return;
    }
    
    switch (kb.key()) {
      case UP, Z -> jeu.deplacer(0, -1);
      case DOWN, S -> jeu.deplacer(0, 1);
      case LEFT, Q -> jeu.deplacer(-1, 0);
      case RIGHT, D -> jeu.deplacer(1, 0);
      case I -> {
        System.out.println("=== INVENTAIRE ===");
        jeu.getView().printBackPack();
      }
      case O -> {
        viewGraphic.reorganize();
      }
      default -> {}
    }
  }

  private void handleMouse(PointerEvent pe, ScreenInfo screenInfo) {
    if (pe.action() != PointerEvent.Action.POINTER_DOWN)
      return;

    int mx = pe.location().x();
    int my = pe.location().y();

    if (viewGraphic.getMode() == ViewGraphic.InteractionMode.WAITING_POSITION 
        || viewGraphic.getMode() == ViewGraphic.InteractionMode.REORGANIZE
        || viewGraphic.getMode() == ViewGraphic.InteractionMode.LEVEL_UP) {
      viewGraphic.handleMouseClick(mx, my, backpackStartX, backpackStartY);
      return;
    }

    if (jeu.getMode() == Mode.COMBAT) {
      handleCombatClick(mx, my, screenInfo);
      return;
    }

    if (jeu.getMode() == Mode.BOUTIQUE) {
      gererClicBoutique(mx, my);
      return;
    }

    if (jeu.getMode() == Mode.SOIN) {
      handleHealerClick(mx, my, screenInfo);
      return;
    }

    var floor = jeu.getDonjon().getCurrentFloor();
    int col = (mx - dungeonStartX) / TILE_SIZE;
    int row = (my - dungeonStartY) / TILE_SIZE;

    if (col >= 0 && col < floor.width() && row >= 0 && row < floor.height()) {
      var room = floor.getRoom(col, row);

      if (room != null) {
        if (Math.abs(col - jeu.getX()) + Math.abs(row - jeu.getY()) <= 1) {
          jeu.deplacer(col - jeu.getX(), row - jeu.getY());
          room.onClick(jeu);
        }
      }
    }
  }

  private void gererClicBoutique(int mx, int my) {
    if (mx > 250 && mx < 330 && my > 350 && my < 430) {
      System.out.println("Objet acheté graphiquement !");
    }
  }

  private void handleHealerClick(int mx, int my, ScreenInfo screenInfo) {
    int panelWidth = 300;
    int startX = (screenInfo.width() - panelWidth) / 2;
    int startY = 200;
    
    if (mx >= startX + 50 && mx <= startX + 250 && my >= startY + 100 && my <= startY + 140) {
      if (jeu.getHeros().getGold() >= 5) {
        jeu.getHeros().soigner(10);
        jeu.getHeros().payer(-5);
        System.out.println("Soin léger effectué !");
      } else {
        System.out.println("Pas assez d'or !");
      }
    }
    
    if (mx >= startX + 50 && mx <= startX + 250 && my >= startY + 200 && my <= startY + 240) {
      if (jeu.getHeros().getGold() >= 15) {
        jeu.getHeros().soigner(jeu.getHeros().getPvMax());
        jeu.getHeros().payer(-15);
        System.out.println("Soin total effectué !");
      } else {
        System.out.println("Pas assez d'or !");
      }
    }
  }

  private void drawButton(Graphics2D g, String text, int x, int y, int w, int h, Color c) {
    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
    g.fillRect(x, y, w, h);
    g.setColor(c);
    g.setStroke(new BasicStroke(2));
    g.drawRect(x, y, w, h);
    g.setFont(new Font("Arial", Font.BOLD, 14));
    g.setColor(Color.WHITE);
    g.drawString(text, x + 20, y + 25);
  }

  private void drawAttackButton(Graphics2D g, int x, int y) {
    g.setColor(new Color(255, 0, 0, 200));
    g.fillRoundRect(x, y, 100, 30, 10, 10);
    g.setColor(Color.WHITE);
    g.drawRoundRect(x, y, 100, 30, 10, 10);
    g.setFont(new Font("Arial", Font.BOLD, 12));
    g.drawString("ATTAQUER", x + 18, y + 20);
  }

  private void renderEnemyHealthBar(Graphics2D g, Ennemi e, int x, int y) {
    int width = 128;
    g.setColor(Color.GRAY);
    g.fillRect(x, y, width, 10);

    float ratio = (float) e.getHp() / e.getMaxHp();
    g.setColor(Color.RED);
    g.fillRect(x, y, (int) (width * ratio), 10);
    g.setColor(Color.WHITE);
    g.drawRect(x, y, width, 10);
  }

  private void handleCombatClick(int mx, int my, ScreenInfo screenInfo) {
    var combat = jeu.getCombat();
    if (combat == null)
      return;

    List<Ennemi> ennemis = combat.getAliveEnemies();
    if (ennemis.isEmpty())
      return;
      
    int spacing = screenInfo.width() / (ennemis.size() + 1);

    for (int i = 0; i < ennemis.size(); i++) {
      Ennemi e = ennemis.get(i);
      int x = (i + 1) * spacing - 64;
      int y = 250;
      
      // Zone cliquable élargie (image entière + zone du bouton)
      if (mx >= x && mx <= x + 128 && my >= y - 30 && my <= y + 170) {
        System.out.println("Clic détecté sur " + e.getName());
        e.recevoirDegats(7);
        System.out.println("BAM ! 7 dégâts infligés à " + e.getName() + " (HP: " + e.getHp() + ")");
        jeu.updateCombatState();
        return;
      }
    }
  }
  
  private void renderGameOver(Graphics2D g, ScreenInfo screenInfo) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, screenInfo.width(), screenInfo.height());
    g.setColor(Color.RED);
    g.setFont(new Font("Serif", Font.BOLD, 48));
    FontMetrics fm = g.getFontMetrics();
    String text = "VOUS ÊTES MORT";
    int x = (screenInfo.width() - fm.stringWidth(text)) / 2;
    g.drawString(text, x, screenInfo.height() / 2);
  }

  private void renderVictory(Graphics2D g, ScreenInfo screenInfo) {
    g.setColor(new Color(0, 100, 0, 200));
    g.fillRect(0, 0, screenInfo.width(), screenInfo.height());
    g.setColor(Color.YELLOW);
    g.setFont(new Font("Serif", Font.BOLD, 48));
    FontMetrics fm = g.getFontMetrics();
    String text = "VICTOIRE !";
    int x = (screenInfo.width() - fm.stringWidth(text)) / 2;
    g.drawString(text, x, screenInfo.height() / 2);
  }

  private void renderCombatUI(Graphics2D g, ScreenInfo screenInfo) {
    var combat = jeu.getCombat();
    if (combat == null)
      return;

    g.setColor(new Color(0, 0, 0, 180));
    g.fillRect(0, 0, screenInfo.width(), 600);

    List<Ennemi> ennemis = combat.getAliveEnemies();
    int spacing = screenInfo.width() / (ennemis.size() + 1);

    for (int i = 0; i < ennemis.size(); i++) {
      Ennemi e = ennemis.get(i);
      int x = (i + 1) * spacing - 64;
      int y = 250;

      g.drawImage(img.getImage(e.getName()), x, y, 128, 128, null);
      renderEnemyHealthBar(g, e, x, y - 30);

      g.setFont(new Font("Arial", Font.BOLD, 14));
      g.setColor(Color.WHITE);
      g.drawString(e.getName(), x + 20, y - 40);
      g.drawString("HP: " + e.getHp() + "/" + e.getMaxHp(), x + 20, y + 150);

      // Toujours afficher le bouton quand la souris survole l'ennemi
      if (mouseX >= x && mouseX <= x + 128 && mouseY >= y - 30 && mouseY <= y + 170) {
        drawAttackButton(g, x + 14, y + 45);
      }
    }
    
    // Instructions
    g.setFont(new Font("Arial", Font.PLAIN, 16));
    g.setColor(Color.YELLOW);
    g.drawString("Cliquez sur un ennemi pour attaquer !", screenInfo.width() / 2 - 150, 150);
  }
  
  private void renderHealerUI(Graphics2D g, ScreenInfo screenInfo) {
    int panelWidth = 300;
    int panelHeight = 350;
    int startX = (screenInfo.width() - panelWidth) / 2;
    int startY = 200;

    g.setColor(new Color(20, 20, 20, 240));
    g.fillRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);
    g.setColor(new Color(255, 100, 100));
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(startX, startY, panelWidth, panelHeight, 15, 15);

    g.setFont(new Font("Serif", Font.BOLD, 24));
    g.setColor(Color.WHITE);
    g.drawString("Le Guérisseur", startX + 70, startY + 50);

    drawButton(g, "Soin Léger (+10 PV)", startX + 50, startY + 100, 200, 40, Color.GREEN);
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.drawString("Coût : 5 Or", startX + 100, startY + 160);

    drawButton(g, "Soin Total (MAX)", startX + 50, startY + 200, 200, 40, Color.CYAN);
    g.drawString("Coût : 15 Or", startX + 100, startY + 260);

    g.setFont(new Font("Arial", Font.PLAIN, 12));
    g.setColor(Color.WHITE);
    g.drawString("Cliquez sur un bouton pour acheter", startX + 50, startY + 320);
  }
  
  private void renderMerchantUI(Graphics2D g, fr.uge.backpackhero.donjon.MerchantRoom room) {
    int winW = 500;
    int winH = 300;
    int startX = 200;
    int startY = 250;

    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(startX, startY, winW, winH, 20, 20);
    g.setColor(new Color(212, 175, 55));
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(startX, startY, winW, winH, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 30));
    g.setColor(Color.WHITE);
    g.drawString("--- Boutique du Marchand ---", startX + 60, startY + 50);

    var stock = room.stock();
    for (int i = 0; i < stock.size(); i++) {
      var item = stock.get(i);
      int itemX = startX + 50 + (i * 150);
      int itemY = startY + 100;

      String fileName = item.getItem().name().replace(" ", "_");
      g.drawImage(img.getImage(fileName), itemX, itemY, 80, 80, null);

      g.setFont(new Font("Arial", Font.BOLD, 18));
      g.setColor(Color.YELLOW);
      g.drawString("10 Or", itemX + 15, itemY + 110);
    }

    g.setFont(new Font("Arial", Font.ITALIC, 14));
    g.setColor(Color.WHITE);
    g.drawString("Cliquez sur un objet pour acheter", startX + 130, startY + 270);
  }
  
  public ViewGraphic getViewGraphic() {
    return viewGraphic;
  }
}