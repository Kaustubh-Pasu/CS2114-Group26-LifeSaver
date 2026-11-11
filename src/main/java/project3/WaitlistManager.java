package project3;

import java.time.LocalDateTime;
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
    /** Organ types the system accepts. */
    private static final String[] ORGANS = { "heart", "kidney", "liver",
        "lung", "pancreas", "intestine" };

    private HashMap<String, WaitlistDeque> waitlists;
    private HashMap<Integer, WaitlistDeque.DLNode<Recipient>> lookup;

    // ----------------------------------------------------------
    /**
     * Create a new WaitlistManager with an empty waitlist for every
     * supported organ type.
     */
    public WaitlistManager() {
        waitlists = new HashMap<>();
        lookup = new HashMap<>();
        for (String organ : ORGANS) {
            waitlists.put(organ, new WaitlistDeque());
        }
    }

    // ----------------------------------------------------------
    /**
     * Register a new recipient and add them to the waitlist for their organ.
     *
     * @param id
     *                  unique recipient id
     * @param name
     *                  recipient name
     * @param organ
     *                  organ needed
     * @param bloodType
     *                  recipient blood type
     * @param urgency
     *                  urgency level (1-10)
     * @throws IllegalArgumentException
     *                  if the id is already registered, the organ is
     *                  unknown, or the blood type is invalid
     */
    public void addRecipient(
            int id,
            String name,
            String organ,
            String bloodType,
            int urgency) {
        if (lookup.containsKey(id)) {
            throw new IllegalArgumentException(
                    "Recipient with id " + id + " already exists");
        }
        String organKey = normalizeOrgan(organ);
        if (!waitlists.containsKey(organKey)) {
            throw new IllegalArgumentException("Unknown organ: " + organ);
        }
        aboGroup(bloodType); // throws if the blood type is invalid

        Recipient recipient = new Recipient(
                id,
                name,
                organKey,
                bloodType,
                LocalDateTime.now(),
                urgency,
                "waiting");
        enqueRecipient(recipient);
    }

    // ----------------------------------------------------------
    /**
     * Remove a recipient from whatever waitlist they are on.
     *
     * @param id
     *           id of the recipient to remove
     * @return true if the recipient was found and removed, false otherwise
     */
    public boolean removeRecipient(int id) {
        WaitlistDeque.DLNode<Recipient> node = lookup.remove(id);
        if (node == null) {
            return false;
        }
        Recipient recipient = node.getData();
        waitlists.get(recipient.getOrganNeeded()).remove(node);
        recipient.setStatus("removed");
        return true;
    }

    // ----------------------------------------------------------
    /**
     * Dequeue the recipient at the front of an organ's waitlist (the one who
     * has waited longest) and mark them removed.
     *
     * @param organType
     *                  organ waitlist to dequeue from
     * @return the dequeued Recipient, or null if that waitlist is empty
     * @throws IllegalArgumentException
     *                  if the organ is unknown
     */
    public Recipient dequeueRecipient(String organType) {
        WaitlistDeque deque = getDeque(organType);
        if (deque.isEmpty()) {
            return null;
        }
        Recipient recipient = deque.dequeue();
        lookup.remove(recipient.getId());
        recipient.setStatus("removed");
        return recipient;
    }

    // ----------------------------------------------------------
    /**
     * Find the first blood-compatible recipient on the waitlist for the
     * given organ and remove them from the waitlist.
     *
     * @param organType
     *                       organ that was donated
     * @param donorBloodType
     *                       donor's blood type
     * @return the matched Recipient, or null if no compatible match
     * @throws IllegalArgumentException
     *                       if the organ is unknown or the blood type is
     *                       invalid
     */
    public Recipient matchOrgan(String organType, String donorBloodType) {
        WaitlistDeque deque = getDeque(organType);
        String donorGroup = aboGroup(donorBloodType);

        // FIFO: walk in registration order, take the first compatible one
        for (Recipient candidate : deque.getAllRecipients()) {
            String recipientGroup = aboGroup(candidate.getBloodType());
            if (isCompatible(donorGroup, recipientGroup)) {
                WaitlistDeque.DLNode<Recipient> node = lookup.remove(candidate.getId());
                deque.remove(node);
                candidate.setStatus("matched");
                return candidate;
            }
        }
        return null;
    }

    // ----------------------------------------------------------
    /**
     * Get every recipient on the waitlist for the given organ, in
     * registration (FIFO) order.
     *
     * @param organType
     *                  organ waitlist to return
     * @return array of Recipients on that waitlist
     * @throws IllegalArgumentException
     *                  if the organ is unknown
     */
    public Recipient[] getWaitlist(String organType) {
        return getDeque(organType).getAllRecipients();
    }

    // ----------------------------------------------------------
    /**
     * Add a recipient to the back of their organ's waitlist and record the
     * Node holding them in the lookup table.
     *
     * @param recipient
     *                  the recipient to enqueue
     */
    private void enqueRecipient(Recipient recipient) {
        WaitlistDeque deque = waitlists.get(recipient.getOrganNeeded());
        WaitlistDeque.DLNode<Recipient> node = deque.enqueue(recipient);
        lookup.put(recipient.getId(), node);
    }

    // ----------------------------------------------------------
    /**
     * Look up the waitlist for an organ.
     *
     * @param organType
     *                  organ name (any case)
     * @return the matching WaitlistDeque
     * @throws IllegalArgumentException
     *                  if the organ is unknown
     */
    private WaitlistDeque getDeque(String organType) {
        WaitlistDeque deque = waitlists.get(normalizeOrgan(organType));
        if (deque == null) {
            throw new IllegalArgumentException("Unknown organ: " + organType);
        }
        return deque;
    }

    // ----------------------------------------------------------
    /**
     * Normalize an organ name so "Kidney " and "kidney" match.
     *
     * @param organ
     *              raw organ name
     * @return trimmed, lower-case organ name ("" if null)
     */
    private String normalizeOrgan(String organ) {
        if (organ == null) {
            return "";
        }
        return organ.trim().toLowerCase();
    }

    // ----------------------------------------------------------
    /**
     * Strip the Rh factor (+/-) from a blood type and validate the ABO group.
     *
     * @param bloodType
     *                  blood type such as "A+", "O-", or "AB"
     * @return the ABO group: "A", "B", "AB", or "O"
     * @throws IllegalArgumentException
     *                  if the blood type is not valid
     */
    private String aboGroup(String bloodType) {
        if (bloodType == null) {
            throw new IllegalArgumentException("Blood type is required");
        }
        String group = bloodType.trim().toUpperCase();
        if (group.endsWith("+") || group.endsWith("-")) {
            group = group.substring(0, group.length() - 1);
        }
        if (!group.equals("A") && !group.equals("B") && !group.equals("AB")
                && !group.equals("O")) {
            throw new IllegalArgumentException(
                    "Invalid blood type: " + bloodType);
        }
        return group;
    }

    // ----------------------------------------------------------
    /**
     * ABO compatibility for organ donation: O gives to everyone, AB receives
     * from everyone, otherwise the groups must match.
     *
     * @param donorGroup
     *                       donor ABO group
     * @param recipientGroup
     *                       recipient ABO group
     * @return true if the donor can give to the recipient
     */
    private boolean isCompatible(String donorGroup, String recipientGroup) {
        return donorGroup.equals("O")
                || recipientGroup.equals("AB")
                || donorGroup.equals(recipientGroup);
    }
}
