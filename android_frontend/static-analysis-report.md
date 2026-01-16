# Static Analysis Report (android_frontend)

Date: 2026-01-16  
Tooling: Android Lint via Gradle (`:app:lintDebug`) + Gradle Problems Report review

## Commands executed

- `sh ./gradlew :app:lintDebug`
- `sh ./gradlew --warning-mode all :app:lintDebug`

Reports reviewed:

- `app/build/reports/lint-results-debug.txt`
- `build/reports/problems/problems-report.html`

## Summary

- **Errors:** 0
- **Warnings (Android Lint):** 5
- **Warnings (Gradle problems/deprecations):** 7

## Android Lint Warnings (5)

### 1) Android 12 exported requirement
- **Severity:** Warning
- **Issue:** `IntentFilterExportedReceiver`
- **File:** `app/src/main/AndroidManifest.xml:6`
- **Suggested fix:** Add `android:exported="true"` to the launcher activity (`.MainActivity`).

### 2) Overdraw (performance)
- **Severity:** Warning
- **Issue:** `Overdraw`
- **File:** `app/src/main/res/layout/activity_main.xml:9`
- **Suggested fix:** Remove root background or move the background into the theme to avoid drawing the background twice.

### 3) Unused resource
- **Severity:** Warning
- **Issue:** `UnusedResources`
- **File:** `app/src/main/res/values/strings.xml:7`
- **Resource:** `@string/player_x`
- **Suggested fix:** Remove if not needed, or start referencing it.

### 4) Unused resource
- **Severity:** Warning
- **Issue:** `UnusedResources`
- **File:** `app/src/main/res/values/strings.xml:8`
- **Resource:** `@string/player_o`
- **Suggested fix:** Remove if not needed, or start referencing it.

### 5) Missing application icon
- **Severity:** Warning
- **Issue:** `MissingApplicationIcon`
- **File:** `app/src/main/AndroidManifest.xml:3`
- **Suggested fix:** Set `android:icon="@mipmap/ic_launcher"` (and add corresponding resources) or another drawable/mipmap.

## Gradle Problems Report (7 warnings)

- **Severity:** WARNING (Deprecation)
- **Problem:** “Retrieving attribute with a null key. This behavior has been deprecated. This will fail with an error in Gradle 10.”
- **Where:** Appears against various lint analyze tasks (e.g., `:list:lintAnalyzeDebug`, `:utilities:lintAnalyzeDebugAndroidTest`, `:app:lintAnalyzeDebugUnitTest`, etc.)
- **Likely source:** Build tooling / plugin internals rather than app code.
- **Suggested fix:** Upgrade/adjust plugins/tooling to avoid null attribute lookups; see the documentation link embedded in the Gradle report.

## Notes

- No detekt/ktlint tasks were identified in the available Gradle tasks list, so only Android Lint + Gradle deprecation reporting were used.
