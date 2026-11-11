package project3;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * A stubbed / thinned WaitlistManager for building and testing the view
 * without the real model. It overrides only the public methods the view uses
 * and returns fixed recipients instead of real queue data, so it shows that
 * OutputViewer depends only on the controller's public methods.
 *
 * Fixed behavior:
 * <ul>
 * <li>getWaitlist("kidney") returns two fixed recipients (IDs 1 and 2);
 * every other known organ is empty; an unknown organ throws.</li>
 * <li>addRecipient throws for ID 1 or 2 (already "registered") or an unknown
 * organ; otherwise it just records the call. Nothing is stored.</li>
 * <li>removeRecipient returns true for IDs 1 and 2, false otherwise.</li>
 * <li>dequeueRecipient("kidney") returns fixed recipient 1; other known
 * organs return null (empty); an unknown organ throws.</li>
 * <li>matchOrgan("kidney", ...) returns fixed recipient 1; other known organs
 * return null; an unknown organ throws.</li>
 * </ul>
 * The last call is recorded in public fields so tests can check what the
 * view sent.
 *
 * @author Prateek Malekar
 * @version 2026.11.11
 */
public class StubWaitlistManager extends WaitlistManager {
    private static final String[] ORGANS = { "heart", "kidney", "liver",
        "lung", "pancreas", "intestine" };

    /** Fixed recipients returned for the kidney waitlist. */
    private final Recipient[] kidneyList;

    /** Name of the last method the view called. */
    public String lastCall;
    /** Last ID passed in. */
    public int lastId;
    /** Last name passed in. */
    public String lastName;
    /** Last organ passed in. */
    public String lastOrgan;
    /** Last blood type passed in. */
    public String lastBloodType;
    /** Last urgency passed in. */
    public int lastUrgency;

    // ----------------------------------------------------------
    /**
     * Create the stub with its fixed data.
     */
    public StubWaitlistManager() {
        super();
        LocalDateTime date = LocalDateTime.of(2026, 11, 9, 10, 30);
        kidneyList = new Recipient[] {
            new Recipient(1, "Jax", "kidney", "A+", date, 6, "waiting"),
            new Recipient(2, "Andrew", "kidney", "O-", date.plusDays(1), 4,
                "waiting") };
    }


    @Override
    public void addRecipient(
        int id,
        String name,
        String organ,
        String bloodType,
        int urgency) {
        record("addRecipient", organ);
        lastId = id;
        lastName = name;
        lastBloodType = bloodType;
        lastUrgency = urgency;
        if (id == 1 || id == 2) {
            throw new IllegalArgumentException(
                "Recipient with id " + id + " already exists");
        }
        checkOrgan(organ);
    }


    @Override
    public boolean removeRecipient(int id) {
        record("removeRecipient", null);
        lastId = id;
        return id == 1 || id == 2;
    }


    @Override
    public Recipient dequeueRecipient(String organType) {
        record("dequeueRecipient", organType);
        if (checkOrgan(organType).equals("kidney")) {
            return kidneyList[0];
        }
        return null;
    }


    @Override
    public Recipient matchOrgan(String organType, String donorBloodType) {
        record("matchOrgan", organType);
        lastBloodType = donorBloodType;
        if (checkOrgan(organType).equals("kidney")) {
            return kidneyList[0];
        }
        return null;
    }


    @Override
    public Recipient[] getWaitlist(String organType) {
        // not recorded as lastCall so redraws don't hide the real action
        if (checkOrgan(organType).equals("kidney")) {
            return Arrays.copyOf(kidneyList, kidneyList.length);
        }
        return new Recipient[0];
    }


    private void record(String call, String organ) {
        lastCall = call;
        lastOrgan = organ;
    }


    /** Same organ rule as the real manager: trim, lower-case, must exist. */
    private String checkOrgan(String organ) {
        String key = organ == null ? "" : organ.trim().toLowerCase();
        for (String o : ORGANS) {
            if (o.equals(key)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Unknown organ: " + organ);
    }
}
