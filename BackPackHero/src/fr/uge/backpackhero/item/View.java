package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import fr.uge.backpackhero.combat.CombatInteractionDelegate;
import fr.uge.backpackhero.entites.Heros;



/**
 * Console-based interaction class responsible for handling all user input 
 * and displaying the state of the {@code BackPack} and combat choices.
 */
public class View implements CombatInteractionDelegate {
	private final BackPack backPack;
	private final StuffFactory factory;
	private final Heros heros;
	private final Scanner scanner = new Scanner(System.in);

	/**
     * Initializes the View with references to the core game objects.
     *
     * @param backPack The hero's backpack instance.
     * @param factory The item factory for generating new items.
     * @param heros The hero entity.
     * @throws NullPointerException if any argument is {@code null}.
     */
	public View(BackPack backPack, StuffFactory factory, Heros heros) {
		Objects.requireNonNull(backPack);
		Objects.requireNonNull(factory);
		Objects.requireNonNull(heros);
		this.backPack = backPack;
		this.factory = factory;
		this.heros = heros;
	}
	
	/**
	 * Displays the current state of the backpack in a grid format to the console.
	 * * @param backpack The backpack to display.
	 * @throws NullPointerException if the argument is {@code null}.
	 */
    public void printBackPack() {
    	var rows = backPack.getRows();
        var cols = backPack.getColumns();
        System.out.println("\n--- Current Backpack State (" + rows + "x" + cols + ") ---");
        System.out.print("  ");
        for (int i = 0; i < cols; i++) {
            System.out.printf("%-3d", i);
        }
        System.out.println("\n  " + "---".repeat(cols));
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d|", i);
            for (int j = 0; j < cols; j++) {
            	var itemInstance = backPack.getItemAt(new Position(i, j)).orElse(null); 
                var content = (itemInstance == null) ? "." : itemInstance.toString();
                System.out.printf(" %-2s", content);
            }
            System.out.println("|");
        }
        System.out.println("---".repeat(cols + 1));
    }
	
	/**
     * Executes a demo scenario for item placement and forced curse handling.
     * 1. Places a specified number of random items interactively.
     * 2. Forces the placement of a Curse item.
     *
     * @param numItemsToRoll The number of random items to generate before the curse.
     */
	public void testCurseScenario(int turns) {
        for (int i = 0; i < turns; i++) {
            ItemInstance item = new ItemInstance(factory.randomItem());
            printBackPack();
            System.out.println("\n--- Turn " + (i + 1) + "/" + turns + " ---");
            displayItemFound(item);

            if (!interactBeforePlacement(item)) {
                System.out.println("Interaction canceled.");
                return;
            }
            attemptPlacement(item);
        }
        ItemInstance curseInst = new ItemInstance(factory.create(Stuff.Curse));
        printBackPack();
        displayItemFound(curseInst);
        forceCursePlacement(curseInst);
        printBackPack();
        ItemInstance nextItem = new ItemInstance(factory.randomItem());
        displayItemFound(nextItem);
        if (interactBeforePlacement(nextItem)) {
            attemptPlacement(nextItem);
        }
        printBackPack();
        System.out.println("Demo complete.");
    }
    
	/**
	 * Displays information about a newly found item, including its name,
	 * occupied slots, and current shape.
	 *
	 * @param item the {@code ItemInstance} that has been found
	 * @throws NullPointerException if {@code item} is {@code null}
	 */
    public void displayItemFound(ItemInstance item) {
        System.out.println("You found: " + item.getName()
                           + " (" + item.getCurrentShape().size() + " slots)");
        System.out.println("Shape: " + item.getCurrentShape());
    }
    
    /**
     * Handles the interaction loop before placing an item.
     * Allows the user to rotate the item, remove an existing item,
     * quit the interaction, or proceed to placement.
     *
     * @param item the {@code ItemInstance} the user is about to place
     * @return {@code true} if the user proceeds to placement,
     *         {@code false} if the interaction is cancelled
     * @throws NullPointerException if {@code item} is {@code null}
     */
    public boolean interactBeforePlacement(ItemInstance item) {
        while (true) {
            System.out.println("[r] Remove, [t] Rotate, [q] Quit, Enter = Place");
            String command = readLine();

            switch (command.toLowerCase()) {
                case "q": return false;
                case "r": processItemRemoval(); printBackPack(); continue;
                case "t": rotateItem(item); continue;
                case "": return true;
                default: System.err.println("Invalid command."); continue;
            }
        }
    }
    
    /**
     * Rotates the given item by 90 degrees clockwise and displays
     * its new rotation angle and shape.
     *
     * @param item the {@code ItemInstance} to rotate
     * @throws NullPointerException if {@code item} is {@code null}
     * @throws IllegalStateException if the item cannot be rotated
     */
    public void rotateItem(ItemInstance item) {
        item.rotate();
        System.out.println("Rotated to " + item.getRotationAngle() + "°");
        System.out.println(item.getCurrentShape());
    }
    
    /**
     * Prompts the user for placement coordinates and attempts
     * to place the given item into the backpack.
     *
     * @param item the {@code ItemInstance} to place
     * @throws NullPointerException if {@code item} is {@code null}
     */
    public void attemptPlacement(ItemInstance item) {
        System.out.println("Enter placement position (format: row col):");
        Position pos = readPosition();

        if (backPack.add(item, pos)) {
            System.out.println("Placed " + item.getName() + " at " + pos);
        } else {
            System.err.println("Failed to place at " + pos);
        }
    }
    
    /**
     * Reads a trimmed line of input from the console.
     *
     * @return the user input, without leading or trailing spaces
     */
    private String readLine() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }
    
    /**
     * Handles the removal of an item from the backpack.
     * If the selected item is a {@code Curse}, a confirmation
     * and penalty choice is required.
     */
    public void processItemRemoval() {
        System.out.println("Enter the row and col to remove (e.g., 1 3):");
        Position pos = readPosition();

        var instance = backPack.getItemAt(pos).orElse(null);
        if (instance == null) {
            System.err.println("No item found at " + pos);
            return;
        }

        if (instance.getItem() instanceof Curse) {
            askCurseRemoval(instance);
        } else {
            backPack.removeItem(instance);
            System.out.println("Removed " + instance.getName());
        }
    }
    
    /**
     * Asks the user whether to remove or keep a {@code Curse} item.
     * Removing the curse applies a penalty to the hero.
     *
     * @param inst the {@code ItemInstance} representing the curse
     * @throws NullPointerException if {@code inst} is {@code null}
     */
    public void askCurseRemoval(ItemInstance inst) {
        System.out.println("This is a curse: remove (r) or keep (k)?");
        String choice = readLine().toLowerCase();

        if ("r".equals(choice)) {
            backPack.removeItem(inst);
            heros.applyCurseRemovalPenalty();
            System.out.println("Curse removed. Penalty applied.");
        } else {
            System.out.println("Curse kept.");
        }
    }
    
    /**
     * Forces the user to place a curse item into the backpack.
     * The method loops until a valid placement is performed.
     *
     * @param curseItem the {@code ItemInstance} of the curse to place
     * @throws NullPointerException if {@code curseItem} is {@code null}
     */
    private void forceCursePlacement(ItemInstance curseItem) {
        System.out.println("You must place the curse now!");
        while (true) {
            System.out.println("Enter position to place curse (row col):");
            Position pos = readPosition();
            if (backPack.add(curseItem, pos)) {
                System.out.println("Curse placed at " + pos);
                return;
            }
            System.err.println("Cannot place curse there. Try again.");
        }
    }
    
    /**
     * Implementation of the {@code CombatInteractionDelegate} interface.
     * Handles the forced {@code Curse} interaction initiated by an enemy during combat.
     *
     * @param heros The Hero targeted by the curse.
     * @param curse The Curse object to be handled.
     * @throws NullPointerException if heros or curse is {@code null}.
     */
    public void handleForcedCurse(Heros heros, Curse curse) {
        System.out.println("Enemy casts a Curse!");
        System.out.println("Accept (a) & place, or refuse (r) and take damage?");
        String choice = readLine().toLowerCase();

        if ("a".equals(choice)) {
            heros.acceptCurseImmediate();
            forceCursePlacement(new ItemInstance(curse));
        } else {
            heros.refuseCurseImmediate();
            System.out.println("You chose to refuse the curse.");
        }
    }
    
    /**
     * Reads and validates a position entered by the user.
     * The input must consist of two integers corresponding to
     * a valid row and column inside the backpack.
     *
     * @return a valid {@code Position} inside the backpack
     */
    private Position readPosition() {
        while (true) {
            String input = readLine();
            String[] parts = input.split("\\s+");
            if (parts.length == 2 && isInteger(parts[0]) && isInteger(parts[1])) {
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                if (isInside(row, col)) {
                    return new Position(row, col);
                }
            }
            System.err.println("Invalid position. Expected: row col inside backpack.");
        }
    }

    /**
     * Checks whether the given coordinates are inside the backpack bounds.
     *
     * @param row the row index
     * @param col the column index
     * @return {@code true} if the coordinates are valid, {@code false} otherwise
     */
    boolean isInside(int row, int col) {
        return row >= 0 && row < backPack.getRows()
            && col >= 0 && col < backPack.getColumns();
    }
    
    /**
     * Checks whether the given string represents a non-negative integer.
     *
     * @param s the string to test
     * @return {@code true} if the string contains only digits, {@code false} otherwise
     */
    private boolean isInteger(String s) {
        return s.matches("\\d+");
    }
}
