# Adaptive UI Redesign

This pass converts the shared Compose UI from a desktop-only workbench into a responsive interface for phones, tablets and desktop windows.

## Device classes

The UI now uses three shared width buckets:

- Compact: phones and narrow split-screen windows.
- Medium: tablets, foldables and narrow desktop windows.
- Expanded: full desktop workbench layouts.

The logic lives in `ui/ResponsiveLayout.kt` so screens do not duplicate breakpoint decisions.

## Shell behavior

- Compact uses a top horizontal navigation chip bar and stacked content.
- Medium uses a slim tablet rail and scrollable content.
- Expanded uses the persistent desktop sidebar and wide workbench content.

## Screen improvements

- Dashboard cards use adaptive 1/2/4-column grids.
- Profile list actions stack on compact devices and align on wide layouts.
- Profile editor fields no longer force two-column rows on phones.
- Console now adapts its command center and traffic monitor across all devices.
- History, Templates and Settings use shared two-pane behavior with compact stacking.
- Payload editors use adaptive heights instead of fixed desktop-only sizes.

## Design rule

All screen-level layout decisions remain in `commonMain`, while platform-specific code stays in Android, Desktop and iOS source sets.
