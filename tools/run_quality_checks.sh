#!/usr/bin/env bash
set -euo pipefail

python3 tools/static_audit.py

if [[ -x ./gradlew ]]; then
  ./gradlew :composeApp:allTests
  ./gradlew :composeApp:compileKotlinDesktop
  ./gradlew :composeApp:assembleDebug
else
  echo "Gradle wrapper was not found. Run Gradle checks from Android Studio/IntelliJ or generate ./gradlew locally."
fi
