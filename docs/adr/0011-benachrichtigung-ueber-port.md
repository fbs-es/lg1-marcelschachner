# ADR 0011: Benachrichtigungen über einen Port, Versand in der Transaktion

**Status:** angenommen (2026-11-12)

## Kontext

Die Mahnprüfung verschickt E-Mails ([D-03](../01-entscheidungen.md#d-03-zahlungsrückstand-braucht-eine-fälligkeit)). Scheitert der Versand, darf keine Mahnung protokolliert werden, sonst gilt der Kunde als gemahnt, ohne eine Nachricht erhalten zu haben (US-06.1 AK7, [D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)). Tests dürfen keine echten E-Mails verschicken.

## Entscheidung

- Port `Benachrichtigungsversand.sende(Benachrichtigung)` in `gemeinsam.application`; Adapter `MailBenachrichtigungsversand` in `gemeinsam.infrastruktur` mit `JavaMailSender` (SMTP).
- Lokal nimmt **Mailpit** alle Mails an (SMTP :1025, Web http://localhost:8025).
- Der Versand läuft **innerhalb** der Transaktion der Mahnprüfung, vor dem Commit. Scheitert er, rollt die Transaktion zurück und die manuelle Prüfung antwortet mit 503. Scheitert erst der Commit, kann die nächste Prüfung erneut mahnen: Zustellung **mindestens einmal** ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)).
- Der tägliche Mahnlauf prüft jeden Kunden in einer eigenen Transaktion; Fehler werden geloggt und gezählt, der Lauf geht weiter ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)).
- Tests ersetzen den Port durch `BenachrichtigungsAttrappe`; der Adapter selbst wird gegen einen gemockten `JavaMailSender` getestet.

## Konsequenzen

- Kein Outbox-Muster im MVP; doppelte Mahn-E-Mails sind bei Commit-Fehlern möglich und akzeptiert.
- Weitere Kanäle (SMS, Push) sind als zusätzliche Adapter nachrüstbar.
