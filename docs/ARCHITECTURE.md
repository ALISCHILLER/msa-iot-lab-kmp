# Architecture

MSA IoT Lab follows a shared-domain, platform-engine architecture.

## Dependency direction

```text
Compose UI
  -> use cases / controllers / repositories / services / contracts
      -> Room abstractions / protocol abstractions
          -> platform engines
```

The shared UI never directly opens sockets, writes Room entities, validates binary payloads, creates IDs/timestamps, parses transport headers or creates protocol-specific messages. These responsibilities are delegated to dedicated services:

- `AppDependencies`: explicit composition root
- `ProfileDraft`: UI form boundary before domain conversion
- `ProfileDraftDefaults`: deterministic editor defaults
- `ProfileDraftValidator`: raw form validation before coercion/defaults
- `TimeProvider` / `IdProvider`: deterministic runtime seams for factories, repositories and tests
- `ProfileFactory`: profile construction and safe coercion
- `SaveProfileUseCase`: profile save orchestration
- `ProfileValidator`: reusable final-domain validation
- `PayloadTemplateRepository`: domain repository contract for template persistence
- `RoomPayloadTemplateRepository`: Room-backed implementation of template persistence
- `SavePayloadTemplateUseCase`: template validation and persistence orchestration
- `ExportWorkspaceUseCase`: portable JSON export workflow
- `ImportWorkspaceUseCase`: portable JSON import workflow
- `ConsoleController`: live-console orchestration, retry handling, repeat lifecycle and bounded event state
- `ConsoleControllerFactory`: profile-scoped controller construction
- `ConnectionRetryPolicy`: auto-reconnect connection policy
- `ConsoleSessionManager`: console session lifecycle and event persistence
- `ConsoleCommandService`: pre-send payload validation, JSON normalization and variable expansion
- `PayloadSizePolicy`: maximum payload guard for stress-test safety
- `BaseProtocolClient`: shared protocol state/event plumbing
- `DefaultPayloadProvider`: safe starter payloads for each encoding
- `PayloadTemplateValidator`: validation for saved/imported templates
- `ProtocolCapabilityRegistry`: protocol feature metadata
- `HeaderJsonParser`: flat WebSocket header JSON parsing for validation and Ktor setup
- `ProtocolEventLogger`: event-to-log conversion, including lifecycle events
- `ProtocolMessageFactory`: normalized message creation
- `ProtocolClientFactory`: platform protocol engine creation

## Shared layer

The shared layer owns:

- Domain models split into focused protocol, profile, payload and event files
- Protocol contracts
- Room KMP database schema
- Repositories
- Use cases
- Payload tools with deterministic runtime-variable expansion
- Compose Multiplatform UI
- Validation and mapping services

## Platform layer

Platform layers own only platform APIs:

- Database builder path
- HTTP/WebSocket engine selection
- Raw TCP/UDP implementation
- MQTT implementation

## Main protocol contract

```kotlin
interface ProtocolClient {
    val state: StateFlow<ConnectionState>
    val events: Flow<ProtocolEvent>
    suspend fun connect(profile: ConnectionProfile)
    suspend fun disconnect()
    suspend fun send(payload: OutgoingPayload)
}
```

All protocol implementations emit the same event model, so the Live Console and history logger do not care whether traffic comes from MQTT, WebSocket, TCP, or UDP.

## SOLID notes

### SRP

Each class has one clear reason to change. For example, `ProfileDraft` changes when editable UI fields change, `ProfileDraftValidator` changes when form-input rules change, `ProfileFactory` changes when domain construction rules change, and `ProfileValidator` changes only when final profile rules change.

### OCP

To add a protocol, introduce a new `ProtocolType`, implement `ProtocolClient`, update `ProtocolCapabilityRegistry`, then register it in the platform factory. Existing console and history code do not need protocol-specific changes.

### LSP

`UnsupportedProtocolClient` can stand in for unavailable platform engines because it emits the same states and events as real clients.

### ISP

`ProtocolClient` stays small: connect, disconnect, send, state and events. Protocol-specific options live in profile option models.

### DIP

Shared UI receives use cases, controllers and repository contracts as dependencies. Platform details are injected from Android, Desktop and iOS entry points, while tests use fakes/in-memory repositories and deterministic runtime providers behind the same contracts.
