package fr.uge.backpackhero.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.github.forax.zen.ApplicationContext;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.combat.Combat;
import fr.uge.backpackhero.donjon.Corridor;
import fr.uge.backpackhero.donjon.EnemyRoom;
import fr.uge.backpackhero.donjon.ExitRoom;
import fr.uge.backpackhero.donjon.HealerRoom;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.donjon.TreasureRoom;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

/**
 * Handles the graphical rendering of the game using the Zen 5 library.
 * This class draws the dungeon, the combat interface, and the inventory.
 */
public class GameView {

  private static final int CASE_SIZE = 50;
  private static final int MARGIN_X = 50;
  private static final int MARGIN_Y = 50;

  /**
   * Main entry point to draw the entire game frame.
   *
   * @param context      The Zen application context.
   * @param jeu          The game model containing data.
   * @param backpackView The specific view for the backpack grid.
   */
  public static void draw(ApplicationContext context, Jeu jeu, BackPackView backpackView) {
    var screenInfo = context.getScreenInfo();
    float width = screenInfo.width();
    float height = screenInfo.height();

    context.renderFrame(graphics -> {
      clearScreen(graphics, width, height);
      renderGameContent(graphics, jeu, backpackView, width, height);
    });
  }

  /**
   * Clears the screen with a black background.
   *
   * @param g      The graphics context.
   * @param width  Screen width.
   * @param height Screen height.
   */
  private static void clearScreen(Graphics2D g, float width, float height) {
    g.setColor(Color.BLACK);
    g.fill(new Rectangle2D.Float(0, 0, width, height));
  }

  /**
   * Selects the appropriate drawing method based on the current game mode.
   *
   * @param g            The graphics context.
   * @param jeu          The game model.
   * @param backpackView The backpack view.
   * @param w            Screen width.
   * @param h            Screen height.
   */
  private static void renderGameContent(Graphics2D g, Jeu jeu, BackPackView backpackView, float w, float h) {
    if (jeu.getMode() == Mode.COMBAT) {
      drawCombat(g, jeu, backpackView, w, h);
    } else {
      drawDungeon(g, jeu);
      // Draw a mini-backpack during exploration
      backpackView.draw(g, 20, (int) h - 200);
    }
  }

  // --- DUNGEON RENDERING ---

  /**
   * Draws the exploration view (dungeon map).
   *
   * @param g   The graphics context.
   * @param jeu The game model.
   */
  private static void drawDungeon(Graphics2D g, Jeu jeu) {
    g.setColor(Color.WHITE);
    g.drawString("EXPLORATION - Floor " + jeu.getDonjon().getFloorNumber(), 50, 30);
    
    var floor = jeu.getDonjon().getCurrentFloor();
    for (int y = 0; y < floor.height(); y++) {
      for (int x = 0; x < floor.width(); x++) {
        drawCell(g, jeu, floor.getRoom(x, y), x, y);
      }
    }
  }

  /**
   * Draws a single cell of the dungeon grid (Room or Wall).
   *
   * @param g    The graphics context.
   * @param jeu  The game model.
   * @param room The room at these coordinates (can be null).
   * @param x    The grid x-coordinate.
   * @param y    The grid y-coordinate.
   */
  private static void drawCell(Graphics2D g, Jeu jeu, Room room, int x, int y) {
    float drawX = MARGIN_X + x * CASE_SIZE;
    float drawY = MARGIN_Y + y * CASE_SIZE;

    if (room != null) {
      drawRoomBackground(g, room, drawX, drawY);
      if (x == jeu.getX() && y == jeu.getY()) {
        drawHeroMarker(g, drawX, drawY);
      }
    } else {
      drawWall(g, drawX, drawY);
    }
  }

  private static void drawRoomBackground(Graphics2D g, Room room, float x, float y) {
    g.setColor(getColorForRoom(room));
    g.fill(new Rectangle2D.Float(x, y, CASE_SIZE - 2, CASE_SIZE - 2));
  }

  private static void drawHeroMarker(Graphics2D g, float x, float y) {
    g.setColor(Color.CYAN);
    g.fillOval((int) x + 10, (int) y + 10, (int) CASE_SIZE - 20, (int) CASE_SIZE - 20);
  }

  private static void drawWall(Graphics2D g, float x, float y) {
    g.setColor(Color.DARK_GRAY);
    g.draw(new Rectangle2D.Float(x, y, CASE_SIZE - 2, CASE_SIZE - 2));
  }

  // --- COMBAT RENDERING ---

  /**
   * Draws the combat interface.
   *
   * @param g            The graphics context.
   * @param jeu          The game model.
   * @param backpackView The view to draw the inventory.
   * @param w            Screen width.
   * @param h            Screen height.
   */
  private static void drawCombat(Graphics2D g, Jeu jeu, BackPackView backpackView, float w, float h) {
    drawCombatBackground(g, w, h);
    drawHeroStats(g, jeu.getHeros());
    
    if (jeu.getCombat() != null) {
      drawEnemies(g, jeu.getCombat());
    }

    drawCombatInstructions(g);
    backpackView.draw(g, 50, 320);
  }

  private static void drawCombatBackground(Graphics2D g, float w, float h) {
    g.setColor(new Color(50, 0, 0)); // Dark red background
    g.fill(new Rectangle2D.Float(0, 0, w, h));
    g.setColor(Color.WHITE);
    g.drawString("COMBAT!", 300, 50);
  }

  private static void drawHeroStats(Graphics2D g, Heros heros) {
    g.drawString("HERO: " + heros.getPv() + "/" + heros.getPvMax() + " HP", 50, 100);
    g.drawString("Energy: " + heros.getEnergie(), 50, 120);
  }

  private static void drawEnemies(Graphics2D g, Combat combat) {
    int yPos = 100;
    for (Ennemi e : combat.getAliveEnemies()) {
      drawSingleEnemy(g, e, yPos);
      yPos += 80;
    }
  }

  private static void drawSingleEnemy(Graphics2D g, Ennemi e, int y) {
    g.setColor(Color.RED);
    g.fillRect(400, y, 50, 50);
    g.setColor(Color.WHITE);
    g.drawString("HP: " + e.getHp(), 460, y + 25);
    g.drawString("Intent: " + e.getActionAnnoncee().description(), 460, y + 45);
  }

  private static void drawCombatInstructions(Graphics2D g) {
    g.drawString("YOUR BAG (Press A, Z, E... to use items)", 50, 300);
    g.setColor(Color.YELLOW);
    g.drawString("[F] End Turn", 50, 550);
  }

  // --- UTILS ---

  /**
   * Determines the color associated with a room type.
   *
   * @param room The room to check.
   * @return The color representing the room.
   */
  private static Color getColorForRoom(Room room) {
    return switch (room) {
      case Corridor c -> Color.LIGHT_GRAY;
      case EnemyRoom e -> Color.RED;
      case TreasureRoom t -> Color.YELLOW;
      case ExitRoom x -> Color.GREEN;
      case MerchantRoom m -> Color.MAGENTA;
      case HealerRoom h -> Color.PINK;
    };
  }
}