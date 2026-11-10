package Project3;

import java.util.NoSuchElementException;

public class WaitlistDeque<T> extends Recipient
{
    //~ Fields ................................................................
    /**
     * References node at front of deque
     */
    protected DLNode<T> head;
    /**
     * References node at back of deque
     */
    protected DLNode<T> tail;
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
     * @param r recipient class
     */
    //~Public  Methods ........................................................
    public void enqueue(Recipient r) {
        if (r == null) {
            throw new IllegalArgumentException();
        }
        DLNode<T> newEntry = new DLNode<T> (r);
        tail.setNextNode(newEntry);
        size++;
        
    }
    
    // ----------------------------------------------------------
    /**
     * Removes and returns the recipient at front
     * @return recipient at front
     */
    public Recipient dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Recipient r = head.getData();
        head = head.getNextNode();
        size--;
    }
    
    // ----------------------------------------------------------
    /**
     * Returns recipient at front, does not remove them
     * @return recipient at front
     */
    public Recipient peek() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return head.getData();
    }
    
    // ----------------------------------------------------------
    /**
     * returns true if no one is waiting
     * @return true or false
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    // ----------------------------------------------------------
    /**
     * Size of the deque
     * @return size
     */
    public int size() {
        return this.size;
    }
    
    // ----------------------------------------------------------
    /**
     * Supposed to remove a specific node with ID, incomplete. Need ID
     * @param n
     */
    public void remove(Recipient r) {
        int id = r.getId;
    }
    
    // ----------------------------------------------------------
    /**
     * @return array with all recipients
     */
    public Recipient[] get_all_recipients() {
        int l = size();
        DLNode<T> p = head;
        Recipient[] all = new Recipient[l];
        for (int i = 0; i < l; i++) {
            all.add(p);
            p = p.getNextNode();
        }
        return all;
    }
    
    // -------------------------------------------------------------------------
    /**
     *  Class for DLNode and its methods
     *  @param <T>
     * 
     *  @author Andrew Kim
     *  @version Nov 10, 2026
     */
    protected static final class DLNode<T>
    {

        /**
         * Deque entry
         */
        private T data;
        /**
         * Link to next node
         */
        private DLNode<T> next;
        /**
         * Link to previous node
         */
        private DLNode<T> previous;

        /**
         * Constructor
         * 
         * @param dataPortion
         *            the data to be stored in the node
         */
        protected DLNode(T dataPortion)
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
            DLNode<T> previousNode,
            T dataPortion,
            DLNode<T> nextNode)
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
        protected T getData()
        {
            return data;
        }


        /**
         * Sets the data portion of the node
         * 
         * @param newData
         *            data to store in the node
         */
        protected void setData(T newData)
        {
            data = newData;
        }


        /**
         * Returns the next node
         * 
         * @return reference to the next node
         */
        protected DLNode<T> getNextNode()
        {
            return next;
        }


        /**
         * Sets the next node reference
         * 
         * @param nextNode
         *            reference to next node
         */
        protected void setNextNode(DLNode<T> nextNode)
        {
            next = nextNode;
        }


        /**
         * Returns the previous node
         * 
         * @return reference to the previous node
         */
        protected DLNode<T> getPreviousNode()
        {
            return previous;
        }


        /**
         * Sets the previous node reference
         * 
         * @param previousNode
         *            reference to previous node
         */
        protected void setPreviousNode(DLNode<T> previousNode)
        {
            previous = previousNode;
        }
    }
}
