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
     * Executes a demo scenario for item placement and forced curse handling.
     * 1. Places a specified number of random items interactively.
     * 2. Forces the placement of a Curse item.
     *
     * @param numItemsToRoll The number of random items to generate before the curse.
     */
    public void testCurseScenario(int numItemsToRoll) {
        System.out.println("\n#################################################");
        System.out.println("          DEMO SCÉNARIO : PLACEZ VOS ITEMS");
        System.out.println("#################################################");

        // 1. Phase de placement d'items aléatoires
        for (int i = 0; i < numItemsToRoll; i++) {
            var baseItem = factory.randomItem();
            var currentInstance = new ItemInstance(baseItem);
            
            printBackPack(backPack);
            System.out.println("\n--- Turn " + (i + 1) + "/" + numItemsToRoll + " ---");
            System.out.println("You find a new Item: **" + currentInstance.getName() + "** (" + currentInstance.getCurrentShape().size() + " slots)");
            System.out.println("Shape : " + currentInstance.getCurrentShape());
            
            // Gère les commandes (rotation, suppression, etc.)
            if (!handleCommands(backPack, currentInstance, this.scanner)) {
                System.out.println("Quitting interaction.");
                this.scanner.close();
                return;
            }
            
            // Tente le placement
            if (handlePlacement(backPack, currentInstance, this.scanner)) {
                // Item placé, continue la boucle
            } else {
                System.out.println("\nCannot place the item, it is lost. Moving to the next turn.");
            }
        }

        // 2. Phase de la Malédiction (Curse)
        System.out.println("\n#################################################");
        System.out.println("              !!! UNE MALÉDICTION APPARAÎT !!!");
        System.out.println("#################################################");
        
        // Générer spécifiquement l'item Curse
        var curseItem = factory.create(Stuff.Curse);
        var curseInstance = new ItemInstance(curseItem);
        
        printBackPack(backPack);
        System.out.println("\nVous êtes maudit ! Vous devez placer la **MALÉDICTION** (" + curseInstance.getCurrentShape().size() + " slots)");
        System.out.println("Forme de la Malédiction: " + curseInstance.getCurrentShape());
        
        // La malédiction ne peut pas être tournée (rotatable() retourne false)
        // On ne gère pas les commandes ici, on passe directement au placement
        
        var placed = false;
        while (!placed) {
            System.out.println("\nVous devez placer la Malédiction pour continuer...");
            if (handlePlacement(backPack, curseInstance, this.scanner)) {
                placed = true;
                System.out.println("\nLa Malédiction est placée. La partie continue...");
            } else {
                // Le placement a échoué (hors limite ou espace occupé)
                System.err.println("Vous DEVEZ trouver un endroit pour la placer.");
            }
        }
        
        printBackPack(backPack);
        var newItem = factory.randomItem();
        var newCurrentInstance = new ItemInstance(newItem);
        System.out.println("Vous trouvez un nouvel Item: **" + newCurrentInstance.getName() + "** (" + newCurrentInstance.getCurrentShape().size() + " slots)");
        
        // Gère les commandes (rotation, suppression, etc.)
        if (!handleCommands(backPack, newCurrentInstance, this.scanner)) {
            System.out.println("Quittez l'interaction.");
            this.scanner.close();
            return;
        }
        
        // Tente le placement
        if (handlePlacement(backPack, newCurrentInstance, this.scanner)) {
            // Item placé, continue la boucle
        } else {
            System.out.println("\nImpossible de placer l'item, il est perdu. Passons au suivant.");
        }
        printBackPack(backPack);
        this.scanner.close();
        System.out.println("Scénario de test terminé.");
    }
    
    @Override
    public void handleForcedCurse(Heros heros, Curse curse) {
            handleForcedCurse(heros, curse, this.scanner);
    }
    
    /**
     * Implementation of the {@code CombatInteractionDelegate} interface.
     * Handles the forced {@code Curse} interaction initiated by an enemy during combat.
     *
     * @param heros The Hero targeted by the curse.
     * @param curse The Curse object to be handled.
     * @throws NullPointerException if heros or curse is {@code null}.
     */
    public void handleForcedCurse(Heros heros, Curse curse, Scanner scanner) {
        Objects.requireNonNull(heros);
        Objects.requireNonNull(curse);
        Objects.requireNonNull(scanner);
        System.out.println("The enemy casts a Curse on you !");
        System.out.println("Choice: (A)ccept the Curse and place it in the backpack or (R)efuse and suffer damage ?");
        System.out.print("> ");
        String choix = scanner.nextLine().trim().toUpperCase();
        if (choix.equals("A")) {
            heros.acceptCurseImmediate();
            var instance = new ItemInstance(curse);
            printBackPack(heros.getBackpack());
            System.out.println("\nYou must immediately place the Curse : " + instance.getName());
            var placed = false;
            while(!placed) {
                 Position startPos = readPlacementCoordinates(scanner);
                 if (ItemPlacement(heros.getBackpack(), instance, startPos)) {
                     placed = true;
                     System.out.println("Curse placed. You continue the fight.");
                 } else {
                     System.err.println("Placement impossible. Please retry.");
                 }
            }
        } else {
            heros.refuseCurseImmediate();
            System.out.println("Curse refused. Damage penalty applied.");
        }
    }
	
    /**
	 * Displays the current state of the backpack in a grid format to the console.
	 * * @param backpack The backpack to display.
	 * @throws NullPointerException if the argument is {@code null}.
	 */
    public static void printBackPack(BackPack backpack) {
    	Objects.requireNonNull(backpack);
        var rows = 3;
        var cols = 5;
        System.out.println("\n--- Current Backpack State (" + rows + "x" + cols + ") ---");
        System.out.print("  ");
        for (int i = 0; i < cols; i++) {
            System.out.printf("%-3d", i);
        }
        System.out.println("\n  " + "---".repeat(cols));
        for (int i = 0; i < rows; i++) {
            System.out.printf("%d|", i);
            for (int j = 0; j < cols; j++) {
            	var itemInstance = backpack.getItemAt(new Position(i, j)).orElse(null); 
                var content = (itemInstance == null) ? "." : itemInstance.toString();
                System.out.printf(" %-2s", content);
            }
            System.out.println("|");
        }
        System.out.println("---".repeat(cols + 1));
    }
    
    /**
     * Enters an interactive loop mode allowing the user to find and place items indefinitely.
     *
     * @param backpack The backpack to modify.
     * @param factory The factory to create random items.
     * @throws NullPointerException if arguments are {@code null}.
     */
    public void editBackPack(BackPack backpack, StuffFactory factory) {
    	Objects.requireNonNull(backpack);
    	Objects.requireNonNull(factory);
        System.out.println("\n#################################################");
        System.out.println("                   BACKPACK");
        System.out.println("#################################################");
        while (true) {
        	var baseItem = factory.randomItem();
            var currentInstance = new ItemInstance(baseItem);
            printBackPack(backpack);
            System.out.println("\nYou found a new Item: **" + currentInstance.getName() + "** (" + currentInstance.getCurrentShape().size() + " slots)");
            System.out.println("Item Shape (relative positions): " + currentInstance.getCurrentShape());
            if (!handleCommands(backpack, currentInstance, this.scanner)) {
                break;
            }
            if (handlePlacement(backpack, currentInstance, this.scanner)) {
            }
        }
        this.scanner.close();
        System.out.println("Interactive mode finished.");
    }

    /**
     * Reads and validates the user's input for the placement coordinates (top-left corner).
     *
     * @param scanner The Scanner object for reading input.
     * @return The validated {@code Position}.
     * @throws NullPointerException if the argument is {@code null}.
     */
    private Position readPlacementCoordinates(Scanner scanner) {
        Objects.requireNonNull(scanner);

        while (true) {
            try {
                System.out.print("Enter the ROW (0 to 2): ");
                int row = Integer.parseInt(scanner.nextLine().trim());

                System.out.print("Enter the COLUMN (0 to 4): ");
                int col = Integer.parseInt(scanner.nextLine().trim());

                if (row < 0 || row >= 3 || col < 0 || col >= 5) {
                    System.err.println("Coordinates out of bounds. Backpack is 3 rows (0–2) and 5 cols (0–4).");
                    continue;
                }

                return new Position(row, col);

            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Please enter numbers.");
            }
        }
    }

    /**
     * Attempts to add the given item to the backpack at the specified starting position and displays the result.
     *
     * @param backpack The target backpack.
     * @param newItem The item instance to add.
     * @param startPos The top-left position for placement.
     * @return {@code true} if the item was added successfully, {@code false} otherwise.
     * @throws NullPointerException if any argument is {@code null}.
     */
    private boolean ItemPlacement(BackPack backpack, ItemInstance newItem, Position startPos) {
    	Objects.requireNonNull(backpack);
    	Objects.requireNonNull(newItem);
    	Objects.requireNonNull(startPos);
        if (backpack.add(newItem, startPos)) {
            System.out.print("Successful placement of " + newItem.getName() + " at " + startPos + ".");
            return true;
        } else {
            System.out.print("Placement impossible at " + startPos + ". Space already occupied or out of bounds.");
            return false;
        }
    }
    
    /**
     * Handles the final reading of coordinates and the attempt to add the item.
     *
     * @param backpack The target backpack.
     * @param currentInstance The {@code ItemInstance} to place.
     * @param scanner The Scanner object for reading input.
     * @return {@code true} if the item was added successfully, {@code false} otherwise.
     * @throws NullPointerException if any argument is {@code null}.
     */
    private boolean handlePlacement(BackPack backpack, ItemInstance currentInstance, Scanner scanner) {
        Position startPos = readPlacementCoordinates(scanner);
        if (startPos == null) return false;
        return ItemPlacement(backpack, currentInstance, startPos);
    }
    
    /**
     * Reads and validates the coordinates for the item the user wishes to remove.
     *
     * @param scanner The Scanner object for reading input.
     * @return The entered {@code Position} or {@code null} in case of irrecoverable input error.
     * @throws NullPointerException if the argument is {@code null}.
     */
    private Position readItemRemovalCoordinates(Scanner scanner) {
        Objects.requireNonNull(scanner);
        System.out.print("Enter the ROW and COLUMN of the Item to remove (e.g., 1 3): ");
        
        try {
            String[] parts = scanner.nextLine().trim().split("\\s+");
            if (parts.length < 2) {
                System.err.println("Error: Invalid coordinate format. Expected: ROW COLUMN.");
                return null;
            }
            var r = Integer.parseInt(parts[0]);
            var c = Integer.parseInt(parts[1]);
            return new Position(r, c);
        } catch (NumberFormatException e) {
            System.err.println("Error: Row or column must be numeric.");
            return null;
        }
    }

    /**
     * Handles the interaction and consequences for removing a {@code Curse} item.
     *
     * @param itemToRemove The {@code ItemInstance} of the Curse.
     * @param scanner The Scanner object.
     * @throws NullPointerException if itemToRemove or scanner is {@code null}.
     */
    private void handleCurseRemovalChoice(ItemInstance itemToRemove, Scanner scanner) {
        System.out.println("This is a Curse! Do you want to **Remove** (r) it, suffering a penalty, or **Keep** (g) it?");
        System.out.print("Choice (r/g): ");
        String curseChoice = scanner.nextLine().trim().toUpperCase();

        if (curseChoice.equals("R")) {
            if (this.backPack.removeItem(itemToRemove)) {
                this.heros.applyCurseRemovalPenalty(); //Application de la pénalité HP Max
                System.out.println("Curse removed. Max HP penalty applied for 2 combats.");
            } else {
                System.err.println("Internal error while removing the curse.");
            }
        } else {
            System.out.println("Curse kept in place.");
        }
    }


    /**
     * Executes the removal logic based on the target position, distinguishing between
     * a {@code Curse} (forced destruction/penalty) and a normal {@code Item} (choice to discard/reserve).
     *
     * @param scanner The Scanner object for interaction.
     * @throws NullPointerException if scanner is {@code null}.
     */
    public void processItemRemoval(Scanner scanner) {
        Position targetPos = readItemRemovalCoordinates(scanner);
        if (targetPos == null) {
            return;
        }
        var itemToRemove = this.backPack.getItemAt(targetPos).orElse(null);
        if (itemToRemove != null) {
            if (itemToRemove.getItem() instanceof Curse) {
                handleCurseRemovalChoice(itemToRemove, scanner);
            } else {
                if (this.backPack.removeItem(itemToRemove)) {
                    System.out.println("Item " + itemToRemove.getName() + " removed successfully.");
                } else {
                    System.err.println("Error during item removal.");
                }
            }
        } else {
            System.err.println("No item found at position " + targetPos + ".");
        }
    }
    
    /**
     * Handles user commands ('r', 't', 'q') before proceeding to item placement.
     * Allows rotation of the current item, removal (and potential reservation) of an existing item, or quitting.
     *
     * @param backpack The current backpack.
     * @param currentInstance The {@code ItemInstance} the user is trying to place.
     * @param scanner The Scanner object for reading commands.
     * @return {@code true} to continue (placement attempt or wait for next command), {@code false} to quit the loop.
     * @throws NullPointerException if arguments are {@code null}.
     */
    private boolean handleCommands(BackPack backpack, ItemInstance currentInstance, Scanner scanner) {
    	Objects.requireNonNull(backpack);
    	Objects.requireNonNull(scanner);
    	Objects.requireNonNull(currentInstance);
        while (true) {
            System.out.println("Enter (r) to Remove an item, (t) to Rotate current item, or (q) to Quit.");
            System.out.print("Or press Enter to place the current item: ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("q")) {
                return false;
            } else if (command.equalsIgnoreCase("r")) {
            	processItemRemoval(scanner);
                printBackPack(backpack);
            } else if (command.equalsIgnoreCase("t")) {
            	currentInstance.rotate();
                System.out.println("Item " + currentInstance.getName() + " rotated 90 degrees. New Shape (Angle: " + currentInstance.getRotationAngle() + "°):");
                System.out.println(currentInstance.getCurrentShape());
            } else if (command.isEmpty()) {
                return true;
            } else {
                 System.err.println("Invalid command.");
            }
        }
    }
}
