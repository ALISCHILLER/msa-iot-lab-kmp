# SOLID Architecture Notes

## Layer direction

```text
Compose UI
  -> Use Cases / Controllers / Repositories / Factories
      -> Domain Models / Validation / Protocol Contracts
          -> Room DAOs / Platform Protocol Clients
```

The codebase enforces this direction with `tools/static_audit.py` guards:

- UI must not import Room database classes.
- UI must not import platform protocol implementations.
- UI must not create timestamps or IDs directly.
- UI must not construct use cases directly.
- Protocol code must not import Compose.
- `commonMain` must not import JVM/iOS platform APIs.

## Main boundaries

- `ProtocolClient` is the transport abstraction.
- `ProtocolClientFactory` is the platform bridge.
- `ConsoleController` is the live console orchestration boundary.
- `SaveProfileUseCase` is the profile persistence workflow.
- `SavePayloadTemplateUseCase` is the template persistence workflow.
- `ExportWorkspaceUseCase` and `ImportWorkspaceUseCase` isolate portable JSON workflows.
- `ProfileDraftValidator` validates raw UI input before coercion/defaults.
- `ProfileValidator` validates final domain profiles before persistence.

## Why unsupported iOS clients still follow SOLID

The iOS factory returns `UnsupportedProtocolClient` for MQTT/TCP/UDP in this build. That client implements the same `ProtocolClient` interface and emits standard state/events. The UI therefore stays substitutable and does not branch on platform-specific limitations.

## Extension path for new protocols

1. Add protocol type and option model.
2. Add capabilities to `ProtocolCapabilityRegistry`.
3. Implement a `ProtocolClient` behind `BaseProtocolClient`.
4. Register it in the relevant platform factory.
5. Add profile editor fields and mapper support.
6. Add protocol-specific validation and tests.
