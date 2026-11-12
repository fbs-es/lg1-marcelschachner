plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.11.0"
}

group = "de.bestellsystem"
version = "0.1.0-SNAPSHOT"
description = "Bestellsystem: REST-Backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.10.0")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// OpenApiSpezifikationIT vergleicht gegen diese Datei; Änderungen daran sollen Tests neu auslösen.
tasks.test {
    inputs.file("../frontend/openapi.json").withPropertyName("openApiSpezifikation").optional()
}

spotless {
    java {
        googleJavaFormat().aosp()
        removeUnusedImports()
    }
}

// ADR 0002: schreibt frontend/openapi.json neu, Quelle für npm run generate:api.
tasks.register<Test>("aktualisiereOpenApi") {
    description = "Exportiert die OpenAPI-Spezifikation nach frontend/openapi.json."
    group = "documentation"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    filter { includeTestsMatching("*.OpenApiSpezifikationIT") }
    systemProperty("openapi.aktualisieren", "true")
    outputs.upToDateWhen { false }
}
