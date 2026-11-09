package fbs.lg1.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import java.time.LocalDate;
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
class SenderServiceTest {

    @Autowired
    private SenderService senderService;

    private SenderRequest request;

    @BeforeEach
    void prepare() {
        request = new SenderRequest("Radio AirControl", 98.5, LocalDate.of(2026, 11, 9));
    }

    @Test
    void testSenderAnlegen() {
        SenderDto sender = senderService.senderAnlegen(request);

        assertThat(sender.id()).isNotNull();
        assertThat(sender.programmId()).isNotNull();
        assertThat(sender.name()).isEqualTo("Radio AirControl");
        assertThat(sender.frequenz()).isEqualTo(98.5);
        assertThat(sender.datum()).isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    void testSenderAnlegenInvalid() {
        SenderRequest ohneName = new SenderRequest(" ", 98.5, LocalDate.of(2026, 11, 9));
        SenderRequest ohneFrequenz = new SenderRequest("Radio AirControl", 0, LocalDate.of(2026, 11, 9));
        SenderRequest ohneDatum = new SenderRequest("Radio AirControl", 98.5, null);

        assertThatThrownBy(() -> senderService.senderAnlegen(ohneName))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> senderService.senderAnlegen(ohneFrequenz))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> senderService.senderAnlegen(ohneDatum))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSenderLesen() {
        SenderDto angelegt = senderService.senderAnlegen(request);

        SenderDto gelesen = senderService.senderLesen(angelegt.id());
        List<SenderDto> alle = senderService.senderAuflisten();

        assertThat(gelesen).isEqualTo(angelegt);
        assertThat(alle).contains(angelegt);
    }

    @Test
    void testSenderLesenNichtGefunden() {
        assertThatThrownBy(() -> senderService.senderLesen(999L))
                .isInstanceOf(NichtGefundenException.class);
    }
}
