# UI Bilingual Final Review

## Summary

The UI has been upgraded from English-only hardcoded screens to a bilingual runtime experience. The shared Compose UI now exposes a language switcher in the adaptive shell and propagates language through a CompositionLocal. Persian also switches layout direction to RTL.

## Screens covered

- Workbench shell and navigation
- Dashboard
- Profile list
- Profile editor
- Live console command center
- Traffic intelligence panel
- History
- Payload templates
- Workspace settings
- Operator guide

## Architecture decisions

- Localization is implemented in `commonMain`, so Android, Desktop and iOS share the same bilingual UI layer.
- Runtime language is intentionally kept in UI state for now. Persisting the selected language can be added later using `AppSettingDao`.
- Compose resources are still compatible with this architecture if the project later moves strings to XML resources.

## Known next-step improvements

- Persist selected language in Room settings.
- Add a `DomainErrorLocalizer` for validation and protocol error messages.
- Move large text dictionaries to Compose Multiplatform string resources if translation volume grows.
