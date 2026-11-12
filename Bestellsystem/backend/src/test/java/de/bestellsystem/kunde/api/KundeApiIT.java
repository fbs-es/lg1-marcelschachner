package de.bestellsystem.kunde.api;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.kunde.persistence.KundeRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersKonfiguration.class)
class KundeApiIT {

    private static final String KUNDEN = "/api/v1/kunden";
    private static final String ADRESSEN = KUNDEN + "/{kundenId}/adressen";
    private static final String ADRESSE = ADRESSEN + "/{adressId}";

    private static final String BERLIN =
            """
            {"strasse": "Hauptstraße 1", "stadt": "Berlin", "postleitzahl": "10115", "land": "DE"}
            """;
    private static final String WIEN =
            """
            {"strasse": "Ring 2", "stadt": "Wien", "postleitzahl": "1010", "land": "AT"}
            """;

    @Autowired private MockMvc mockMvc;
    @Autowired private KundeRepository repository;

    @BeforeEach
    void leereKunden() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("US-01.1 AK1: Registrierung liefert 201, Kunde mit genau dieser Lieferadresse")
    void registrierung() throws Exception {
        String id = registriere("anna@example.de");

        mockMvc.perform(get(KUNDEN + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.vorname").value("Anna"))
                .andExpect(jsonPath("$.nachname").value("Muster"))
                .andExpect(jsonPath("$.email").value("anna@example.de"))
                .andExpect(jsonPath("$.adressen.length()").value(1))
                .andExpect(jsonPath("$.adressen[0].id").isNotEmpty())
                .andExpect(jsonPath("$.adressen[0].anschrift.strasse").value("Hauptstraße 1"))
                .andExpect(jsonPath("$.adressen[0].anschrift.stadt").value("Berlin"))
                .andExpect(jsonPath("$.adressen[0].anschrift.postleitzahl").value("10115"))
                .andExpect(jsonPath("$.adressen[0].anschrift.land").value("DE"))
                .andExpect(jsonPath("$.adressen[0].istLieferadresse").value(true))
                .andExpect(jsonPath("$.adressen[0].istRechnungsadresse").value(false));
    }

    @Test
    @DisplayName("US-01.1 AK2: Registrierung ohne Adresse ergibt 400 mit Hinweis Lieferadresse")
    void ohneAdresse() throws Exception {
        mockMvc.perform(
                        json(post(KUNDEN))
                                .content(
                                        """
                                        {"vorname": "Anna", "nachname": "Muster",
                                         "email": "anna@example.de"}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ungültige Eingabe"))
                .andExpect(jsonPath("$.fehler[0].feld").value("lieferadresse"))
                .andExpect(
                        jsonPath("$.fehler[0].meldung")
                                .value("Eine Lieferadresse ist erforderlich"));
    }

    @Test
    @DisplayName("US-01.1 AK3: E-Mail vergeben, unabhängig von Groß-/Kleinschreibung ergibt 409")
    void emailVergeben() throws Exception {
        registriere("anna@example.de");

        mockMvc.perform(json(post(KUNDEN)).content(registrierung(" Anna@Example.DE ", BERLIN)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Konflikt"))
                .andExpect(jsonPath("$.detail").value(endsWith("bereits vergeben.")));
    }

    @Test
    @DisplayName("US-01.1 AK4: fehlendes Adressfeld ergibt 400 und benennt das Feld")
    void pflichtfelderDerAdresse() throws Exception {
        for (String feld : new String[] {"strasse", "stadt", "postleitzahl", "land"}) {
            String adresse =
                    BERLIN.replaceFirst("\"" + feld + "\": \"[^\"]*\"", "\"" + feld + "\": \"\"");
            mockMvc.perform(json(post(KUNDEN)).content(registrierung("anna@example.de", adresse)))
                    .andExpect(status().isBadRequest())
                    .andExpect(
                            jsonPath("$.fehler[*].feld").value(hasItem("lieferadresse." + feld)));
        }
    }

    @Test
    @DisplayName("US-01.1 AK5: Lieferadresse zugleich Rechnungsadresse trägt beide Kennzeichen")
    void auchRechnungsadresse() throws Exception {
        String antwort =
                mockMvc.perform(
                                json(post(KUNDEN))
                                        .content(
                                                registrierung("anna@example.de", BERLIN)
                                                        .replace(
                                                                "\"istAuchRechnungsadresse\": false",
                                                                "\"istAuchRechnungsadresse\": true")))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        mockMvc.perform(get(KUNDEN + "/{id}", JsonPath.<String>read(antwort, "$.id")))
                .andExpect(jsonPath("$.adressen[0].istLieferadresse").value(true))
                .andExpect(jsonPath("$.adressen[0].istRechnungsadresse").value(true));
    }

    @Test
    @DisplayName("D-16: ungültige E-Mail und zu langer Name ergeben 400")
    void ungueltigeStammdaten() throws Exception {
        mockMvc.perform(json(post(KUNDEN)).content(registrierung("anna@", BERLIN)))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("email")));
        mockMvc.perform(
                        json(post(KUNDEN))
                                .content(
                                        registrierung("anna@example.de", BERLIN)
                                                .replace(
                                                        "\"Anna\"", "\"" + "A".repeat(101) + "\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("vorname")));
    }

    @Test
    void listeOhneAdressenNachNachname() throws Exception {
        registriere("zora@example.de", "Zora", "Zander");
        registriere("anna@example.de", "Anna", "Albers");

        mockMvc.perform(get(KUNDEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nachname").value("Albers"))
                .andExpect(jsonPath("$[0].email").value("anna@example.de"))
                .andExpect(jsonPath("$[0].adressen").doesNotExist())
                .andExpect(jsonPath("$[1].nachname").value("Zander"));
    }

    @Test
    void unbekannterKundeErgibt404() throws Exception {
        mockMvc.perform(get(KUNDEN + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Nicht gefunden"));
    }

    @Test
    @DisplayName("US-01.2 AK1: weitere Adresse liefert 201 mit Location und steht im Adressbuch")
    void adresseHinzufuegen() throws Exception {
        String kunde = registriere("anna@example.de");

        String adresse = fuegeHinzu(kunde, WIEN, false, true);

        mockMvc.perform(get(ADRESSE, kunde, adresse))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anschrift.stadt").value("Wien"))
                .andExpect(jsonPath("$.istLieferadresse").value(false))
                .andExpect(jsonPath("$.istRechnungsadresse").value(true));
        mockMvc.perform(get(KUNDEN + "/{id}", kunde))
                .andExpect(jsonPath("$.adressen.length()").value(2))
                .andExpect(jsonPath("$.adressen[1].id").value(adresse));
    }

    @Test
    @DisplayName("US-01.2 AK2: Adresse ohne Kennzeichen ergibt 400 (I-2)")
    void hinzufuegenOhneKennzeichen() throws Exception {
        String kunde = registriere("anna@example.de");

        mockMvc.perform(json(post(ADRESSEN, kunde)).content(adresseJson(WIEN, false, false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("I-2")));
    }

    @Test
    @DisplayName("US-01.2 AK3: fehlendes Adressfeld ergibt 400 und benennt das Feld")
    void hinzufuegenOhnePflichtfeld() throws Exception {
        String kunde = registriere("anna@example.de");

        mockMvc.perform(
                        json(post(ADRESSEN, kunde))
                                .content(
                                        adresseJson(
                                                WIEN.replace("\"Wien\"", "\" \""), true, false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("anschrift.stadt")));
    }

    @Test
    @DisplayName("US-01.2 AK4: Adresse für unbekannten Kunden ergibt 404")
    void hinzufuegenUnbekannterKunde() throws Exception {
        mockMvc.perform(
                        json(post(ADRESSEN, UUID.randomUUID()))
                                .content(adresseJson(WIEN, true, false)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("US-01.3 AK1: Adresse ändern, Kennung bleibt")
    void aendern() throws Exception {
        String kunde = registriere("anna@example.de");
        String adresse = fuegeHinzu(kunde, WIEN, true, false);

        mockMvc.perform(
                        json(put(ADRESSE, kunde, adresse))
                                .content(adresseJson(BERLIN, false, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(adresse))
                .andExpect(jsonPath("$.anschrift.stadt").value("Berlin"))
                .andExpect(jsonPath("$.istLieferadresse").value(false))
                .andExpect(jsonPath("$.istRechnungsadresse").value(true));
    }

    @Test
    @DisplayName("US-01.3 AK2: letzter Lieferadresse das Kennzeichen entziehen ergibt 422")
    void letzteLieferadresseBehaeltKennzeichen() throws Exception {
        String kunde = registriere("anna@example.de");
        String einzige = ersteAdresse(kunde);

        mockMvc.perform(
                        json(put(ADRESSE, kunde, einzige))
                                .content(adresseJson(BERLIN, false, true)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Fachlich abgelehnt"));
        mockMvc.perform(get(ADRESSE, kunde, einzige))
                .andExpect(jsonPath("$.istLieferadresse").value(true));
    }

    @Test
    @DisplayName("US-01.3 AK3: Adresse ohne Kennzeichen speichern ergibt 400 (I-2)")
    void aendernOhneKennzeichen() throws Exception {
        String kunde = registriere("anna@example.de");
        String adresse = fuegeHinzu(kunde, WIEN, true, false);

        mockMvc.perform(json(put(ADRESSE, kunde, adresse)).content(adresseJson(WIEN, false, false)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("US-01.3 AK4: eine von zwei Lieferadressen löschen ergibt 204")
    void loeschen() throws Exception {
        String kunde = registriere("anna@example.de");
        String erste = ersteAdresse(kunde);
        String zweite = fuegeHinzu(kunde, WIEN, true, false);

        mockMvc.perform(delete(ADRESSE, kunde, erste)).andExpect(status().isNoContent());

        mockMvc.perform(get(KUNDEN + "/{id}", kunde))
                .andExpect(jsonPath("$.adressen.length()").value(1))
                .andExpect(jsonPath("$.adressen[0].id").value(zweite));
    }

    @Test
    @DisplayName("US-01.3 AK5: letzte Lieferadresse löschen ergibt 422")
    void letzteLieferadresseNichtLoeschbar() throws Exception {
        String kunde = registriere("anna@example.de");
        String einzige = ersteAdresse(kunde);
        fuegeHinzu(kunde, WIEN, false, true);

        mockMvc.perform(delete(ADRESSE, kunde, einzige))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(
                        jsonPath("$.detail")
                                .value(
                                        "Die letzte Lieferadresse kann nicht gelöscht werden (I-1)."));
        mockMvc.perform(get(KUNDEN + "/{id}", kunde))
                .andExpect(jsonPath("$.adressen.length()").value(2));
    }

    @Test
    @DisplayName("US-01.3 AK6: letzte Rechnungsadresse löschen ergibt 204")
    void letzteRechnungsadresseLoeschbar() throws Exception {
        String kunde = registriere("anna@example.de");
        String rechnung = fuegeHinzu(kunde, WIEN, false, true);

        mockMvc.perform(delete(ADRESSE, kunde, rechnung)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("US-01.3 AK7: fremde oder unbekannte Adresse ändern oder löschen ergibt 404")
    void fremdeAdresse() throws Exception {
        String anna = registriere("anna@example.de");
        String bernd = registriere("bernd@example.de");
        String adresseVonBernd = fuegeHinzu(bernd, WIEN, true, false);

        mockMvc.perform(delete(ADRESSE, anna, adresseVonBernd)).andExpect(status().isNotFound());
        mockMvc.perform(
                        json(put(ADRESSE, anna, adresseVonBernd))
                                .content(adresseJson(BERLIN, true, false)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get(ADRESSE, anna, adresseVonBernd)).andExpect(status().isNotFound());
        mockMvc.perform(delete(ADRESSE, anna, UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(delete(ADRESSE, UUID.randomUUID(), adresseVonBernd))
                .andExpect(status().isNotFound());
    }

    private String registriere(String email) throws Exception {
        return registriere(email, "Anna", "Muster");
    }

    private String registriere(String email, String vorname, String nachname) throws Exception {
        String inhalt =
                registrierung(email, BERLIN)
                        .replace("\"Anna\"", "\"" + vorname + "\"")
                        .replace("\"Muster\"", "\"" + nachname + "\"");
        ResultActions antwort =
                mockMvc.perform(json(post(KUNDEN)).content(inhalt)).andExpect(status().isCreated());
        String id = JsonPath.read(antwort.andReturn().getResponse().getContentAsString(), "$.id");
        antwort.andExpect(header().string("Location", endsWith(KUNDEN + "/" + id)));
        return id;
    }

    private String fuegeHinzu(
            String kunde, String anschrift, boolean istLieferadresse, boolean istRechnungsadresse)
            throws Exception {
        ResultActions antwort =
                mockMvc.perform(
                                json(post(ADRESSEN, kunde))
                                        .content(
                                                adresseJson(
                                                        anschrift,
                                                        istLieferadresse,
                                                        istRechnungsadresse)))
                        .andExpect(status().isCreated());
        String id = JsonPath.read(antwort.andReturn().getResponse().getContentAsString(), "$.id");
        antwort.andExpect(
                header().string("Location", endsWith(KUNDEN + "/" + kunde + "/adressen/" + id)));
        return id;
    }

    private String ersteAdresse(String kunde) throws Exception {
        String antwort =
                mockMvc.perform(get(KUNDEN + "/{id}", kunde))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return JsonPath.read(antwort, "$.adressen[0].id");
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder anfrage) {
        return anfrage.contentType(MediaType.APPLICATION_JSON);
    }

    private static String registrierung(String email, String lieferadresse) {
        return """
        {"vorname": "Anna", "nachname": "Muster", "email": "%s",
         "lieferadresse": %s, "istAuchRechnungsadresse": false}
        """
                .formatted(email, lieferadresse);
    }

    private static String adresseJson(
            String anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {
        return """
        {"anschrift": %s, "istLieferadresse": %b, "istRechnungsadresse": %b}
        """
                .formatted(anschrift, istLieferadresse, istRechnungsadresse);
    }
}
