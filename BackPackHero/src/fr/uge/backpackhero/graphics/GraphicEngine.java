package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;

import java.awt.*;

public class GraphicEngine {
    private final Jeu jeu;
    private final ImageManager img = new ImageManager();
    private static final int TILE_SIZE = 64;

    public GraphicEngine(Jeu jeu) {
        this.jeu = jeu;
    }

    /*public void start() {
        Application.run(Color.decode("#0d0d0d"), context -> {
            while (true) {
                // 1. Gérer les entrées (Clavier)
                Event event = context.pollEvent();
                if (event instanceof KeyboardEvent kb && kb.action() == KeyboardEvent.Action.KEY_PRESSED) {
                    handleInput(kb, context);
                }

                // 2. Dessiner l'interface
                context.renderFrame(g -> {
                    renderBackground(g, context.getScreenInfo());
                    renderDungeon(g);
                    renderBackpack(g);
                    renderHUD(g);
                });

                try { Thread.sleep(15); } catch (InterruptedException e) { return; }
            }
        });
    }*/
    
    public void start() {
      Application.run(Color.decode("#0d0d0d"), context -> {
          while (true) {
              Event event = context.pollEvent();
              
              // Événements clavier (déplacement héros)
              if (event instanceof KeyboardEvent kb && kb.action() == KeyboardEvent.Action.KEY_PRESSED) {
                  handleInput(kb, context);
              }
              
              // Événements souris (Drag & Drop)
              if (event instanceof PointerEvent pe) {
                  handleMouse(pe);
              }

              context.renderFrame(g -> {
                  renderBackground(g, context.getScreenInfo());
                  renderDungeon(g);
                  renderBackpack(g);
                  renderHUD(g);
                  
                  // Dessiner l'objet tenu par la souris par-dessus tout le reste
                  if (selectedItem != null) {
                      String fileName = selectedItem.getItem().name().replace(" ", "_");
                      Image ghost = img.getImage(fileName);
                      g.drawImage(ghost, mouseX - 32, mouseY - 32, null);
                  }
              });
              try { Thread.sleep(15); } catch (InterruptedException e) { return; }
          }
      });
  }

    private void renderBackground(Graphics2D g, ScreenInfo info) {
        // Un dégradé sombre pour l'ambiance
        g.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, info.height(), Color.decode("#1a0f00")));
        g.fillRect(0, 0, info.width(), info.height());
    }
