# Testfälle – Aufgabe 08: Paketannahme

| ID    | Vorbedingung    | Testschritte                                                                                         | Erwartetes Ergebnis                                                                                    |
| ----- | --------------- | ---------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| TC-01 | Paket abgegeben | Gefährliche Güter: `J`                                                                               | `"Vorgang abgebrochen. Annahme verweigert."`                                                           |
| TC-02 | Paket abgegeben | Gefährliche Güter: `N`, Gewicht über 31,5 kg, Maße im Normalbereich                                 | `"Sperrgut-Verweis: Bitte geben Sie das Paket am Schalter ab."`                                        |
| TC-03 | Paket abgegeben | Gefährliche Güter: `N`, Gewicht im Normalbereich, mindestens eine Maßangabe überschritten            | `"Sperrgut-Verweis: Bitte geben Sie das Paket am Schalter ab."`                                        |
| TC-04 | Paket abgegeben | Gefährliche Güter: `N`, Gewicht über 31,5 kg, mindestens eine Maßangabe überschritten               | `"Sperrgut-Verweis: Bitte geben Sie das Paket am Schalter ab."`                                        |
| TC-05 | Paket abgegeben | Gefährliche Güter: `N`, Gewicht im Normalbereich, Maße im Normalbereich, zerbrechlich: `N`          | `"Paket angenommen. Versandbestätigung wird gedruckt."`                                                |
| TC-06 | Paket abgegeben | Gefährliche Güter: `N`, Gewicht im Normalbereich, Maße im Normalbereich, zerbrechlich: `J`          | `"Paket angenommen. Versandbestätigung wird gedruckt." + "Vermerk: Vorsicht - zerbrechlicher Inhalt."` |
