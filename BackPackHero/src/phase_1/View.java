package phase_1;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

/**
 * Classe d'interaction en mode console pour la classe {@code BackPack}.
 */
public class View {

	/**
	 * Affiche l'état actuel du sac à dos
	 * @param backpack le sac à dos à afficher
	 * @throws NullPointerException si l'argument est {@code null}
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
     * Modifie le sac à dos en placant un nouvel item
     * @param backpack
     * @param factory
     * @throws NullPointerException si les arguments sont {@code null}
     */
    public static void editBackPack(BackPack backpack, StuffFactory factory) {
    	Objects.requireNonNull(backpack);
    	Objects.requireNonNull(factory);
        var scanner = new Scanner(System.in);
        var stuffs = List.of(Stuff.WoodSword, Stuff.RoughBuckler, Stuff.ElectricWand, Stuff.ManaStone);
        var i = 0;
        System.out.println("\n#################################################");
        System.out.println("                   BACKPACK");
        System.out.println("#################################################");
        while (true) {
        	var baseItem = factory.create(stuffs.get(i % stuffs.size()));
            var currentInstance = new ItemInstance(baseItem);
            printBackPack(backpack);
            System.out.println("\nYou found a new Item: **" + currentInstance.getName() + "** (" + currentInstance.getCurrentShape().size() + " slots)");
            System.out.println("Item Shape (relative positions): " + currentInstance.getCurrentShape());
            if (!handleCommands(backpack, currentInstance, scanner)) {
                break;
            }
            if (handlePlacement(backpack, currentInstance, scanner)) {
                 i++;
            }
        }
        scanner.close();
        System.out.println("Interactive mode finished.");
    }

    /**
     * Lit et valide la saisie des coordonnées de placement par l'utilisateur
     * @param scanner
     * @return La Position saisie ou null en cas d'erreur irrécupérable
     * @throws NullPointerException si l'argument est {@code null}
     */
    private static Position readPlacementCoordinates(Scanner scanner) {
    	Objects.requireNonNull(scanner);
        var row = -1;
        var col = -1;
        var validCoords = false;
        while (!validCoords) {
            try {
                System.out.print("Enter the ROW (0 to 2) to place the top-left corner: ");
                var rowInput = scanner.nextLine().strip();
                row = Integer.parseInt(rowInput);
                System.out.print("Enter the COLUMN (0 to 4) to place the top-left corner: ");
                var colInput = scanner.nextLine().strip();
                col = Integer.parseInt(colInput);
                validCoords = true;
            } catch (NumberFormatException e) {
                System.err.println("Input Error: Please enter valid numbers (0-2 for row, 0-4 for column).");
            }
        }
        return new Position(row, col);
    }

    /**
     * Tente d'ajouter l'Item au sac à dos et affiche le résultat
     * @param backpack
     * @param newItem
     * @param startPos
     * @return true si l'Item a été ajouté, false sinon
     * @throws NullPointerException si les arguments sont {@code null}
     */
    private static boolean ItemPlacement(BackPack backpack, ItemInstance newItem, Position startPos) {
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
     * Gère la lecture finale des coordonnées et la tentative d'ajout de l'item.
     * @param backpack Le sac à dos cible.
     * @param currentInstance L'ItemInstance à placer.
     * @param scanner L'objet Scanner pour la lecture.
     * @return {@code true} si l'item a été ajouté avec succès, {@code false} sinon.
     * @throws NullPointerException si les arguments sont {@code null}
     */
    private static boolean handlePlacement(BackPack backpack, ItemInstance currentInstance, Scanner scanner) {
        Position startPos = readPlacementCoordinates(scanner);
        if (startPos == null) return false;
        return ItemPlacement(backpack, currentInstance, startPos);
    }
    
    /**
     * Gère les commandes utilisateur ('r', 't', 'q') avant de procéder au placement.
     * Permet la rotation de l'item en cours ou le retrait d'un item existant.
     * @param backpack Le sac à dos actuel.
     * @param currentInstance L'ItemInstance que l'utilisateur tente de placer.
     * @param scanner L'objet Scanner pour la lecture des commandes.
     * @return {@code true} pour continuer (placement ou attente), {@code false} pour quitter.
     *  @throws NullPointerException si les arguments sont {@code null}
     */
    private static boolean handleCommands(BackPack backpack, ItemInstance currentInstance, Scanner scanner) {
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
                handleRemoveItem(backpack, scanner);
                printBackPack(backpack);
            } else if (command.equalsIgnoreCase("t")) {
                currentInstance.rotate();
                System.out.println("Item " + currentInstance.getName() + " rotated 90 degrees. New Shape (Angle: " + currentInstance.getRotationAngle() + "°):");
                System.out.println(currentInstance.getCurrentShape());
            } else if (command.isEmpty()) {
                return true;
            } else {
                 System.err.println("❌ Invalid command.");
            }
        }
    }
    
    /**
     * Gère la commande de l'utilisateur pour retirer un item du sac à dos.
     * L'utilisateur doit saisir les coordonnées de l'item à retirer.
     * @param backpack Le sac à dos cible.
     * @param scanner L'objet Scanner pour la lecture des coordonnées.
     * @throws NullPointerException si les arguments sont {@code null}
     */
    private static void handleRemoveItem(BackPack backpack, Scanner scanner) {
    	Objects.requireNonNull(backpack);
    	Objects.requireNonNull(scanner);
        System.out.print("Enter the ROW and COLUMN of the Item to remove (e.g., 1 3): ");
        try {
            String[] parts = scanner.nextLine().trim().split("\\s+");
            if (parts.length < 2) {
                 System.err.println("Error: Invalid coordinate format.");
                 return;
            }
            var r = Integer.parseInt(parts[0]);
            var c = Integer.parseInt(parts[1]);
            Position targetPos = new Position(r, c);
            var itemToRemove = backpack.getItemAt(targetPos).orElse(null);
            if (itemToRemove != null) {
                if (backpack.removeItem(itemToRemove)) {
                    System.out.println("Item " + itemToRemove.getName() + " successfully removed.");
                } else {
                    System.err.println("Internal removal error."); 
                }
            } else {
                System.err.println("No item found at position " + targetPos + ".");
            }

        } catch (NumberFormatException e) {
            System.err.println("Error: Row or column input must be numeric.");
        }
    }

    /**
     * Point d'entrée principal du programme de test.
     * @param args Les arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        var backpack = new BackPack(); 
        var factory = new StuffFactory();
        editBackPack(backpack, factory);
    }
}
