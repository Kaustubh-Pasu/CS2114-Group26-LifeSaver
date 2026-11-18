package project3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests OutputViewer against StubWaitlistManager, feeding it scripted input
 * and checking what it printed and what it sent to the manager.
 *
 * @author Prateek Malekar
 * @version 2026.11.18
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
    void testAddRecipientDuplicateIdAsksOnlyForId() {
        // taken ID is caught right away; the rest is typed only once
        viewer("1", "8", "Jax", "kidney", "A+", "6").promptAddRecipient();
        assertTrue(printed().contains("ID 1 is already taken."));
        assertTrue(printed().contains("Added Jax (ID 8)."));
        assertEquals(8, stub.lastId);
    }


    @Test
    void testAddRecipientUnknownOrganAsksOnlyForOrgan() {
        viewer("7", "Eli", "brain", "liver", "A+", "6").promptAddRecipient();
        assertTrue(printed().contains("Unknown organ: brain."));
        assertEquals("liver", stub.lastOrgan);
        assertEquals("Eli", stub.lastName);
    }


    @Test
    void testAddRecipientBlankNameAsksAgain() {
        viewer("7", "", "Eli", "heart", "B+", "5").promptAddRecipient();
        assertTrue(printed().contains("Name can't be blank."));
        assertEquals("Eli", stub.lastName);
    }


    @Test
    void testAddRecipientBadBloodTypeAsksOnlyForBloodType() {
        viewer("7", "Eli", "heart", "Q+", "AB-", "5").promptAddRecipient();
        assertTrue(printed().contains("Invalid blood type: Q+."));
        assertEquals("AB-", stub.lastBloodType);
        assertEquals("heart", stub.lastOrgan);
    }


    @Test
    void testAddRecipientUrgencyOutOfRangeAsksAgain() {
        viewer("7", "Eli", "heart", "B+", "0", "11", "-3", "10")
            .promptAddRecipient();
        String text = printed();
        assertEquals(3, text.split("Urgency must be from 1 to 10.", -1).length
            - 1);
        assertEquals(10, stub.lastUrgency);
    }


    @Test
    void testAddRecipientUrgencyEdgesAccepted() {
        viewer("7", "Eli", "heart", "B+", "1").promptAddRecipient();
        assertEquals(1, stub.lastUrgency);
        assertFalse(printed().contains("Urgency must be"));
    }


    @Test
    void testAddRecipientUrgencyNotNumberAsksAgain() {
        viewer("7", "Eli", "heart", "B+", "high", "4").promptAddRecipient();
        assertTrue(printed().contains("\"high\" is not a whole number."));
        assertEquals(4, stub.lastUrgency);
    }


    @Test
    void testAddRecipientCancelLaterField() {
        viewer("7", "Eli", "heart", "cancel").promptAddRecipient();
        assertTrue(printed().contains("Cancelled."));
        assertEquals(null, stub.lastCall);
    }


    @Test
    void testAddRecipientNoCallUntilAllFieldsValid() {
        // stop input mid-way through bad blood types: manager never called
        try {
            viewer("7", "Eli", "heart", "Z").promptAddRecipient();
        }
        catch (RuntimeException e) {
            // input ran out
        }
        assertEquals(null, stub.lastCall);
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
    void testDisplayWaitlistsSummaryLine() {
        viewer().displayWaitlists();
        String text = printed();
        assertTrue(text.contains("=== All waitlists (2 waiting) ==="));
        assertTrue(text.contains("heart: 0 | kidney: 2 | liver: 0 | "
            + "lung: 0 | pancreas: 0 | intestine: 0"));
    }


    @Test
    void testDisplayWaitlistsOnlyNonEmptyLists() {
        viewer().displayWaitlists();
        String text = printed();
        assertTrue(text.contains("--- kidney waitlist (2) ---"));
        assertFalse(text.contains("Nobody is waiting for"));
        assertFalse(text.contains("--- heart waitlist"));
    }


    @Test
    void testDisplayWaitlistsAllEmpty() {
        new OutputViewer(new WaitlistManager(), new Scanner(""),
            new PrintStream(output)).displayWaitlists();
        assertTrue(printed().contains("=== All waitlists (0 waiting) ==="));
        assertTrue(printed().contains("Nobody is waiting on any list."));
    }


    @Test
    void testViewOneUnknownOrganAsksAgain() {
        viewer("spleen", "kidney").promptViewWaitlist();
        assertTrue(printed().contains("Unknown organ: spleen"));
        assertTrue(printed().contains("--- kidney waitlist (2) ---"));
    }


    // ----------------------------------------------------------
    // promptEstimateWaitTime / formatDuration
    // ----------------------------------------------------------

    @Test
    void testEstimatePrintsWait() {
        viewer("1").promptEstimateWaitTime();
        assertEquals("estimateWaitTime", stub.lastCall);
        assertTrue(printed().contains(
            "Estimated wait for patient 1: 20 days, 0 hours, 0 minutes"));
    }


    @Test
    void testEstimateNoHistory() {
        viewer("2").promptEstimateWaitTime();
        assertTrue(printed().contains(
            "Not enough history yet to estimate a wait time for patient 2."));
    }


    @Test
    void testEstimateUnknownIdAsksAgain() {
        viewer("99", "1").promptEstimateWaitTime();
        assertTrue(printed().contains("No patient with id 99. Please try "
            + "again."));
        assertTrue(printed().contains("Estimated wait for patient 1:"));
    }


    @Test
    void testEstimateNotNumberAndCancel() {
        viewer("abc", "cancel").promptEstimateWaitTime();
        assertTrue(printed().contains("\"abc\" is not a whole number."));
        assertTrue(printed().contains("Cancelled."));
        assertEquals(null, stub.lastCall);
    }


    @Test
    void testEstimateFromMenu() {
        viewer("6", "1", "0").run();
        assertTrue(printed().contains("6. Estimate wait time"));
        assertTrue(printed().contains("Estimated wait for patient 1: 20 days"));
    }


    @Test
    void testFormatDurationMixed() {
        Duration d = Duration.ofDays(2).plusHours(3).plusMinutes(4);
        assertEquals("2 days, 3 hours, 4 minutes", viewer().formatDuration(d));
    }


    @Test
    void testFormatDurationZero() {
        assertEquals("0 days, 0 hours, 0 minutes",
            viewer().formatDuration(Duration.ZERO));
    }


    @Test
    void testFormatDurationRollsOver() {
        // 25 hours 61 minutes = 1 day, 2 hours, 1 minute
        Duration d = Duration.ofHours(25).plusMinutes(61);
        assertEquals("1 days, 2 hours, 1 minutes",
            viewer().formatDuration(d));
    }


    @Test
    void testFormatDurationLongWait() {
        assertEquals("400 days, 0 hours, 0 minutes",
            viewer().formatDuration(Duration.ofDays(400)));
    }


    @Test
    void testFormatDurationDropsSeconds() {
        assertEquals("0 days, 0 hours, 1 minutes",
            viewer().formatDuration(Duration.ofSeconds(119)));
    }


    // ----------------------------------------------------------
    // promptLookupPatient
    // ----------------------------------------------------------

    @Test
    void testLookupShowsDetailsPositionAndWait() {
        viewer("1").promptLookupPatient();
        String text = printed();
        assertTrue(text.contains("ID 1 | Jax | kidney | A+ | urgency 6"));
        assertTrue(text.contains("Position: 1 of 2 on the kidney waitlist"));
        assertTrue(text.contains(
            "Estimated wait: 20 days, 0 hours, 0 minutes"));
    }


    @Test
    void testLookupNoHistory() {
        viewer("2").promptLookupPatient();
        assertTrue(printed().contains("Position: 2 of 2"));
        assertTrue(printed().contains(
            "Estimated wait: not enough history yet"));
    }


    @Test
    void testLookupUnknownIdAsksAgain() {
        viewer("42", "2").promptLookupPatient();
        assertTrue(printed().contains("No waiting patient has ID 42."));
        assertTrue(printed().contains("ID 2 | Andrew"));
    }


    @Test
    void testLookupCancel() {
        viewer("cancel").promptLookupPatient();
        assertTrue(printed().contains("Cancelled."));
        assertEquals(null, stub.lastCall);
    }


    @Test
    void testLookupFromMenu() {
        assertEquals(OutputViewer.LOOKUP, viewer("7").getMenuChoice());
        assertTrue(printed().contains("7. Look up a patient"));
    }


    @Test
    void testMenuRejectsEight() {
        assertEquals(OutputViewer.EXIT, viewer("8", "0").getMenuChoice());
        assertTrue(printed().contains("Enter a number from 0 to 7."));
    }


    // ----------------------------------------------------------
    // real manager + fake clock (wait time end to end)
    // ----------------------------------------------------------

    @Test
    void testWaitTimeEndToEndWithFakeClock() {
        FakeClock clock = new FakeClock(LocalDateTime.of(2026, 1, 1, 9, 0));
        WaitlistManager real = new WaitlistManager(clock);
        real.addRecipient(1, "Ana", "liver", "O+", 5);
        real.addRecipient(2, "Ben", "liver", "O+", 5);
        real.addRecipient(3, "Cy", "liver", "O+", 5);
        clock.advance(Duration.ofDays(12).plusHours(6)); // no real waiting
        real.dequeueRecipient("liver"); // Ana was at the front 12d 6h

        new OutputViewer(real, new Scanner("3\n"), new PrintStream(output))
            .promptLookupPatient();
        String text = printed();
        assertTrue(text.contains("Position: 2 of 2 on the liver waitlist"));
        // 2nd in line x 12d 6h average = 24 days 12 hours
        assertTrue(text.contains(
            "Estimated wait: 24 days, 12 hours, 0 minutes"));
        assertTrue(text.contains("since 2026-01-01 09:00"));
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
