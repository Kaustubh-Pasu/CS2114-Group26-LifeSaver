package project3;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;


// -------------------------------------------------------------------------
/**
 *  Testing methods on WaitlistDeque
 * 
 *  @author Andrew Kim
 *  @version Nov 11, 2026
 */
public class WaitlistDequeTest 
{
    //~ Fields ................................................................

    private WaitlistDeque deque;
    private Recipient r;
    private LocalDateTime date;
    private IllegalArgumentException iae;
    private NoSuchElementException nse;
    //~ Constructors ..........................................................

    //~Public  Methods ........................................................

    public void setUp() {
        deque = new WaitlistDeque();
        date = LocalDateTime.of(2026, 11, 9, 10, 30);
        r = new Recipient(1, "Jax", "Kidney", "O+", date, 6, "Waiting");
    }
    
    
    public void testEnqueue() {
        assertTrue(deque.isEmpty());
        assertEquals(r, deque.enqueue(r));
        try {
            deque.enqueue(null);        
        }
        catch(IllegalArgumentException e)
        {
            iae = e;
        }
        assertNotNull(iae);
    }
    
    public void testDequeue() {
        deque.enqueue(r);
        assertEquals(r, deque.dequeue());
        try {
            deque.dequeue();        
        }
        catch(NoSuchElementException e)
        {
            nse = e;
        }
        assertNotNull(nse);
    }
    
    public void testPeek() {
        try {
            deque.peek();        
        }
        catch(NoSuchElementException e)
        {
            nse = e;
        }
        assertNotNull(nse);
        deque.enqueue(r);
        assertEquals(r, deque.peek());
    }
    
    public void testIsEmpty() {
        assertTrue(deque.isEmpty());
        deque.enqueue(r);
        assertFalse(deque.isEmpty());
    }
    
    public void testSize() {
        assertEquals(0, deque.size());
        deque.enqueue(r);
        assertEquals(1, deque.size());
    }
    
    public void testGetAllRecipients() {
        Recipient[] allRecipients = new Recipient[0];
        assertEquals(allRecipients, deque.getAllRecipients());
        Recipient[] allRecipients1 = new Recipient[1];
        allRecipients1[0] = r;
        assertEquals(allRecipients1, deque.getAllRecipients());
    }
    
    
}
