package fbs.lg1.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fbs.lg1.web.dto.BeitragDto;
import fbs.lg1.web.dto.BeitragRequest;
import fbs.lg1.web.dto.EilmeldungEinfuegenRequest;
import fbs.lg1.web.dto.EilmeldungErgebnisDto;
import fbs.lg1.web.dto.MusikbeitragRequest;
import fbs.lg1.web.dto.SendeplanDto;
import fbs.lg1.web.service.NichtGefundenException;
import fbs.lg1.web.service.SendungService;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(SendungController.class)
class SendungControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SendungService sendungService;

    private SendeplanDto plan;

    @BeforeEach
    void prepare() {
        BeitragDto moderation =
                new BeitragDto(
                        10L, "MODERATION", "Begrüßung", 60, 0, null, null, null, null, null,
                        "Guten Morgen!", null, null);
        plan =
                new SendeplanDto(
                        3L,
                        "Morgenshow",
                        "Marcel Schachner",
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 0),
                        600,
                        60,
                        540,
                        false,
                        List.of(moderation));
    }

    @Test
    void testSendeplanLesen() {
        when(sendungService.sendeplanLesen(3L)).thenReturn(plan);

        assertThat(mvc.get().uri("/api/sendungen/3"))
                .hasStatus(200)
                .bodyJson()
                .isLenientlyEqualTo(
                        "{\"id\":3,\"gesamtdauer\":60,\"restzeit\":540,"
                                + "\"zeitlimitUeberschritten\":false,\"beitraege\":[{\"id\":10,"
                                + "\"typ\":\"MODERATION\",\"startzeitpunkt\":0,\"dauer\":60}]}");
    }

    @Test
    void testSendeplanLesenNichtGefunden() {
        when(sendungService.sendeplanLesen(9L))
                .thenThrow(new NichtGefundenException("Sendung wurde nicht gefunden!"));

        assertThat(mvc.get().uri("/api/sendungen/9"))
                .hasStatus(404)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sendung wurde nicht gefunden!");
    }

    @Test
    void testBeitragHinzufuegenMusik() {
        when(sendungService.beitragHinzufuegen(eq(3L), any(BeitragRequest.class))).thenReturn(plan);

        String json =
                "{\"typ\":\"MUSIK\",\"titel\":\"Wolkenlos\",\"dauer\":240,"
                        + "\"kuenstler\":\"Die Wolken\",\"album\":\"Himmelblau\",\"genre\":\"POP\"}";

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .hasStatus(201);
        ArgumentCaptor<BeitragRequest> captor = ArgumentCaptor.forClass(BeitragRequest.class);
        verify(sendungService).beitragHinzufuegen(eq(3L), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(MusikbeitragRequest.class);
        assertThat(((MusikbeitragRequest) captor.getValue()).kuenstler()).isEqualTo("Die Wolken");
    }

    @Test
    void testBeitragHinzufuegenUnbekannterTyp() {
        String json = "{\"typ\":\"PODCAST\",\"titel\":\"X\",\"dauer\":10}";

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Anfrage ist ungültig oder unvollständig!");
    }

    @Test
    void testBeitragHinzufuegenZuLang() {
        when(sendungService.beitragHinzufuegen(eq(3L), any(BeitragRequest.class)))
                .thenThrow(
                        new IllegalStateException("Beitrag überschreitet die Maximaldauer der Sendung!"));

        String json =
                "{\"typ\":\"MODERATION\",\"titel\":\"Begrüßung\",\"dauer\":900,"
                        + "\"sprechertext\":\"Guten Morgen!\"}";

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .hasStatus(409)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Beitrag überschreitet die Maximaldauer der Sendung!");
    }

    @Test
    void testBeitragKuerzen() {
        when(sendungService.beitragKuerzen(3L, 10L, 30)).thenReturn(plan);

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege/10/kuerzen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"neueDauer\":30}"))
                .hasStatus(200);
        verify(sendungService).beitragKuerzen(3L, 10L, 30);
    }

    @Test
    void testBeitragKuerzenInvalid() {
        when(sendungService.beitragKuerzen(3L, 10L, 90))
                .thenThrow(
                        new IllegalArgumentException("Neue Dauer muss kürzer als die bisherige sein!"));

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege/10/kuerzen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"neueDauer\":90}"))
                .hasStatus(400)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Neue Dauer muss kürzer als die bisherige sein!");
    }

    @Test
    void testBeitragVerschieben() {
        when(sendungService.beitragVerschieben(3L, 10L, 0)).thenReturn(plan);

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/beitraege/10/verschieben")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"neuerIndex\":0}"))
                .hasStatus(200);
        verify(sendungService).beitragVerschieben(3L, 10L, 0);
    }

    @Test
    void testBeitragLoeschen() {
        when(sendungService.beitragLoeschen(3L, 10L)).thenReturn(plan);

        assertThat(mvc.delete().uri("/api/sendungen/3/beitraege/10")).hasStatus(200);
        verify(sendungService).beitragLoeschen(3L, 10L);
    }

    @Test
    void testEilmeldungEinfuegenWarnung() {
        String warnung =
                "WARNUNG an Marcel Schachner: Sendung 'Morgenshow' überschreitet durch die"
                        + " Eilmeldung ihr Zeitlimit um 15 Sekunden!";
        EilmeldungErgebnisDto ergebnis = new EilmeldungErgebnisDto(plan, warnung);
        when(sendungService.eilmeldungEinfuegen(eq(3L), any(EilmeldungEinfuegenRequest.class)))
                .thenReturn(ergebnis);

        String json =
                "{\"titel\":\"Sturmwarnung\",\"dauer\":45,\"dringlichkeitsstufe\":4,"
                        + "\"region\":\"Tirol\",\"laufzeit\":120}";

        assertThat(
                        mvc.post()
                                .uri("/api/sendungen/3/eilmeldung")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$.warnung")
                .isEqualTo(warnung);
    }
}
