package fbs.lg1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BuchTest {

    private Buch buch;

    @BeforeEach
    void prepare() {
        buch = new Buch("978-3-16-148410-0", "Der Prozess", "Franz Kafka");
    }

    @Test
    void testInit() {
        assertThat(buch.showIsbn()).isEqualTo("978-3-16-148410-0");
        assertThat(buch.showTitel()).isEqualTo("Der Prozess");
        assertThat(buch.showAutor()).isEqualTo("Franz Kafka");
        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Buch(null, "Der Prozess", "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Buch(" ", "Der Prozess", "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Buch("978-3-16-148410-0", null, "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Buch("978-3-16-148410-0", "", "Franz Kafka"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Buch("978-3-16-148410-0", "Der Prozess", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Buch("978-3-16-148410-0", "Der Prozess", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testAusleihen() {
        buch.ausleihen();

        assertThat(buch.istVerfuegbar()).isFalse();
    }

    @Test
    void testAusleihenBereitsAusgeliehen() {
        buch.ausleihen();

        assertThatThrownBy(() -> buch.ausleihen()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testZurueckgeben() {
        buch.ausleihen();

        buch.zurueckgeben();

        assertThat(buch.istVerfuegbar()).isTrue();
    }

    @Test
    void testZurueckgebenNichtAusgeliehen() {
        assertThatThrownBy(() -> buch.zurueckgeben()).isInstanceOf(IllegalStateException.class);
    }
}
