package project3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests OutputViewer against StubWaitlistManager, feeding it scripted input
 * and checking what it printed and what it sent to the manager.
 *
 * @author Prateek Malekar
 * @version 2026.11.11
 */
class OutputViewerTest {
    private StubWaitlistManager stub;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() {
        stub = new StubWaitlistManager();
        output = new ByteArrayOutputStream();
    }


    /** Build a viewer whose input is the given lines. */
    private OutputViewer viewer(String... lines) {
        String input = String.join("\n", lines) + "\n";
        return new OutputViewer(stub, new Scanner(input),
            new PrintStream(output));
    }


    private String printed() {
        return output.toString();
    }


    // ----------------------------------------------------------
    // constructor
    // ----------------------------------------------------------

    @Test
    void testConstructorRejectsNull() {
        assertThrows(IllegalArgumentException.class,
            () -> new OutputViewer(null, new Scanner("")));
    }


    // ----------------------------------------------------------
    // getMenuChoice
    // ----------------------------------------------------------

    @Test
    void testMenuChoiceValid() {
        assertEquals(OutputViewer.VIEW_ALL, viewer("5").getMenuChoice());
    }


    @Test
    void testMenuChoiceRepromptsOnText() {
        assertEquals(OutputViewer.ADD, viewer("abc", "1").getMenuChoice());
        assertTrue(printed().contains("\"abc\" is not one of the options"));
    }


    @Test
    void testMenuChoiceRepromptsOnOutOfRange() {
        assertEquals(OutputViewer.EXIT,
            viewer("-1", "9", "0").getMenuChoice());
        assertTrue(printed().contains("\"-1\" is not one of the options"));
        assertTrue(printed().contains("\"9\" is not one of the options"));
    }


    // ----------------------------------------------------------
    // promptAddRecipient
    // ----------------------------------------------------------

    @Test
    void testAddRecipientPassesValues() {
        viewer("7", "Eli", "heart", "B+", "5").promptAddRecipient();
        assertEquals("addRecipient", stub.lastCall);
        assertEquals(7, stub.lastId);
        assertEquals("Eli", stub.lastName);
        assertEquals("heart", stub.lastOrgan);
        assertEquals("B+", stub.lastBloodType);
        assertEquals(5, stub.lastUrgency);
        assertTrue(printed().contains("Added Eli (ID 7)."));
        // pulls and redraws the affected waitlist
        assertTrue(printed().contains("Nobody is waiting for heart."));
    }


    @Test
    void testAddRecipientRepromptsOnNonNumberId() {
        viewer("seven", "7", "Eli", "heart", "B+", "5").promptAddRecipient();
        assertTrue(printed().contains("\"seven\" is not a whole number."));
        assertEquals(7, stub.lastId);
    }


    @Test
    void testAddRecipientDuplicateIdAsksAgain() {
        viewer("1", "Jax", "kidney", "A+", "6",
            "8", "Jax", "kidney", "A+", "6").promptAddRecipient();
        assertTrue(printed().contains(
            "Could not add recipient: Recipient with id 1 already exists"));
        assertTrue(printed().contains("Added Jax (ID 8)."));
    }


    @Test
    void testAddRecipientUnknownOrganAsksAgain() {
        viewer("7", "Eli", "brain", "A+", "6",
            "7", "Eli", "liver", "A+", "6").promptAddRecipient();
        assertTrue(printed().contains("Unknown organ: brain"));
        assertEquals("liver", stub.lastOrgan);
    }


    @Test
    void testAddRecipientCancel() {
        viewer("7", "cancel").promptAddRecipient();
        assertTrue(printed().contains("Cancelled."));
        assertEquals(null, stub.lastCall);
    }


    // ----------------------------------------------------------
    // promptRemoveRecipient
    // ----------------------------------------------------------

    @Test
    void testRemoveExisting() {
        viewer("1", "2").promptRemoveRecipient();
        assertEquals("removeRecipient", stub.lastCall);
        assertTrue(printed().contains("Removed patient 2."));
    }


    @Test
    void testRemoveUnknownId() {
        viewer("1", "99").promptRemoveRecipient();
        assertTrue(printed().contains("No patient has ID 99."));
    }


