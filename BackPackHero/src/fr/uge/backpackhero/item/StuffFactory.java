package fr.uge.backpackhero.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class StuffFactory {
	public static final Map<Rarity, Integer> RARITY_WEIGHTS = Map.of(
	        Rarity.COMMON, 800,
	        Rarity.UNCOMMON, 150,
	        Rarity.RARE, 45,
	        Rarity.LEGENDARY, 5
	    );
	private final Random random = new Random();
	
	
	public StuffFactory() {
	}
	
	private Rarity randomRarity() {
	    int total = RARITY_WEIGHTS.values().stream()
	    		.mapToInt(i -> i)
	    		.sum();
	    int roll = random.nextInt(total);

	    int cumulative = 0;
	    for (var entry : RARITY_WEIGHTS.entrySet()) {
	        cumulative += entry.getValue();
	        if (roll < cumulative) {
	            return entry.getKey();
	        }
	    }
	    throw new IllegalStateException("Unexpected rarity roll");
	}
	
	private Stuff randomStuffOfRarity(Rarity rarity) {
	    var list = Arrays.stream(Stuff.values())
	            .filter(s -> s.rarity() == rarity)
	            .toList();
	    return list.get(random.nextInt(list.size()));
	}
	
	public Item randomItem() {
	    var rarity = randomRarity();              // 1) tirer une rareté pondérée
	    var stuff = randomStuffOfRarity(rarity);  // 2) tirer un item de cette rareté
	    return create(stuff);                    // 3) créer l’objet final
	}
	
	/**
	 * Crée une arme en fonction du stuff passé en argument, voir l'enum stuff pour voir quels sont les stuff disposnibles.
	 * @param stuff
	 * @return l'item crée
	 * 
	 */
	public Item create(Stuff stuff) {
		Objects.requireNonNull(stuff);
		return switch(stuff) {
		case WoodSword -> new MeleeWeapon("Wood Sword", List.of(new Position(0,0), new Position(1,0), new Position(2,0)), stuff.rarity(), 7, 1, 10);
		case MouseBow -> new RangeWeapon("Mouse Bow", List.of(new Position(0,0), new Position(1,0)), stuff.rarity(), 1, 12);
		case ShortArrow -> new Arrow("Short Arrow", List.of(new Position(0,0)), stuff.rarity(), 2, 3);
		case LeatherCap -> new Armor("Leather Cap", List.of(new Position(0,0)), stuff.rarity(), 1, 7);
		case RoughBuckler -> new Shield("Rough Buckler", List.of(new Position(0,0), new Position(0,1), new Position(1,0), new Position(1,1)), stuff.rarity(), 7, 1, 10);
		case ElectricWand -> new MagicItem("Electric Wand", List.of(new Position(0,1), new Position(1,0)), stuff.rarity(), 5, 1, 13);
		case CloudSword -> new MeleeWeapon("Cloud Sword", List.of(new Position(0,0), new Position(1,0), new Position(2,0)), stuff.rarity(), 8, 1, 30);
		case ManaStone -> new ManaStone("Mana Stone", List.of(new Position(0,0)), stuff.rarity(), 1, 3);
		case Curse -> new Curse(List.of(new Position(0,0), new Position(0,1), new Position(1,1), new Position(1,2)));
		};
	}
}
