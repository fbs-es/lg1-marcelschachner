# ADR 0001: Gradle als Build-Werkzeug

**Status:** angenommen (2026-11-12)

## Kontext

Ursprünglich war Maven mit Wrapper vorgesehen. Vor dem ersten Backend-Code wurde entschieden, auf Gradle umzusteigen.

## Entscheidung

- Backend baut mit **Gradle 9.9.1** über den Wrapper `./gradlew`, Build-Skripte in **Kotlin DSL** (`build.gradle.kts`, `settings.gradle.kts`).
- Spring Boot Gradle Plugin plus `io.spring.dependency-management` für die Versionsverwaltung aus dem Boot-BOM.
- Java-Toolchain 21.
- Formatierung mit Spotless (google-java-format, AOSP); `./gradlew build` prüft sie über `spotlessCheck`.
- `./gradlew build` ersetzt `./mvnw verify`: kompiliert, führt alle Tests (Unit und Testcontainers) aus und prüft die Formatierung.
- `gradle/wrapper/gradle-wrapper.jar` wird versioniert; `build/` und `.gradle/` nicht.

## Konsequenzen

- Das System-`gradle` (4.4) wird nie verwendet, nur der Wrapper.
- Unit- und Integrationstests laufen in einem gemeinsamen `test`-Task; eine Trennung ist bei Bedarf über ein eigenes Source-Set nachrüstbar.
