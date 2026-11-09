package fbs.lg1.web.service;

import fbs.lg1.Radioprogramm;
import fbs.lg1.Radiosender;
import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import fbs.lg1.web.entity.RadioprogrammEntity;
import fbs.lg1.web.entity.RadiosenderEntity;
import fbs.lg1.web.repository.RadiosenderRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Legt Radiosender mit ihrem Programm an und liest sie. Die Werte prüft die Domäne.
 */
@Service
public class SenderService {

    private final RadiosenderRepository senderRepository;

    /**
     * Legt den Service an.
     *
     * @param senderRepository der Datenbankzugriff auf Sender
     */
    public SenderService(RadiosenderRepository senderRepository) {
        this.senderRepository = senderRepository;
    }

    /**
     * Legt einen Sender mit einem leeren Programm für den angegebenen Tag an.
     *
     * @param request Name, Frequenz und Datum
     * @return der gespeicherte Sender
     * @throws IllegalArgumentException wenn die Domäne die Werte ablehnt
     */
    @Transactional
    public SenderDto senderAnlegen(SenderRequest request) {
        Radioprogramm programm = new Radioprogramm(request.datum());
        Radiosender sender = new Radiosender(request.name(), request.frequenz(), programm);

        RadioprogrammEntity programmEntity = new RadioprogrammEntity(programm.showDatum());
        RadiosenderEntity senderEntity =
                new RadiosenderEntity(sender.showName(), sender.showFrequenz(), programmEntity);
        RadiosenderEntity gespeichert = senderRepository.save(senderEntity);
        return zuDto(gespeichert);
    }

    /**
     * @return alle gespeicherten Sender
     */
    @Transactional(readOnly = true)
    public List<SenderDto> senderAuflisten() {
        List<SenderDto> ergebnis = new ArrayList<>();
        for (RadiosenderEntity sender : senderRepository.findAll()) {
            SenderDto dto = zuDto(sender);
            ergebnis.add(dto);
        }
        return ergebnis;
    }

    /**
     * Liest einen Sender.
     *
     * @param id die ID des Senders
     * @return der Sender
     * @throws NichtGefundenException wenn es den Sender nicht gibt
     */
    @Transactional(readOnly = true)
    public SenderDto senderLesen(Long id) {
        RadiosenderEntity sender = senderRepository.findOneById(id);
        if (sender == null) {
            throw new NichtGefundenException("Sender wurde nicht gefunden!");
        }
        return zuDto(sender);
    }

    private SenderDto zuDto(RadiosenderEntity sender) {
        RadioprogrammEntity programm = sender.showProgramm();
        return new SenderDto(
                sender.showId(),
                sender.showName(),
                sender.showFrequenz(),
                programm.showId(),
                programm.showDatum());
    }
}
