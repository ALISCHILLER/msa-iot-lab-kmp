# MSA IoT Lab

A professional Kotlin Multiplatform + Compose Multiplatform protocol workbench for IoT and backend realtime testing.

## Purpose

MSA IoT Lab is a Postman-like tool for device/backend protocols:

- MQTT publish/subscribe
- WebSocket send/receive
- Raw TCP socket send/receive
- UDP send/listen/broadcast
- Connection profiles
- Live console with IN/OUT/SYSTEM/ERROR events
- Session history and message logs
- Payload templates
- Payload template validation
- Auto-repeat sender
- Retry-aware connection attempts
- Pre-send payload validation
- Payload size safety policy
- MQTT topic validation
- WebSocket header JSON validation
- Protocol capability registry
- Payload variables: `{timestamp}`, `{uuid}`, `{counter}`
- Payload encodings: TEXT, JSON, HEX, BASE64
- JSON pretty/minify tools
- Profile/template export and import as JSON
- Professional commonTest suite with protocol fakes, deterministic coroutine dispatchers, deterministic time/ID providers and in-memory repositories
- Import/export tests with default secret masking, explicit secret inclusion and deterministic export timestamps
- Connection-state send guards that prevent disconnected transports from receiving payloads
- Strict MQTT wildcard validation for publish/subscribe topics
- Built-in in-app operator guide and complete Persian operator manual

## Engineering focus

This version has been refactored around SOLID/Clean Architecture principles:

- **Single Responsibility:** UI renders and delegates; validation, profile construction, use-case orchestration, console controller creation, event logging, command preparation and message creation live in dedicated services.
- **Open/Closed:** New protocols can be added by implementing `ProtocolClient` and registering them in a platform factory.
- **Liskov Substitution:** `UnsupportedProtocolClient` follows the same contract as real engines, so UI does not special-case unsupported targets.
- **Interface Segregation:** persistence, protocol and import/export concerns use focused contracts.
- **Dependency Inversion:** shared UI depends on use cases, controllers, repositories and protocol factories, not concrete socket or MQTT implementations.

## Targets

| Target | Room KMP | Compose UI | MQTT | WebSocket | TCP | UDP |
|---|---:|---:|---:|---:|---:|---:|
| Android | yes | yes | yes | yes | yes | yes |
| Desktop JVM | yes | yes | yes | yes | yes | yes |
| iOS | yes | yes | safe unsupported client | yes | safe unsupported client | safe unsupported client |

The Android and Desktop targets are the complete raw IoT testing targets. iOS includes Room, shared UI and WebSocket, while MQTT/TCP/UDP return clear unsupported events through the same `ProtocolClient` contract so the app does not crash or branch UI logic.

## Stack

- Kotlin Multiplatform
- Compose Multiplatform
- Room KMP
- KSP
- Bundled SQLite driver
- Ktor WebSockets
- HiveMQ MQTT client for Android/Desktop JVM
- Coroutines + Flow
- kotlinx.serialization
- Explicit composition root via `AppDependencies`

## Project structure

```text
composeApp/src/commonMain/kotlin/com/msa/iotlab
├── console       # console orchestration, retry policy, controller factory, command validation and runtime limits
├── core          # runtime providers, clock, IDs, dispatchers, validation exception and small shared utilities
├── database      # Room KMP database, entities and DAOs
├── di            # shared composition root
├── export        # JSON import/export bundle handling and use cases
├── history       # session and protocol message persistence
├── payload       # payload codec, JSON formatter, defaults and template variables
├── profile       # profile repository, draft defaults, use case, factory and mapper
├── protocol      # ProtocolClient contract, events, models and message factory
├── template      # payload template repository contract, Room implementation, draft, save use case and mapper
├── ui            # shared Compose screens
└── validation    # reusable domain validation
```

Platform implementations:

```text
composeApp/src/androidMain/kotlin/com/msa/iotlab/platform
composeApp/src/desktopMain/kotlin/com/msa/iotlab/platform
composeApp/src/iosMain/kotlin/com/msa/iotlab/platform
composeApp/src/jvmSharedMain/kotlin/com/msa/iotlab/platform
```

## Run

### Android

Open the project in Android Studio / IntelliJ IDEA with Kotlin Multiplatform support and run the `composeApp` Android configuration.

### Desktop

```bash
./gradlew :composeApp:run
```

If a Gradle wrapper is not present in your clone, generate it once from Android Studio/IntelliJ or run `bash tools/bootstrap_gradle_wrapper.sh` on a machine with Gradle installed.

### Tests

```bash
./gradlew :composeApp:allTests
```

## Important implementation notes

Room entities, DAOs, and `AppDatabase` live in `commonMain`. Platform-specific Room builders are implemented separately for Android, Desktop, and iOS because each platform has a different filesystem/database path model.

The app uses `BundledSQLiteDriver` to keep SQLite behavior consistent across platforms.

Room schema exports are intentionally not ignored so future migrations can be reviewed and tested.

