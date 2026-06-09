# Testing Strategy

MSA IoT Lab uses a layered testing strategy designed for Kotlin Multiplatform projects where the highest-value logic lives in `commonMain`.

## Test Pyramid

1. **Domain tests** validate pure rules such as profile validation, payload validation, MQTT topic validation, payload formatting and capability modeling.
2. **Use-case tests** verify application boundaries such as saving profiles, saving templates, importing/exporting workspaces and masking secrets.
3. **Controller tests** verify live console orchestration including retry, send, disconnect, session closing and invalid auto-repeat handling.
4. **Gateway/fake tests** keep persistence and transport testable without opening real sockets or requiring Room.
5. **Deterministic runtime tests** inject `TimeProvider` and `IdProvider` so generated timestamps and IDs can be asserted exactly.
6. **Platform smoke tests** should be added in Android/Desktop CI once dependency resolution is available.

## Current Professional Test Fixtures

- `FakeProtocolClient`: deterministic transport fake for console orchestration.
- `RecordingConsoleHistoryGateway`: verifies session and log side effects.
- `InMemoryProfileRepository`: tests profile use cases without Room.
- `InMemoryPayloadTemplateRepository`: tests template/export use cases without Room.
- `TestAppDispatchers`: routes coroutines through a deterministic test scheduler.
- `FixedTimeProvider`: stable clock for timestamp assertions.
- `SequentialIdProvider`: stable ID generator for factory/use-case assertions.

## High-Value Test Coverage

- Console retry success/failure paths.
- Session status persistence for finished/failed/cancelled flows.
- Payload validation before send and before auto-repeat.
- JSON payload minification before transport send.
- Runtime payload variables using injected time/ID providers.
- MQTT topic validation edge cases.
- Workspace export secret redaction and optional secret inclusion.
- Workspace export timestamp determinism.
- Workspace import restoration for profiles and templates.
- Protocol message normalization for preview, hex, metadata and direction.
- Profile editor default generation for new and existing profiles.
- Profile and template creation with injected clocks and IDs.

## Recommended Next Step

After Gradle dependencies resolve locally, run:

```bash
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
```

Then add Android instrumentation tests for Room migrations and platform protocol smoke tests with local mock servers.

## Latest Regression Tests

- Console reconnect after manual disconnect starts a new persisted session.
- Console reconnect after a failed connection starts a fresh session instead of reusing the failed one.
- Workspace imports reject duplicate profile IDs, unsupported schema versions and invalid template payloads before persistence.
- Import use case rejects invalid profiles before touching target repositories.
