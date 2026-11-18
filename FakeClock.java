package project3;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A Clock for tests whose time only moves when the test says so. Pass it to
 * WaitlistManager, then call advance(Duration.ofDays(10)) to make ten days
 * "pass" instantly. Everything that asks the manager for the current time
 * (registration dates, time at the front of the line, wait estimates) sees
 * the fake time.
 *
 * @author Prateek Malekar
 * @version 2026.11.18
 */
public class FakeClock extends Clock {
    private Instant now;
    private final ZoneId zone;

    // ----------------------------------------------------------
    /**
     * Create a fake clock frozen at the given time (UTC).
     *
     * @param start
     *            the time the clock starts at
     */
    public FakeClock(LocalDateTime start) {
        this(start.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }


    private FakeClock(Instant now, ZoneId zone) {
        this.now = now;
        this.zone = zone;
    }


    // ----------------------------------------------------------
    /**
     * Move the clock forward.
     *
     * @param amount
     *            how much time should "pass"
     */
    public void advance(Duration amount) {
        now = now.plus(amount);
    }


    // ----------------------------------------------------------
    /**
     * The current fake time as a LocalDateTime (handy in assertions).
     *
     * @return current fake time
     */
    public LocalDateTime currentTime() {
        return LocalDateTime.now(this);
    }


    @Override
    public ZoneId getZone() {
        return zone;
    }


    @Override
    public Clock withZone(ZoneId newZone) {
        return new FakeClock(now, newZone);
    }


    @Override
    public Instant instant() {
        return now;
    }
}
