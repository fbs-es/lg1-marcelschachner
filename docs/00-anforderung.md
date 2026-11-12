# Anforderung (Originaltext)

Ein führendes E-Commerce-Unternehmen stellt sein Softwaresystem für Bestellungen um.

Im System wird jeder Kunde mit einer eindeutigen Kundenkennung, seinem Vor- und Nachnamen sowie einer eindeutigen E-Mail-Adresse erfasst. Ein Kunde muss in der Lage sein, seine aktuell beobachteten Artikel abzurufen. Zudem muss für ein Kundenkonto eine automatische Mahnprüfung durchgeführt werden können, die alle Bestellungen des Kunden auf offene Beträge untersucht und im Fall von Zahlungsrückständen eine Mahn-Benachrichtigung mit dem jeweils offenen Betrag an den Kunden versendet.

Ein Kunde muss mindestens eine Lieferadresse für spätere Einkäufe in seinem persönlichen Adressbuch hinterlegen. Bei Bedarf können weitere Liefer- und Rechnungsadressen hinzugefügt werden. Eine Adresse setzt sich aus einer Adresskennung, der Straße, der Stadt, der Postleitzahl und dem Land zusammen. Jede gespeicherte Adresse ist dabei stets genau einem Kunden zugeordnet.

Jedes Produkt speichert seine Identifikationsnummer, den Produkttitel, den aktuellen Lagerbestand sowie den aktuellen Listenpreis. Das System muss jederzeit prüfen können, ob ein Produkt aktuell verfügbar ist.

Kunden können Produkte auf eine persönliche Merkliste setzen. Ein Kunde kann beliebig viele Produkte auf seine Liste setzen. Umgekehrt kann ein Produkt auf der Merkliste mehrerer Kunden stehen oder von niemandem beobachtet werden. Für jedem Eintrag auf der Merkliste muss zwingend festgehalten werden, an welchem Datum das Produkt hinzugefügt wurde.

Eine getätigte Bestellung wird zentral im System abgewickelt. Sie speichert die Bestellnummer, das Bestelldatum, den berechneten Gesamtwert sowie einen historischen Snapshot der Lieferadresse (bestehend aus Straße, Stadt, Postleitzahl und Land). Der Gesamtwert muss dynamisch berechnet werden können. Zudem muss für jede Bestellung jederzeit geprüft werden können, ob sie bereits vollständig bezahlt wurde (durch Abgleich des berechneten Gesamtwerts mit der Summe aller geleisteten Zahlungen). Ein Kunde kann im Laufe der Zeit beliebig viele Bestellungen aufgeben oder noch keine getätigt haben. Jede Bestellung gehört jedoch zu genau einem Kunden.

Eine Bestellung setzt sich aus einzelnen Positionen zusammen. Jede Position speichert ihre Zeilennummer, die gewünschte Bestellmenge und den zum Kaufzeitpunkt eingefrorenen Einzelpreis. Für jede Position muss ein Zwischenbetrag berechnet werden können. Eine Bestellung besitzt mindestens eine bis beliebig viele Positionen, und jede Position gehört zu genau einer Bestellung.

Jede Bestellposition verweist zudem über eine einfache Beziehung auf genau ein Produkt aus dem Katalog. Ein Produkt kann in beliebig vielen Bestellpositionen verbucht sein (oder in gar keiner), falls es noch nie gekauft wurde.

Zur Erfassung von Zahlungsströmen werden Zahlungen mit einer Zahlungs-ID, der Zahlungsart, dem Transaktionsstatus, dem gezahlten Betrag und dem Zahlungszeitpunkt erfasst. Eine Bestellung erfordert mindestens eine bis beliebig viele Zahlungsbuchungen (z. B. bei Teilzahlungen oder Erstattungen), während jede Zahlung genau einer Bestellung zugewiesen ist.

Für die Logistik existieren Versandpakete, die eine Sendungsverfolgungsnummer, den Logistikdienstleister und den Lieferstatus erfassen. Eine Bestellung kann in mehrere Versandpakete aufgeteilt werden oder noch keines aufweisen. Jedes Paket gehört zu genau einer Bestellung.

Aktionscodes werden als Gutscheine mit einem Gutscheincode, einem Rabattwert und einem Ablaufdatum hinterlegt. Ein Gutschein kann bei mehreren Bestellungen angewendet werden oder noch ungenutzt im System verbleiben. Eine Bestellung kann von maximal einem Gutschein profitieren oder ohne Gutschein getätigt werden.
