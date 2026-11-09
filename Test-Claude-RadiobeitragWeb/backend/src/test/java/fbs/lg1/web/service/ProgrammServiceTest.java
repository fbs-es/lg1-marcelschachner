package fbs.lg1.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fbs.lg1.web.dto.ProgrammDto;
import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.dto.SendungRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProgrammServiceTest {

    @Autowired
    private SenderService senderService;

    @Autowired
    private ProgrammService programmService;

    private Long programmId;
    private SendungRequest morgenshow;

    @BeforeEach
    void prepare() {
        SenderRequest request = new SenderRequest("Radio AirControl", 98.5, LocalDate.of(2026, 11, 9));
        SenderDto sender = senderService.senderAnlegen(request);
        programmId = sender.programmId();
        morgenshow =
                new SendungRequest(
                        "Morgenshow", "Marcel Schachner", LocalTime.of(8, 0), LocalTime.of(9, 0), 900);
    }

    @Test
    void testSendungAnlegen() {
        SendungDto sendung = programmService.sendungAnlegen(programmId, morgenshow);

        assertThat(sendung.id()).isNotNull();
        assertThat(sendung.name()).isEqualTo("Morgenshow");
        assertThat(sendung.moderator()).isEqualTo("Marcel Schachner");
        assertThat(sendung.gesamtdauer()).isEqualTo(0);
        assertThat(sendung.restzeit()).isEqualTo(900);
        assertThat(sendung.zeitlimitUeberschritten()).isFalse();
    }

    @Test
    void testSendungAnlegenUeberschneidung() {
        programmService.sendungAnlegen(programmId, morgenshow);
        SendungRequest ueberschneidend =
                new SendungRequest(
                        "Mittagsshow", "Max Mustermann", LocalTime.of(8, 30), LocalTime.of(10, 0), 900);

        assertThatThrownBy(() -> programmService.sendungAnlegen(programmId, ueberschneidend))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Zeitblock überschneidet sich mit einer anderen Sendung!");
    }

    @Test
    void testSendungAnlegenInvalid() {
        SendungRequest zuLang =
                new SendungRequest(
                        "Morgenshow", "Marcel Schachner", LocalTime.of(8, 0), LocalTime.of(9, 0), 3601);

        assertThatThrownBy(() -> programmService.sendungAnlegen(programmId, zuLang))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> programmService.sendungAnlegen(999L, morgenshow))
                .isInstanceOf(NichtGefundenException.class);
    }

    @Test
    void testProgrammLesenSortiert() {
        SendungRequest abendshow =
                new SendungRequest(
                        "Abendshow", "Max Mustermann", LocalTime.of(18, 0), LocalTime.of(19, 0), 900);
        programmService.sendungAnlegen(programmId, abendshow);
        programmService.sendungAnlegen(programmId, morgenshow);

        ProgrammDto programm = programmService.programmLesen(programmId);
        List<SendungDto> sendungen = programmService.sendungenAuflisten(programmId);

        assertThat(programm.datum()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(programm.sendungen()).hasSize(2);
        assertThat(sendungen).hasSize(2);
        assertThat(sendungen.get(0).name()).isEqualTo("Morgenshow");
        assertThat(sendungen.get(1).name()).isEqualTo("Abendshow");
    }

    @Test
    void testProgrammLesenNichtGefunden() {
        assertThatThrownBy(() -> programmService.programmLesen(999L))
                .isInstanceOf(NichtGefundenException.class);
    }
}
