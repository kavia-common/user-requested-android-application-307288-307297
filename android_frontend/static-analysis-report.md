# Static Analysis Report (android_frontend)

Date: 2026-01-16  
Tooling: Android Lint via Gradle (`:app:lintDebug`) + Gradle deprecation warnings (`--warning-mode all`) + Gradle Problems Report review

## Commands executed

- `sh ./gradlew :app:lintDebug --no-daemon`
- `sh ./gradlew --warning-mode all :app:lintDebug --no-daemon`

Reports reviewed:

- `app/build/reports/lint-results-debug.txt`
- `build/reports/problems/problems-report.html`

## Summary (latest run)

- **Errors:** 0
- **Warnings (Android Lint):** 6
- **Warnings (Gradle deprecations / Problems Report):** 3

## Delta vs prior report

Previously reported **15** Android Lint warnings; now **6**.

**Resolved since prior report:**
- **HardcodedText** warnings in `activity_main.xml` (cell contentDescription strings) — no longer reported.
- **Overdraw** warning in `activity_main.xml` — no longer reported.

**New / still present:**
- **NestedWeights** (3 occurrences) in `activity_main.xml` (still present).
- **UnusedResources** (1): `@color/ocean_disabled_bg` in `colors.xml` (new in this run).
- **IntentFilterExportedReceiver** (1) in `AndroidManifest.xml` (still present).
- **MissingApplicationIcon** (1) in `AndroidManifest.xml` (still present).

## Android Lint Warnings (6)

Source: `app/build/reports/lint-results-debug.txt`

### 1) Android 12 exported requirement (launcher activity)
- **Severity:** Warning
- **Issue:** `IntentFilterExportedReceiver`
- **File:** `app/src/main/AndroidManifest.xml:6`
- **Details:** As of Android 12, `android:exported` must be set when an `intent-filter` is present.
- **Suggested fix:** Add `android:exported="true"` to the launcher activity (`.MainActivity`).

### 2) Nested weights (performance) — 3 warnings
- **Severity:** Warning
- **Issue:** `NestedWeights`
- **File/lines (per lint):**
  - `app/src/main/res/layout/activity_main.xml:75`
  - `app/src/main/res/layout/activity_main.xml:111`
  - `app/src/main/res/layout/activity_main.xml:147`
- **Why it matters:** LinearLayout weights trigger extra measurement passes; nested weights can multiply measurement work.
- **Suggested fix:** Consider `ConstraintLayout` or `GridLayout` for the 3x3 board (or remove nested weights).

### 3) Unused resource — 1 warning
- **Severity:** Warning
- **Issue:** `UnusedResources`
- **File:** `app/src/main/res/values/colors.xml:17`
- **Resource:** `@color/ocean_disabled_bg`
- **Suggested fix:** Remove the color if unused, or reference it (e.g., in styles/backgrounds) if intended.

### 4) Missing application icon — 1 warning
- **Severity:** Warning
- **Issue:** `MissingApplicationIcon`
- **File:** `app/src/main/AndroidManifest.xml:3`
- **Suggested fix:** Set `android:icon="@mipmap/ic_launcher"` (and add the corresponding resource), or another drawable/mipmap.

## Gradle deprecations / Problems Report (3 warnings)

These warnings were printed with `--warning-mode all` and also reflected by the Problems Report generation.

All 3 are **Deprecation** warnings associated with the Android/Gradle tooling (not directly app source):

1) Declaring `crunchPngs` as an `is-` boolean property is deprecated (Gradle 10 behavior change)
2) Declaring `useProguard` as an `is-` boolean property is deprecated (Gradle 10 behavior change)
3) Declaring `wearAppUnbundled` as an `is-` boolean property is deprecated (Gradle 10 behavior change)

**Suggested fix:** Update/adjust Android Gradle Plugin / related tooling when feasible; these are generally not fixable purely in app code.

## Notes

- Lint and build completed successfully (`BUILD SUCCESSFUL`).
- `chmod +x ./gradlew` is not permitted in this environment; all Gradle commands were executed via `sh ./gradlew ...`.
- Full details are in the generated Lint and Problems Report artifacts referenced above.
