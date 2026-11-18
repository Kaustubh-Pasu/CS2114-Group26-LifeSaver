package project3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the wait-time feature and the lookup / validation helpers the view
 * uses. Uses a FakeClock so "days" pass instantly: the test calls
 * clock.advance(...) instead of actually waiting.
 *
 * How the estimate works: every time someone leaves the FRONT of an organ's
 * line (dequeued or matched), the manager records how long they were at the
 * front. The estimate for a patient is (average time at front) x (their
 * position in line).
 *
 * @author Prateek Malekar
 * @version 2026.11.18
 */
class WaitTimeTest {
    private static final LocalDateTime START =
        LocalDateTime.of(2026, 1, 1, 9, 0);

    private FakeClock clock;
    private WaitlistManager manager;

    @BeforeEach
    void setUp() {
        clock = new FakeClock(START);
        manager = new WaitlistManager(clock);
    }


    /** Add three kidney patients (IDs 1, 2, 3) at the current fake time. */
    private void addThreeKidney() {
        manager.addRecipient(1, "Ana", "kidney", "O+", 5);
        manager.addRecipient(2, "Ben", "kidney", "O+", 5);
        manager.addRecipient(3, "Cy", "kidney", "O+", 5);
    }


    // ----------------------------------------------------------
    // the fake clock itself
    // ----------------------------------------------------------

    @Test
    void testFakeClockAdvances() {
        assertEquals(START, clock.currentTime());
        clock.advance(Duration.ofDays(30));
        assertEquals(START.plusDays(30), clock.currentTime());
    }


    @Test
    void testRegistrationDateUsesClock() {
        clock.advance(Duration.ofDays(3));
        manager.addRecipient(1, "Ana", "kidney", "O+", 5);
        assertEquals(START.plusDays(3),
            manager.getRecipient(1).getRegistrationDate());
    }


