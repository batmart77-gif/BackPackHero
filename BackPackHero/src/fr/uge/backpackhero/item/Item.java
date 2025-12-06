package fr.uge.backpackhero.item;

import java.util.List;

import fr.uge.backpackhero.entites.Ennemi;
import fr.uge.backpackhero.entites.Heros;


public sealed interface Item permits RangeWeapon, Arrow, MeleeWeapon, Armor, Shield, ManaStone, MagicItem, Gold, Curse {
	public abstract int price();
	public abstract String toString();
	public abstract List<Position> pos();
	public abstract String name();
	public abstract String details();
	public abstract Rarity rarity();
	
	
	/**
   * Tente d'utiliser l'objet en combat.
   * @param heros Le héros qui utilise l'objet.
   * @param cible L'ennemi visé (peut être null pour un bouclier).
   * @return true si l'objet a été utilisé, false sinon.
   */
  boolean utiliser(Heros heros, Ennemi cible);
  
  /**
   * Indique si l'objet peut être tourné.
   */
  default boolean rotatable() {
      return true;
  }
}


