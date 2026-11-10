package project3;

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Arrays;

// -------------------------------------------------------------------------
/**
 * Store each recipient as its value. Doubly linked queue that represents the
 * waitlist itself.
 * 
 * @author Andrew Kim
 * @version Nov 10, 2026
 */
public class WaitlistDeque
{
    // ~ Fields ................................................................
    /**
     * References node at front of deque
     */
    protected DLNode<Recipient> head;
    /**
     * References node at back of deque
     */
    protected DLNode<Recipient> tail;
    /**
     * number of elements in the deque
     */
    protected int size;

    /**
     * Create a new DLinkedDeque object.
     */
    public WaitlistDeque()
    {
        head = null;
        tail = null;
        size = 0;
    }


    // ----------------------------------------------------------
    /**
     * Adds new recipient to end of list
     * 
     * @param r
     *            recipient class
     */
    // ~Public Methods ........................................................
    public void enqueue(Recipient r)
    {
        if (r == null)
        {
            throw new IllegalArgumentException();
        }
        DLNode<Recipient> newEntry = new DLNode<Recipient>(r);
        tail.setNextNode(newEntry);
        newEntry.setPreviousNode(tail);
        tail = newEntry;
        size++;

    }


    // ----------------------------------------------------------
    /**
     * Removes and returns the recipient at front
     * 
     * @return recipient at front
     */
    public Recipient dequeue()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        Recipient r = head.getData();
        head = head.getNextNode();
        head.setPreviousNode(null);
        size--;
        return r;
    }


    // ----------------------------------------------------------
    /**
     * Returns recipient at front, does not remove them
     * 
     * @return recipient at front
     */
    public Recipient peek()
    {
        if (isEmpty())
        {
            throw new NoSuchElementException();
        }
        return head.getData();
    }


    // ----------------------------------------------------------
    /**
     * returns true if no one is waiting
     * 
     * @return true or false
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }


    // ----------------------------------------------------------
    /**
     * Size of the deque
     * 
     * @return size
     */
    public int size()
    {
        return this.size;
    }


    // ----------------------------------------------------------
    /**
     * Supposed to remove a specific node with ID, incomplete. Need ID
     * 
     * @param n
     */
    public void remove(DLNode<Recipient> n)
    {
        if (n == null)
        {
            throw new IllegalArgumentException();
        }

        DLNode<Recipient> newNext = n.getNextNode();
        DLNode<Recipient> prev = n.getPreviousNode();

        prev.setNextNode(newNext);
        newNext.setPreviousNode(prev);
        size--;
    }


    // ----------------------------------------------------------
    /**
     * @return array with all recipients
     */
    public Recipient[] get_all_recipients()
    {
        int l = size();
        DLNode<Recipient> p = head;
        Recipient[] allRecipients = new Recipient[l];
        for (int i = 0; i < l; i++)
        {
            Recipient current = p.getData();
            allRecipients[i] = current;
            p = p.getNextNode();
        }
        return allRecipients;
    }

    // -------------------------------------------------------------------------
    /**
     * Class for DLNode and its methods
     * 
     * @param <Recipient>
     * @author Andrew Kim
     * @version Nov 10, 2026
     */
    @SuppressWarnings("hiding")
    protected static final class DLNode<Recipient>
    {

        /**
         * Deque entry
         */
        private Recipient data;
        /**
         * Link to next node
         */
        private DLNode<Recipient> next;
        /**
         * Link to previous node
         */
        private DLNode<Recipient> previous;

        /**
         * Constructor
         * 
         * @param dataPortion
         *            the data to be stored in the node
         */
        protected DLNode(Recipient dataPortion)
        {
            data = dataPortion;
            next = null;
            previous = null;
        }


        /**
         * Constructor
         * 
         * @param previousNode
         *            the reference to the previous node
         * @param dataPortion
         *            the data to be stored in the node
         * @param nextNode
         *            the reference to the next node
         */
        protected DLNode(
            DLNode<Recipient> previousNode,
            Recipient dataPortion,
            DLNode<Recipient> nextNode)
        {
            // data = dataPortion;
            this(dataPortion);
            next = nextNode;
            previous = previousNode;
        }


        /**
         * Returns the data portion of the node
         * 
         * @return data portion
         */
        protected Recipient getData()
        {
            return data;
        }


        /**
         * Sets the data portion of the node
         * 
         * @param newData
         *            data to store in the node
         */
        protected void setData(Recipient newData)
        {
            data = newData;
        }


        /**
         * Returns the next node
         * 
         * @return reference to the next node
         */
        protected DLNode<Recipient> getNextNode()
        {
            return next;
        }


        /**
         * Sets the next node reference
         * 
         * @param nextNode
         *            reference to next node
         */
        protected void setNextNode(DLNode<Recipient> nextNode)
        {
            next = nextNode;
        }


        /**
         * Returns the previous node
         * 
         * @return reference to the previous node
         */
        protected DLNode<Recipient> getPreviousNode()
        {
            return previous;
        }


        /**
         * Sets the previous node reference
         * 
         * @param previousNode
         *            reference to previous node
         */
        protected void setPreviousNode(DLNode<Recipient> previousNode)
        {
            previous = previousNode;
        }
    }
}
