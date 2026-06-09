# Final Delivery Notes

This package is the cleaned and production-checked KMP delivery for MSA IoT Lab.

## Verification performed

- Static audit passed with zero errors.
- ZIP cache artifacts were removed before packaging.
- Header parser security tests were added.
- Console lifecycle persistence tests were expanded.
- TCP/UDP/MQTT failure cleanup was improved.

## Suggested local verification

```bash
python3 tools/static_audit.py
bash tools/bootstrap_gradle_wrapper.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:run
```

## Platform support

- Android: MQTT, WebSocket, TCP, UDP, Room, shared Compose UI.
- Desktop JVM: MQTT, WebSocket, TCP, UDP, Room, shared Compose UI.
- iOS: Room, shared Compose UI and WebSocket; raw MQTT/TCP/UDP are intentionally unsupported stubs in this build.

## Documentation and operator-guide completion pass

This pass added a complete Persian operator manual and an in-app Guide destination. It also fixed a missing Compose `FontWeight` import in `ConsoleScreen` and strengthened the static audit tool to catch required-import issues for common UI symbols.

### Added

- `docs/COMPLETE_OPERATOR_GUIDE_FA.md`
- `docs/UI_UX_FINAL_REVIEW.md`
- `GuideScreen` in shared Compose UI
- `Guide` route in the adaptive workbench navigation
- Dashboard quick action to open the operator guide

### Verified

```text
Kotlin files: 131
Type declarations: 169
KDoc-covered declarations: 169
Static audit errors: 0
```

## Final documentation and setup guide pass

This delivery adds a complete Persian setup/requirements/runbook documentation pack, updates the in-app Guide screen with setup and quality-gate instructions, and keeps all KDoc and architecture audit checks passing. The project now contains explicit operator, developer, release and code-commenting guides.


## Final setup and requirements documentation pass

This final package includes a complete Persian setup and requirements documentation set:

- `docs/PROJECT_DOCUMENTATION_INDEX_FA.md`
- `docs/REQUIREMENTS_FA.md`
- `docs/SETUP_AND_RUN_FA.md`
- `docs/OPERATOR_RUNBOOK_FA.md`
- `docs/DEVELOPER_GUIDE_FA.md`
- `docs/FINAL_RELEASE_CHECKLIST_FA.md`
- `docs/COMMENTING_AND_CODE_STYLE_FA.md`

The in-app Guide screen was also expanded with setup, platform and quality-gate instructions. Dependency-free static audit passed with zero errors after this update.
