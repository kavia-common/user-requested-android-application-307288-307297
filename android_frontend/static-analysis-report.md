# Static Analysis Report (android_frontend)

Date: 2026-01-16  
Tooling: Android Lint via Gradle (`:app:lintDebug`) + Gradle Problems Report review

## Commands executed

- `sh ./gradlew :app:lintDebug`
- `sh ./gradlew --warning-mode all :app:lintDebug`

Reports reviewed:

- `app/build/reports/lint-results-debug.txt`
- `build/reports/problems/problems-report.html`

## Summary (latest run)

- **Errors:** 0
- **Warnings (Android Lint):** 15
- **Warnings (Gradle problems/deprecations):** 3

## Delta vs prior report (when available)

Previously reported **5** Android Lint warnings; now **15**.

**New warnings introduced (10):**
- **NestedWeights** (3 occurrences) in `activity_main.xml`
- **HardcodedText** (9 occurrences) in `activity_main.xml` (cell content descriptions)

**No longer present:**
- `UnusedResources` warnings for `@string/player_x` and `@string/player_o` (these are now referenced in `TicTacToeState.statusText()`)

## Android Lint Warnings (15)

### 1) Android 12 exported requirement
- **Severity:** Warning
- **Issue:** `IntentFilterExportedReceiver`
- **File:** `app/src/main/AndroidManifest.xml:6`
- **Suggested fix:** Add `android:exported="true"` to the launcher activity (`.MainActivity`).

### 2) Overdraw (performance)
- **Severity:** Warning
- **Issue:** `Overdraw`
- **File:** `app/src/main/res/layout/activity_main.xml:9`
- **Suggested fix:** Remove root background or move background into the theme (or use a theme with null window background) to avoid drawing background twice.

### 3) Missing application icon
- **Severity:** Warning
- **Issue:** `MissingApplicationIcon`
- **File:** `app/src/main/AndroidManifest.xml:3`
- **Suggested fix:** Set `android:icon="@mipmap/ic_launcher"` (and add corresponding resources), or another drawable/mipmap.

### 4) Nested weights (performance) — 3 warnings
- **Severity:** Warning
- **Issue:** `NestedWeights`
- **Files/lines:**
  - `app/src/main/res/layout/activity_main.xml:76`
  - `app/src/main/res/layout/activity_main.xml:139`
  - `app/src/main/res/layout/activity_main.xml:202`
- **Why it matters:** LinearLayout weights cause additional measure passes; nested weighted LinearLayouts can cause a large performance hit.
- **Suggested fix:** Consider using `ConstraintLayout` for the board grid, `GridLayout`, or avoid nested weights.

### 5) Hardcoded contentDescription strings — 9 warnings
- **Severity:** Warning
- **Issue:** `HardcodedText`
- **Files/lines:** `app/src/main/res/layout/activity_main.xml` lines 78, 96, 114, 141, 159, 177, 204, 222, 240
- **Suggested fix:** Move “Cell 1”…”Cell 9” into `strings.xml` resources (or use a single formatted string like “Cell %1$d”).

## Gradle Problems Report (latest: 3 warnings)

All 3 are **Deprecation** warnings associated with the Android/Gradle tooling (plugin `com.android.internal.application`), not directly app code:

1) Declaring `crunchPngs` as an `is-` boolean property is deprecated (Gradle 10 behavior change)
2) Declaring `useProguard` as an `is-` boolean property is deprecated (Gradle 10 behavior change)
3) Declaring `wearAppUnbundled` as an `is-` boolean property is deprecated (Gradle 10 behavior change)

**Suggested fix:** Upgrade/adjust Android Gradle Plugin / related tooling when feasible; these are not typically fixable in app source.

## Notes

- Build and lint run succeeded (`0 errors`).
- This report intentionally summarizes warnings; full details are in the generated lint/proble report files listed above.
