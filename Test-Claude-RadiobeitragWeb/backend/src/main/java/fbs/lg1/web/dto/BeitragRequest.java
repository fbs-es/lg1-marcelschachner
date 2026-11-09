package fbs.lg1.web.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import fbs.lg1.Radiobeitrag;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Anfrage zum Hinzufügen eines Beitrags. Das JSON-Feld {@code typ} legt die Beitragsart fest.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "typ")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MusikbeitragRequest.class, name = "MUSIK"),
    @JsonSubTypes.Type(value = WerbebeitragRequest.class, name = "WERBUNG"),
    @JsonSubTypes.Type(value = ModerationRequest.class, name = "MODERATION"),
    @JsonSubTypes.Type(value = EilmeldungRequest.class, name = "EILMELDUNG")
})
@Schema(
        description = "Neuer Beitrag, die Art steht im Feld typ",
        discriminatorProperty = "typ",
        oneOf = {
            MusikbeitragRequest.class,
            WerbebeitragRequest.class,
            ModerationRequest.class,
            EilmeldungRequest.class
        },
        discriminatorMapping = {
            @DiscriminatorMapping(value = "MUSIK", schema = MusikbeitragRequest.class),
            @DiscriminatorMapping(value = "WERBUNG", schema = WerbebeitragRequest.class),
            @DiscriminatorMapping(value = "MODERATION", schema = ModerationRequest.class),
            @DiscriminatorMapping(value = "EILMELDUNG", schema = EilmeldungRequest.class)
        })
public interface BeitragRequest {

    /**
     * Erstellt aus den Anfragedaten das passende Domänenobjekt. Die Domäne prüft dabei die Werte.
     *
     * @return der neue Beitrag
     * @throws IllegalArgumentException wenn die Werte ungültig sind
     */
    Radiobeitrag erstelleBeitrag();
}
