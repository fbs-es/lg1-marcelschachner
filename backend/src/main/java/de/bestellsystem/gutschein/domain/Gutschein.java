package de.bestellsystem.gutschein.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Aktionscode mit prozentualem oder festem Rabatt auf den Warenwert (D-04, D-17). */
@Entity
@Table(name = "gutschein")
public class Gutschein {

    public static final int CODE_MAXIMALE_LAENGE = 50;

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    @Id private UUID id;

    private String code;

    @Enumerated(EnumType.STRING)
    private RabattArt rabattArt;

    @Column(precision = 5, scale = 2)
    private BigDecimal prozent;

    private Long festbetragCent;

    private LocalDate ablaufdatum;

    protected Gutschein() {}

    private Gutschein(
            String code,
            RabattArt rabattArt,
            BigDecimal prozent,
            Long festbetragCent,
            LocalDate ablaufdatum) {
        if (ablaufdatum == null) {
            throw new IllegalArgumentException("ablaufdatum ist erforderlich");
        }
        this.id = UUID.randomUUID();
        this.code = normalisiere(code);
        this.rabattArt = rabattArt;
        this.prozent = prozent;
        this.festbetragCent = festbetragCent;
        this.ablaufdatum = ablaufdatum;
    }

    public static Gutschein prozent(String code, BigDecimal prozent, LocalDate ablaufdatum) {
        if (prozent == null
                || prozent.signum() <= 0
                || prozent.compareTo(HUNDERT) > 0
                || prozent.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "prozent muss größer 0 und höchstens 100 sein, mit höchstens zwei"
                            + " Nachkommastellen");
        }
        return new Gutschein(code, RabattArt.PROZENT, prozent, null, ablaufdatum);
    }

    public static Gutschein festbetrag(String code, Geld festbetrag, LocalDate ablaufdatum) {
        if (festbetrag == null || festbetrag.compareTo(Geld.NULL) <= 0) {
            throw new IllegalArgumentException("festbetrag muss größer 0 sein");
        }
        return new Gutschein(code, RabattArt.FESTBETRAG, null, festbetrag.cent(), ablaufdatum);
    }

    /** Codes werden ohne Beachtung der Groß-/Kleinschreibung verglichen (D-04, D-17). */
    public static String normalisiere(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code darf nicht leer sein");
        }
        String normalisiert = code.strip().toUpperCase(Locale.ROOT);
        if (normalisiert.length() > CODE_MAXIMALE_LAENGE) {
            throw new IllegalArgumentException(
                    "code darf höchstens " + CODE_MAXIMALE_LAENGE + " Zeichen haben");
        }
        return normalisiert;
    }

    /** Ablaufdatum gilt inklusive (D-04). */
    public boolean istGueltigAm(LocalDate datum) {
        return !datum.isAfter(ablaufdatum);
    }

    /** Rabatt auf den Warenwert, nie höher als dieser (D-01, D-04, D-11). */
    public Geld berechneRabatt(Geld warenwert) {
        Geld rabatt =
                switch (rabattArt) {
                    case PROZENT -> warenwert.prozentsatz(prozent);
                    case FESTBETRAG -> Geld.ausCent(festbetragCent);
                };
        return rabatt.compareTo(warenwert) > 0 ? warenwert : rabatt;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public RabattArt getRabattArt() {
        return rabattArt;
    }

    public LocalDate getAblaufdatum() {
        return ablaufdatum;
    }

    // Die UUID wird im Konstruktor vergeben und ändert sich nie, daher stabil als Identität.
    @Override
    public boolean equals(Object anderes) {
        return this == anderes
                || (anderes instanceof Gutschein gutschein && id.equals(gutschein.getId()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
