# Quality Audit

This delivery includes a dependency-free static audit that can run before Gradle dependencies are downloaded.

## Latest local audit result

```text
Kotlin files: 120
Common test files: 27
Type declarations: 154
KDoc-covered declarations: 154
Static audit errors: 0
```

## Checks covered

- Exactly one package declaration per Kotlin file.
- Package path matches the folder path under `kotlin/`.
- Balanced braces after stripping strings and comments.
- KDoc above every Kotlin type declaration.
- No `TODO`, `FIXME`, or non-null assertions.
- No generated Python cache files committed.
- No direct database/platform imports inside Compose UI.
- No direct timestamp or ID generation inside Compose UI.
- No Compose dependency inside protocol/domain layers.
- No JVM/iOS platform APIs inside `commonMain`.
- Explicit `kotlin.test` imports for every test assertion.
- Basic Kotlin syntax-marker scan across `commonMain` and `commonTest`.
- Android security basics: `INTERNET`, disabled backup, explicit network security config.

## Run locally

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
```

`run_quality_checks.sh` will run Gradle tasks when `./gradlew` exists.


## Latest Final Pass

- Kotlin files: 121
- Common test files: 28
- Type declarations: 155
- KDoc coverage: 155/155
- Static audit errors: 0
- Added auto-reconnect session lifecycle regression coverage.

## Adaptive all-device UI pass

This version adds a responsive Compose UI layer for compact phones, medium tablets/foldables and expanded desktop windows. Navigation, grids, action rows, console panes, profile forms, history, templates and settings now adapt through shared `ResponsiveLayout.kt` helpers.


## Final documentation and UI-guide pass

- Added `GuideScreen` to the shared Compose UI.
- Added `docs/COMPLETE_OPERATOR_GUIDE_FA.md` as the complete Persian operator manual.
- Added `docs/UI_UX_FINAL_REVIEW.md` for the final UX summary.
- Fixed a missing `FontWeight` import in `ConsoleScreen`.
- Strengthened `tools/static_audit.py` with a required-import guard for common Compose UI symbols.

Latest static audit result:

```text
Kotlin files: 131
Type declarations: 169
KDoc-covered declarations: 169
Errors: 0
```

## Final setup/documentation polish pass

- Added complete Persian requirements, setup/run, operator runbook, developer guide, release checklist, code-commenting guide and documentation index.
- Updated in-app `GuideScreen` with setup, platform requirements and quality-gate guidance.
- Re-ran dependency-free static audit after all documentation/UI changes.

Latest static audit result:

```text
Kotlin files: 131
Type declarations: 169
KDoc-covered declarations: 169
Errors: 0
```

Gradle execution note: `./gradlew --version --offline` was attempted in the sandbox, but the wrapper could not download `gradle-9.5.0-bin.zip` because the environment has no network access to `services.gradle.org`. Run the Gradle tasks on a local machine with internet access or a pre-populated Gradle distribution cache.


## Bilingual UI Update

- Added runtime English/Persian language switcher.
- Added RTL layout direction for Persian.
- Added docs/BILINGUAL_UI_GUIDE_FA.md and docs/UI_BILINGUAL_FINAL_REVIEW.md.
- Added LocalizationSmokeTest for explicit language helpers.
