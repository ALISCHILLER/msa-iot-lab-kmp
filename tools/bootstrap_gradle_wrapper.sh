#!/usr/bin/env bash
set -euo pipefail

if [[ -x ./gradlew ]]; then
  echo "Gradle wrapper already exists."
  exit 0
fi

if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle is not installed. Install a compatible Gradle version or generate the wrapper from Android Studio/IntelliJ." >&2
  exit 1
fi

gradle wrapper --gradle-version 8.10.2 --distribution-type bin
