# Desktop UI Redesign Pass

This pass upgrades the shared Compose Multiplatform UI into a desktop-first protocol workbench while keeping the implementation inside `commonMain`.

## What changed

- Added a persistent workbench shell with sidebar navigation for desktop widths.
- Added compact top navigation fallback for Android and narrow windows.
- Reworked Dashboard into a protocol launcher plus workspace health panel.
- Reworked Profiles into an inventory panel and saved-profile browser.
- Reworked Console into a two-pane command center and traffic monitor.
- Reworked History into separate session and message panels.
- Reworked Templates into editor/library panes.
- Reworked Settings into side-by-side import/export panels.
- Added shared UI primitives: `SectionCard`, `StatusBadge`, `MetricCard`, `MetricRow`, and `EmptyState`.

## Desktop focus

The desktop layout is optimized around a workbench pattern:

- left navigation stays visible;
- active route context appears in the header;
- metrics summarize local Room data;
- console input and output are visible at the same time;
- message streams use bounded vertical panels instead of full-page scrolling.

## KMP alignment

The Kotlin docs recommend writing common code whenever possible and limiting platform source sets to platform-specific APIs and UX flows. The redesign follows that model: all shared UI remains in `composeApp/src/commonMain`, while Android/Desktop/iOS entry points still only provide platform database and protocol factories.
