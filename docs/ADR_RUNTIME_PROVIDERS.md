# ADR: Runtime Time and ID Providers

## Status

Accepted.

## Context

Earlier iterations used `AppClock` and `IdGenerator` directly from factories, repositories and use cases. That is convenient in production code, but it makes high-value tests harder because generated timestamps and identifiers cannot be asserted exactly.

## Decision

The project now exposes two small contracts in `core`:

```kotlin
interface TimeProvider { fun nowMillis(): Long }
interface IdProvider { fun newId(): String }
```

`AppClock` implements `TimeProvider`, and `IdGenerator` implements `IdProvider`. Production code still has ergonomic defaults, while tests inject `FixedTimeProvider` and `SequentialIdProvider`.

## Consequences

- Use cases and factories stay deterministic under test.
- UI still does not create timestamps or identifiers directly.
- Import/export, profile creation, template creation and protocol message creation can be tested with exact values.
- The change follows dependency inversion without adding a heavy DI framework dependency to common code.
