package fbs.lg1.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@SpringBootTest(
        properties =
                "spring.datasource.url=jdbc:h2:mem:ablauf;MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AblaufIntegrationTest {

    @Autowired
    private MockMvcTester mvc;

    private int sendungId;

    @BeforeEach
    void prepare() throws UnsupportedEncodingException {
        String sender =
                post(
                        "/api/sender",
                        "{\"name\":\"Radio AirControl\",\"frequenz\":98.5,\"datum\":\"2026-11-09\"}");
        int programmId = JsonPath.read(sender, "$.programmId");
        String sendung =
                post(
                        "/api/programme/" + programmId + "/sendungen",
                        "{\"name\":\"Morgenshow\",\"moderator\":\"Marcel Schachner\","
                                + "\"startzeit\":\"08:00\",\"endzeit\":\"09:00\",\"maximaldauer\":600}");
        sendungId = JsonPath.read(sendung, "$.id");
    }

    @Test
    void testAblaufEilmeldungMitSplit() throws UnsupportedEncodingException {
        String basis = "/api/sendungen/" + sendungId;
        post(
                basis + "/beitraege",
                "{\"typ\":\"MODERATION\",\"titel\":\"Begrüßung\",\"dauer\":60,"
                        + "\"sprechertext\":\"Guten Morgen!\"}");
        post(
                basis + "/beitraege",
                "{\"typ\":\"MUSIK\",\"titel\":\"Wolkenlos\",\"dauer\":240,"
                        + "\"kuenstler\":\"Die Wolken\",\"album\":\"Himmelblau\",\"genre\":\"POP\"}");
        post(
                basis + "/beitraege",
                "{\"typ\":\"WERBUNG\",\"titel\":\"Kaffee-Spot\",\"dauer\":290,"
                        + "\"kunde\":\"Bohnenglück GmbH\",\"werbebudget\":5000.0}");

        String ergebnis =
                post(
                        basis + "/eilmeldung",
                        "{\"titel\":\"Sturmwarnung\",\"dauer\":45,\"dringlichkeitsstufe\":4,"
                                + "\"region\":\"Tirol\",\"laufzeit\":160}");
        MvcTestResult planResult = mvc.get().uri(basis).exchange();
        String plan = planResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        String warnung = JsonPath.read(ergebnis, "$.warnung");
        List<String> titel = JsonPath.read(plan, "$.beitraege[*].titel");
        List<Integer> startzeitpunkte = JsonPath.read(plan, "$.beitraege[*].startzeitpunkt");
        List<Integer> dauern = JsonPath.read(plan, "$.beitraege[*].dauer");
        List<String> typen = JsonPath.read(plan, "$.beitraege[*].typ");
        int gesamtdauer = JsonPath.read(plan, "$.gesamtdauer");
        int restzeit = JsonPath.read(plan, "$.restzeit");
        boolean ueberschritten = JsonPath.read(plan, "$.zeitlimitUeberschritten");
        assertThat(planResult).hasStatus(200);
        assertThat(warnung)
                .isEqualTo(
                        "WARNUNG an Marcel Schachner: Sendung 'Morgenshow' überschreitet durch die"
                                + " Eilmeldung ihr Zeitlimit um 35 Sekunden!");
        assertThat(titel)
                .containsExactly(
                        "Begrüßung",
                        "Wolkenlos",
                        "Sturmwarnung",
                        "Wolkenlos (Fortsetzung)",
                        "Kaffee-Spot");
        assertThat(typen).containsExactly("MODERATION", "MUSIK", "EILMELDUNG", "MUSIK", "WERBUNG");
        assertThat(startzeitpunkte).containsExactly(0, 60, 160, 205, 345);
        assertThat(dauern).containsExactly(60, 100, 45, 140, 290);
        assertThat(gesamtdauer).isEqualTo(635);
        assertThat(restzeit).isEqualTo(-35);
        assertThat(ueberschritten).isTrue();
    }

    @Test
    void testAblaufUeberLimitWeiterBearbeiten() throws UnsupportedEncodingException {
        String basis = "/api/sendungen/" + sendungId;
        post(
                basis + "/beitraege",
                "{\"typ\":\"WERBUNG\",\"titel\":\"Kaffee-Spot\",\"dauer\":600,"
                        + "\"kunde\":\"Bohnenglück GmbH\",\"werbebudget\":5000.0}");
        post(
                basis + "/eilmeldung",
                "{\"titel\":\"Sturmwarnung\",\"dauer\":45,\"dringlichkeitsstufe\":4,"
                        + "\"region\":\"Tirol\",\"laufzeit\":0}");
        MvcTestResult planResult = mvc.get().uri(basis).exchange();
        String plan = planResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        int werbungId = JsonPath.read(plan, "$.beitraege[1].id");

        MvcTestResult gekuerzt =
                mvc.post()
                        .uri(basis + "/beitraege/" + werbungId + "/kuerzen")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"neueDauer\":555}")
                        .exchange();
        MvcTestResult zuLang =
                mvc.post()
                        .uri(basis + "/beitraege")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"typ\":\"MODERATION\",\"titel\":\"Begrüßung\",\"dauer\":1,"
                                        + "\"sprechertext\":\"Guten Morgen!\"}")
                        .exchange();

        assertThat(gekuerzt).hasStatus(200).bodyJson().extractingPath("$.restzeit").isEqualTo(0);
        assertThat(zuLang).hasStatus(409);
    }

    private String post(String uri, String json) throws UnsupportedEncodingException {
        MvcTestResult result =
                mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
        assertThat(result).hasStatus2xxSuccessful();
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
