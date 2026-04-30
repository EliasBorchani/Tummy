#!/bin/sh
# Contenu à coller dans la Build Phase Run Script d'iosApp.
# Cette phase doit s'exécuter AVANT "Compile Sources" pour que Swift voie le framework.
#
# Rôle : appeler Gradle pour produire + embed + signer TummyShared.framework
# pour la configuration + SDK courants (Xcode fournit les env vars).

set -e

cd "$SRCROOT/.."
./gradlew :umbrella:embedAndSignAppleFrameworkForXcode
