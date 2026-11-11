package project3;

import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * OutputViewer is the console view for LifeSaver. It prints the menu, reads
 * what the user types, turns it into the ints and Strings WaitlistManager
 * expects, and prints whatever comes back.
 *
 * The view only checks that typed text can be read as the right type. All
 * other validation (duplicate IDs, unknown organs, bad blood types) lives in
 * the model; the view catches the IllegalArgumentException, prints its
 * message, and asks again. The view never touches a WaitlistDeque, a node, or
 * the ID lookup map.
 *
 * After every action that changes state, the view pulls the affected waitlist
 * again with getWaitlist() and redraws it (no observer needed in a
 * single-threaded console app).
 *
 * @author Prateek Malekar
 * @version 2026.11.11
 */
public class OutputViewer {
    /** Menu option numbers. */
    public static final int EXIT = 0;
    /** Add a recipient. */
    public static final int ADD = 1;
    /** Remove a recipient. */
    public static final int REMOVE = 2;
    /** Match a donor organ. */
    public static final int MATCH = 3;
    /** View one organ's waitlist. */
    public static final int VIEW_ONE = 4;
    /** View every waitlist. */
    public static final int VIEW_ALL = 5;

    /** Organ types shown by "view all"; must match WaitlistManager. */
    private static final String[] ORGANS = { "heart", "kidney", "liver",
        "lung", "pancreas", "intestine" };

    /** Typing this at any prompt returns to the menu. */
    private static final String CANCEL = "cancel";

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private WaitlistManager manager;
    private Scanner scanner;
    private PrintStream out;

    // ----------------------------------------------------------
    /**
     * Create a viewer that prints to System.out.
     *
     * @param manager
     *            the controller to send requests to
     * @param scanner
     *            where user input is read from
     */
    public OutputViewer(WaitlistManager manager, Scanner scanner) {
        this(manager, scanner, System.out);
    }


    // ----------------------------------------------------------
    /**
     * Create a viewer with a chosen output stream (used by the tests).
     *
     * @param manager
     *            the controller to send requests to
     * @param scanner
     *            where user input is read from
     * @param out
     *            where output is printed
     */
    public OutputViewer(
        WaitlistManager manager,
        Scanner scanner,
        PrintStream out) {
        if (manager == null || scanner == null || out == null) {
            throw new IllegalArgumentException(
                "manager, scanner and out are required");
        }
        this.manager = manager;
        this.scanner = scanner;
        this.out = out;
    }


    // ----------------------------------------------------------
    /**
     * Main loop: show the menu, run the chosen action, repeat until the user
     * exits or input runs out.
     */
    public void run() {
        out.println("=== LifeSaver Organ Waitlist ===");
        try {
            int choice = getMenuChoice();
            while (choice != EXIT) {
                handleChoice(choice);
                choice = getMenuChoice();
            }
        }
        catch (InputClosedException e) {
            // input ran out (e.g. end of a test script); just stop
        }
        out.println("Goodbye.");
    }


    // ----------------------------------------------------------
    /**
     * Print the main menu.
     */
    public void displayMenu() {
        out.println();
        out.println(ADD + ". Add recipient");
        out.println(REMOVE + ". Remove recipient");
        out.println(MATCH + ". Match donor organ");
        out.println(VIEW_ONE + ". View one organ's waitlist");
        out.println(VIEW_ALL + ". View all waitlists");
        out.println(EXIT + ". Exit");
    }


