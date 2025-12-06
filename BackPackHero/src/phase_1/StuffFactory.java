package phase_1;

import java.util.List;
import java.util.Objects;

public class StuffFactory {
	
	public StuffFactory() {}
	
	/**
	 * Crée une arme en fonction du stuff passé en argument, voir l'enum stuff pour voir quels sont les stuff disposnibles.
	 * @param stuff
	 * @return l'item crée
	 * 
	 */
	public Item create(Stuff stuff) {
		Objects.requireNonNull(stuff);
		return switch(stuff) {
		case WoodSword -> new MeleeWeapon("Wood Sword", List.of(new Position(0,0), new Position(1,0), new Position(2,0)), Rarity.COMMON, 7, 1, 10);
		case CompositeBow -> new RangeWeapon("Composite Bow", List.of(new Position(0,0), new Position(1,0)), Rarity.COMMON, 1, 12);
		case ShortArrow -> new Arrow("Short Arrow", List.of(new Position(0,0)), Rarity.COMMON, 2, 3);
		case LeatherCap -> new Armor("Leather Cap", List.of(new Position(0,0)), Rarity.COMMON, 1, 7);
		case RoughBuckler -> new Shield("Rough Buckler", List.of(new Position(0,0), new Position(0,1), new Position(1,0), new Position(1,1)), Rarity.COMMON, 7, 1, 10);
		case ElectricWand -> new MagicItem("Electric Wand", List.of(new Position(0,1), new Position(1,0)), Rarity.COMMON, 5, 1, 13);
		case ManaStone -> new ManaStone("Mana Stone", List.of(new Position(0,0)), 1, 3);
		};
	}
}
