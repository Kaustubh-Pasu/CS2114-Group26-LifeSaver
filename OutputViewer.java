package project3;
import java.time.Duration;
import java.io.PrintStream;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * OutputViewer is the console view for LifeSaver. It prints the menu, reads
 * what the user types, turns it into the ints and Strings WaitlistManager
 * expects, and prints whatever comes back.
 *
 * When adding a recipient, each field is checked as soon as it is typed
 * (asking the manager whether an ID is taken, an organ exists, or a blood
 * type is valid) so the user only re-enters the field that was wrong. The
 * model still does its own validation; if it rejects something anyway, the
 * view catches the IllegalArgumentException, prints its message, and asks
 * again. The view never touches a WaitlistDeque, a node, or the ID lookup
 * map.
 *
 * After every action that changes state, the view pulls the affected waitlist
 * again with getWaitlist() and redraws it (no observer needed in a
 * single-threaded console app).
 *
 * @author Prateek Malekar
 * @version 2026.11.18
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
    /** Estimate one patient's wait time. */
    public static final int ESTIMATE = 6;
    /** Look up one patient's details. */
    public static final int LOOKUP = 7;
    /** Highest menu option. */
    public static final int MAX_OPTION = LOOKUP;

    /** Lowest allowed urgency. */
    public static final int MIN_URGENCY = 1;
    /** Highest allowed urgency. */
    public static final int MAX_URGENCY = 10;

    /** Remove sub-menu: remove by patient ID. */
    public static final int REMOVE_BY_ID = 1;
    /** Remove sub-menu: dequeue the front of an organ's waitlist. */
    public static final int REMOVE_DEQUEUE = 2;

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
        out.println(ESTIMATE + ". Estimate wait time");
        out.println(LOOKUP + ". Look up a patient");
        out.println(EXIT + ". Exit");
    }


    // ----------------------------------------------------------
    /**
     * Show the menu and read a choice. Text that is not an integer, or a
     * number outside the menu, is rejected and the menu is redrawn.
     *
     * @return a valid menu option (EXIT through MAX_OPTION)
     */
    public int getMenuChoice() {
        while (true) {
            displayMenu();
            String line = readLine("Choose an option: ");
            Integer choice = parseInt(line);
            if (choice != null && choice >= EXIT && choice <= MAX_OPTION) {
                return choice;
            }
            out.println("\"" + line + "\" is not one of the options. "
                + "Enter a number from " + EXIT + " to " + MAX_OPTION + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a new recipient's details one field at a time. Each field is
     * checked right after it is typed, and only that field is asked again if
     * it is wrong. Typing "cancel" at any prompt returns to the menu.
     */
    public void promptAddRecipient() {
        out.println("Add recipient (type \"" + CANCEL
            + "\" at any prompt to go back)");
        while (true) {
            Integer id = promptNewId();
            if (id == null) {
                return;
            }
            String name = promptName();
            if (name == null) {
                return;
            }
            String organ = promptOrgan("Organ needed " + organList() + ": ");
            if (organ == null) {
                return;
            }
            String bloodType = promptBloodType("Blood type (e.g. O+): ");
            if (bloodType == null) {
                return;
            }
            Integer urgency = promptUrgency();
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
                // backstop: the model rejected something the field checks
                // let through
                out.println("Could not add recipient: " + e.getMessage());
                out.println("Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a patient ID that is not already in use.
     *
     * @return the ID, or null if the user cancelled
     */
    public Integer promptNewId() {
        while (true) {
            Integer id = readIntOrCancel("Patient ID: ");
            if (id == null || !manager.isRegistered(id)) {
                return id;
            }
            out.println("ID " + id + " is already taken. Enter a different "
                + "ID.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a name that is not blank.
     *
     * @return the name, or null if the user cancelled
     */
    public String promptName() {
        while (true) {
            String name = readLineOrCancel("Name: ");
            if (name == null || !name.isEmpty()) {
                return name;
            }
            out.println("Name can't be blank.");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for an organ the system knows about.
     *
     * @param prompt
     *            text to show
     * @return the organ as typed, or null if the user cancelled
     */
    public String promptOrgan(String prompt) {
        while (true) {
            String organ = readLineOrCancel(prompt);
            if (organ == null || manager.isKnownOrgan(organ)) {
                return organ;
            }
            out.println("Unknown organ: " + organ + ". Choose one of "
                + organList() + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a valid blood type.
     *
     * @param prompt
     *            text to show
     * @return the blood type as typed, or null if the user cancelled
     */
    public String promptBloodType(String prompt) {
        while (true) {
            String bloodType = readLineOrCancel(prompt);
            if (bloodType == null || manager.isValidBloodType(bloodType)) {
                return bloodType;
            }
            out.println("Invalid blood type: " + bloodType
                + ". Use A, B, AB or O followed by + or - (e.g. AB-).");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for an urgency between MIN_URGENCY and MAX_URGENCY.
     *
     * @return the urgency, or null if the user cancelled
     */
    public Integer promptUrgency() {
        String prompt = "Urgency (" + MIN_URGENCY + "-" + MAX_URGENCY + "): ";
        while (true) {
            Integer urgency = readIntOrCancel(prompt);
            if (urgency == null
                || (urgency >= MIN_URGENCY && urgency <= MAX_URGENCY)) {
                return urgency;
            }
            out.println("Urgency must be from " + MIN_URGENCY + " to "
                + MAX_URGENCY + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask how to remove a patient: by ID (anywhere in a waitlist) or by
     * dequeuing the front of an organ's waitlist.
     */
    public void promptRemoveRecipient() {
        out.println(REMOVE_BY_ID + ". Remove by patient ID");
        out.println(REMOVE_DEQUEUE
            + ". Dequeue next patient from an organ's waitlist");
        while (true) {
            Integer mode = readIntOrCancel("Choose a removal option: ");
            if (mode == null) {
                return;
            }
            if (mode == REMOVE_BY_ID) {
                promptRemoveById();
                return;
            }
            if (mode == REMOVE_DEQUEUE) {
                promptDequeue();
                return;
            }
            out.println("Enter " + REMOVE_BY_ID + " or " + REMOVE_DEQUEUE
                + ".");
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for an organ and dequeue the patient at the front of its waitlist.
     */
    public void promptDequeue() {
        while (true) {
            String organ = readLineOrCancel("Organ " + organList() + ": ");
            if (organ == null) {
                return;
            }
            try {
                Recipient r = manager.dequeueRecipient(organ);
                if (r == null) {
                    out.println("Nobody is waiting for "
                        + organ.trim().toLowerCase() + ".");
                }
                else {
                    out.println("Dequeued: " + formatRecipient(r));
                    displayWaitlist(organ);
                }
                return;
            }
            catch (IllegalArgumentException e) {
                out.println(e.getMessage() + ". Please try again.");
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Ask for a patient ID and remove that patient from their waitlist.
     */
    public void promptRemoveById() {
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
     * Print a one-line count for every organ, then the full list for each
     * organ that has anyone waiting. Empty organs only appear in the count
     * line.
     */
    public void displayWaitlists() {
        int total = 0;
        StringBuilder counts = new StringBuilder();
        for (String organ : ORGANS) {
            int size = manager.getWaitlist(organ).length;
            total += size;
            if (counts.length() > 0) {
                counts.append(" | ");
            }
            counts.append(organ).append(": ").append(size);
        }
        out.println("=== All waitlists (" + total + " waiting) ===");
        out.println(counts);
        if (total == 0) {
            out.println("Nobody is waiting on any list.");
            return;
        }
        for (String organ : ORGANS) {
            if (manager.getWaitlist(organ).length > 0) {
                displayWaitlist(organ);
            }
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
            + r.getOrganNeeded() + " | " + r.getBloodType().toUpperCase() + " | urgency "
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
            case ESTIMATE:
                promptEstimateWaitTime();
                break;
            case LOOKUP:
                promptLookupPatient();
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
     * Ask for a patient ID and print that patient's estimated wait. Unknown
     * IDs are rejected and asked again.
     */
    public void promptEstimateWaitTime() {
        while (true) {
            Integer id = readIntOrCancel("Patient ID: ");
            if (id == null) {
                return;
            }
            try {
                Duration wait = manager.estimateWaitTime(id);
                if (wait == null) {
                    out.println("Not enough history yet to estimate a wait "
                        + "time for patient " + id + ".");
                }
                else {
                    out.println("Estimated wait for patient " + id + ": "
                        + formatDuration(wait));
                }
                return;
            }
            catch (IllegalArgumentException e) {
                out.println(e.getMessage() + ". Please try again.");
            }
        }
    }
    
    // ----------------------------------------------------------
    /**
     * Ask for a patient ID and print their details, their place in line,
     * and their estimated wait. Unknown IDs are rejected and asked again.
     */
    public void promptLookupPatient() {
        while (true) {
            Integer id = readIntOrCancel("Patient ID: ");
            if (id == null) {
                return;
            }
            Recipient r = manager.getRecipient(id);
            if (r == null) {
                out.println("No waiting patient has ID " + id
                    + ". Please try again.");
                continue;
            }
            String organ = r.getOrganNeeded();
            int lineLength = manager.getWaitlist(organ).length;
            out.println(formatRecipient(r));
            out.println("Position: " + manager.getPosition(id) + " of "
                + lineLength + " on the " + organ + " waitlist");
            Duration wait = manager.estimateWaitTime(id);
            if (wait == null) {
                out.println("Estimated wait: not enough history yet");
            }
            else {
                out.println("Estimated wait: " + formatDuration(wait));
            }
            return;
        }
    }


    // ----------------------------------------------------------
    /**
     * Format a duration as days, hours and minutes.
     *
     * @param d
     *            duration to format
     * @return text like "2 days, 3 hours, 4 minutes"
     */
    public String formatDuration(Duration d) {
        return d.toDays() + " days, " + (d.toHours() % 24) + " hours, "
            + (d.toMinutes() % 60) + " minutes";
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
