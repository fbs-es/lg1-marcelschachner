package fbs.lg1.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import fbs.lg1.web.service.NichtGefundenException;
import fbs.lg1.web.service.SenderService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(SenderController.class)
class SenderControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private SenderService senderService;

    private SenderDto sender;

    @BeforeEach
    void prepare() {
        sender = new SenderDto(1L, "Radio AirControl", 98.5, 2L, LocalDate.of(2026, 11, 9));
    }

    @Test
    void testSenderAnlegen() {
        when(senderService.senderAnlegen(any(SenderRequest.class))).thenReturn(sender);

        String json = "{\"name\":\"Radio AirControl\",\"frequenz\":98.5,\"datum\":\"2026-11-09\"}";

        assertThat(mvc.post().uri("/api/sender").contentType(MediaType.APPLICATION_JSON).content(json))
                .hasStatus(201)
                .bodyJson()
                .isLenientlyEqualTo(
                        "{\"id\":1,\"name\":\"Radio AirControl\",\"frequenz\":98.5,"
                                + "\"programmId\":2,\"datum\":\"2026-11-09\"}");
    }

    @Test
    void testSenderAnlegenInvalid() {
        when(senderService.senderAnlegen(any(SenderRequest.class)))
                .thenThrow(new IllegalArgumentException("Name darf nicht leer sein!"));

        String json = "{\"name\":\" \",\"frequenz\":98.5,\"datum\":\"2026-11-09\"}";

        assertThat(mvc.post().uri("/api/sender").contentType(MediaType.APPLICATION_JSON).content(json))
                .hasStatus(400)
                .bodyJson()
                .isLenientlyEqualTo("{\"status\":400,\"message\":\"Name darf nicht leer sein!\"}");
    }

    @Test
    void testSenderAuflisten() {
        when(senderService.senderAuflisten()).thenReturn(List.of(sender));

        assertThat(mvc.get().uri("/api/sender"))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Radio AirControl");
    }

    @Test
    void testSenderLesenNichtGefunden() {
        when(senderService.senderLesen(9L))
                .thenThrow(new NichtGefundenException("Sender wurde nicht gefunden!"));

        assertThat(mvc.get().uri("/api/sender/9"))
                .hasStatus(404)
                .bodyJson()
                .extractingPath("$.message")
                .isEqualTo("Sender wurde nicht gefunden!");
    }

    @Test
    void testSenderLesenFalscheId() {
        assertThat(mvc.get().uri("/api/sender/abc")).hasStatus(400);
    }
}
