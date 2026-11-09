package fbs.lg1.web.mapper;

import fbs.lg1.Eilmeldung;
import fbs.lg1.Moderation;
import fbs.lg1.Musikbeitrag;
import fbs.lg1.Musikgenre;
import fbs.lg1.Radiobeitrag;
import fbs.lg1.Radioprogramm;
import fbs.lg1.Radiosendung;
import fbs.lg1.Werbebeitrag;
import fbs.lg1.web.dto.BeitragDto;
import fbs.lg1.web.dto.SendeplanDto;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.entity.EilmeldungEntity;
import fbs.lg1.web.entity.ModerationEntity;
import fbs.lg1.web.entity.MusikbeitragEntity;
import fbs.lg1.web.entity.RadiobeitragEntity;
import fbs.lg1.web.entity.RadioprogrammEntity;
import fbs.lg1.web.entity.RadiosendungEntity;
import fbs.lg1.web.entity.WerbebeitragEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Übersetzt zwischen den JPA-Entities und den Domänenklassen aus {@code Test-Claude-Radiobeitrag}.
 *
 * <p>Beim Aufbau einer Sendung merkt sich der Mapper in einer Zuordnung, welches Domänenobjekt zu
 * welcher Entity gehört. Nach einer Domänenmethode werden so bekannte Beiträge aktualisiert und
 * neue Beiträge (zum Beispiel Fortsetzungen nach einer Eilmeldung) als neue Entities angelegt.
 */
@Component
public class DomainMapper {

    /**
     * Baut eine Sendung samt Sendeplan aus der Datenbank wieder auf. Die Beiträge werden ohne
     * Prüfung der Maximaldauer wiederhergestellt, weil ein gespeicherter Sendeplan nach einer
     * Eilmeldung über dem Zeitlimit liegen darf.
     *
     * @param entity die gespeicherte Sendung
     * @param zuordnung leere Zuordnung, die mit Domänenobjekt zu Entity befüllt wird
     * @return die Domänen-Sendung mit allen Beiträgen
     */
    public Radiosendung zuSendung(
            RadiosendungEntity entity, Map<Radiobeitrag, RadiobeitragEntity> zuordnung) {
        Radiosendung sendung = zuSendungOhneBeitraege(entity);
        for (RadiobeitragEntity beitragEntity : entity.showBeitraege()) {
            Radiobeitrag beitrag = zuBeitrag(beitragEntity);
            sendung.beitragWiederherstellen(beitrag);
            zuordnung.put(beitrag, beitragEntity);
        }
        return sendung;
    }

    /**
     * Baut ein Programm mit allen Sendungen ohne Sendeplan auf, zum Beispiel für die Prüfung auf
     * Überschneidungen.
     *
     * @param entity das gespeicherte Programm
     * @return das Domänen-Programm
     */
    public Radioprogramm zuProgramm(RadioprogrammEntity entity) {
        Radioprogramm programm = new Radioprogramm(entity.showDatum());
        for (RadiosendungEntity sendungEntity : entity.showSendungen()) {
            Radiosendung sendung = zuSendungOhneBeitraege(sendungEntity);
            programm.sendungHinzufuegen(sendung);
        }
        return programm;
    }

    /**
     * Schreibt den Sendeplan der Domäne in die Entity zurück. Bekannte Beiträge übernehmen ihre
     * Dauer, neue Beiträge werden als neue Entities angelegt, entfernte Beiträge fallen weg.
     *
     * @param sendung die Domänen-Sendung nach der Änderung
     * @param entity die zu aktualisierende Entity
     * @param zuordnung die Zuordnung aus {@link #zuSendung}
     */
    public void sendeplanZurueckschreiben(
            Radiosendung sendung,
            RadiosendungEntity entity,
            Map<Radiobeitrag, RadiobeitragEntity> zuordnung) {
        List<RadiobeitragEntity> neueBeitraege = new ArrayList<>();
        for (Radiobeitrag beitrag : sendung.showBeitraege()) {
            RadiobeitragEntity beitragEntity = zuordnung.get(beitrag);
            if (beitragEntity == null) {
                beitragEntity = zuEntity(beitrag);
                zuordnung.put(beitrag, beitragEntity);
            } else {
                beitragEntity.aendereDauer(beitrag.showDauer());
            }
            neueBeitraege.add(beitragEntity);
        }
        entity.beitraegeErsetzen(neueBeitraege);
    }

    /**
     * Erstellt den Sendeplan als Antwortobjekt. Startzeitpunkte und Zeitwerte stammen aus der
     * Domäne, die IDs aus den Entities.
     *
     * @param sendung die Domänen-Sendung
     * @param entity die gespeicherte Sendung mit gleicher Beitragsreihenfolge
     * @return der Sendeplan als DTO
     */
    public SendeplanDto zuSendeplanDto(Radiosendung sendung, RadiosendungEntity entity) {
        List<BeitragDto> beitraege = new ArrayList<>();
        int index = 0;
        for (Radiobeitrag beitrag : sendung.showBeitraege()) {
            RadiobeitragEntity beitragEntity = entity.showBeitraege().get(index);
            BeitragDto beitragDto = zuBeitragDto(beitrag, beitragEntity.showId());
            beitraege.add(beitragDto);
            index++;
        }
        return new SendeplanDto(
                entity.showId(),
                sendung.showName(),
                sendung.showModerator(),
                sendung.showStartzeit(),
                sendung.showEndzeit(),
                sendung.showMaximaldauer(),
                sendung.berechneGesamtdauer(),
                sendung.berechneRestzeit(),
                sendung.istZeitlimitUeberschritten(),
                beitraege);
    }

