package fbs.lg1.web.service;

import fbs.lg1.Eilmeldung;
import fbs.lg1.Radiobeitrag;
import fbs.lg1.Radiosendung;
import fbs.lg1.web.dto.BeitragRequest;
import fbs.lg1.web.dto.EilmeldungEinfuegenRequest;
import fbs.lg1.web.dto.EilmeldungErgebnisDto;
import fbs.lg1.web.dto.SendeplanDto;
import fbs.lg1.web.entity.RadiobeitragEntity;
import fbs.lg1.web.entity.RadiosendungEntity;
import fbs.lg1.web.mapper.DomainMapper;
import fbs.lg1.web.repository.RadiosendungRepository;
import java.util.IdentityHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bearbeitet den Sendeplan einer Sendung. Jede Änderung läuft so ab: Entity laden, Domänenobjekt
 * aufbauen, Domänenmethode aufrufen, Ergebnis zurückschreiben und speichern. Die Business-Regeln
 * stecken nur in {@link Radiosendung}.
 */
@Service
public class SendungService {

    private final RadiosendungRepository sendungRepository;
    private final DomainMapper mapper;

    /**
     * Legt den Service an.
     *
     * @param sendungRepository der Datenbankzugriff auf Sendungen
     * @param mapper der Mapper zwischen Entity und Domäne
     */
    public SendungService(RadiosendungRepository sendungRepository, DomainMapper mapper) {
        this.sendungRepository = sendungRepository;
        this.mapper = mapper;
    }

    /**
     * Liest den Sendeplan einer Sendung mit Startzeitpunkten und Zeitwerten.
     *
     * @param sendungId die ID der Sendung
     * @return der Sendeplan
     * @throws NichtGefundenException wenn es die Sendung nicht gibt
     */
    @Transactional(readOnly = true)
    public SendeplanDto sendeplanLesen(Long sendungId) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        return mapper.zuSendeplanDto(sendung, entity);
    }

    /**
     * Hängt einen neuen Beitrag an das Ende des Sendeplans an.
     *
     * @param sendungId die ID der Sendung
     * @param request die Daten des Beitrags mit Typ
     * @return der neue Sendeplan
     * @throws NichtGefundenException wenn es die Sendung nicht gibt
     * @throws IllegalArgumentException wenn die Domäne die Werte ablehnt
     * @throws IllegalStateException wenn die Maximaldauer überschritten würde
     */
    @Transactional
    public SendeplanDto beitragHinzufuegen(Long sendungId, BeitragRequest request) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        Radiobeitrag beitrag = request.erstelleBeitrag();

        sendung.beitragHinzufuegen(beitrag);

        return speichern(sendung, entity, zuordnung);
    }

    /**
     * Kürzt einen Beitrag auf eine neue Dauer.
     *
     * @param sendungId die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @param neueDauer die neue Dauer in Sekunden
     * @return der neue Sendeplan
     * @throws NichtGefundenException wenn es die Sendung oder den Beitrag nicht gibt
     * @throws IllegalArgumentException wenn die neue Dauer nicht kürzer ist
     */
    @Transactional
    public SendeplanDto beitragKuerzen(Long sendungId, Long beitragId, int neueDauer) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        Radiobeitrag beitrag = findBeitrag(zuordnung, beitragId);

        sendung.beitragKuerzen(beitrag, neueDauer);

        return speichern(sendung, entity, zuordnung);
    }

    /**
     * Löscht einen Beitrag aus dem Sendeplan.
     *
     * @param sendungId die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @return der neue Sendeplan
     * @throws NichtGefundenException wenn es die Sendung oder den Beitrag nicht gibt
     */
    @Transactional
    public SendeplanDto beitragLoeschen(Long sendungId, Long beitragId) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        Radiobeitrag beitrag = findBeitrag(zuordnung, beitragId);

        sendung.beitragLoeschen(beitrag);

        return speichern(sendung, entity, zuordnung);
    }

    /**
     * Verschiebt einen Beitrag an eine neue Position.
     *
     * @param sendungId die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @param neuerIndex die neue Position, beginnend bei 0
     * @return der neue Sendeplan
     * @throws NichtGefundenException wenn es die Sendung oder den Beitrag nicht gibt
     * @throws IllegalArgumentException wenn der Index außerhalb des Sendeplans liegt
     */
    @Transactional
    public SendeplanDto beitragVerschieben(Long sendungId, Long beitragId, int neuerIndex) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        Radiobeitrag beitrag = findBeitrag(zuordnung, beitragId);

        sendung.beitragVerschieben(beitrag, neuerIndex);

        return speichern(sendung, entity, zuordnung);
    }

    /**
     * Fügt eine Eilmeldung an der aktuellen Laufstelle ein. Liegt die Laufstelle mitten in einem
     * Beitrag, teilt die Domäne diesen Beitrag. Überschreitet die Sendung danach ihr Zeitlimit,
     * enthält das Ergebnis eine Warnung an den Moderator.
     *
     * @param sendungId die ID der Sendung
     * @param request die Daten der Eilmeldung mit Laufzeit
     * @return der neue Sendeplan und die Warnung oder null
     * @throws NichtGefundenException wenn es die Sendung nicht gibt
     * @throws IllegalArgumentException wenn die Domäne die Werte ablehnt
     */
    @Transactional
    public EilmeldungErgebnisDto eilmeldungEinfuegen(
            Long sendungId, EilmeldungEinfuegenRequest request) {
        RadiosendungEntity entity = findSendung(sendungId);
        Map<Radiobeitrag, RadiobeitragEntity> zuordnung = new IdentityHashMap<>();
        Radiosendung sendung = mapper.zuSendung(entity, zuordnung);
        Eilmeldung eilmeldung =
                new Eilmeldung(
                        request.titel(),
                        request.dauer(),
                        request.dringlichkeitsstufe(),
                        request.region());

        sendung.eilmeldungEinfuegen(eilmeldung, request.laufzeit());

        SendeplanDto sendeplan = speichern(sendung, entity, zuordnung);
        String warnung = null;
        if (sendung.istZeitlimitUeberschritten()) {
            int ueberschreitung = -sendung.berechneRestzeit();
            warnung =
                    "WARNUNG an "
                            + sendung.showModerator()
                            + ": Sendung '"
                            + sendung.showName()
                            + "' überschreitet durch die Eilmeldung ihr Zeitlimit um "
                            + ueberschreitung
                            + " Sekunden!";
        }
        return new EilmeldungErgebnisDto(sendeplan, warnung);
    }

    private SendeplanDto speichern(
            Radiosendung sendung,
            RadiosendungEntity entity,
            Map<Radiobeitrag, RadiobeitragEntity> zuordnung) {
        mapper.sendeplanZurueckschreiben(sendung, entity, zuordnung);
        sendungRepository.flush();
        return mapper.zuSendeplanDto(sendung, entity);
    }

    private RadiosendungEntity findSendung(Long sendungId) {
        RadiosendungEntity entity = sendungRepository.findOneById(sendungId);
        if (entity == null) {
            throw new NichtGefundenException("Sendung wurde nicht gefunden!");
        }
        return entity;
    }

    private Radiobeitrag findBeitrag(Map<Radiobeitrag, RadiobeitragEntity> zuordnung, Long beitragId) {
        for (Map.Entry<Radiobeitrag, RadiobeitragEntity> eintrag : zuordnung.entrySet()) {
            if (eintrag.getValue().showId().equals(beitragId)) {
                return eintrag.getKey();
            }
        }
        throw new NichtGefundenException("Beitrag wurde nicht gefunden!");
    }
}
