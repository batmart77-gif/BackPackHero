package fr.uge.backpackhero.donjon;

import java.awt.Graphics2D;
import java.util.Objects;

import fr.uge.backpackhero.Jeu;
import fr.uge.backpackhero.entites.Heros;
import fr.uge.backpackhero.graphics.ImageManager;

/**
 * Représente une salle de rencontre aléatoire.
 * Le contenu est révélé uniquement lorsque le héros y pénètre.
 */
public record EventRoom(String description) implements Room {
  public EventRoom {
    Objects.requireNonNull(description);
  }
  
  public enum EventType {
    FAVORABLE,   // Gain de PV ou d'or
    DANGEROUS,    // Piège (perte de PV)
    MYSTERIOUS   // Trouve un objet rare
  }
  

    public void triggerEffect(Heros heros) {
        java.util.Random rdm = new java.util.Random();
        int chance = rdm.nextInt(3);
        
        switch (chance) {
            case 0 -> {
                heros.gagnerOr(10);
                System.out.println("Surprise ! Vous trouvez une bourse d'or.");
            }
            case 1 -> {
                heros.recevoirDegats(5);
                System.out.println("Aïe ! Un piège se déclenche.");
            }
            default -> {
                heros.soigner(5);
                System.out.println("Une fontaine magique vous redonne de la vie.");
            }
        }
    }
    
    
    @Override
    public void draw(Graphics2D g, int x, int y, int size, ImageManager img) {
        g.drawImage(img.getImage("event"), x, y, size, size, null);
    }
    
    @Override
    public void onClick(Jeu jeu) {
        this.triggerEffect(jeu.getHeros()); // Phase 3 : Effet aléatoire
    }
}