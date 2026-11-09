package fbs.lg1.web.service;

import fbs.lg1.Radiobeitrag;
import fbs.lg1.Radioprogramm;
import fbs.lg1.Radiosendung;
import fbs.lg1.web.dto.ProgrammDto;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.dto.SendungRequest;
import fbs.lg1.web.entity.RadiobeitragEntity;
import fbs.lg1.web.entity.RadioprogrammEntity;
import fbs.lg1.web.entity.RadiosendungEntity;
import fbs.lg1.web.mapper.DomainMapper;
import fbs.lg1.web.repository.RadioprogrammRepository;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Liest Tagesprogramme und nimmt neue Sendungen auf. Die Prüfung auf Überschneidungen übernimmt
 * {@link Radioprogramm#sendungHinzufuegen}.
 */
@Service
public class ProgrammService {

    private final RadioprogrammRepository programmRepository;
    private final DomainMapper mapper;

    /**
     * Legt den Service an.
     *
     * @param programmRepository der Datenbankzugriff auf Programme
     * @param mapper der Mapper zwischen Entity und Domäne
     */
    public ProgrammService(RadioprogrammRepository programmRepository, DomainMapper mapper) {
        this.programmRepository = programmRepository;
        this.mapper = mapper;
    }

    /**
     * Liest ein Programm mit allen Sendungen.
     *
     * @param id die ID des Programms
     * @return das Programm
     * @throws NichtGefundenException wenn es das Programm nicht gibt
     */
    @Transactional(readOnly = true)
    public ProgrammDto programmLesen(Long id) {
        RadioprogrammEntity programm = findProgramm(id);
        List<SendungDto> sendungen = sendungenZuDtos(programm);
        return new ProgrammDto(programm.showId(), programm.showDatum(), sendungen);
    }

    /**
     * Listet alle Sendungen eines Programms nach Startzeit sortiert auf.
     *
     * @param id die ID des Programms
     * @return die Sendungen
     * @throws NichtGefundenException wenn es das Programm nicht gibt
     */
    @Transactional(readOnly = true)
    public List<SendungDto> sendungenAuflisten(Long id) {
        RadioprogrammEntity programm = findProgramm(id);
        return sendungenZuDtos(programm);
    }

    /**
     * Nimmt eine neue Sendung in das Programm auf.
     *
     * @param id die ID des Programms
     * @param request die Daten der Sendung
     * @return die gespeicherte Sendung
     * @throws NichtGefundenException wenn es das Programm nicht gibt
     * @throws IllegalArgumentException wenn die Domäne die Werte ablehnt
     * @throws IllegalStateException wenn sich der Zeitblock mit einer anderen Sendung überschneidet
     */
    @Transactional
    public SendungDto sendungAnlegen(Long id, SendungRequest request) {
        RadioprogrammEntity programmEntity = findProgramm(id);
        Radioprogramm programm = mapper.zuProgramm(programmEntity);
        Radiosendung sendung =
                new Radiosendung(
                        request.name(),
                        request.moderator(),
                        request.startzeit(),
                        request.endzeit(),
                        request.maximaldauer());

        programm.sendungHinzufuegen(sendung);

        RadiosendungEntity sendungEntity = mapper.zuSendungEntity(sendung, programmEntity);
        int index = programm.showSendungen().indexOf(sendung);
        programmEntity.sendungAufnehmen(index, sendungEntity);
        programmRepository.flush();
        return mapper.zuSendungDto(sendung, sendungEntity.showId());
    }

    private List<SendungDto> sendungenZuDtos(RadioprogrammEntity programm) {
        List<SendungDto> ergebnis = new ArrayList<>();
        for (RadiosendungEntity sendungEntity : programm.showSendungen()) {
            Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
            Radiosendung sendung = mapper.zuSendung(sendungEntity, zuordnung);
            SendungDto dto = mapper.zuSendungDto(sendung, sendungEntity.showId());
            ergebnis.add(dto);
        }
        return ergebnis;
    }

    private RadioprogrammEntity findProgramm(Long id) {
        RadioprogrammEntity programm = programmRepository.findOneById(id);
        if (programm == null) {
            throw new NichtGefundenException("Programm wurde nicht gefunden!");
        }
        return programm;
    }
}
