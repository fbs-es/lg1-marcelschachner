#!/bin/bash

PACKAGE="fbs.lg1"
PKG_PATH=$(echo $PACKAGE | tr '.' '/')

gum style \
	--border rounded --border-foreground 212 \
	--padding "1 3" --margin "1 0" \
	--bold "Neues Gradle-Projekt erstellen"

NAME=$(gum input --placeholder "Ordnername" --prompt "> ")
if [[ ! "$NAME" =~ ^[a-zA-Z0-9_-]+$ ]]; then
	gum log --level error "Ordnername '$NAME' enthaelt ungueltige Zeichen."
	exit 1
fi

DESC=$(gum input --placeholder "Beschreibung" --prompt "> ")

CLASSES_INPUT=$(gum input --placeholder "Klassennamen, Komma-getrennt (leer = aus Ordnername)" --prompt "> ")

if [ -z "$CLASSES_INPUT" ]; then
	CLASSNAMES=($(echo $NAME | sed 's/[_-]//g'))
else
	IFS=',' read -ra CLASSNAMES <<<"$CLASSES_INPUT"
	CLASSNAMES=("${CLASSNAMES[@]// /}")
fi

for CLASSNAME in "${CLASSNAMES[@]}"; do
	if [[ ! "$CLASSNAME" =~ ^[a-zA-Z][a-zA-Z0-9]*$ ]]; then
		gum log --level error "Klassenname '$CLASSNAME' ist ungueltig."
		exit 1
	fi
done

SELECTED_TESTS=$(gum choose --no-limit \
	--header "Tests auswaehlen (leer lassen = alle):" \
	"${CLASSNAMES[@]}")
if [ -z "$SELECTED_TESTS" ]; then
	TEST_CLASSNAMES=("${CLASSNAMES[@]}")
else
	mapfile -t TEST_CLASSNAMES <<<"$SELECTED_TESTS"
fi

echo ""

mkdir -p "$NAME/src/main/java/$PKG_PATH"
mkdir -p "$NAME/src/test/java/$PKG_PATH"

if [ ! -f "$NAME/build.gradle" ]; then
	echo "description = '$DESC'" >"$NAME/build.gradle"
	gum log --level info "build.gradle erstellt"
fi

for CN in "${CLASSNAMES[@]}"; do
	FILE_PATH="$NAME/src/main/java/$PKG_PATH/${CN}.java"
	if [ ! -f "$FILE_PATH" ]; then
		cat <<EOF >"$FILE_PATH"
package $PACKAGE;

public class ${CN} {
    public String sagHallo() {
        return "Hallo aus dem Package $PACKAGE";
    }
}
EOF
		gum log --level info "${CN}.java erstellt"
	fi

	in_tests=false
	for t in "${TEST_CLASSNAMES[@]}"; do
		[[ "${t//[$'\r\n']/}" == "$CN" ]] && in_tests=true && break
	done
	if $in_tests; then
		TEST_PATH="$NAME/src/test/java/$PKG_PATH/${CN}Test.java"
		if [ ! -f "$TEST_PATH" ]; then
			cat <<EOF >"$TEST_PATH"
package $PACKAGE;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ${CN}Test {
    @Test
    void testSagHallo() {
        ${CN} aufgabe = new ${CN}();
        assertThat(aufgabe.sagHallo()).contains("Hallo");
    }
}
EOF
			gum log --level info "${CN}Test.java erstellt"
		fi
	fi
done

if ! grep -q "include '$NAME'" settings.gradle; then
	printf "\ninclude '%s'" "$NAME" >>settings.gradle
	gum log --level info "settings.gradle aktualisiert"
fi

echo ""
gum style --foreground 212 --bold "Projekt '$NAME' erfolgreich erstellt."

PROJECT_DIR="$(pwd)/$NAME"
tmux new-window -c "$PROJECT_DIR" "nvim"
