# UI/UX Final Review

This release adds an operator-grade documentation layer and an in-app Guide screen. The UI is now designed around three usage modes:

1. **Desktop engineering workbench** with a persistent sidebar, dense metrics and two-pane console/history workflows.
2. **Tablet/foldable field workstation** with a rail navigation and adaptive card grids.
3. **Phone quick diagnostic mode** with compact navigation, stacked forms and full-width actions.

## Key UX goals

- Keep connection state visible before sending payloads.
- Keep protocol diagnostics close to command composition.
- Make incoming/outgoing traffic easy to scan during active IoT tests.
- Prevent invalid sends before the protocol client reaches `Connected`.
- Provide an embedded guide so new users understand MQTT/WebSocket/TCP/UDP workflows without leaving the app.

## New in-app guide

The `GuideScreen` explains:

- profile creation workflow
- protocol-specific checklists
- desktop workbench layout
- troubleshooting steps
- data and security notes

## Professional UX backlog

- Add searchable documentation in the Guide screen.
- Add copy buttons for guide payload examples.
- Add onboarding hints for first-run empty states.
- Add keyboard shortcuts for desktop power users.
- Add timeline charts for throughput and latency.
