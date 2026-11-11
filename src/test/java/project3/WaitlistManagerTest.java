package project3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for WaitlistManager
 *
 * @author Kaustubh
 * @version 2026.11.11
 */
class WaitlistManagerTest {
    private static final String[] ORGANS = { "heart", "kidney", "liver",
        "lung", "pancreas", "intestine" };

    private WaitlistManager manager;

    @BeforeEach
    void setUp() {
        manager = new WaitlistManager();
    }


    // ----------------------------------------------------------
    // Constructor
    // ----------------------------------------------------------

    @Test
    void testConstructorAllWaitlistsEmpty() {
        for (String organ : ORGANS) {
            assertEquals(0, manager.getWaitlist(organ).length);
        }
    }


    // ----------------------------------------------------------
    // addRecipient
    // ----------------------------------------------------------

    @Test
    void testAddRecipient() {
        LocalDateTime before = LocalDateTime.now();
        manager.addRecipient(1, "Jax", "kidney", "O+", 6);
        LocalDateTime after = LocalDateTime.now();

        Recipient[] list = manager.getWaitlist("kidney");
        assertEquals(1, list.length);
        Recipient r = list[0];
        assertEquals(1, r.getId());
        assertEquals("Jax", r.getName());
        assertEquals("kidney", r.getOrganNeeded());
        assertEquals("O+", r.getBloodType());
        assertEquals(6, r.getUrgencyLevel());
        assertEquals("waiting", r.getStatus());
        assertNotNull(r.getRegistrationDate());
        assertFalse(r.getRegistrationDate().isBefore(before));
        assertFalse(r.getRegistrationDate().isAfter(after));
    }


    @Test
    void testAddRecipientEveryOrgan() {
        for (int i = 0; i < ORGANS.length; i++) {
            manager.addRecipient(i, "R" + i, ORGANS[i], "A+", 5);
        }
        for (int i = 0; i < ORGANS.length; i++) {
            Recipient[] list = manager.getWaitlist(ORGANS[i]);
            assertEquals(1, list.length);
            assertEquals(i, list[0].getId());
        }
    }


    @Test
    void testAddRecipientOnlyAffectsItsOrgan() {
        manager.addRecipient(1, "Jax", "heart", "A+", 5);
        assertEquals(1, manager.getWaitlist("heart").length);
        assertEquals(0, manager.getWaitlist("kidney").length);
        assertEquals(0, manager.getWaitlist("liver").length);
    }


    @Test
    void testAddRecipientNormalizesOrgan() {
        manager.addRecipient(1, "Jax", "  KiDnEy  ", "A+", 5);
        Recipient[] list = manager.getWaitlist("kidney");
        assertEquals(1, list.length);
        assertEquals("kidney", list[0].getOrganNeeded());
    }


