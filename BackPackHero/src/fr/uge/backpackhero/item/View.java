package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import fr.uge.backpackhero.combat.CombatInteractionDelegate;
import fr.uge.backpackhero.entites.Heros;



/**
 * Classe d'interaction en mode console pour la classe {@code BackPack}.
 */
public class View implements CombatInteractionDelegate {
	private final BackPack backPack;
	private final StuffFactory factory;
	private final Heros heros;
	private final Scanner scanner = new Scanner(System.in);

	public View(BackPack backPack, StuffFactory factory, Heros heros) {
		Objects.requireNonNull(backPack);
		Objects.requireNonNull(factory);
		Objects.requireNonNull(heros);
		this.backPack = backPack;
		this.factory = factory;
		this.heros = heros;
	}
	
	/**
     * Nouvelle méthode pour exécuter un scénario de test:
     * 1. Place un certain nombre d'items aléatoires.
     * 2. Force le placement d'une Malédiction (Curse).
     * @param numItemsToRoll Le nombre d'items aléatoires à générer avant la malédiction.
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
            System.out.println("\n--- Tour " + (i + 1) + "/" + numItemsToRoll + " ---");
            System.out.println("Vous trouvez un nouvel Item: **" + currentInstance.getName() + "** (" + currentInstance.getCurrentShape().size() + " slots)");
            
            // Gère les commandes (rotation, suppression, etc.)
            if (!handleCommands(backPack, currentInstance, this.scanner)) {
                System.out.println("Quittez l'interaction.");
                this.scanner.close();
                return;
            }
            
            // Tente le placement
            if (handlePlacement(backPack, currentInstance, this.scanner)) {
                // Item placé, continue la boucle
            } else {
                System.out.println("\nImpossible de placer l'item, il est perdu. Passons au suivant.");
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
        this.scanner.close();
        System.out.println("Scénario de test terminé.");
    }
    
    @Override
    public void handleForcedCurse(Heros heros, Curse curse) {
            handleForcedCurse(heros, curse, this.scanner);
    }
    
    /**
     * Gère l'interaction lorsque le Héros reçoit une Malédiction forcée (ex: par un ennemi).
     * @param heros Le Héros cible.
     * @param curse La Malédiction à gérer.
     * @param scanner L'objet Scanner existant.
     */
    public void handleForcedCurse(Heros heros, Curse curse, Scanner scanner) {
        Objects.requireNonNull(heros);
        Objects.requireNonNull(curse);
        Objects.requireNonNull(scanner);
        System.out.println("L'ennemi vous lance une Malédiction !");
        System.out.println("Choix : (A)ccepter la Malédiction et la placer dans le sac ou (R)efuser et subir des dégâts ?");
        System.out.print("> ");
        String choix = scanner.nextLine().trim().toUpperCase();
        if (choix.equals("A")) {
            heros.acceptCurseImmediate();
            var instance = new ItemInstance(curse);
            printBackPack(heros.getBackpack());
            System.out.println("\nVous devez immédiatement placer la Malédiction : " + instance.getName());
            var placed = false;
            while(!placed) {
                 Position startPos = readPlacementCoordinates(scanner);
                 if (ItemPlacement(heros.getBackpack(), instance, startPos)) {
                     placed = true;
                     System.out.println("Malédiction placée. Vous continuez le combat.");
                 } else {
                     System.err.println("Placement impossible. Réessayez.");
                 }
            }
        } else {
            heros.refuseCurseImmediate();
        }
    }
	
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
     * Lit et valide la saisie des coordonnées de placement par l'utilisateur
     * @param scanner
     * @return La Position saisie ou null en cas d'erreur irrécupérable
     * @throws NullPointerException si l'argument est {@code null}
     */
    private Position readPlacementCoordinates(Scanner scanner) {
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
     * Gère la lecture finale des coordonnées et la tentative d'ajout de l'item.
     * @param backpack Le sac à dos cible.
     * @param currentInstance L'ItemInstance à placer.
     * @param scanner L'objet Scanner pour la lecture.
     * @return {@code true} si l'item a été ajouté avec succès, {@code false} sinon.
     * @throws NullPointerException si les arguments sont {@code null}
     */
    private boolean handlePlacement(BackPack backpack, ItemInstance currentInstance, Scanner scanner) {
        Position startPos = readPlacementCoordinates(scanner);
        if (startPos == null) return false;
        return ItemPlacement(backpack, currentInstance, startPos);
    }
    
    /**
     * 1. Lit et valide les coordonnées de l'item que l'utilisateur veut retirer.
     * @param scanner L'objet Scanner pour la lecture.
     * @return La Position saisie ou null en cas d'erreur irrécupérable.
     */
    private Position readItemRemovalCoordinates(Scanner scanner) {
        Objects.requireNonNull(scanner);
        System.out.print("Entrez la LIGNE et la COLONNE de l'Item à retirer (ex: 1 3): ");
        
        try {
            String[] parts = scanner.nextLine().trim().split("\\s+");
            if (parts.length < 2) {
                System.err.println("Erreur: Format de coordonnées invalide. Attendu: LIGNE COLONNE.");
                return null;
            }
            var r = Integer.parseInt(parts[0]);
            var c = Integer.parseInt(parts[1]);
            return new Position(r, c);
        } catch (NumberFormatException e) {
            System.err.println("Erreur: La ligne ou la colonne doit être numérique.");
            return null;
        }
    }

    /**
     * 3. Gère l'interaction et les conséquences pour le retrait d'une Malédiction (Scénario 2).
     * @param itemToRemove L'ItemInstance de la Malédiction.
     * @param scanner L'objet Scanner.
     */
    private void handleCurseRemovalChoice(ItemInstance itemToRemove, Scanner scanner) {
        System.out.println("C'est une Malédiction ! Voulez-vous la **retirer** (r) en subissant une pénalité de durée, ou la **garder** (g) ?");
        System.out.print("Choix (r/g): ");
        String curseChoice = scanner.nextLine().trim().toUpperCase();

        if (curseChoice.equals("R")) {
            if (this.backPack.removeItem(itemToRemove)) {
                this.heros.applyCurseRemovalPenalty(); //Application de la pénalité HP Max
                System.out.println("Malédiction retirée. Pénalité de HP Max appliquée pour 2 combats.");
            } else {
                System.err.println("Erreur interne lors du retrait de la malédiction.");
            }
        } else {
            System.out.println("Malédiction conservée.");
        }
    }


    /**
     * 2. Exécute la logique de retrait à partir de la position cible.
     * C'est l'ancienne méthode handleRemoveItem, renommée pour clarifier son rôle.
     * @param scanner L'objet Scanner pour l'interaction.
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
                    System.out.println("Item " + itemToRemove.getName() + " retiré avec succès.");
                } else {
                    System.err.println("Erreur interne lors du retrait de l'item.");
                }
            }
        } else {
            System.err.println("Aucun item trouvé à la position " + targetPos + ".");
        }
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
