package project3;

import java.util.HashMap;

/**
 * WaitlistManager is the controller of the LifeSaver system. It keeps one
 * WaitlistDeque per organ type and a lookup table from recipient id to the
 * Node holding that recipient. It checks for duplicate / unknown IDs, unknown
 * organs, and blood compatibility.
 *
 * @author Kaustubh
 * @version 2026.11.10
 */
public class WaitlistManager {
    private HashMap<String, WaitlistDeque> waitlists;
    private HashMap<Integer, Node> lookup;

    // ----------------------------------------------------------
    /**
     * Create a new, empty WaitlistManager.
     */
    public WaitlistManager() {
        waitlists = new HashMap<>();
        lookup = new HashMap<>();
    }


    // ----------------------------------------------------------
    /**
     * Register a new recipient and add them to the waitlist for their organ.
     *
     * @param id
     *            unique recipient id
     * @param name
     *            recipient name
     * @param organ
     *            organ needed
     * @param bloodType
     *            recipient blood type
     * @param urgency
     *            urgency level (1-10)
     */
    public void addRecipient(
        int id,
        String name,
        String organ,
        String bloodType,
        int urgency) {
        // TODO
    }


    // ----------------------------------------------------------
    /**
     * Remove a recipient from whatever waitlist they are on.
     *
     * @param id
     *            id of the recipient to remove
     * @return true if the recipient was found and removed, false otherwise
     */
    public boolean removeRecipient(int id) {
        // TODO
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Find the first blood-compatible recipient on the waitlist for the
     * given organ and remove them from the waitlist.
     *
     * @param organType
     *            organ that was donated
     * @param donorBloodType
     *            donor's blood type
     * @return the matched Recipient, or null if no compatible match
     */
    public Recipient matchOrgan(String organType, String donorBloodType) {
        // TODO
        return null;
    }


    // ----------------------------------------------------------
    /**
     * Get every recipient on the waitlist for the given organ, in
     * registration (FIFO) order.
     *
     * @param organType
     *            organ waitlist to return
     * @return array of Recipients on that waitlist
     */
    public Recipient[] getWaitlist(String organType) {
        // TODO
        return null;
    }
}
