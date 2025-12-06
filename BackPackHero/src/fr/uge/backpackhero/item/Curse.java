package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;

public record Curse(List<Position> pos) implements Item {
	
	public Curse {
		Objects.requireNonNull(pos);
	}

	@Override
	public int price() {
		return 0;
	}

	@Override
	public String name() {
		return "Curse";
	}

	@Override
	public String details() {
		return "Nothing special about a curse...";
	}

	@Override
	public Rarity rarity() {
		return Rarity.CURSE;
	}

	@Override
	public boolean utiliser(Heros heros, Ennemi cible) {
		return false;
	}
	
	@Override
    public boolean rotatable() {
        return false;
    }
	
	@Override
	public String toString() {
		return "C";
	}

}
