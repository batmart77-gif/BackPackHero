package fr.uge.backpackhero.item;

import java.util.List;
import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

public record Gold() implements Item {
    
	public Gold {
	}
	
	@Override
	public int price() {
		return 0;
	}

    @Override
	public String toString() {
		return "Gld";
	}
    
    @Override
    public Rarity rarity() {
    	return Rarity.COMMON;
    }

    @Override
	public List<Position> pos() {
		return List.of(new Position(0, 0));
	}

    @Override
	public String name() {
		return "Gold";
	}
	
    @Override
	public String details() {
		return "A simple gold coin";
	}
	
	/**
    * Tente d'utiliser l'objet en combat.
    * L'or n'étant pas un objet utilisable, cette méthode ne fait rien et retourne false.
    */
	@Override
	public boolean utiliser(Heros heros, Ennemi cible) {
		return false; 
	}
}
