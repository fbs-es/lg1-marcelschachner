#!/bin/bash

cd "$(dirname "$0")" || exit 1

PACKAGE="fbs.lg1"
PKG_PATH=$(echo $PACKAGE | tr '.' '/')

mapfile -t PROJECTS < <(grep -oP "^include '\K[^']+" settings.gradle | tac)

if [ ${#PROJECTS[@]} -eq 0 ]; then
	gum log --level error "Keine Aufgaben in settings.gradle gefunden."
	exit 1
fi

OPTIONS=()
for P in "${PROJECTS[@]}"; do
	DESC=$(grep -oP "^description\s*=\s*'\K[^']*" "$P/build.gradle" 2>/dev/null)
	OPTIONS+=("$P | ${DESC:----}")
done

CHOICE=$(gum choose --header "Aufgabe auswaehlen:" "${OPTIONS[@]}")
[ -z "$CHOICE" ] && exit 0
NAME="${CHOICE%% |*}"

MODE=$(gum choose --header "Aktion auswaehlen:" "Programm starten" "Tests ausfuehren")
[ -z "$MODE" ] && exit 0

if [ "$MODE" = "Tests ausfuehren" ]; then
	gum log --level info "Teste $NAME ..."
	if ! ./gradlew ":$NAME:test" --rerun; then
		gum log --level error "Tests fehlgeschlagen."
		exit 1
	fi
	exit 0
fi

mapfile -t MAIN_FILES < <(grep -l "static void main" "$NAME/src/main/java/$PKG_PATH"/*.java 2>/dev/null)

if [ ${#MAIN_FILES[@]} -eq 0 ]; then
	gum log --level error "Keine Klasse mit main-Methode in '$NAME' gefunden."
	exit 1
elif [ ${#MAIN_FILES[@]} -eq 1 ]; then
	MAIN_CLASS=$(basename "${MAIN_FILES[0]}" .java)
else
	MAIN_CLASS=$(gum choose --header "Main-Klasse auswaehlen:" \
		$(for F in "${MAIN_FILES[@]}"; do basename "$F" .java; done))
	[ -z "$MAIN_CLASS" ] && exit 0
fi

gum log --level info "Baue $NAME ..."
if ! ./gradlew -q ":$NAME:classes"; then
	gum log --level error "Build fehlgeschlagen."
	exit 1
fi

gum log --level info "Starte $PACKAGE.$MAIN_CLASS"
echo ""
java -ea -cp "$NAME/build/classes/java/main" "$PACKAGE.$MAIN_CLASS"
