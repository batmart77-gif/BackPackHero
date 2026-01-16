package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent.Key;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.*;
import java.util.List;
import java.util.Objects;
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

  private String messageFlash = "";
  private int messageTimer = 0;
  
  public GraphicEngine(Jeu jeu, ViewGraphic viewGraphic) {
    this.jeu = Objects.requireNonNull(jeu);
    this.viewGraphic = Objects.requireNonNull(viewGraphic);
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
          handleInput(kb);
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
          renderFlashMessage(g, screenInfo);
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
          default -> {
          }
          }

          viewGraphic.render(g, screenInfo);

          // Afficher l'objet en cours de placement
          ItemInstance itemToShow = viewGraphic.getCurrentItem();
          if (itemToShow != null && (viewGraphic.getMode() == ViewGraphic.InteractionMode.WAITING_POSITION 
              || viewGraphic.getMode() == ViewGraphic.InteractionMode.REORGANIZE)) {
              
              g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
              
              // On dessine l'item au niveau de la souris
              // On décale de -32 pour que la souris soit au centre de la première case
              drawItem(g, itemToShow, mouseX - 32, mouseY - 32, TILE_SIZE);
              
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
      g.drawRect(dungeonStartX + hoverCol * TILE_SIZE, dungeonStartY + hoverRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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
        int px = backpackStartX + p.column() * TILE_SIZE;
        int py = backpackStartY + p.row() * TILE_SIZE;

        // On appelle notre nouvelle fonction !
        drawItem(g, item, px, py, TILE_SIZE);
      }
    }

    int hoverCol = (mouseX - backpackStartX) / TILE_SIZE;
    int hoverRow = (mouseY - backpackStartY) / TILE_SIZE;

    if (hoverCol >= 0 && hoverCol < bp.getWidth() && hoverRow >= 0 && hoverRow < bp.getHeight()) {
      g.setColor(new Color(100, 200, 255, 100));
      g.setStroke(new BasicStroke(2));
      g.drawRect(backpackStartX + hoverCol * TILE_SIZE, backpackStartY + hoverRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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

  private void handleInput(KeyboardEvent kb) {
    if (jeu.getMode() == Mode.PERDU || jeu.getMode() == Mode.GAGNE) return;

    // 1. On récupère la touche
    KeyboardEvent.Key key = kb.key();

    // 2. Si un menu est ouvert (Malédiction, Inventaire, etc.)
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE) {
      // On laisse ViewGraphic gérer les touches spécifiques aux menus
      viewGraphic.handleKeyPress(key); 
      return; // On arrête là pour ne pas déplacer le perso en même temps
    }

    // 3. Sinon, contrôles classiques
    switch (key) {
      case UP, Z -> jeu.deplacer(0, -1);
      case DOWN, S -> jeu.deplacer(0, 1);
      case LEFT, Q -> jeu.deplacer(-1, 0);
      case RIGHT, D -> jeu.deplacer(1, 0);
      case I -> jeu.getView().printBackPack();
      case O -> viewGraphic.reorganize();
      default -> { }
    }
  }

  /**
   * Main mouse event dispatcher. Splits logic to stay under 20 lines.
   */
  private void handleMouse(PointerEvent pe, ScreenInfo info) {
    Objects.requireNonNull(pe);
    if (jeu.getMode() == Mode.PERDU || jeu.getMode() == Mode.GAGNE) return;
    if (pe.action() != PointerEvent.Action.POINTER_DOWN) return;

    int mx = pe.location().x();
    int my = pe.location().y();

    // 1. Priorité aux interactions de placement/réorganisation
    if (viewGraphic.getMode() != ViewGraphic.InteractionMode.NONE) {
      viewGraphic.handleMouseClick(mx, my, backpackStartX, backpackStartY);
      return;
    }

    // 2. Délégation selon le mode actuel
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
    var floor = jeu.getDonjon().getCurrentFloor();
    int col = (mx - dungeonStartX) / TILE_SIZE;
    int row = (my - dungeonStartY) / TILE_SIZE;

    if (col >= 0 && col < floor.width() && row >= 0 && row < floor.height()) {
      var room = floor.getRoom(col, row);
      if (room != null && Math.abs(col - jeu.getX()) + Math.abs(row - jeu.getY()) <= 1) {
        jeu.deplacer(col - jeu.getX(), row - jeu.getY());
        room.onClick(jeu);
      }
    }
  }

  private void gererClicBoutique(int mx, int my) {
    int startX = 200;
    int startY = 250;
    
    if (mx >= startX + 150 && mx <= startX + 350 && my >= startY + 310 && my <= startY + 350) {
      jeu.setMode(Mode.EXPLORATION);
      return;
    }

    // Vérifier d'abord si on clique sur un objet du marchand
    if (detecterAchat(mx, my, startX, startY)) return;

    // Sinon, vérifier si on clique sur un objet de NOTRE sac pour le vendre
    detecterVente(mx, my);
  }
  
  /**
   * Detects if a mouse click is on a merchant's item.
   * @return true if an item was clicked and purchase logic was triggered.
   */
  private boolean detecterAchat(int mx, int my, int startX, int startY) {
    var room = jeu.getDonjon().getCurrentFloor().getRoom(jeu.getX(), jeu.getY());
    if (room instanceof fr.uge.backpackhero.donjon.MerchantRoom merchant) {
      var stock = merchant.stock();
      for (int i = 0; i < stock.size(); i++) {
        int itemX = startX + 50 + (i * 150);
        int itemY = startY + 100;
        if (mx >= itemX && mx <= itemX + 80 && my >= itemY && my <= itemY + 80) {
          effectuerAchat(merchant, i);
          return true;
        }
      }
    }
    return false;
  }
  
  private void effectuerAchat(MerchantRoom merchant, int index) {
    ItemInstance item = merchant.stock().get(index);
    int price = item.getItem().price();
    
    if (jeu.getHeros().payer(price)) {
      merchant.stock().remove(index);
      this.messageFlash = "Achat réussi ! Placez l'objet.";
      this.messageTimer = 100;
      
      // Active le mode de placement dans le sac
      viewGraphic.displayItemFound(item);
      viewGraphic.attemptPlacement(item);
    } else {
      this.messageFlash = "Pas assez d'or !";
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
    int prixVente = item.getItem().price() / 2;
    jeu.getHeros().getBackpack().removeItem(item);
    jeu.getHeros().gagnerOr(prixVente);
    
    this.messageFlash = "Objet vendu pour " + prixVente + " Or !";
    this.messageTimer = 100;
  }


  private void handleHealerClick(int mx, int my, ScreenInfo screenInfo) {
    int startX = (screenInfo.width() - 300) / 2;
    int startY = 200;

    // Bouton 1 : Soin léger (Coût 5)
    if (mx >= startX + 50 && mx <= startX + 250 && my >= startY + 100 && my <= startY + 140) {
      if (jeu.getHeros().getPv() < jeu.getHeros().getPvMax() && jeu.getHeros().payer(5)) {
        jeu.getHeros().soigner(10);
        this.messageFlash = "Soin effectué !";
      }else {
        this.messageFlash = "Pas assez d'or !";
      }
      this.messageTimer = 100;
    }

    // Bouton 2 : Soin total (Coût 15)
    if (mx >= startX + 50 && mx <= startX + 250 && my >= startY + 200 && my <= startY + 240) {
      if (jeu.getHeros().getPv() < jeu.getHeros().getPvMax() && jeu.getHeros().payer(15)) {
        jeu.getHeros().soigner(jeu.getHeros().getPvMax());
        this.messageFlash = "Soin total effectué !";
      }else {
        this.messageFlash = "Pas assez d'or !";
      }
      this.messageTimer = 100;
    }

    // Bouton 3 : Quitter (Zone en bas du panneau)
    if (mx >= startX + 50 && mx <= startX + 250 && my >= startY + 280 && my <= startY + 320) {
      jeu.setMode(Mode.EXPLORATION);
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
    if (combat == null) return;

    // 1. Bouton Fin de Tour
    int btnX = (screenInfo.width() - 200) / 2;
    if (mx >= btnX && mx <= btnX + 200 && my >= 520 && my <= 560) {
      executerFinDeTour(combat);
      return;
    }

    // 2. Objets du sac ou Clic sur Ennemi
    if (detecterActionSacCombat(mx, my)) return;
    detecterClicEnnemi(mx, my, screenInfo);
  }
  
  /**
   * Triggers the enemy turn and updates the visual state.
   */
  private void executerFinDeTour(fr.uge.backpackhero.combat.Combat combat) {
    this.messageFlash = "Tour des ennemis...";
    this.messageTimer = 60;
    
    // Exécute les actions des ennemis sur le héros
    combat.startEnemyTurn(); 
    
    // Une fois le tour ennemi fini, le héros récupère son énergie/mana
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

  /**
   * Attempts to use an item and provides immediate visual feedback on the enemy's health.
   */
  private void tenterActionCombat(ItemInstance item) {
    var combat = jeu.getCombat();
    var target = combat.getAliveEnemies().get(0); 
    int oldHp = target.getHp();

    if (combat.tryHeroAction(item, target)) {
      int damageDone = oldHp - target.getHp(); // Calcul des dégâts réels
      this.messageFlash = "Utilisé " + item.getName() + " ! (-" + damageDone + " PV)";
      jeu.updateCombatState();
    } else {
      this.messageFlash = "Énergie ou Mana insuffisant !";
    }
    this.messageTimer = 80;
  }
  
  
  private void detecterClicEnnemi(int mx, int my, ScreenInfo info) {
    var ennemis = jeu.getCombat().getAliveEnemies();
    int spacing = info.width() / (ennemis.size() + 1);

    for (int i = 0; i < ennemis.size(); i++) {
      int x = (i + 1) * spacing - 64;
      if (mx >= x && mx <= x + 128 && my >= 220 && my <= 420) {
        this.messageFlash = "Cible : " + ennemis.get(i).getName();
        this.messageTimer = 60;
        return;
      }
    }
  }
  

  private void renderGameOver(Graphics2D g, ScreenInfo screenInfo) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, screenInfo.width(), screenInfo.height());
    
    g.setColor(Color.RED);
    g.setFont(new Font("Serif", Font.BOLD, 48));
    String text = "VOUS ÊTES MORT";
    g.drawString(text, (screenInfo.width() - g.getFontMetrics().stringWidth(text)) / 2, 250);
    
    int score = jeu.getHeros().calculateFinalScore();
    g.setFont(new Font("Arial", Font.BOLD, 24));
    g.setColor(Color.WHITE);
    String scoreText = "Score Final : " + score;
    g.drawString(scoreText, (screenInfo.width() - g.getFontMetrics().stringWidth(scoreText)) / 2, 350);
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
    if (combat == null) return;

    g.setColor(new Color(0, 0, 0, 180));
    g.fillRect(0, 0, screenInfo.width(), 600); // Combat panel background

    renderEnemies(g, combat.getAliveEnemies(), screenInfo);

    // Bouton Fin de Tour positionné au centre bas du panneau de combat
    int btnX = (screenInfo.width() - 200) / 2;
    drawButton(g, "Fin de Tour", btnX, 520, 200, 40, Color.RED);
  }

  /**
   * Renders the list of alive enemies, spacing them across the screen.
   * @param g the graphics context.
   * @param ennemis the list of enemies to display.
   * @param info the screen information for positioning.
   */
  private void renderEnemies(Graphics2D g, List<Ennemi> ennemis, ScreenInfo info) {
    Objects.requireNonNull(ennemis);
    int spacing = info.width() / (ennemis.size() + 1);
    for (int i = 0; i < ennemis.size(); i++) {
      // Calcule la position X pour centrer les ennemis
      drawEnemyCombat(g, ennemis.get(i), (i + 1) * spacing - 64, 250);
    }
  }

  /**
   * Draws an enemy with its intention, health bar, and active status effects.
   * @param g the graphics context.
   * @param e the enemy instance.
   * @param x x-coordinate for the sprite.
   * @param y y-coordinate for the sprite.
   */
  private void drawEnemyCombat(Graphics2D g, Ennemi e, int x, int y) {
    Objects.requireNonNull(e);
    // Sprite and Health
    g.drawImage(img.getImage(e.getName()), x, y, 128, 128, null);
    renderEnemyHealthBar(g, e, x, y - 30);
    
    // New: Intent and Status
    renderEnemyIntent(g, e, x, y);
    renderEnemyStatus(g, e, x, y);

    // Attack button on hover
    if (mouseX >= x && mouseX <= x + 128 && mouseY >= y - 30 && mouseY <= y + 170) {
      drawAttackButton(g, x + 14, y + 45);
    }
  }
  
  /**
   * Displays active status effects (Poison, Burn, etc.) next to the enemy.
   */
  private void renderEnemyStatus(Graphics2D g, Ennemi e, int x, int y) {
    int offset = 0;
    g.setFont(new Font("Arial", Font.BOLD, 12));
    for (fr.uge.backpackhero.combat.Effect effect : fr.uge.backpackhero.combat.Effect.values()) {
      int stacks = e.getStatus(effect);
      if (stacks > 0) {
        g.setColor(getColorForEffect(effect));
        g.drawString(effect.getNom() + " : " + stacks, x + 135, y + 20 + offset);
        offset += 15;
      }
    }
  }

  private Color getColorForEffect(fr.uge.backpackhero.combat.Effect effect) {
    return switch (effect) {
      case POISON -> Color.GREEN;
      case BURN -> Color.ORANGE;
      case DODGE -> Color.WHITE;
      default -> Color.MAGENTA;
    };
  }
  /**
   * Renders the enemy's planned action (intent) as a text bubble.
   */
  private void renderEnemyIntent(Graphics2D g, Ennemi e, int x, int y) {
    var action = e.getActionAnnoncee();
    if (action != null) {
      g.setFont(new Font("Arial", Font.ITALIC, 12));
      g.setColor(Color.YELLOW);
      // Positionné juste au-dessus de la barre de vie
      g.drawString("Intention : " + action.description(), x, y - 45);
    }
  }
 
  private void renderHealerUI(Graphics2D g, ScreenInfo screenInfo) {
    int startX = (screenInfo.width() - 300) / 2;
    int startY = 200;

    g.setColor(new Color(20, 20, 20, 240));
    g.fillRoundRect(startX, startY, 300, 350, 15, 15);
    g.setColor(new Color(255, 100, 100)); // Bordure rouge
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(startX, startY, 300, 350, 15, 15);

    g.setFont(new Font("Serif", Font.BOLD, 24));
    g.setColor(Color.WHITE);
    g.drawString("Le Guérisseur", startX + 70, startY + 50);

    // Boutons d'interaction
    drawButton(g, "Soin Léger (5 Or)", startX + 50, startY + 100, 200, 40, Color.GREEN);
    drawButton(g, "Soin Total (15 Or)", startX + 50, startY + 200, 200, 40, Color.CYAN);
    drawButton(g, "Quitter", startX + 50, startY + 280, 200, 40, Color.GRAY);
  }
  

  private void renderMerchantUI(Graphics2D g, fr.uge.backpackhero.donjon.MerchantRoom room) {
    int startX = 200;
    int startY = 250;
    g.setColor(new Color(0, 0, 0, 220));
    g.fillRoundRect(startX, startY, 500, 380, 20, 20); // Fond
    g.setColor(new Color(212, 175, 55)); // Bordure Or
    g.setStroke(new BasicStroke(3));
    g.drawRoundRect(startX, startY, 500, 380, 20, 20);

    g.setFont(new Font("Serif", Font.BOLD, 30));
    g.setColor(Color.WHITE);
    g.drawString("Boutique du Marchand", startX + 100, startY + 50);

    var stock = room.stock();
    for (int i = 0; i < stock.size(); i++) {
      renderStoreItem(g, stock.get(i), startX + 50 + (i * 150), startY + 100);
    }
    g.setFont(new Font("Arial", Font.ITALIC, 14));
    g.setColor(Color.LIGHT_GRAY);
    g.drawString("Astuce : Cliquez sur un objet de votre sac pour le vendre (50% du prix)", startX + 40, startY + 270);
    drawButton(g, "Quitter la boutique", startX + 150, startY + 310, 200, 40, Color.GRAY);
  }

  private void renderStoreItem(Graphics2D g, ItemInstance item, int x, int y) {
    String fileName = item.getItem().name().replace(" ", "_");
    g.drawImage(img.getImage(fileName), x, y, 80, 80, null);
    g.setFont(new Font("Arial", Font.BOLD, 18));
    g.setColor(Color.YELLOW);
    g.drawString(item.getItem().price() + " Or", x + 10, y + 110);
  }
  
  private void drawItem(Graphics2D g, ItemInstance item, int px, int py, int cellSize) {
    String name = item.getItem().name().replace(" ", "_");
    Image imgFile = img.getImage(name);
    if (imgFile == null)
      return;

    // On calcule la taille de l'item non tourné (sa forme de base)
    // pour que l'image ne soit pas déformée
    var baseShape = item.getItem().shapeAtRotation(0);
    int baseW = baseShape.stream().mapToInt(Position::column).max().getAsInt() + 1;
    int baseH = baseShape.stream().mapToInt(Position::row).max().getAsInt() + 1;

    var oldTransform = g.getTransform();

    // 1. On se place sur la case
    g.translate(px, py);

    // 2. Si l'objet est tourné, on applique la rotation visuelle
    if (item.getRotationAngle() != 0) {
      // On tourne autour du centre de la première case
      g.rotate(Math.toRadians(item.getRotationAngle()), cellSize / 2.0, cellSize / 2.0);
    }

    // 3. On dessine avec les dimensions de base
    g.drawImage(imgFile, 0, 0, baseW * cellSize, baseH * cellSize, null);

    g.setTransform(oldTransform);
  }

  public ViewGraphic getViewGraphic() {
    return viewGraphic;
  }
 
  private void renderFlashMessage(Graphics2D g, ScreenInfo info) {
    if (messageTimer > 0) {
      g.setFont(new Font("Arial", Font.BOLD, 22));
      int alpha = Math.min(255, messageTimer * 5);
      g.setColor(new Color(255, 50, 50, alpha));
      
      FontMetrics fm = g.getFontMetrics();
      int x = (info.width() - fm.stringWidth(messageFlash)) / 2;
      g.drawString(messageFlash, x, 90); 
      
      messageTimer--; 
    }
  }
 
  
}