# Final Project Report - MSA IoT Lab

This snapshot is a Kotlin Multiplatform IoT protocol testing workstation built around shared Compose UI, Room KMP persistence, protocol abstractions, platform-specific transports, and common unit tests.

## Architecture Quality

- UI does not import Room entities or platform transport implementations.
- Protocol clients implement a shared `ProtocolClient` contract.
- Controllers and use cases depend on repository/gateway contracts instead of concrete storage classes.
- Time and ID generation are injected through `TimeProvider` and `IdProvider` for deterministic tests.
- Console session persistence is isolated through `ConsoleHistoryGateway`.
- Import/export is validated before persistence to protect local workspace integrity.
- Security-sensitive exports mask MQTT passwords by default.

## Protocol Coverage

| Target | MQTT | WebSocket | TCP | UDP | Room | Compose UI |
| --- | --- | --- | --- | --- | --- | --- |
| Android | JVM HiveMQ client | Ktor | java.net | java.net | Room KMP | Shared |
| Desktop | JVM HiveMQ client | Ktor | java.net | java.net | Room KMP | Shared |
| iOS | Unsupported stub | Ktor Darwin | Unsupported stub | Unsupported stub | Room KMP | Shared |

The iOS TCP/UDP/MQTT entries are intentionally explicit stubs because production iOS socket support should be implemented with Network.framework or a dedicated native transport layer.

## Test Coverage Focus

- Console connection lifecycle
- Reconnect and session restart regressions
- Send-before-connect guards
- Auto-repeat guards
- Payload codecs and validation
- MQTT topic validation
- Header JSON security validation
- Profile draft/profile validation
- Template validation and save use case
- Import/export validation
- Deterministic ID/time providers
- Secret masking

## Quality Commands

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
```

If the Gradle wrapper is missing:

```bash
bash tools/bootstrap_gradle_wrapper.sh
```

Then run:

```bash
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:run
```

## Latest Static Audit

- Kotlin files: 121
- Common test files: 28
- Type declarations: 155
- KDoc-covered declarations: 155/155
- Static audit errors: 0

## UI modernization pass

The latest iteration upgrades the UI from simple list screens to a desktop-first IoT workbench. The shared Compose UI now has a persistent sidebar, responsive fallback layout, richer dashboard metrics, two-column console, traffic monitor, profile inventory, template workspace and import/export panels. All changes remain in `commonMain`, preserving the Kotlin Multiplatform structure.

## Adaptive all-device UI pass

This version adds a responsive Compose UI layer for compact phones, medium tablets/foldables and expanded desktop windows. Navigation, grids, action rows, console panes, profile forms, history, templates and settings now adapt through shared `ResponsiveLayout.kt` helpers.

## Final documentation and setup guide pass

This delivery adds a complete Persian setup/requirements/runbook documentation pack, updates the in-app Guide screen with setup and quality-gate instructions, and keeps all KDoc and architecture audit checks passing. The project now contains explicit operator, developer, release and code-commenting guides.



## Bilingual UI Update

- Added runtime English/Persian language switcher.
- Added RTL layout direction for Persian.
- Added docs/BILINGUAL_UI_GUIDE_FA.md and docs/UI_BILINGUAL_FINAL_REVIEW.md.
- Added LocalizationSmokeTest for explicit language helpers.
