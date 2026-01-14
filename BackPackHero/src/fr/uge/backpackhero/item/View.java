package fr.uge.backpackhero.item;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import fr.uge.backpackhero.combat.CombatInteractionDelegate;
import fr.uge.backpackhero.entites.Heros;

/**
 * Console-based interaction class responsible for handling all user input and
 * displaying the state of the {@code BackPack} and combat choices.
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
   * @param factory  The item factory for generating new items.
   * @param heros    The hero entity.
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
   * Displays the backpack by calculating the bounding box of all unlocked tiles.
   */
  public void printBackPack() {
    var unlocked = backPack.getUnlockedTiles();
    if (unlocked.isEmpty()) {
      System.out.println("The backpack is completely empty and locked!");
      return;
    }

    // 1. Calculate boundaries (Min/Max for Rows and Columns)
    int minR = unlocked.stream().mapToInt(Position::row).min().getAsInt();
    int maxR = unlocked.stream().mapToInt(Position::row).max().getAsInt();
    int minC = unlocked.stream().mapToInt(Position::column).min().getAsInt();
    int maxC = unlocked.stream().mapToInt(Position::column).max().getAsInt();

    System.out.println("\n--- Current Backpack State ---");

    // 2. Print Column Headers
    System.out.print("     "); // Offset for row numbers
    for (int c = minC; c <= maxC; c++) {
      System.out.printf("%-3d", c);
    }
    System.out.println("\n     " + "---".repeat(maxC - minC + 1));

    // 3. Print Rows
    for (int r = minR; r <= maxR; r++) {
      System.out.printf("%3d |", r); // Row number
      for (int c = minC; c <= maxC; c++) {
        Position pos = new Position(r, c);

        if (!unlocked.contains(pos)) {
          // Case 1: Tile is not unlocked (void outside the backpack shape)
          System.out.print("   ");
        } else {
          var itemOpt = backPack.getItemAt(pos);
          if (itemOpt.isPresent()) {
            // Case 2: Tile is unlocked and occupied
            System.out.printf(" %-2s", itemOpt.get().toString());
          } else {
            // Case 3: Tile is unlocked but empty
            System.out.print(" . ");
          }
        }
      }
      System.out.println("|");
    }
    System.out.println("     " + "---".repeat(maxC - minC + 1));
  }

  /**
   * Executes a demo scenario for item placement and forced curse handling. 1.
   * Places a specified number of random items interactively. 2. Forces the
   * placement of a Curse item.
   *
   * @param numItemsToRoll The number of random items to generate before the
   *                       curse.
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
   * Displays information about a newly found item, including its name, occupied
   * slots, and current shape.
   *
   * @param item the {@code ItemInstance} that has been found
   * @throws NullPointerException if {@code item} is {@code null}
   */
  /*
   * public void displayItemFound(ItemInstance item) {
   * System.out.println("You found: " + item.getName() + " (" +
   * item.getCurrentShape().size() + " slots)"); System.out.println("Shape: " +
   * item.getCurrentShape()); }
   */

  public void displayItemFound(ItemInstance instance) {
    System.out.println("\n--- VOTRE SAC ACTUEL ---");
    printBackPack();
    System.out.println("\nOBJET TROUVÉ : " + instance.getName());
    System.out.println("Taille : " + instance.getCurrentShape().size() + " cases");
    System.out.println("Forme : " + instance.getCurrentShape());
  }

  /**
   * Handles the interaction loop before placing an item. Allows the user to
   * rotate the item, remove an existing item, quit the interaction, or proceed to
   * placement.
   *
   * @param item the {@code ItemInstance} the user is about to place
   * @return {@code true} if the user proceeds to placement, {@code false} if the
   *         interaction is cancelled
   * @throws NullPointerException if {@code item} is {@code null}
   */
  public boolean interactBeforePlacement(ItemInstance item) {
    while (true) {
      System.out.println("[r] Remove, [t] Rotate, [q] Quit, Enter = Place");
      String command = readLine();

      switch (command.toLowerCase()) {
      case "q":
        return false;
      case "r":
        processItemRemoval();
        printBackPack();
        continue;
      case "t":
        rotateItem(item);
        continue;
      case "":
        return true;
      default:
        System.err.println("Invalid command.");
        continue;
      }
    }
  }

  /**
   * Rotates the given item by 90 degrees clockwise and displays its new rotation
   * angle and shape.
   *
   * @param item the {@code ItemInstance} to rotate
   * @throws NullPointerException  if {@code item} is {@code null}
   * @throws IllegalStateException if the item cannot be rotated
   */
  public void rotateItem(ItemInstance item) {
    item.rotate();
    System.out.println("Rotated to " + item.getRotationAngle() + "°");
    System.out.println(item.getCurrentShape());
  }

  /**
   * Prompts the user for placement coordinates and attempts to place the given
   * item into the backpack.
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
   * Handles the removal of an item from the backpack. If the selected item is a
   * {@code Curse}, a confirmation and penalty choice is required.
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
   * Asks the user whether to remove or keep a {@code Curse} item. Removing the
   * curse applies a penalty to the hero.
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
   * Forces the user to place a curse item into the backpack. The method loops
   * until a valid placement is performed.
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
   * Implementation of the {@code CombatInteractionDelegate} interface. Handles
   * the forced {@code Curse} interaction initiated by an enemy during combat.
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
   * Reads and validates a position entered by the user. The input must consist of
   * two integers corresponding to a valid row and column inside the backpack.
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
   * Handles the choice of new tiles to unlock when the hero levels up. * @param
   * levelsGained The amount of levels the user has gained.
   */
  public void handleLevelUpExpansion(int levelsGained) {
    int tilesPerLevel = 3;
    int totalTilesToUnlock = levelsGained * tilesPerLevel;

    System.out.println("\nLEVEL UP! You earned " + totalTilesToUnlock + " new backpack tiles!");

    for (int i = 0; i < totalTilesToUnlock; i++) {
      System.out.println("\nSelect tile " + (i + 1) + "/" + totalTilesToUnlock + " to unlock.");
      printBackPack(); // Affiche le sac avec les '#' pour les cases bloquées
      Position choice = readPosition();

      if (backPack.unlockTile(choice)) {
        System.out.println("Tile " + choice + " is now available!");
      } else {
        System.err.println("Invalid choice (already unlocked or out of bounds). Try again.");
        i--; // On redemande pour cette case
      }
    }
  }
  
  public void reorganize() {
    List<ItemInstance> items = backPack.removeAllItems(); // Utilise la méthode evacuate() vue précédemment
    while (!items.isEmpty()) {
        ItemInstance current = items.get(0);
        displayItemFound(current);
        System.out.println("Commandes : [r] Rotation, [ligne colonne] Position");
        String input = readLine();

        if ("r".equalsIgnoreCase(input)) {
            current.rotate();
        } else {
            String[] p = input.split("\\s+");
            if (p.length == 2 && isInteger(p[0].replace("-", "")) && isInteger(p[1].replace("-", ""))) {
                Position pos = new Position(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
                if (isInside(pos.row(), pos.column()) && backPack.add(current, pos)) {
                    items.remove(0);
                } else System.err.println("❌ Position invalide ou occupée.");
            } else System.err.println("❌ Commande invalide.");
        }
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
    return row >= -10 && row <= 20 && col >= -10 && col <= 20;
  }

  /**
   * Checks whether the given string represents a positive or a negative number.
   *
   * @param s the string to test
   * @return {@code true} if the string contains only digits, {@code false}
   *         otherwise
   */
  private boolean isInteger(String s) {
    return s.matches("-?\\d+"); // Le -? permet de détecter un éventuel signe moins
}
}
