#!/bin/bash

# 1. Parameterüberprüfung
if [ -z "$1" ] || [ -z "$2" ]; then
	echo "❌ Benutzung: ./neue_aufgabe.sh Ordner_Name 'Beschreibung' [KlassenName]"
	exit 1
fi

NAME=$1
DESC=$2
PACKAGE="fbs.lg1"
PKG_PATH=$(echo $PACKAGE | tr '.' '/')

# 2. Validierung: Keine Leerzeichen oder Sonderzeichen im Verzeichnisnamen
# Erlaubt sind nur: A-Z, a-z, 0-9, Unterstrich und Bindestrich
if [[ ! "$NAME" =~ ^[a-zA-Z0-9_-]+$ ]]; then
	echo "❌ FEHLER: Der Ordnername '$NAME' enthält ungültige Zeichen."
	echo "   Erlaubt sind nur Buchstaben, Zahlen, _ und -"
	exit 1
fi

# 3. Klassennamen generieren oder übernehmen
if [ -z "$3" ]; then
	CLASSNAME=$(echo $NAME | sed 's/[_-]//g')
else
	CLASSNAME=$3
fi

# 4. Validierung: Klassennamen (Java-Regeln)
# Erlaubt sind nur: A-Z, a-z, 0-9 (darf aber nicht mit Zahl beginnen)
if [[ ! "$CLASSNAME" =~ ^[a-zA-Z][a-zA-Z0-9]*$ ]]; then
	echo "❌ FEHLER: Der Klassenname '$CLASSNAME' ist ungültig."
	echo "   Ein Java-Klassenname muss mit einem Buchstaben beginnen und darf keine Sonderzeichen enthalten."
	exit 1
fi

echo "🛠  Prüfe Projektstruktur für '$NAME' (Klasse: $CLASSNAME)..."

# 5. Verzeichnisse erstellen
mkdir -p "$NAME/src/main/java/$PKG_PATH"
mkdir -p "$NAME/src/test/java/$PKG_PATH"

# 6. build.gradle erstellen
if [ ! -f "$NAME/build.gradle" ]; then
	echo "description = '$DESC'" >"$NAME/build.gradle"
	echo "  + build.gradle erstellt."
fi

# 7. Klasse erstellen
FILE_PATH="$NAME/src/main/java/$PKG_PATH/${CLASSNAME}.java"
if [ ! -f "$FILE_PATH" ]; then
	cat <<EOF >"$FILE_PATH"
package $PACKAGE;

public class ${CLASSNAME} {
    public String sagHallo() {
        return "Hallo aus dem Package $PACKAGE";
    }
}
EOF
	echo "  + ${CLASSNAME}.java erstellt."
fi

# 8. Test-Klasse erstellen
TEST_PATH="$NAME/src/test/java/$PKG_PATH/${CLASSNAME}Test.java"
if [ ! -f "$TEST_PATH" ]; then
	cat <<EOF >"$TEST_PATH"
package $PACKAGE;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ${CLASSNAME}Test {
    @Test
    void testSagHallo() {
        ${CLASSNAME} aufgabe = new ${CLASSNAME}();
        assertThat(aufgabe.sagHallo()).contains("Hallo");
    }
}
EOF
	echo "  + ${CLASSNAME}Test.java erstellt."
fi

# 9. Subprojekt in settings.gradle registrieren
if ! grep -q "include '$NAME'" settings.gradle; then
	printf "\ninclude '%s'" "$NAME" >>settings.gradle
	echo "  + Projekt in settings.gradle registriert."
fi

## touch settings.gradle
echo "✅ Vorgang erfolgreich abgeschlossen."