    /**
     * Erstellt die Kopfdaten einer Sendung als Antwortobjekt.
     *
     * @param sendung die Domänen-Sendung
     * @param id die Datenbank-ID der Sendung
     * @return die Sendung als DTO
     */
    public SendungDto zuSendungDto(Radiosendung sendung, Long id) {
        return new SendungDto(
                id,
                sendung.showName(),
                sendung.showModerator(),
                sendung.showStartzeit(),
                sendung.showEndzeit(),
                sendung.showMaximaldauer(),
                sendung.berechneGesamtdauer(),
                sendung.berechneRestzeit(),
                sendung.istZeitlimitUeberschritten());
    }

    /**
     * Erstellt aus einer neuen Domänen-Sendung die passende Entity ohne Beiträge.
     *
     * @param sendung die von der Domäne geprüfte Sendung
     * @param programm das Programm, zu dem die Sendung gehört
     * @return die neue Entity
     */
    public RadiosendungEntity zuSendungEntity(Radiosendung sendung, RadioprogrammEntity programm) {
        return new RadiosendungEntity(
                programm,
                sendung.showName(),
                sendung.showModerator(),
                sendung.showStartzeit(),
                sendung.showEndzeit(),
                sendung.showMaximaldauer());
    }

    private Radiosendung zuSendungOhneBeitraege(RadiosendungEntity entity) {
        return new Radiosendung(
                entity.showName(),
                entity.showModerator(),
                entity.showStartzeit(),
                entity.showEndzeit(),
                entity.showMaximaldauer());
    }

    private Radiobeitrag zuBeitrag(RadiobeitragEntity entity) {
        if (entity instanceof MusikbeitragEntity) {
            MusikbeitragEntity musik = (MusikbeitragEntity) entity;
            return new Musikbeitrag(
                    musik.showTitel(),
                    musik.showDauer(),
                    musik.showKuenstler(),
                    musik.showAlbum(),
                    musik.showGenre());
        }
        if (entity instanceof WerbebeitragEntity) {
            WerbebeitragEntity werbung = (WerbebeitragEntity) entity;
            return new Werbebeitrag(
                    werbung.showTitel(),
                    werbung.showDauer(),
                    werbung.showKunde(),
                    werbung.showWerbebudget());
        }
        if (entity instanceof ModerationEntity) {
            ModerationEntity moderation = (ModerationEntity) entity;
            return new Moderation(
                    moderation.showTitel(), moderation.showDauer(), moderation.showSprechertext());
        }
        EilmeldungEntity eilmeldung = (EilmeldungEntity) entity;
        return new Eilmeldung(
                eilmeldung.showTitel(),
                eilmeldung.showDauer(),
                eilmeldung.showDringlichkeitsstufe(),
                eilmeldung.showRegion());
    }

    private RadiobeitragEntity zuEntity(Radiobeitrag beitrag) {
        if (beitrag instanceof Musikbeitrag) {
            Musikbeitrag musik = (Musikbeitrag) beitrag;
            return new MusikbeitragEntity(
                    musik.showTitel(),
                    musik.showDauer(),
                    musik.showKuenstler(),
                    musik.showAlbum(),
                    musik.showGenre());
        }
        if (beitrag instanceof Werbebeitrag) {
            Werbebeitrag werbung = (Werbebeitrag) beitrag;
            return new WerbebeitragEntity(
                    werbung.showTitel(),
                    werbung.showDauer(),
                    werbung.showKunde(),
                    werbung.showWerbebudget());
        }
        if (beitrag instanceof Moderation) {
            Moderation moderation = (Moderation) beitrag;
            return new ModerationEntity(
                    moderation.showTitel(), moderation.showDauer(), moderation.showSprechertext());
        }
        Eilmeldung eilmeldung = (Eilmeldung) beitrag;
        return new EilmeldungEntity(
                eilmeldung.showTitel(),
                eilmeldung.showDauer(),
                eilmeldung.showDringlichkeitsstufe(),
                eilmeldung.showRegion());
    }

    private BeitragDto zuBeitragDto(Radiobeitrag beitrag, Long id) {
        String typ = "EILMELDUNG";
        String kuenstler = null;
        String album = null;
        Musikgenre genre = null;
        String kunde = null;
        Double werbebudget = null;
        String sprechertext = null;
        Integer dringlichkeitsstufe = null;
        String region = null;
        if (beitrag instanceof Musikbeitrag) {
            Musikbeitrag musik = (Musikbeitrag) beitrag;
            typ = "MUSIK";
            kuenstler = musik.showKuenstler();
            album = musik.showAlbum();
            genre = musik.showGenre();
        }
        if (beitrag instanceof Werbebeitrag) {
            Werbebeitrag werbung = (Werbebeitrag) beitrag;
            typ = "WERBUNG";
            kunde = werbung.showKunde();
            werbebudget = werbung.showWerbebudget();
        }
        if (beitrag instanceof Moderation) {
            Moderation moderation = (Moderation) beitrag;
            typ = "MODERATION";
            sprechertext = moderation.showSprechertext();
        }
        if (beitrag instanceof Eilmeldung) {
            Eilmeldung eilmeldung = (Eilmeldung) beitrag;
            dringlichkeitsstufe = eilmeldung.showDringlichkeitsstufe();
            region = eilmeldung.showRegion();
        }
        return new BeitragDto(
                id,
                typ,
                beitrag.showTitel(),
                beitrag.showDauer(),
                beitrag.showStartzeitpunkt(),
                kuenstler,
                album,
                genre,
                kunde,
                werbebudget,
                sprechertext,
                dringlichkeitsstufe,
                region);
    }
}
