package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.donjon.EnemyRoom;
import fr.uge.backpackhero.donjon.MerchantRoom;
import fr.uge.backpackhero.donjon.TreasureRoom;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import java.awt.*;

public class GraphicController {
    private final Jeu jeu;
    private final SpriteLoader sprites = new SpriteLoader();
    private static final int TILE_SIZE = 64;

    public GraphicController(Jeu jeu) {
        this.jeu = jeu;
    }

    /**
     * Transforme "Wood Sword" en "Wood_Sword" pour coller à tes fichiers
     */
    private String formatFileName(String name) {
        return name.replace(" ", "_");
    }

    public void run() {
        Application.run(Color.BLACK, context -> {
            while (true) {
                Event event = context.pollEvent();
                if (event instanceof KeyboardEvent kb && kb.action() == KeyboardEvent.Action.KEY_PRESSED) {
                    if (kb.key() == KeyboardEvent.Key.Q) { context.dispose(); return; }
                    handleMovement(kb);
                }

                context.renderFrame(g -> {
                    drawDungeon(g);
                    drawBackpack(g);
                    drawHUD(g);
                });
                
                try { Thread.sleep(15); } catch (InterruptedException e) { return; }
            }
        });
    }

    private void handleMovement(KeyboardEvent kb) {
        switch (kb.key()) {
            case Z, UP -> jeu.deplacer(0, -1);
            case S, DOWN -> jeu.deplacer(0, 1);
            case Q, LEFT -> jeu.deplacer(-1, 0);
            case D, RIGHT -> jeu.deplacer(1, 0);
            default -> {}
        }
    }
/*
    private void drawDungeon(Graphics2D g) {
        var floor = jeu.getDonjon().getCurrentFloor();
        for (int y = 0; y < floor.height(); y++) {
            for (int x = 0; x < floor.width(); x++) {
                int px = x * TILE_SIZE + 50;
                int py = y * TILE_SIZE + 100;
                
                // On peut utiliser une couleur par défaut pour le sol si floor.png manque
                g.setColor(new Color(40, 40, 40));
                g.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(px, py, TILE_SIZE, TILE_SIZE);

                // Dessiner l'ennemi s'il y en a un (ex: ratloup)
                var room = floor.getRoom(x, y);
                if (room != null && room.toString().equals("E")) {
                    g.drawImage(sprites.get("ratloup"), px, py, TILE_SIZE, TILE_SIZE, null);
                }

                // Dessiner le héros (heros.png)
                if (x == jeu.getX() && y == jeu.getY()) {
                    g.drawImage(sprites.get("heros"), px, py, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }
*/
    
    private void drawDungeon(Graphics2D g) {
        var floor = jeu.getDonjon().getCurrentFloor();
        for (int y = 0; y < floor.height(); y++) {
            for (int x = 0; x < floor.width(); x++) {
                int px = x * TILE_SIZE + 50;
                int py = y * TILE_SIZE + 100;

                var room = floor.getRoom(x, y);
                if (room == null) {
                    g.drawImage(sprites.get("wall"), px, py, TILE_SIZE, TILE_SIZE, null);
                    continue;
                }

                if (room instanceof EnemyRoom) {
                    g.drawImage(sprites.get("ratloup"), px, py, TILE_SIZE, TILE_SIZE, null);
                } else if (room instanceof TreasureRoom) {
                    g.drawImage(sprites.get("treasureroom"), px, py, TILE_SIZE, TILE_SIZE, null);
                } else if (room instanceof MerchantRoom) {
                    g.drawImage(sprites.get("merchantroom"), px, py, TILE_SIZE, TILE_SIZE, null);
                } else {
                    // Par défaut, on dessine le sol pour les couloirs et autres salles
                    g.drawImage(sprites.get("tile_floor"), px, py, TILE_SIZE, TILE_SIZE, null);
                }

                // Dessin du héros
                if (x == jeu.getX() && y == jeu.getY()) {
                    g.drawImage(sprites.get("heros"), px, py, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }
    private void drawBackpack(Graphics2D g) {
        var backpack = jeu.getHeros().getBackpack();
        int offsetX = 800;
        int offsetY = 100;

        // Dessiner la grille du sac
        g.setColor(Color.GRAY);
        for (int r = 0; r < backpack.getHeight(); r++) {
            for (int c = 0; c < backpack.getWidth(); c++) {
                g.drawRect(offsetX + c * TILE_SIZE, offsetY + r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Dessiner les objets (Wood_Sword, Rough_Buckler, etc.)
        for (ItemInstance item : backpack.getItems()) {
            Position p = item.getPos();
            if (p != null) {
                String fileName = formatFileName(item.getItem().name());
                Image img = sprites.get(fileName);
                if (img != null) {
                    g.drawImage(img, offsetX + p.column() * TILE_SIZE, offsetY + p.row() * TILE_SIZE, null);
                }
            }
        }
    }

    private void drawHUD(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.drawString("❤️ HP: " + jeu.getHeros().getPv(), 50, 50);
        g.drawString("💰 Gold: " + jeu.getHeros().getGold(), 200, 50);
    }
}