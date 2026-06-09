# Gradle Review

The project was reviewed against the Kotlin Multiplatform Compose project structure guidance.

## Current decisions

- `composeApp` is the shared Compose Multiplatform module.
- `commonMain` contains shared UI, domain, validation, repositories, Room declarations and protocol contracts.
- `androidMain`, `desktopMain`, `iosMain`, and `jvmSharedMain` contain platform-specific entry points and implementations.
- iOS targets are registered only on macOS hosts to keep Windows/Linux Android + desktop development unblocked.
- Room schema output remains under `composeApp/schemas`.
- Gradle wrapper is preserved; generated `.gradle`, `.idea`, and `local.properties` are excluded from the delivered archive.

## Notes

The Kotlin tutorial describes source sets such as `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `jsMain`, and `wasmJsMain`, and recommends keeping common code shared where possible while platform source sets contain platform-specific APIs. The current Gradle layout follows this principle.