    @Test
    void testAddRecipientDuplicateIdSameOrgan() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Eli", "kidney", "B+", 3));
        assertEquals(1, manager.getWaitlist("kidney").length);
        assertEquals("Jax", manager.getWaitlist("kidney")[0].getName());
    }


    @Test
    void testAddRecipientDuplicateIdDifferentOrgan() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Eli", "heart", "B+", 3));
        assertEquals(0, manager.getWaitlist("heart").length);
    }


    @Test
    void testAddRecipientUnknownOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "brain", "A+", 5));
    }


    @Test
    void testAddRecipientNullOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", null, "A+", 5));
    }


    @Test
    void testAddRecipientEmptyOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "", "A+", 5));
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "   ", "A+", 5));
    }


    @Test
    void testAddRecipientNullBloodType() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "kidney", null, 5));
        assertEquals(0, manager.getWaitlist("kidney").length);
    }


    @Test
    void testAddRecipientInvalidBloodTypes() {
        String[] invalid = { "", " ", "C+", "+", "-", "A++", "AB+-", "OO",
            "BA", "A*" };
        for (String type : invalid) {
            assertThrows(IllegalArgumentException.class,
                () -> manager.addRecipient(1, "Jax", "kidney", type, 5),
                "expected invalid: \"" + type + "\"");
        }
        assertEquals(0, manager.getWaitlist("kidney").length);
    }


    @Test
    void testAddRecipientValidBloodTypes() {
        String[] valid = { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-",
            "A", "B", "AB", "O", "ab+", "o-", " A+ " };
        for (int i = 0; i < valid.length; i++) {
            manager.addRecipient(i, "R" + i, "kidney", valid[i], 5);
        }
        assertEquals(valid.length, manager.getWaitlist("kidney").length);
    }


    @Test
    void testFailedAddDoesNotReserveId() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "kidney", "Z+", 5));
        assertThrows(IllegalArgumentException.class,
            () -> manager.addRecipient(1, "Jax", "brain", "A+", 5));
        // id 1 was never registered, so this must succeed
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertEquals(1, manager.getWaitlist("kidney").length);
    }


    @Test
    void testAddRecipientEdgeIdsAndUrgency() {
        manager.addRecipient(0, "Zero", "lung", "A+", 0);
        manager.addRecipient(-5, "Neg", "lung", "A+", -1);
        manager.addRecipient(Integer.MAX_VALUE, "Max", "lung", "A+", 10);
        manager.addRecipient(Integer.MIN_VALUE, "Min", "lung", "A+", 1);
        Recipient[] list = manager.getWaitlist("lung");
        assertEquals(4, list.length);
        assertEquals(0, list[0].getId());
        assertEquals(-5, list[1].getId());
        assertEquals(Integer.MAX_VALUE, list[2].getId());
        assertEquals(Integer.MIN_VALUE, list[3].getId());
    }


    @Test
    void testAddRecipientNullName() {
        manager.addRecipient(1, null, "kidney", "A+", 5);
        assertNull(manager.getWaitlist("kidney")[0].getName());
    }


    // ----------------------------------------------------------
    // getWaitlist
    // ----------------------------------------------------------

    @Test
    void testGetWaitlistFifoOrder() {
        manager.addRecipient(3, "C", "liver", "A+", 1);
        manager.addRecipient(1, "A", "liver", "B+", 10);
        manager.addRecipient(2, "B", "liver", "O+", 5);
        Recipient[] list = manager.getWaitlist("liver");
        assertEquals(3, list.length);
        assertEquals(3, list[0].getId());
        assertEquals(1, list[1].getId());
        assertEquals(2, list[2].getId());
    }


    @Test
    void testGetWaitlistCaseInsensitive() {
        manager.addRecipient(1, "Jax", "heart", "A+", 5);
        assertEquals(1, manager.getWaitlist("HEART").length);
        assertEquals(1, manager.getWaitlist(" Heart ").length);
    }


    @Test
    void testGetWaitlistUnknownOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.getWaitlist("brain"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.getWaitlist(""));
    }


    @Test
    void testGetWaitlistNullOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.getWaitlist(null));
    }


    @Test
    void testGetWaitlistReturnsCopy() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        Recipient[] list = manager.getWaitlist("kidney");
        list[0] = null;
        assertNotNull(manager.getWaitlist("kidney")[0]);
    }


    // ----------------------------------------------------------
    // removeRecipient
    // ----------------------------------------------------------

    @Test
    void testRemoveRecipient() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        Recipient r = manager.getWaitlist("kidney")[0];
        assertTrue(manager.removeRecipient(1));
        assertEquals(0, manager.getWaitlist("kidney").length);
        assertEquals("removed", r.getStatus());
    }


    @Test
    void testRemoveRecipientUnknownId() {
        assertFalse(manager.removeRecipient(99));
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertFalse(manager.removeRecipient(2));
        assertEquals(1, manager.getWaitlist("kidney").length);
    }


    @Test
    void testDequeueRecipient() {
        addThree("heart");
        Recipient r = manager.dequeueRecipient("Heart ");
        assertEquals(1, r.getId());
        assertEquals("removed", r.getStatus());
        assertIds(manager.getWaitlist("heart"), 2, 3);
        assertFalse(manager.removeRecipient(1)); // gone from lookup too
        manager.addRecipient(1, "A", "heart", "A+", 5); // id is free again
        assertIds(manager.getWaitlist("heart"), 2, 3, 1);
    }


    @Test
    void testDequeueRecipientEmptyAndUnknown() {
        assertNull(manager.dequeueRecipient("kidney"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.dequeueRecipient("spleen"));
    }


    @Test
    void testRemoveRecipientTwice() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertTrue(manager.removeRecipient(1));
        assertFalse(manager.removeRecipient(1));
        assertEquals(0, manager.getWaitlist("kidney").length);
    }


    @Test
    void testRemoveRecipientFront() {
        addThree("heart");
        assertTrue(manager.removeRecipient(1));
        assertIds(manager.getWaitlist("heart"), 2, 3);
    }


    @Test
    void testRemoveRecipientMiddle() {
        addThree("heart");
        assertTrue(manager.removeRecipient(2));
        assertIds(manager.getWaitlist("heart"), 1, 3);
    }


    @Test
    void testRemoveRecipientBack() {
        addThree("heart");
        assertTrue(manager.removeRecipient(3));
        assertIds(manager.getWaitlist("heart"), 1, 2);
    }


    @Test
    void testRemoveAllThenAddAgain() {
        addThree("heart");
        assertTrue(manager.removeRecipient(2));
        assertTrue(manager.removeRecipient(1));
        assertTrue(manager.removeRecipient(3));
        assertEquals(0, manager.getWaitlist("heart").length);

        manager.addRecipient(4, "D", "heart", "A+", 5);
        manager.addRecipient(5, "E", "heart", "A+", 5);
        assertIds(manager.getWaitlist("heart"), 4, 5);
    }


    @Test
    void testRemoveThenAddToBack() {
        addThree("heart");
        assertTrue(manager.removeRecipient(3));
        manager.addRecipient(4, "D", "heart", "A+", 5);
        assertIds(manager.getWaitlist("heart"), 1, 2, 4);
    }


    @Test
    void testRemoveOnlyAffectsItsOrgan() {
        manager.addRecipient(1, "A", "heart", "A+", 5);
        manager.addRecipient(2, "B", "kidney", "A+", 5);
        assertTrue(manager.removeRecipient(1));
        assertEquals(0, manager.getWaitlist("heart").length);
        assertIds(manager.getWaitlist("kidney"), 2);
    }


    @Test
    void testIdReusableAfterRemove() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertTrue(manager.removeRecipient(1));
        manager.addRecipient(1, "Eli", "heart", "B+", 3);
        assertEquals("Eli", manager.getWaitlist("heart")[0].getName());
    }


    // ----------------------------------------------------------
    // matchOrgan
    // ----------------------------------------------------------

    @Test
    void testMatchOrganEmptyWaitlist() {
        assertNull(manager.matchOrgan("kidney", "O-"));
    }


    @Test
    void testMatchOrganBasic() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        Recipient matched = manager.matchOrgan("kidney", "A+");
        assertNotNull(matched);
        assertEquals(1, matched.getId());
        assertEquals("matched", matched.getStatus());
        assertEquals(0, manager.getWaitlist("kidney").length);
    }


    @Test
    void testMatchOrganFifoIgnoresUrgency() {
        manager.addRecipient(1, "Low", "kidney", "A+", 1);
        manager.addRecipient(2, "High", "kidney", "A+", 10);
        assertEquals(1, manager.matchOrgan("kidney", "A+").getId());
        assertEquals(2, manager.matchOrgan("kidney", "A+").getId());
        assertNull(manager.matchOrgan("kidney", "A+"));
    }


    @Test
    void testMatchOrganSkipsIncompatible() {
        manager.addRecipient(1, "B1", "liver", "B+", 5);
        manager.addRecipient(2, "A1", "liver", "A+", 5);
        manager.addRecipient(3, "B2", "liver", "B-", 5);
        Recipient matched = manager.matchOrgan("liver", "A-");
        assertEquals(2, matched.getId());
        // skipped recipients stay in order and keep their status
        Recipient[] list = manager.getWaitlist("liver");
        assertIds(list, 1, 3);
        assertEquals("waiting", list[0].getStatus());
        assertEquals("waiting", list[1].getStatus());
    }


    @Test
    void testMatchOrganNoCompatible() {
        manager.addRecipient(1, "A1", "lung", "A+", 5);
        manager.addRecipient(2, "O1", "lung", "O+", 5);
        assertNull(manager.matchOrgan("lung", "B+"));
        assertIds(manager.getWaitlist("lung"), 1, 2);
    }


    @Test
    void testMatchOrganWrongOrgan() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertNull(manager.matchOrgan("heart", "O-"));
        assertEquals(1, manager.getWaitlist("kidney").length);
    }


    @Test
    void testMatchOrganCompatibilityMatrix() {
        String[] groups = { "O", "A", "B", "AB" };
        // expected[donor][recipient]
        boolean[][] expected = {
            { true, true, true, true }, // O donor -> everyone
            { false, true, false, true }, // A donor -> A, AB
            { false, false, true, true }, // B donor -> B, AB
            { false, false, false, true }, // AB donor -> AB only
        };
        for (int d = 0; d < groups.length; d++) {
            for (int r = 0; r < groups.length; r++) {
                WaitlistManager m = new WaitlistManager();
                m.addRecipient(1, "R", "pancreas", groups[r] + "+", 5);
                Recipient matched = m.matchOrgan("pancreas", groups[d] + "-");
                String msg = groups[d] + " donor -> " + groups[r]
                    + " recipient";
                if (expected[d][r]) {
                    assertNotNull(matched, msg);
                }
                else {
                    assertNull(matched, msg);
                }
            }
        }
    }


    @Test
    void testMatchOrganIgnoresRhFactor() {
        manager.addRecipient(1, "Plus", "kidney", "A+", 5);
        manager.addRecipient(2, "Minus", "kidney", "A-", 5);
        assertEquals(1, manager.matchOrgan("kidney", "A-").getId());
        assertEquals(2, manager.matchOrgan("kidney", "A+").getId());
    }


    @Test
    void testMatchOrganNoRhAndCaseInsensitive() {
        manager.addRecipient(1, "Jax", "intestine", "ab", 5);
        Recipient matched = manager.matchOrgan(" INTESTINE ", "b");
        assertNotNull(matched);
        assertEquals(1, matched.getId());
    }


    @Test
    void testMatchOrganUnknownOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.matchOrgan("brain", "O+"));
    }


    @Test
    void testMatchOrganNullOrgan() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.matchOrgan(null, "O+"));
    }


    @Test
    void testMatchOrganInvalidDonorBloodType() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertThrows(IllegalArgumentException.class,
            () -> manager.matchOrgan("kidney", "X+"));
        assertThrows(IllegalArgumentException.class,
            () -> manager.matchOrgan("kidney", ""));
        assertEquals(1, manager.getWaitlist("kidney").length);
    }


    @Test
    void testMatchOrganNullDonorBloodType() {
        assertThrows(IllegalArgumentException.class,
            () -> manager.matchOrgan("kidney", null));
    }


    @Test
    void testMatchedRecipientCannotBeRemoved() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        Recipient matched = manager.matchOrgan("kidney", "A+");
        assertFalse(manager.removeRecipient(1));
        assertEquals("matched", matched.getStatus());
    }


    @Test
    void testIdReusableAfterMatch() {
        manager.addRecipient(1, "Jax", "kidney", "A+", 5);
        assertNotNull(manager.matchOrgan("kidney", "A+"));
        manager.addRecipient(1, "Eli", "kidney", "B+", 3);
        assertEquals("Eli", manager.getWaitlist("kidney")[0].getName());
    }


    @Test
    void testRemovedRecipientCannotBeMatched() {
        manager.addRecipient(1, "A1", "kidney", "A+", 5);
        manager.addRecipient(2, "A2", "kidney", "A+", 5);
        assertTrue(manager.removeRecipient(1));
        assertEquals(2, manager.matchOrgan("kidney", "A+").getId());
        assertNull(manager.matchOrgan("kidney", "A+"));
    }


    @Test
    void testMatchMiddleThenAddKeepsOrder() {
        manager.addRecipient(1, "B1", "heart", "B+", 5);
        manager.addRecipient(2, "A1", "heart", "A+", 5);
        manager.addRecipient(3, "B2", "heart", "B+", 5);
        assertEquals(2, manager.matchOrgan("heart", "A+").getId());
        manager.addRecipient(4, "A2", "heart", "A+", 5);
        assertIds(manager.getWaitlist("heart"), 1, 3, 4);
        assertSame(manager.getWaitlist("heart")[2],
            manager.matchOrgan("heart", "A+"));
    }


    // ----------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------

    /**
     * Adds recipients with ids 1, 2, 3 (all A+) to the given organ.
     */
    private void addThree(String organ) {
        manager.addRecipient(1, "A", organ, "A+", 5);
        manager.addRecipient(2, "B", organ, "A+", 5);
        manager.addRecipient(3, "C", organ, "A+", 5);
    }


    /**
     * Asserts the waitlist holds exactly the given ids, in order.
     */
    private void assertIds(Recipient[] list, int... ids) {
        assertEquals(ids.length, list.length);
        for (int i = 0; i < ids.length; i++) {
            assertEquals(ids[i], list[i].getId(), "index " + i);
        }
    }
}