/*
    
    private void renderDungeon(Graphics2D g) {
      var floor = jeu.getDonjon().getCurrentFloor();
      for (int y = 0; y < floor.height(); y++) {
          for (int x = 0; x < floor.width(); x++) {
              int px = x * TILE_SIZE + 100;
              int py = y * TILE_SIZE + 180;
              var room = floor.getRoom(x, y);

              if (room != null) {
                  // ON DESSINE LE SOL POUR CHAQUE SALLE EXISTANTE
                  g.drawImage(img.getImage("tile_floor"), px, py, TILE_SIZE, TILE_SIZE, null);
                  
                  // On dessine le contenu spécial s'il existe
                  if (room instanceof fr.uge.backpackhero.donjon.EnemyRoom) {
                      g.drawImage(img.getImage("ratloup"), px, py, TILE_SIZE, TILE_SIZE, null);
                  } else if (room instanceof fr.uge.backpackhero.donjon.TreasureRoom) {
                      g.drawImage(img.getImage("beequeen"), px, py, TILE_SIZE, TILE_SIZE, null);
                  }
              } else {
                  // SI ROOM EST NULL, C'EST UN MUR
                  g.drawImage(img.getImage("wall"), px, py, TILE_SIZE, TILE_SIZE, null);
              }

              // DESSIN DU HÉROS PAR-DESSUS
              if (x == jeu.getX() && y == jeu.getY()) {
                  g.drawImage(img.getImage("heros"), px, py, TILE_SIZE, TILE_SIZE, null);
              }
          }
      }
  }
  */
    
    private void renderDungeon(Graphics2D g) {
      var floor = jeu.getDonjon().getCurrentFloor();
      for (int y = 0; y < floor.height(); y++) {
          for (int x = 0; x < floor.width(); x++) {
              int px = x * TILE_SIZE + 100;
              int py = y * TILE_SIZE + 180;
              var room = floor.getRoom(x, y);

              if (room != null) {
                  // 1. DESSINER LE SOL (Toujours présent pour une salle)
                  g.drawImage(img.getImage("tile_floor"), px, py, TILE_SIZE, TILE_SIZE, null);
                  
                  // 2. DESSINER LE CONTENU SELON LE TYPE DE SALLE
                  renderRoomContent(g, room, px, py);
              } else {
                  // 3. MUR (Si pas de salle)
                  g.drawImage(img.getImage("wall"), px, py, TILE_SIZE, TILE_SIZE, null);
              }

              // 4. DESSINER LE HÉROS PAR-DESSUS
              if (x == jeu.getX() && y == jeu.getY()) {
                  g.drawImage(img.getImage("heros"), px, py, TILE_SIZE, TILE_SIZE, null);
              }
          }
      }
  }

    private void renderRoomContent(Graphics2D g, Room room, int px, int py) {
      // Une seule ligne suffit !
      // Java appellera automatiquement la bonne méthode draw selon le type de salle.
      room.draw(g, px, py, TILE_SIZE, img);
    }
    private void renderBackpack(Graphics2D g) {
      var bp = jeu.getHeros().getBackpack();
      int startX = 850;
      int startY = 150;

      // 1. Dessiner la grille (C'est ce que tu vois actuellement)
      g.setColor(new Color(255, 255, 255, 40));
      for (int r = 0; r < bp.getHeight(); r++) {
          for (int c = 0; c < bp.getWidth(); c++) {
              g.drawRect(startX + c * TILE_SIZE, startY + r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
          }
      }
      
      // 2. Dessiner les objets réels
      for (ItemInstance item : bp.getItems()) {
          Position p = item.getPos();
          if (p != null) {
              String nameForFile = item.getItem().name().replace(" ", "_");
              Image objImg = img.getImage(nameForFile);
              
              if (objImg != null) {
                  g.drawImage(objImg, 
                      startX + p.column() * TILE_SIZE, 
                      startY + p.row() * TILE_SIZE, 
                      TILE_SIZE, TILE_SIZE, null);
              }
          }
      }
  }

    private void renderHUD(Graphics2D g) {
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRect(80, 45, 500, 50); // Fond du texte
      
      g.setColor(Color.WHITE);
      g.setFont(new Font("Serif", Font.BOLD, 28));
      g.drawString("HP: " + jeu.getHeros().getPv() + " / 40", 100, 80);
      g.drawString("Gold: " + jeu.getHeros().getGold(), 400, 80);
  }

    private void handleInput(KeyboardEvent kb, ApplicationContext context) {
        switch (kb.key()) {
            case UP, Z -> jeu.deplacer(0, -1);
            case DOWN, S -> jeu.deplacer(0, 1);
            case LEFT, Q -> jeu.deplacer(-1, 0);
            case RIGHT, D -> jeu.deplacer(1, 0);
            case ESCAPE -> context.dispose();
            default -> {}
        }
    }
    
    private ItemInstance selectedItem = null; // L'objet tenu par la souris
    private int mouseX, mouseY;              // Position actuelle du curseur

    private void handleMouse(PointerEvent pe) {
        this.mouseX = pe.location().x();
        this.mouseY = pe.location().y();
        var backpack = jeu.getHeros().getBackpack();

        // Conversion des pixels en coordonnées de grille (row, col)
        int col = (mouseX - 850) / TILE_SIZE;
        int row = (mouseY - 150) / TILE_SIZE;

        if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
            // Clic : on cherche si un objet est présent à cette position
            var opt = backpack.getItemAt(new Position(row, col));
            if (opt.isPresent()) {
                selectedItem = opt.get();
                backpack.removeItem(selectedItem); // On le retire du sac pendant qu'on le déplace
            }
        } 
        else if (pe.action() == PointerEvent.Action.POINTER_UP && selectedItem != null) {
            // Relâchement : on tente de poser l'objet à la nouvelle position
            if (!backpack.add(selectedItem, new Position(row, col))) {
                // Si le placement échoue, on pourrait le remettre à sa place d'origine ou le jeter
                System.out.println("Placement impossible !");
            }
            selectedItem = null;
        }
    }
    
    
}