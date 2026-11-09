package fbs.lg1.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fbs.lg1.Musikgenre;
import fbs.lg1.web.dto.BeitragDto;
import fbs.lg1.web.dto.EilmeldungEinfuegenRequest;
import fbs.lg1.web.dto.EilmeldungErgebnisDto;
import fbs.lg1.web.dto.ModerationRequest;
import fbs.lg1.web.dto.MusikbeitragRequest;
import fbs.lg1.web.dto.SendeplanDto;
import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.dto.SendungRequest;
import fbs.lg1.web.dto.WerbebeitragRequest;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SendungServiceTest {

    @Autowired
    private SenderService senderService;

    @Autowired
    private ProgrammService programmService;

    @Autowired
    private SendungService sendungService;

    @Autowired
    private EntityManager entityManager;

    private Long sendungId;
    private ModerationRequest moderation;
    private MusikbeitragRequest musik;
    private WerbebeitragRequest werbung;

    @BeforeEach
    void prepare() {
        SenderRequest senderRequest =
                new SenderRequest("Radio AirControl", 98.5, LocalDate.of(2026, 11, 9));
        SenderDto sender = senderService.senderAnlegen(senderRequest);
        SendungRequest sendungRequest =
                new SendungRequest(
                        "Morgenshow", "Marcel Schachner", LocalTime.of(8, 0), LocalTime.of(9, 0), 600);
        SendungDto sendung = programmService.sendungAnlegen(sender.programmId(), sendungRequest);
        sendungId = sendung.id();
        moderation = new ModerationRequest("Begrüßung", 60, "Guten Morgen!");
        musik = new MusikbeitragRequest("Wolkenlos", 240, "Die Wolken", "Himmelblau", Musikgenre.POP);
        werbung = new WerbebeitragRequest("Kaffee-Spot", 330, "Bohnenglück GmbH", 5000.0);
    }

    @Test
    void testBeitragHinzufuegen() {
        sendungService.beitragHinzufuegen(sendungId, moderation);
        sendungService.beitragHinzufuegen(sendungId, musik);

        SendeplanDto plan = sendungService.sendeplanLesen(sendungId);

        assertThat(plan.beitraege()).hasSize(2);
        assertThat(plan.beitraege().get(0).typ()).isEqualTo("MODERATION");
        assertThat(plan.beitraege().get(1).typ()).isEqualTo("MUSIK");
        assertThat(plan.beitraege().get(1).startzeitpunkt()).isEqualTo(60);
        assertThat(plan.beitraege().get(1).genre()).isEqualTo(Musikgenre.POP);
        assertThat(plan.gesamtdauer()).isEqualTo(300);
        assertThat(plan.restzeit()).isEqualTo(300);
    }

    @Test
    void testBeitragHinzufuegenZuLang() {
        sendungService.beitragHinzufuegen(sendungId, werbung);
        sendungService.beitragHinzufuegen(sendungId, musik);

        assertThatThrownBy(() -> sendungService.beitragHinzufuegen(sendungId, moderation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Beitrag überschreitet die Maximaldauer der Sendung!");
    }

    @Test
    void testBeitragHinzufuegenInvalid() {
        ModerationRequest ohneText = new ModerationRequest("Begrüßung", 60, " ");

        assertThatThrownBy(() -> sendungService.beitragHinzufuegen(sendungId, ohneText))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendungService.beitragHinzufuegen(999L, moderation))
                .isInstanceOf(NichtGefundenException.class);
    }

    @Test
    void testBeitragKuerzen() {
        sendungService.beitragHinzufuegen(sendungId, musik);
        SendeplanDto vorher = sendungService.beitragHinzufuegen(sendungId, moderation);
        Long musikId = vorher.beitraege().get(0).id();

        SendeplanDto plan = sendungService.beitragKuerzen(sendungId, musikId, 180);

        assertThat(plan.beitraege().get(0).dauer()).isEqualTo(180);
        assertThat(plan.beitraege().get(1).startzeitpunkt()).isEqualTo(180);
        assertThat(plan.gesamtdauer()).isEqualTo(240);
    }

    @Test
    void testBeitragKuerzenInvalid() {
        SendeplanDto vorher = sendungService.beitragHinzufuegen(sendungId, musik);
        Long musikId = vorher.beitraege().get(0).id();

        assertThatThrownBy(() -> sendungService.beitragKuerzen(sendungId, musikId, 300))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendungService.beitragKuerzen(sendungId, 999L, 100))
                .isInstanceOf(NichtGefundenException.class);
    }

    @Test
    void testBeitragLoeschen() {
        sendungService.beitragHinzufuegen(sendungId, moderation);
        SendeplanDto vorher = sendungService.beitragHinzufuegen(sendungId, musik);
        Long moderationId = vorher.beitraege().get(0).id();

        SendeplanDto plan = sendungService.beitragLoeschen(sendungId, moderationId);

        assertThat(plan.beitraege()).hasSize(1);
        assertThat(plan.beitraege().get(0).titel()).isEqualTo("Wolkenlos");
        assertThat(plan.beitraege().get(0).startzeitpunkt()).isEqualTo(0);
    }

    @Test
    void testBeitragVerschieben() {
        sendungService.beitragHinzufuegen(sendungId, moderation);
        SendeplanDto vorher = sendungService.beitragHinzufuegen(sendungId, musik);
        Long musikId = vorher.beitraege().get(1).id();

        sendungService.beitragVerschieben(sendungId, musikId, 0);
        entityManager.flush();
        entityManager.clear();
        SendeplanDto plan = sendungService.sendeplanLesen(sendungId);

        assertThat(plan.beitraege().get(0).id()).isEqualTo(musikId);
        assertThat(plan.beitraege().get(1).titel()).isEqualTo("Begrüßung");
        assertThat(plan.beitraege().get(1).startzeitpunkt()).isEqualTo(240);
    }

    @Test
    void testBeitragVerschiebenInvalid() {
        SendeplanDto vorher = sendungService.beitragHinzufuegen(sendungId, moderation);
        Long moderationId = vorher.beitraege().get(0).id();

        assertThatThrownBy(() -> sendungService.beitragVerschieben(sendungId, moderationId, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEilmeldungEinfuegenSplitting() {
        sendungService.beitragHinzufuegen(sendungId, moderation);
        sendungService.beitragHinzufuegen(sendungId, musik);
        EilmeldungEinfuegenRequest request =
                new EilmeldungEinfuegenRequest("Sturmwarnung", 45, 4, "Tirol", 160);

        EilmeldungErgebnisDto ergebnis = sendungService.eilmeldungEinfuegen(sendungId, request);

        SendeplanDto plan = ergebnis.sendeplan();
        assertThat(ergebnis.warnung()).isNull();
        assertThat(plan.beitraege()).hasSize(4);
        BeitragDto teil1 = plan.beitraege().get(1);
        BeitragDto eilmeldung = plan.beitraege().get(2);
        BeitragDto fortsetzung = plan.beitraege().get(3);
        assertThat(teil1.dauer()).isEqualTo(100);
        assertThat(eilmeldung.typ()).isEqualTo("EILMELDUNG");
        assertThat(eilmeldung.startzeitpunkt()).isEqualTo(160);
        assertThat(fortsetzung.titel()).isEqualTo("Wolkenlos (Fortsetzung)");
        assertThat(fortsetzung.dauer()).isEqualTo(140);
        assertThat(fortsetzung.startzeitpunkt()).isEqualTo(205);
        assertThat(fortsetzung.id()).isNotNull();
        assertThat(plan.gesamtdauer()).isEqualTo(345);
    }

    @Test
    void testEilmeldungEinfuegenZeitlimitUeberschritten() {
        sendungService.beitragHinzufuegen(sendungId, werbung);
        sendungService.beitragHinzufuegen(sendungId, musik);
        EilmeldungEinfuegenRequest request =
                new EilmeldungEinfuegenRequest("Sturmwarnung", 45, 4, "Tirol", 0);

        EilmeldungErgebnisDto ergebnis = sendungService.eilmeldungEinfuegen(sendungId, request);
        entityManager.flush();
        entityManager.clear();
        SendeplanDto plan = sendungService.sendeplanLesen(sendungId);

        assertThat(ergebnis.warnung())
                .isEqualTo(
                        "WARNUNG an Marcel Schachner: Sendung 'Morgenshow' überschreitet durch die"
                                + " Eilmeldung ihr Zeitlimit um 15 Sekunden!");
        assertThat(plan.zeitlimitUeberschritten()).isTrue();
        assertThat(plan.restzeit()).isEqualTo(-15);
        assertThat(plan.beitraege().get(0).typ()).isEqualTo("EILMELDUNG");
    }

    @Test
    void testEilmeldungEinfuegenInvalid() {
        EilmeldungEinfuegenRequest negativ =
                new EilmeldungEinfuegenRequest("Sturmwarnung", 45, 4, "Tirol", -1);
        EilmeldungEinfuegenRequest stufe =
                new EilmeldungEinfuegenRequest("Sturmwarnung", 45, 6, "Tirol", 0);

        assertThatThrownBy(() -> sendungService.eilmeldungEinfuegen(sendungId, negativ))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sendungService.eilmeldungEinfuegen(sendungId, stufe))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
