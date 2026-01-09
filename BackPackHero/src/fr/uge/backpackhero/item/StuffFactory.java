package fr.uge.backpackhero.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * A factory class responsible for creating concrete {@code Item} objects
 * based on a weighted rarity distribution and item type {@code Stuff}.
 */
public class StuffFactory {
	
	/**
     * Defines the weighted probability for drawing a specific rarity.
     * Higher weight means higher chance of being selected.
     */
	public static final Map<Rarity, Integer> RARITY_WEIGHTS = Map.of(
	        Rarity.COMMON, 800,
	        Rarity.UNCOMMON, 150,
	        Rarity.RARE, 45,
	        Rarity.LEGENDARY, 5
	    );
	private final Random random = new Random();
	
	/**
     * Creates a new instance of the {@code StuffFactory}.
     */
	public StuffFactory() {
	}
	
	/**
     * Randomly selects a {@code Rarity} level based on the defined {@code RARITY_WEIGHTS}.
     *
     * @return A randomly determined {@code Rarity}.
     * @throws IllegalStateException if the random roll is outside the cumulative weight range.
     */
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
	
	/**
     * Randomly selects an item type ({@code Stuff}) that matches the given rarity level.
     *
     * @param rarity The target rarity level.
     * @return A randomly selected {@code Stuff} enumeration constant of the specified rarity.
     */
	private Stuff randomStuffOfRarity(Rarity rarity) {
	    var list = Arrays.stream(Stuff.values())
	            .filter(s -> s.rarity() == rarity)
	            .toList();
	    return list.get(random.nextInt(list.size()));
	}
	
	/**
     * Generates a complete, random {@code Item} instance by first rolling a rarity
     * and then selecting a corresponding item type.
     *
     * @return A newly created {@code Item} instance.
     */
	public Item randomItem() {
	    var rarity = randomRarity();              // 1) tirer une rareté pondérée
	    var stuff = randomStuffOfRarity(rarity);  // 2) tirer un item de cette rareté
	    return create(stuff);                    // 3) créer l’objet final
	}
	
	/**
	 * Creates a concrete {@code Item} object based on the provided {@code Stuff} type.
	 *
	 * @param stuff The item type enumeration constant specifying which item to create.
	 * @return The newly created concrete {@code Item} object.
	 * @throws NullPointerException if {@code stuff} is {@code null}.
	 * @throws IllegalArgumentException if the {@code Stuff} type is unknown or improperly handled.
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
	
	/**
	 * Crée un Item à partir de son nom exact.
	 */
	public Item getItem(String name) {
	  Objects.requireNonNull(name);
    return switch (name) {
    case "Wood Sword"    -> create(Stuff.WoodSword);
    case "Mouse Bow"     -> create(Stuff.MouseBow);
    case "Short Arrow"   -> create(Stuff.ShortArrow);
    case "Leather Cap"   -> create(Stuff.LeatherCap);
    case "Rough Buckler" -> create(Stuff.RoughBuckler);
    case "Electric Wand" -> create(Stuff.ElectricWand);
    case "Cloud Sword"   -> create(Stuff.CloudSword);
    case "Mana Stone"    -> create(Stuff.ManaStone);
    default -> throw new IllegalArgumentException("Nom d'item inconnu : " + name);
    };
	}
}
