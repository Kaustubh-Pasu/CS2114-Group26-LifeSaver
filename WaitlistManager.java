package project3;
import java.time.Clock;
import java.time.Duration;
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
    private HashMap<String, Long> totalFrontSeconds;
    private HashMap<String, Integer> frontCount;
    /** Where the current time comes from (fake in tests). */
    private Clock clock;

    // ----------------------------------------------------------
    /**
     * Create a new WaitlistManager with an empty waitlist for every
     * supported organ type, using the real system clock.
     */
    public WaitlistManager() {
        this(Clock.systemDefaultZone());
    }

    // ----------------------------------------------------------
    /**
     * Create a new WaitlistManager that reads the time from the given clock.
     * Tests pass a fake clock they can move forward, so wait times of days
     * can be tested without actually waiting.
     *
     * @param clock
     *              source of the current time
     */
    public WaitlistManager(Clock clock) {
        if (clock == null) {
            throw new IllegalArgumentException("clock is required");
        }
        this.clock = clock;
        waitlists = new HashMap<>();
        lookup = new HashMap<>();
        totalFrontSeconds = new HashMap<>();
        frontCount = new HashMap<>();
        for (String organ : ORGANS) {
            waitlists.put(organ, new WaitlistDeque(clock));
            totalFrontSeconds.put(organ, 0L);
            frontCount.put(organ, 0);
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
                LocalDateTime.now(clock),
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
        recordFrontWait(recipient);
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
                recordFrontWait(candidate);
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
    
    // ----------------------------------------------------------
    /**
     * Add the time a recipient spent at the front of their waitlist to that
     * organ's running total. Called when someone leaves the front by being
     * dequeued or matched (not when removed by ID).
     *
     * @param r
     *          recipient who just left the waitlist
     */
    private void recordFrontWait(Recipient r) {
        if (r.getFrontSince() == null) {
            return;
        }
        long seconds = Duration.between(
            r.getFrontSince(), LocalDateTime.now(clock)).getSeconds();
        String organ = r.getOrganNeeded();
        totalFrontSeconds.put(organ, totalFrontSeconds.get(organ) + seconds);
        frontCount.put(organ, frontCount.get(organ) + 1);
    }

    // ----------------------------------------------------------
    /**
     * Estimate how long a recipient will wait: the organ's average time spent
     * at the front of the line, times the recipient's position.
     *
     * @param id
     *           id of a recipient who is still waiting
     * @return estimated wait, or null if nobody has left this organ's
     *         waitlist yet (no history to average)
     * @throws IllegalArgumentException
     *           if no waiting recipient has that id
     */
    public Duration estimateWaitTime(int id) {
        WaitlistDeque.DLNode<Recipient> node = lookup.get(id);
        if (node == null) {
            throw new IllegalArgumentException("No patient with id " + id);
        }
        String organ = node.getData().getOrganNeeded();
        int count = frontCount.get(organ);
        if (count == 0) {
            return null;
        }
        long averageSeconds = totalFrontSeconds.get(organ) / count;
        int position = waitlists.get(organ).positionOf(node);
        return Duration.ofSeconds(averageSeconds * position);
    }

    // ----------------------------------------------------------
    /**
     * Find a waiting recipient by id.
     *
     * @param id
     *           recipient id
     * @return the recipient, or null if nobody waiting has that id
     */
    public Recipient getRecipient(int id) {
        WaitlistDeque.DLNode<Recipient> node = lookup.get(id);
        return node == null ? null : node.getData();
    }

    // ----------------------------------------------------------
    /**
     * Find a waiting recipient's place in line (1 = front).
     *
     * @param id
     *           recipient id
     * @return position starting at 1, or -1 if nobody waiting has that id
     */
    public int getPosition(int id) {
        WaitlistDeque.DLNode<Recipient> node = lookup.get(id);
        if (node == null) {
            return -1;
        }
        return waitlists.get(node.getData().getOrganNeeded()).positionOf(node);
    }

    // ----------------------------------------------------------
    /**
     * Check whether an id belongs to someone currently waiting.
     *
     * @param id
     *           recipient id
     * @return true if the id is already in use
     */
    public boolean isRegistered(int id) {
        return lookup.containsKey(id);
    }

    // ----------------------------------------------------------
    /**
     * Check whether an organ name is one the system accepts (any case,
     * surrounding spaces ignored).
     *
     * @param organ
     *              organ name
     * @return true if the organ has a waitlist
     */
    public boolean isKnownOrgan(String organ) {
        return waitlists.containsKey(normalizeOrgan(organ));
    }

    // ----------------------------------------------------------
    /**
     * Check whether a blood type would be accepted by addRecipient.
     *
     * @param bloodType
     *                  blood type such as "A+"
     * @return true if valid
     */
    public boolean isValidBloodType(String bloodType) {
        try {
            aboGroup(bloodType);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }
}
