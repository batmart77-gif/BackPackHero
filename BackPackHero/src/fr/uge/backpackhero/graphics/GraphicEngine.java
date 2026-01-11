package fr.uge.backpackhero.graphics;

import com.github.forax.zen.*;
import com.github.forax.zen.Event;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.Mode;
import fr.uge.backpackhero.donjon.Room;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.item.ItemInstance;
import fr.uge.backpackhero.item.Position;
import java.util.List; 

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
                          
              
              if (event instanceof PointerEvent pe) {
                  // ON MET À JOUR LES COORDONNÉES TOUT LE TEMPS
                  mouseX = pe.location().x();
                  mouseY = pe.location().y();

                  // ON NE TRAITE LE CLIC (onClick) QUE SI LE BOUTON EST PRESSÉ
                  if (pe.action() == PointerEvent.Action.POINTER_DOWN) {
                      handleMouse(pe); 
                  }
              }
/*
              context.renderFrame(g -> {
                  renderBackground(g, context.getScreenInfo());
                  renderDungeon(g);
                  renderBackpack(g);
                  renderHUD(g);
  
                  // DÉTECTION DU MARCHAND
                  var floor = jeu.getDonjon().getCurrentFloor();
                  var currentRoom = floor.getRoom(jeu.getX(), jeu.getY());
  
                  if (currentRoom instanceof fr.uge.backpackhero.donjon.MerchantRoom merchant) {
                      renderMerchantUI(g, merchant);
                  }
                  
                  
                  if (selectedItem != null) {
                    String fileName = selectedItem.getItem().name().replace(" ", "_");
                    Image ghost = img.getImage(fileName);
                    
                    // On rend l'image un peu transparente (50%)
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g.drawImage(ghost, mouseX - 32, mouseY - 32, TILE_SIZE, TILE_SIZE, null);
                    // On n'oublie pas de remettre la transparence à 100% pour le reste du dessin
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                }
                  
              });  */
              
              context.renderFrame(g -> {
                renderDungeon(g);
                renderBackpack(g);
                renderHUD(g);

                // On récupère la salle où se trouve le héros
                var floor = jeu.getDonjon().getCurrentFloor();
                var currentRoom = floor.getRoom(jeu.getX(), jeu.getY());

                // On vérifie le mode pour afficher l'interface correspondante
                switch (jeu.getMode()) {
                    case BOUTIQUE -> {
                        // CAST OBLIGATOIRE : On transforme Room en MerchantRoom
                        if (currentRoom instanceof fr.uge.backpackhero.donjon.MerchantRoom merchant) {
                            renderMerchantUI(g, merchant);
                        }
                    }
                    case SOIN -> renderHealerUI(g);
                    case COMBAT -> renderCombatUI(g);
                    case PERDU -> renderGameOver(g);
                    case GAGNE -> renderVictory(g);
                    default -> {} 
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
      
   // On récupère la position de la souris via les variables que nous avons déjà
      int hoverCol = (mouseX - 100) / TILE_SIZE;
      int hoverRow = (mouseY - 180) / TILE_SIZE;

      // Si la souris est dans le donjon, on dessine un cadre
      if (hoverCol >= 0 && hoverCol < floor.width() && hoverRow >= 0 && hoverRow < floor.height()) {
          g.setColor(new Color(255, 255, 255, 80)); // Blanc très transparent
          g.setStroke(new BasicStroke(3)); // Un trait un peu plus épais
          g.drawRect(hoverCol * TILE_SIZE + 100, hoverRow * TILE_SIZE + 180, TILE_SIZE, TILE_SIZE);
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
/*
    private void renderHUD(Graphics2D g) {
      g.setColor(new Color(0, 0, 0, 150));
      g.fillRect(80, 45, 500, 50); // Fond du texte
      
      g.setColor(Color.WHITE);
      g.setFont(new Font("Serif", Font.BOLD, 28));
      g.drawString("HP: " + jeu.getHeros().getPv() + " / 40", 100, 80);
      g.drawString("Gold: " + jeu.getHeros().getGold(), 400, 80);
  }
*/
    
    private void renderHUD(Graphics2D g) {
      int hp = jeu.getHeros().getPv();
      int hpMax = 40; // Ou jeu.getHeros().getMaxPv()
      int barWidth = 250;
      
      // 1. Dessiner le fond de la barre (Noir/Gris)
      g.setColor(new Color(50, 50, 50));
      g.fillRect(100, 50, barWidth, 25);
      
      // 2. Calculer et dessiner la partie pleine (Vert ou Rouge si bas)
      float ratio = (float) hp / hpMax;
      g.setColor(ratio > 0.3 ? new Color(46, 204, 113) : new Color(231, 76, 60));
      g.fillRect(100, 50, (int)(barWidth * ratio), 25);
      
      // 3. Dessiner le contour et le texte
      g.setColor(Color.WHITE);
      g.drawRect(100, 50, barWidth, 25);
      g.setFont(new Font("Arial", Font.BOLD, 14));
      g.drawString("HP: " + hp + " / " + hpMax, 100 + 80, 68);
      
      // 4. Icône pour l'Or
      g.drawString("Gold: " + jeu.getHeros().getGold(), 400, 68);
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

   
    
    private void renderMerchantUI(Graphics2D g, fr.uge.backpackhero.donjon.MerchantRoom room) {
      int winW = 500;
      int winH = 300;
      int startX = 200;
      int startY = 250;

      // 1. Fond de la fenêtre (Noir transparent avec bordure dorée)
      g.setColor(new Color(0, 0, 0, 220));
      g.fillRoundRect(startX, startY, winW, winH, 20, 20);
      g.setColor(new Color(212, 175, 55)); // Or
      g.setStroke(new BasicStroke(3));
      g.drawRoundRect(startX, startY, winW, winH, 20, 20);

      // 2. Titre
      g.setFont(new Font("Serif", Font.BOLD, 30));
      g.drawString("--- Boutique du Marchand ---", startX + 60, startY + 50);

      // 3. Affichage des objets du stock
      var stock = room.stock(); // On récupère la liste des ItemInstance
      for (int i = 0; i < stock.size(); i++) {
          var item = stock.get(i);
          int itemX = startX + 50 + (i * 150);
          int itemY = startY + 100;

          // Image de l'objet
          String fileName = item.getItem().name().replace(" ", "_");
          g.drawImage(img.getImage(fileName), itemX, itemY, 80, 80, null);

          // Prix fictif (ex: 10 Gold)
          g.setFont(new Font("Arial", Font.BOLD, 18));
          g.setColor(Color.YELLOW);
          g.drawString("10", itemX + 15, itemY + 110);
      }
      
      g.setFont(new Font("Arial", Font.ITALIC, 14));
      g.setColor(Color.WHITE);
      g.drawString("Appuyez sur une touche pour quitter", startX + 130, startY + 270);
  }
    
    private void handleMouse(PointerEvent pe) {
      if (pe.action() != PointerEvent.Action.POINTER_DOWN) return;
      
      int mx = pe.location().x();
      int my = pe.location().y();
      
      if (jeu.getMode() == Mode.COMBAT) {
        handleCombatClick(mx, my);
        return; 
      }

      // SI ON EST DANS LA BOUTIQUE
      if (jeu.getMode() == Mode.BOUTIQUE) {
          gererClicBoutique(mx, my); 
          return; // On s'arrête ici pour ne pas déplacer le héros sous la fenêtre
      }
      
      // SINON : Déplacement normal dans le donjon
      int col = (mx - 100) / TILE_SIZE;
      int row = (my - 180) / TILE_SIZE;

      var floor = jeu.getDonjon().getCurrentFloor();

      // 3. Vérifier si on a cliqué dans la grille
      if (col >= 0 && col < floor.width() && row >= 0 && row < floor.height()) {
          var room = floor.getRoom(col, row);
          
          if (room != null) {
              // Optionnel : On ne peut cliquer que si la salle est à côté du héros
              if (Math.abs(col - jeu.getX()) + Math.abs(row - jeu.getY()) <= 1) {
                  // On déplace le héros
                  jeu.deplacer(col - jeu.getX(), row - jeu.getY());
                  
                  // On active l'interaction de la salle !
                  room.onClick(jeu);
              }
          }
      }
    }
    
    private void gererClicBoutique(int mx, int my) {
      // Exemple : Si on clique dans la zone du premier objet
      // x entre 250 et 330, y entre 350 et 430
      if (mx > 250 && mx < 330 && my > 350 && my < 430) {
          // Exécuter l'achat visuellement !
          System.out.println("Objet acheté graphiquement !");
      }
    }
    
    
 // --- MÉTHODES DE RENDU DES INTERFACES ---
    private void renderHealerUI(Graphics2D g) {
      int startX = 250;
      int startY = 200;
      
      // Fenêtre principale
      g.setColor(new Color(20, 20, 20, 240));
      g.fillRoundRect(startX, startY, 300, 350, 15, 15);
      g.setColor(new Color(255, 100, 100)); // Rouge soin
      g.drawRoundRect(startX, startY, 300, 350, 15, 15);

      g.setFont(new Font("Serif", Font.BOLD, 24));
      g.drawString("Le Guérisseur", startX + 70, startY + 50);

      // Bouton Option 1
      drawButton(g, "1. Soin Léger (+10 PV)", startX + 50, startY + 100, 200, 40, Color.GREEN);
      g.drawString("Coût : 5 Or", startX + 100, startY + 160);

      // Bouton Option 2
      drawButton(g, "2. Soin Total (MAX)", startX + 50, startY + 200, 200, 40, Color.CYAN);
      g.drawString("Coût : 15 Or", startX + 100, startY + 260);
      
      g.setFont(new Font("Arial", Font.PLAIN, 12));
      g.setColor(Color.WHITE);
      g.drawString("Cliquez sur un bouton ou 'Q' pour quitter", startX + 40, startY + 320);
  }

  private void drawButton(Graphics2D g, String text, int x, int y, int w, int h, Color c) {
      g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
      g.fillRect(x, y, w, h);
      g.setColor(c);
      g.drawRect(x, y, w, h);
      g.setFont(new Font("Arial", Font.BOLD, 14));
      g.drawString(text, x + 20, y + 25);
  }
   



    

  

    private void renderGameOver(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 1200, 800);
        g.setColor(Color.RED);
        g.drawString("VOUS ÊTES MORT", 500, 400);
    }

    private void renderVictory(Graphics2D g) {
        g.setColor(new Color(0, 100, 0, 200));
        g.fillRect(0, 0, 1200, 800);
        g.setColor(Color.YELLOW);
        g.drawString("VICTOIRE !", 550, 400);
    }
    
    private void renderCombatUI(Graphics2D g) {
      var combat = jeu.getCombat();
      if (combat == null) return;

      g.setColor(new Color(0, 0, 0, 180));
      g.fillRect(0, 0, 800, 600); 

      List<Ennemi> ennemis = combat.getAliveEnemies(); 
      int spacing = 800 / (ennemis.size() + 1);

      for (int i = 0; i < ennemis.size(); i++) {
          Ennemi e = ennemis.get(i);
          int x = (i + 1) * spacing - 64; 
          int y = 250;

          g.drawImage(img.getImage(e.getName()), x, y, 128, 128, null);
          renderEnemyHealthBar(g, e, x, y - 30);

          // LOGIQUE DU BOUTON : Augmentons un peu la zone de détection
          if (mouseX >= x && mouseX <= x + 128 && mouseY >= y && mouseY <= y + 128) {
              // On dessine le bouton BIEN AU CENTRE de l'image pour être sûr de le voir
              drawAttackButton(g, x + 14, y + 45); 
          }
      }
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
      g.fillRect(x, y, (int)(width * ratio), 10);
      g.setColor(Color.WHITE);
      g.drawRect(x, y, width, 10);
    }
    
    private void handleCombatClick(int mx, int my) {
      var combat = jeu.getCombat();
      if (combat == null) return;

      List<Ennemi> ennemis = combat.getAliveEnemies();
      int spacing = 800 / (ennemis.size() + 1);

      for (int i = 0; i < ennemis.size(); i++) {
          Ennemi e = ennemis.get(i);
          int x = (i + 1) * spacing - 64;
          int y = 250;
          // Si on clique sur le monstre (ou son bouton d'attaque)
          if (mx >= x && mx <= x + 128 && my >= y && my <= y + 170) {
              // Pour l'instant, on simule une attaque simple de 7 dégâts (comme ton épée bois)
              // Dans une version finale, on utilisera l'objet sélectionné dans le sac
              e.recevoirDegats(7); 
              System.out.println("BAM ! 7 dégâts infligés à " + e.getName());
              
              // On vérifie si le combat est fini après l'attaque
              jeu.updateCombatState(); 
          }
      }
  }
}