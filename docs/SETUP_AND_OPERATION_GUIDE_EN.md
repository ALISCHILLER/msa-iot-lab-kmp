# MSA IoT Lab Setup and Operation Guide

This guide explains how to open, sync, test and run the project after extracting the ZIP. The app is built with Kotlin Multiplatform, Compose Multiplatform and Room KMP. Shared UI and domain code live in `composeApp/src/commonMain`.

## Requirements

- Windows, Linux or macOS for Android/Desktop development.
- macOS with Xcode for iOS builds.
- JDK 21.
- Android Studio or IntelliJ IDEA with Kotlin Multiplatform support.
- Internet access for the first Gradle sync.
- Python 3 for `tools/static_audit.py`.
- Network access to the MQTT broker, WebSocket backend or IoT device under test.

## Open the project

Open the project root that contains:

```text
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
composeApp/build.gradle.kts
```

Do not open only the `composeApp` folder.

## First checks

```bash
./gradlew --version
python3 tools/static_audit.py
```

If the wrapper is missing or not executable:

```bash
bash tools/bootstrap_gradle_wrapper.sh
chmod +x gradlew
```

## Run Desktop

Desktop is the preferred engineering target for heavy protocol debugging because it shows the persistent sidebar, Command Center and Traffic Intelligence panels.

```bash
./gradlew :composeApp:run
```

## Run Android

```bash
./gradlew :composeApp:assembleDebug
```

You can also run the `composeApp` Android target from Android Studio.

## Run tests

```bash
./gradlew :composeApp:allTests
```

The test suite covers validation, payload tools, protocol diagnostics, traffic analysis, import/export, console orchestration, localization and security helpers.

## Release quality gate

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
```

## Operator workflow

1. Create a profile from Dashboard or Profiles.
2. Configure host, port, encoding and protocol-specific options.
3. Open Live Console.
4. Read diagnostics and connect.
5. Send TEXT/JSON/HEX/Base64 payloads.
6. Inspect IN/OUT/SYSTEM/ERROR cards in Traffic Intelligence.
7. Review persisted history.
8. Export/import the workspace from Settings.

## Bilingual UI

The app supports English and Persian at runtime. Persian uses RTL layout direction. The selected language is persisted through the Room-backed settings repository so it survives restart.