    // ----------------------------------------------------------
    /**
     * Show the menu and read a choice. Text that is not an integer, or a
     * number outside the menu, is rejected and the menu is redrawn.
     *
     * @return a valid menu option (EXIT through VIEW_ALL)
     */
    public int getMenuChoice() {
        while (true) {
            displayMenu();
            String line = readLine("Choose an option: ");
            Integer choice = parseInt(line);
            if (choice != null && choice >= EXIT && choice <= VIEW_ALL) {
                return choice;
            }
            out.println("\"" + line + "\" is not one of the options. "
                + "Enter a number from " + EXIT + " to " + VIEW_ALL + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a new recipient's details and pass them to the manager. If the
     * model rejects them, print why and ask again. Typing "cancel" returns to
     * the menu.
     */
    public void promptAddRecipient() {
        out.println("Add recipient (type \"" + CANCEL
            + "\" at any prompt to go back)");
        while (true) {
            Integer id = readIntOrCancel("Patient ID: ");
            if (id == null) {
                return;
            }
            String name = readLineOrCancel("Name: ");
            if (name == null) {
                return;
            }
            String organ = readLineOrCancel(
                "Organ needed " + organList() + ": ");
            if (organ == null) {
                return;
            }
            String bloodType = readLineOrCancel("Blood type (e.g. O+): ");
            if (bloodType == null) {
                return;
            }
            Integer urgency = readIntOrCancel("Urgency (1-10): ");
            if (urgency == null) {
                return;
            }
            try {
                manager.addRecipient(id, name, organ, bloodType, urgency);
                out.println("Added " + name + " (ID " + id + ").");
                displayWaitlist(organ);
                return;
            }
            catch (IllegalArgumentException e) {
                out.println("Could not add recipient: " + e.getMessage());
                out.println("Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a patient ID and remove that patient from their waitlist.
     */
    public void promptRemoveRecipient() {
        Integer id = readIntOrCancel("ID of patient to remove: ");
        if (id == null) {
            return;
        }
        if (manager.removeRecipient(id)) {
            out.println("Removed patient " + id + ".");
        }
        else {
            out.println("No patient has ID " + id + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a donor organ and blood type, then print who was matched.
     */
    public void promptMatchOrgan() {
        while (true) {
            String organ = readLineOrCancel(
                "Donor organ " + organList() + ": ");
            if (organ == null) {
                return;
            }
            String bloodType = readLineOrCancel("Donor blood type: ");
            if (bloodType == null) {
                return;
            }
            try {
                Recipient match = manager.matchOrgan(organ, bloodType);
                if (match == null) {
                    out.println("No compatible recipient is waiting for "
                        + organ.trim() + ".");
                }
                else {
                    out.println("Matched: " + formatRecipient(match));
                }
                displayWaitlist(organ);
                return;
            }
            catch (IllegalArgumentException e) {
                out.println("Could not match organ: " + e.getMessage());
                out.println("Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask which organ to show and print its waitlist.
     */
    public void promptViewWaitlist() {
        while (true) {
            String organ = readLineOrCancel("Organ " + organList() + ": ");
            if (organ == null) {
                return;
            }
            try {
                displayWaitlist(organ);
                return;
            }
            catch (IllegalArgumentException e) {
                out.println(e.getMessage() + ". Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Print every organ's waitlist.
     */
    public void displayWaitlists() {
        for (String organ : ORGANS) {
            displayWaitlist(organ);
        }
    }


    // ----------------------------------------------------------
    /**
     * Pull one organ's waitlist from the manager and print it in order.
     *
     * @param organ
     *            organ to show
     * @throws IllegalArgumentException
     *             if the manager does not know the organ
     */
    public void displayWaitlist(String organ) {
        Recipient[] list = manager.getWaitlist(organ);
        String label = organ.trim().toLowerCase();
        if (list.length == 0) {
            out.println("Nobody is waiting for " + label + ".");
            return;
        }
        out.println("--- " + label + " waitlist (" + list.length + ") ---");
        for (int i = 0; i < list.length; i++) {
            out.println((i + 1) + ". " + formatRecipient(list[i]));
        }
    }


    // ----------------------------------------------------------
    /**
     * One-line description of a recipient.
     *
     * @param r
     *            recipient to describe
     * @return formatted text
     */
    public String formatRecipient(Recipient r) {
        String date = r.getRegistrationDate() == null
            ? "unknown"
            : r.getRegistrationDate().format(DATE_FORMAT);
        return "ID " + r.getId() + " | " + r.getName() + " | "
            + r.getOrganNeeded() + " | " + r.getBloodType() + " | urgency "
            + r.getUrgencyLevel() + " | " + r.getStatus() + " | since "
            + date;
    }


    // ~ Private helpers .....................................................

    private void handleChoice(int choice) {
        switch (choice) {
            case ADD:
                promptAddRecipient();
                break;
            case REMOVE:
                promptRemoveRecipient();
                break;
            case MATCH:
                promptMatchOrgan();
                break;
            case VIEW_ONE:
                promptViewWaitlist();
                break;
            case VIEW_ALL:
                displayWaitlists();
                break;
            default:
                break;
        }
    }


    /**
     * Print a prompt and read one line.
     *
     * @throws InputClosedException
     *             if there is no more input
     */
    private String readLine(String prompt) {
        out.print(prompt);
        if (!scanner.hasNextLine()) {
            out.println();
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }


    /** Read a line; null means the user typed "cancel". */
    private String readLineOrCancel(String prompt) {
        String line = readLine(prompt);
        if (line.equalsIgnoreCase(CANCEL)) {
            out.println("Cancelled.");
            return null;
        }
        return line;
    }


    /**
     * Read an integer, re-prompting until one is typed. Returns null if the
     * user typed "cancel".
     */
    private Integer readIntOrCancel(String prompt) {
        while (true) {
            String line = readLineOrCancel(prompt);
            if (line == null) {
                return null;
            }
            Integer value = parseInt(line);
            if (value != null) {
                return value;
            }
            out.println("\"" + line + "\" is not a whole number.");
        }
    }


    /** Parse an int, or return null if the text is not one. */
    private static Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        }
        catch (NumberFormatException e) {
            return null;
        }
    }


    private static String organList() {
        return "(" + String.join(", ", ORGANS) + ")";
    }


    /** Thrown internally when the input stream runs out. */
    private static class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }


    // ----------------------------------------------------------
    /**
     * Start the console app. Run with the argument "stub" to use the stubbed
     * manager (fixed fake data) instead of the real one.
     *
     * @param args
     *            optional "stub"
     */
    public static void main(String[] args) {
        WaitlistManager manager;
        if (args.length > 0 && args[0].equalsIgnoreCase("stub")) {
            manager = new StubWaitlistManager();
            System.out.println("(running with StubWaitlistManager)");
        }
        else {
            manager = new WaitlistManager();
        }
        new OutputViewer(manager, new Scanner(System.in)).run();
    }
}
