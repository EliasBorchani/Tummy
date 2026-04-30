#!/bin/sh
# Régénère iosApp.xcodeproj depuis project.yml.
# Lance-moi à chaque modif de project.yml (xcconfig, build phases, fichiers, settings…).
#
# Pré-requis : `brew bundle install` à la racine du repo (xcodegen disponible).

set -e
cd "$(dirname "$0")"

if ! command -v xcodegen >/dev/null 2>&1; then
    echo "xcodegen introuvable. Lance d'abord 'brew bundle install' à la racine du repo."
    exit 1
fi

xcodegen generate --spec project.yml
echo "✓ iosApp.xcodeproj regenerated"
