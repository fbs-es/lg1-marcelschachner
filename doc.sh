#!/bin/bash

cd "$(dirname "$0")" || exit 1

mapfile -t PROJECTS < <(grep -oP "^include '\K[^']+" settings.gradle | tac)

OPTIONS=()
for P in "${PROJECTS[@]}"; do
	if grep -rqs '/\*\*' "$P/src/main/java"; then
		DESC=$(grep -oP "^description\s*=\s*'\K[^']*" "$P/build.gradle" 2>/dev/null)
		if [ -f "$P/build/docs/javadoc/index.html" ]; then
			STATUS="generiert"
		else
			STATUS="nicht generiert"
		fi
		OPTIONS+=("$P | ${DESC:----} | $STATUS")
	fi
done

if [ ${#OPTIONS[@]} -eq 0 ]; then
	gum log --level error "Keine Aufgaben mit Javadoc gefunden."
	exit 1
fi

CHOICE=$(gum choose --header "Javadoc oeffnen:" "${OPTIONS[@]}")
[ -z "$CHOICE" ] && exit 0
NAME="${CHOICE%% |*}"

INDEX="$NAME/build/docs/javadoc/index.html"

if [ ! -f "$INDEX" ]; then
	gum log --level info "Generiere Javadoc fuer $NAME ..."
	if ! ./gradlew -q ":$NAME:javadoc"; then
		gum log --level error "Javadoc-Generierung fehlgeschlagen."
		exit 1
	fi
fi

gum log --level info "Oeffne $INDEX"
xdg-open "file://$(realpath "$INDEX")" >/dev/null 2>&1 &