Every class/interface/object/enum/data/sealed declaration in the project has a short KDoc-style explanation above it. The included `tools/static_audit.py` script checks this automatically together with package declarations, TODO/FIXME markers, brace balance outside literals/comments, architecture import guards, UI boundary guards, common-test assertion import checks, and Kotlin syntax markers such as unescaped string issues.

## Quality audit

```bash
python3 tools/static_audit.py
```

Current static audit result:

```text
Kotlin files: 131
Type declarations: 169
KDoc-covered declarations: 169
Errors: 0
```

## Complete operator guide

A full Persian operator manual is included at `docs/COMPLETE_OPERATOR_GUIDE_FA.md`. It covers installation, Android/Desktop usage, profile creation, MQTT/WebSocket/TCP/UDP workflows, payload encodings, templates, import/export, troubleshooting, testing and release checks.

The app also contains an in-app **Guide** destination so operators can read the most important workflow and troubleshooting notes directly inside the Compose UI.

## Test strategy

See `docs/TESTING_STRATEGY.md` for the unit-test layers, test doubles, and the recommended integration-test roadmap.

## Recommended local verification

A dependency-resolving Gradle build should be run on a local machine with internet access:

```bash
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:run
```


## Persian setup and delivery documentation

The project now includes a complete Persian documentation pack for setup, requirements, operations, development and release readiness:

- `docs/PROJECT_DOCUMENTATION_INDEX_FA.md` – documentation map.
- `docs/REQUIREMENTS_FA.md` – system, toolchain, Android/Desktop/iOS and network requirements.
- `docs/SETUP_AND_RUN_FA.md` – step-by-step setup, run, build and troubleshooting guide.
- `docs/OPERATOR_RUNBOOK_FA.md` – operational MQTT/WebSocket/TCP/UDP testing runbook.
- `docs/DEVELOPER_GUIDE_FA.md` – architecture, extension rules and test guidelines for developers.
- `docs/FINAL_RELEASE_CHECKLIST_FA.md` – final release checklist.
- `docs/COMMENTING_AND_CODE_STYLE_FA.md` – KDoc and code-commenting rules.

## Production hardening backlog

- Add native iOS MQTT/TCP/UDP engines behind the existing `ProtocolClient` contract if iOS raw IoT testing becomes a release requirement.
- Add platform file pickers for import/export instead of copy/paste text boxes.
- Add secure storage for MQTT passwords.
- Add TLS certificate pinning/custom trust store options.
- Add paging for very large message logs.
- Add CI build matrix for Android, Desktop, and iOS framework builds.
- Add protocol-specific integration tests against local test servers/brokers.
- Add coverage reports once the Gradle wrapper/dependency environment is available.

## Android manifest hardening

The Android target disables Auto Backup because profiles may contain sensitive connection data, and it declares an explicit network security config for intentional local IoT cleartext testing. See `docs/ANDROID_SECURITY.md`.

## Latest UI pass

This version includes a desktop-first Compose Multiplatform UI redesign:

- Persistent workbench sidebar on desktop.
- Responsive compact navigation on smaller screens.
- Dashboard metrics for profiles, sessions and traffic.
- Two-pane live console with command editor and traffic monitor.
- Professional profile inventory and editor sections.
- History, template and import/export screens redesigned as workbench panels.

The UI remains shared in `composeApp/src/commonMain`, while platform-specific source sets only provide database/protocol implementations.

## Adaptive all-device UI pass

This version adds a responsive Compose UI layer for compact phones, medium tablets/foldables and expanded desktop windows. Navigation, grids, action rows, console panes, profile forms, history, templates and settings now adapt through shared `ResponsiveLayout.kt` helpers.

## Protocol + UI ultra pass

This release includes protocol diagnostics, traffic intelligence, adaptive UI improvements and an in-app operator guide. The documentation set now includes:

- `docs/COMPLETE_OPERATOR_GUIDE_FA.md`
- `docs/UI_UX_FINAL_REVIEW.md`
- `docs/PROTOCOL_UI_PRO_REVIEW.md`
- `docs/GRADLE_REVIEW.md`



## Bilingual UI Update

- Added runtime English/Persian language switcher.
- Added RTL layout direction for Persian.
- Added docs/BILINGUAL_UI_GUIDE_FA.md and docs/UI_BILINGUAL_FINAL_REVIEW.md.
- Added LocalizationSmokeTest for explicit language helpers.

## Final professional setup/UI pass

This delivery adds a stronger in-app setup guide and a more production-oriented bilingual UX layer:

- Runtime English/Persian language switching.
- RTL layout direction for Persian.
- Room-backed language persistence through `AppSettingsRepository`.
- In-app setup runbook, platform requirements and quality-gate panels.
- Updated setup documentation in both Persian and English.
- UI implementation notes for future development.

New documentation files:

- `docs/SETUP_AND_OPERATION_GUIDE_FA.md`
- `docs/SETUP_AND_OPERATION_GUIDE_EN.md`
- `docs/UI_IMPLEMENTATION_FINAL_NOTES_FA.md`

Recommended verification after extracting the project:

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:run
```
