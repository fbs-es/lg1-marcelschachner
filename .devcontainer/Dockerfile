FROM mcr.microsoft.com/devcontainers/java:1-21

# Install Maven directly from Apache distribution — bypasses SDKMAN API
ARG MAVEN_VERSION=3.9.9
RUN set -eux; \
    curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
        | tar -xz -C /opt; \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}/bin/mvn" /usr/local/bin/mvn

# Install Gradle directly from Gradle distribution — bypasses SDKMAN API
ARG GRADLE_VERSION=9.2.0
RUN set -eux; \
    curl -fsSL "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" -o /tmp/gradle.zip; \
    unzip /tmp/gradle.zip -d /opt; \
    rm /tmp/gradle.zip; \
    ln -s "/opt/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle
