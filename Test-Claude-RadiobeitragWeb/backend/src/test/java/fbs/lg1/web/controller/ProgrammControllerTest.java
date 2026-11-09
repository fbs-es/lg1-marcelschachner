package fbs.lg1.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fbs.lg1.web.dto.ProgrammDto;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.dto.SendungRequest;
import fbs.lg1.web.service.ProgrammService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(ProgrammController.class)
class ProgrammControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private ProgrammService programmService;

    private SendungDto sendung;
    private String sendungJson;

    @BeforeEach
    void prepare() {
        sendung =
                new SendungDto(
                        5L,
                        "Morgenshow",
                        "Marcel Schachner",
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 0),
                        900,
                        0,
                        900,
                        false);
        sendungJson =
                "{\"name\":\"Morgenshow\",\"moderator\":\"Marcel Schachner\","
                        + "\"startzeit\":\"08:00\",\"endzeit\":\"09:00\",\"maximaldauer\":900}";
    }

    @Test
    void testProgrammLesen() {
        ProgrammDto programm = new ProgrammDto(1L, LocalDate.of(2026, 11, 9), List.of(sendung));
        when(programmService.programmLesen(1L)).thenReturn(programm);

        assertThat(mvc.get().uri("/api/programme/1"))
                .hasStatus(200)
                .bodyJson()
                .isLenientlyEqualTo(
                        "{\"id\":1,\"datum\":\"2026-11-09\",\"sendungen\":[{\"id\":5,"
                                + "\"startzeit\":\"08:00:00\",\"moderator\":\"Marcel Schachner\"}]}");
    }

    @Test
    void testSendungenAuflisten() {
        when(programmService.sendungenAuflisten(1L)).thenReturn(List.of(sendung));

        assertThat(mvc.get().uri("/api/programme/1/sendungen"))
                .hasStatus(200)
                .bodyJson()
                .extractingPath("$[0].restzeit")
                .isEqualTo(900);
    }

    @Test
    void testSendungAnlegen() {
        when(programmService.sendungAnlegen(eq(1L), any(SendungRequest.class))).thenReturn(sendung);

        assertThat(
                        mvc.post()
                                .uri("/api/programme/1/sendungen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(sendungJson))
                .hasStatus(201)
                .bodyJson()
                .extractingPath("$.id")
                .isEqualTo(5);
    }

    @Test
    void testSendungAnlegenUeberschneidung() {
        when(programmService.sendungAnlegen(eq(1L), any(SendungRequest.class)))
                .thenThrow(
                        new IllegalStateException(
                                "Zeitblock überschneidet sich mit einer anderen Sendung!"));

        assertThat(
                        mvc.post()
                                .uri("/api/programme/1/sendungen")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(sendungJson))
                .hasStatus(409)
                .bodyJson()
                .isLenientlyEqualTo(
                        "{\"status\":409,"
                                + "\"message\":\"Zeitblock überschneidet sich mit einer anderen Sendung!\"}");
    }
}
