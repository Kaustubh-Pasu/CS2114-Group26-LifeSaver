package project3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for Recipient
 *
 * @author Elijah Jax O'Donoghue
 * @version 2026.11.11
 */
class RecipientTest {
    private Recipient recipient;
    private LocalDateTime date;

    @BeforeEach
    void setUp() {
        date = LocalDateTime.of(2026, 11, 9, 10, 30);
        recipient = new Recipient(1, "Jax", "Kidney", "O+", date, 6, "Waiting");
    }


    @Test
    void testConstructor() {
        assertEquals(1, recipient.getId());
        assertEquals("Jax", recipient.getName());
        assertEquals("Kidney", recipient.getOrganNeeded());
        assertEquals(date, recipient.getRegistrationDate());
        assertEquals(6, recipient.getUrgencyLevel());
        assertEquals("Waiting", recipient.getStatus());
    }


    @Test
    void testId() {
        assertEquals(1, recipient.getId());
        recipient.setId(67);
        assertEquals(67, recipient.getId());
    }


    @Test
    void testName() {
        assertEquals("Jax", recipient.getName());
        recipient.setName("Elijah");
        assertEquals("Elijah", recipient.getName());
    }


    @Test
    void testOrganNeeded() {
        assertEquals("Kidney", recipient.getOrganNeeded());
        recipient.setOrganNeeded("Liver");
        assertEquals("Liver", recipient.getOrganNeeded());
    }


    @Test
    void testRegistrationDate() {
        assertEquals(date, recipient.getRegistrationDate());
        LocalDateTime newDate = LocalDateTime.of(2026, 1, 15, 8, 0);
        recipient.setRegistrationDate(newDate);
        assertEquals(newDate, recipient.getRegistrationDate());
        assertFalse(date.equals(recipient.getRegistrationDate()));
    }


    @Test
    void testUrgencyLevel() {
        assertEquals(6, recipient.getUrgencyLevel());
        recipient.setUrgencyLevel(10);
        assertEquals(10, recipient.getUrgencyLevel());
    }


    @Test
    void testStatus() {
        assertEquals("Waiting", recipient.getStatus());
        recipient.setStatus("Matched");
        assertEquals("Matched", recipient.getStatus());
    }


    @Test
    void testNullFields() {
        recipient.setName(null);
        assertNull(recipient.getName());

        recipient.setOrganNeeded(null);
        assertNull(recipient.getOrganNeeded());

        recipient.setRegistrationDate(null);
        assertNull(recipient.getRegistrationDate());

        recipient.setStatus(null);
        assertNull(recipient.getStatus());
    }


    @Test
    void testConstructorWithNulls() {
        Recipient empty = new Recipient(0, null, null, null, null, 0, null);
        assertEquals(0, empty.getId());
        assertNull(empty.getName());
        assertNull(empty.getOrganNeeded());
        assertNull(empty.getRegistrationDate());
        assertEquals(0, empty.getUrgencyLevel());
        assertNull(empty.getStatus());
    }


    @Test
    void testBloodType() {
        assertEquals("O+", recipient.getBloodType());
        recipient.setBloodType("A-");
        assertEquals("A-", recipient.getBloodType());
    }
}