    @Test
    void testRemoveRepromptsOnText() {
        viewer("1", "x", "1").promptRemoveRecipient();
        assertTrue(printed().contains("\"x\" is not a whole number."));
        assertEquals(1, stub.lastId);
    }


    @Test
    void testRemoveBadOptionReprompts() {
        viewer("5", "1", "2").promptRemoveRecipient();
        assertTrue(printed().contains("Enter 1 or 2."));
        assertEquals("removeRecipient", stub.lastCall);
    }


    @Test
    void testRemoveCancelAtOption() {
        viewer("cancel").promptRemoveRecipient();
        assertTrue(printed().contains("Cancelled."));
        assertEquals(null, stub.lastCall);
    }


    @Test
    void testDequeueFront() {
        viewer("2", "kidney").promptRemoveRecipient();
        assertEquals("dequeueRecipient", stub.lastCall);
        assertTrue(printed().contains("Dequeued: ID 1 | Jax"));
    }


    @Test
    void testDequeueEmpty() {
        viewer("2", "heart").promptRemoveRecipient();
        assertTrue(printed().contains("Nobody is waiting for heart."));
    }


    @Test
    void testDequeueUnknownOrganReprompts() {
        viewer("2", "spleen", "kidney").promptRemoveRecipient();
        assertTrue(printed().contains("Unknown organ: spleen"));
        assertEquals("kidney", stub.lastOrgan);
    }


    // ----------------------------------------------------------
    // promptMatchOrgan
    // ----------------------------------------------------------

    @Test
    void testMatchFound() {
        viewer("kidney", "O-").promptMatchOrgan();
        assertEquals("O-", stub.lastBloodType);
        assertTrue(printed().contains("Matched: ID 1 | Jax"));
    }


    @Test
    void testMatchNoneCompatible() {
        viewer("liver", "AB+").promptMatchOrgan();
        assertTrue(printed().contains(
            "No compatible recipient is waiting for liver."));
    }


    @Test
    void testMatchUnknownOrganAsksAgain() {
        viewer("spleen", "O+", "kidney", "O+").promptMatchOrgan();
        assertTrue(printed().contains("Could not match organ"));
        assertTrue(printed().contains("Matched: ID 1"));
    }


    // ----------------------------------------------------------
    // displayWaitlist / displayWaitlists
    // ----------------------------------------------------------

    @Test
    void testDisplayWaitlistInOrder() {
        viewer().displayWaitlist("kidney");
        String text = printed();
        assertTrue(text.contains("--- kidney waitlist (2) ---"));
        assertTrue(text.indexOf("1. ID 1 | Jax") < text.indexOf(
            "2. ID 2 | Andrew"));
    }


    @Test
    void testDisplayWaitlistsEveryOrgan() {
        viewer().displayWaitlists();
        String text = printed();
        assertTrue(text.contains("--- kidney waitlist (2) ---"));
        for (String organ : new String[] { "heart", "liver", "lung",
            "pancreas", "intestine" }) {
            assertTrue(text.contains("Nobody is waiting for " + organ + "."));
        }
    }


    @Test
    void testViewOneUnknownOrganAsksAgain() {
        viewer("spleen", "kidney").promptViewWaitlist();
        assertTrue(printed().contains("Unknown organ: spleen"));
        assertTrue(printed().contains("--- kidney waitlist (2) ---"));
    }


    // ----------------------------------------------------------
    // run
    // ----------------------------------------------------------

    @Test
    void testRunExits() {
        viewer("5", "0").run();
        assertTrue(printed().contains("Goodbye."));
    }


    @Test
    void testRunStopsWhenInputEnds() {
        viewer("4").run(); // input ends mid-prompt; must not crash
        assertTrue(printed().contains("Goodbye."));
        assertFalse(printed().contains("Exception"));
    }


    @Test
    void testRunWithRealManager() {
        String input = String.join("\n", "1", "10", "Kai", "lung", "A+",
            "3", "3", "lung", "O-", "0") + "\n";
        new OutputViewer(new WaitlistManager(), new Scanner(input),
            new PrintStream(output)).run();
        assertTrue(printed().contains("Added Kai (ID 10)."));
        assertTrue(printed().contains("Matched: ID 10 | Kai"));
        assertTrue(printed().contains("Nobody is waiting for lung."));
    }
}
