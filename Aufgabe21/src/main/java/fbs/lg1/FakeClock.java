package fbs.lg1;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

class FakeClock extends Clock {

    private Instant instant;

    FakeClock(Instant instant) {
        this.instant = instant;
    }

    static FakeClock start() {
        return new FakeClock(Instant.parse("2026-09-28T10:00:00Z"));
    }

    void advance(Duration duration) {
        instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
