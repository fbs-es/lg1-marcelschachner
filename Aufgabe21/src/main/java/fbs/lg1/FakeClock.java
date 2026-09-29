package fbs.lg1;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Eine steuerbare {@link Clock} für Demo und Tests.
 *
 * <p>Die Zeit steht still, bis sie mit {@link #advance(Duration)} weitergestellt wird. Die
 * Zeitzone ist immer UTC. Die Klasse ist paketintern und nicht thread-sicher.
 */
class FakeClock extends Clock {

    private Instant instant;

    /**
     * Erzeugt eine Uhr mit dem angegebenen Startzeitpunkt.
     *
     * @param instant der Startzeitpunkt
     */
    FakeClock(Instant instant) {
        this.instant = instant;
    }

    /**
     * Erzeugt eine Uhr mit dem festen Startzeitpunkt 2026-09-28T10:00:00Z.
     *
     * @return die neue Uhr
     */
    static FakeClock start() {
        return new FakeClock(Instant.parse("2026-09-28T10:00:00Z"));
    }

    /**
     * Stellt die Uhr um die angegebene Dauer vor.
     *
     * @param duration die Dauer, um die die Zeit weiterläuft
     */
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