    @Test
    void testNullClockRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> new WaitlistManager(null));
        assertThrows(IllegalArgumentException.class,
            () -> new WaitlistDeque(null));
    }


    // ----------------------------------------------------------
    // frontSince bookkeeping
    // ----------------------------------------------------------

    @Test
    void testFirstPatientMarkedAtFrontWhenAdded() {
        addThreeKidney();
        assertEquals(START, manager.getRecipient(1).getFrontSince());
        assertNull(manager.getRecipient(2).getFrontSince());
    }


    @Test
    void testNextPatientMarkedWhenTheyReachFront() {
        addThreeKidney();
        clock.advance(Duration.ofDays(5));
        manager.dequeueRecipient("kidney");
        assertEquals(START.plusDays(5), manager.getRecipient(2)
            .getFrontSince());
    }


    // ----------------------------------------------------------
    // estimateWaitTime
    // ----------------------------------------------------------

    @Test
    void testNoHistoryReturnsNull() {
        addThreeKidney();
        clock.advance(Duration.ofDays(100));
        assertNull(manager.estimateWaitTime(2));
    }


    @Test
    void testUnknownIdThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.estimateWaitTime(99));
    }


    @Test
    void testEstimateAfterTenDays() {
        addThreeKidney();
        clock.advance(Duration.ofDays(10));
        manager.dequeueRecipient("kidney"); // Ana: 10 days at the front

        assertEquals(Duration.ofDays(10), manager.estimateWaitTime(2));
        assertEquals(Duration.ofDays(20), manager.estimateWaitTime(3));
    }


    @Test
    void testEstimateAveragesSeveralPatients() {
        addThreeKidney();
        manager.addRecipient(4, "Dee", "kidney", "O+", 5);
        clock.advance(Duration.ofDays(10));
        manager.dequeueRecipient("kidney"); // Ana: 10 days
        clock.advance(Duration.ofDays(4));
        manager.dequeueRecipient("kidney"); // Ben: 4 days

        // average 7 days; Cy is 1st, Dee is 2nd
        assertEquals(Duration.ofDays(7), manager.estimateWaitTime(3));
        assertEquals(Duration.ofDays(14), manager.estimateWaitTime(4));
    }


    @Test
    void testEstimateWithHoursAndMinutes() {
        addThreeKidney();
        clock.advance(Duration.ofHours(36).plusMinutes(30));
        manager.dequeueRecipient("kidney");
        assertEquals(Duration.ofHours(36).plusMinutes(30),
            manager.estimateWaitTime(2));
    }


    @Test
    void testVeryLongWait() {
        addThreeKidney();
        clock.advance(Duration.ofDays(400));
        manager.dequeueRecipient("kidney");
        assertEquals(Duration.ofDays(800), manager.estimateWaitTime(3));
    }


    @Test
    void testMatchCountsTowardHistory() {
        addThreeKidney();
        clock.advance(Duration.ofDays(6));
        Recipient matched = manager.matchOrgan("kidney", "O-");
        assertEquals(1, matched.getId());
        assertEquals(Duration.ofDays(6), manager.estimateWaitTime(2));
    }


    @Test
    void testMatchSkippingFrontDoesNotCount() {
        // Ana (A+) is at the front; a B donor skips her and matches Ben
        manager.addRecipient(1, "Ana", "kidney", "A+", 5);
        manager.addRecipient(2, "Ben", "kidney", "B+", 5);
        manager.addRecipient(3, "Cy", "kidney", "A+", 5);
        clock.advance(Duration.ofDays(8));
        assertEquals(2, manager.matchOrgan("kidney", "B-").getId());
        // Ben was never at the front, so there is still no history
        assertNull(manager.estimateWaitTime(3));
    }


    @Test
    void testRemoveByIdDoesNotCount() {
        addThreeKidney();
        clock.advance(Duration.ofDays(9));
        assertTrue(manager.removeRecipient(1));
        assertNull(manager.estimateWaitTime(2));
    }


    @Test
    void testHistoryIsPerOrgan() {
        addThreeKidney();
        manager.addRecipient(10, "Liv", "liver", "O+", 5);
        clock.advance(Duration.ofDays(10));
        manager.dequeueRecipient("kidney");
        assertEquals(Duration.ofDays(10), manager.estimateWaitTime(2));
        assertNull(manager.estimateWaitTime(10));
    }


    @Test
    void testEstimateShrinksAsLineMoves() {
        addThreeKidney();
        clock.advance(Duration.ofDays(10));
        manager.dequeueRecipient("kidney"); // Ana: 10 days
        assertEquals(Duration.ofDays(20), manager.estimateWaitTime(3));
        clock.advance(Duration.ofDays(10));
        manager.dequeueRecipient("kidney"); // Ben: 10 days
        assertEquals(Duration.ofDays(10), manager.estimateWaitTime(3));
    }


    // ----------------------------------------------------------
    // getRecipient / getPosition / isRegistered
    // ----------------------------------------------------------

    @Test
    void testGetRecipient() {
        addThreeKidney();
        assertEquals("Ben", manager.getRecipient(2).getName());
        assertNull(manager.getRecipient(99));
    }


    @Test
    void testGetRecipientGoneAfterDequeue() {
        addThreeKidney();
        Recipient ana = manager.getRecipient(1);
        assertSame(ana, manager.dequeueRecipient("kidney"));
        assertNull(manager.getRecipient(1));
    }


    @Test
    void testGetPosition() {
        addThreeKidney();
        assertEquals(1, manager.getPosition(1));
        assertEquals(3, manager.getPosition(3));
        assertTrue(manager.removeRecipient(2));
        assertEquals(2, manager.getPosition(3));
        assertEquals(-1, manager.getPosition(2));
    }


    @Test
    void testIsRegistered() {
        assertFalse(manager.isRegistered(1));
        addThreeKidney();
        assertTrue(manager.isRegistered(1));
        manager.removeRecipient(1);
        assertFalse(manager.isRegistered(1));
    }


    // ----------------------------------------------------------
    // isKnownOrgan / isValidBloodType
    // ----------------------------------------------------------

    @Test
    void testIsKnownOrgan() {
        assertTrue(manager.isKnownOrgan("kidney"));
        assertTrue(manager.isKnownOrgan("  Heart "));
        assertFalse(manager.isKnownOrgan("brain"));
        assertFalse(manager.isKnownOrgan(""));
        assertFalse(manager.isKnownOrgan(null));
    }


    @Test
    void testIsValidBloodType() {
        assertTrue(manager.isValidBloodType("O+"));
        assertTrue(manager.isValidBloodType("ab-"));
        assertFalse(manager.isValidBloodType("Q+"));
        assertFalse(manager.isValidBloodType(""));
        assertFalse(manager.isValidBloodType(null));
    }
}
