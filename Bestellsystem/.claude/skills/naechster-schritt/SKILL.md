---
name: naechster-schritt
description: Arbeitet den obersten offenen Schritt aus docs/fahrplan.md ab (mit feature-dev oder als goal-Vorschlag), prüft "Fertig wenn", committet immer (auch Teilstand, nie rot) und hakt ab. Verwenden bei "nächster Schritt", "weiter im Fahrplan", "naechster-schritt" oder /naechster-schritt.
---

# Nächster Schritt

1. **Schritt finden.** `docs/fahrplan.md` lesen. Oberster Schritt mit `## [ ]` ist dran. Alle früheren Schritte müssen `[x]` sein; sonst stoppen und melden.

2. **Kontext prüfen.** Läuft diese Sitzung schon länger (früherer Schritt bearbeitet), dem Nutzer `/clear` empfehlen und stoppen.

3. **Eingaben lesen.** Alle unter "Eingaben" genannten Dateien plus `CLAUDE.md` und `docs/ARCHITEKTUR.md` (falls vorhanden) lesen.

4. **Werkzeug wählen** nach der Zeile "Werkzeug":
   - `feature-dev` → Skill `feature-dev:feature-dev` mit dem Prompt aus dem Codeblock des Schritts aufrufen. Architektur-Phase vom Nutzer freigeben lassen, bevor implementiert wird.
   - `goal` → Den `/goal`-Befehl aus dem Codeblock dem Nutzer zum Ausführen anzeigen und stoppen (goal startet der Nutzer selbst).
   - `von Hand` / normaler Auftrag → Punkte direkt abarbeiten; destruktive oder unklare Punkte vorher fragen.

5. **Fertig-wenn prüfen.** Jeden Punkt unter "Fertig wenn" einzeln nachweisen (Befehl ausführen, Ausgabe nennen). Ein Punkt nicht erfüllt → nicht abhaken, offenen Rest melden.

6. **Immer committen.** Jede Sitzung endet mit einem Commit, ohne Rückfrage, auch wenn der Schritt nicht vollständig ist.
   - Vorher `cd backend && ./gradlew build` (und ab Schritt 2 `cd frontend && npm test`) ausführen. Rot → erst reparieren; nicht reparierbar → Nutzer fragen, nie rot committen.
   - `docs/ARCHITEKTUR.md` aktualisieren.
   - Geänderte Dateien gezielt stagen (nie `git add -A`), `git status` prüfen: keine Exporte, Logs, `.env` oder Build-Artefakte.
   - Arbeit committen nach `CLAUDE.md` → Git. Schritt unvollständig → Betreff beschreibt den erreichten Stand, offene "Fertig wenn"-Punkte in den Commit-Body.
   - Nur wenn alle "Fertig wenn"-Punkte erfüllt: im Fahrplan `## [ ]` → `## [x]`, hinter den Titel `(JJJJ-MM-TT, <kurzer Hash des Arbeits-Commits>)` setzen und als eigenen Commit `Hake Schritt <n> im Fahrplan ab` committen.
   - Zwischenänderungen auf Zuruf (z. B. Werkzeugwechsel) ebenfalls als eigener Commit, sobald grün.

7. **Melden.** Was gebaut wurde, Testergebnis, Commit-Hashes, offene Punkte, nächster Schritt.
