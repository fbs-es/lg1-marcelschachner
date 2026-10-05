/**
 * Produktbewertungen in einem E-Commerce-Portal.
 *
 * <p><b>Klassenmodell:</b>
 *
 * <pre>
 * Hersteller 1 ---- * Produkt
 * Kunde      1 ---- * Bewertung * ---- 1 Produkt
 * Bewertung  1 ---- 0..1 Antwort
 * </pre>
 *
 * <ul>
 *   <li>{@link fbs.lg1.Hersteller}: kann viele Produkte anbieten.
 *   <li>{@link fbs.lg1.Produkt}: gehört genau einem Hersteller und berechnet den Durchschnitt
 *       seiner Sterne.
 *   <li>{@link fbs.lg1.Kunde}: kann viele Produkte bewerten.
 *   <li>{@link fbs.lg1.Bewertung}: verknüpft einen Kunden mit einem Produkt.
 *   <li>{@link fbs.lg1.Antwort}: Antwort eines Verkäufers auf genau eine Bewertung.
 * </ul>
 *
 * <p><b>Aufbau der API-Dokumentation:</b> Jede Klasse dokumentiert ihre Invarianten mit dem Tag
 * {@code @invariant}. Jeder Konstruktor und jede Methode mit Vor- oder Nachbedingungen dokumentiert
 * diese mit {@code @pre} und {@code @post}. Eine verletzte Vorbedingung führt zu der unter
 * {@code @throws} angegebenen Exception. Nachbedingungen werden mit {@code assert} geprüft (Start
 * mit {@code java -ea}), Invarianten mit {@code checkInvariant()} am Ende jeder
 * zustandsändernden Methode.
 *
 * <p>Die Tags werden in {@code build.gradle} registriert. Die HTML-Dokumentation entsteht mit
 * {@code ./gradlew :Aufgabe22:javadoc} unter {@code Aufgabe22/build/docs/javadoc}.
 */
package fbs.lg1;
