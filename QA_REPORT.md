# QA Report — v1.1

## P0 fixes implemented

- [x] منع تسجيل الجلسات المستقبلية.
- [x] فصل مرور الوقت عن فتح المرحلة العلاجية التالية.
- [x] بدء المرحلة 2/3 من تاريخ التأكيد بدل القفز لجلسات فاتت.
- [x] تغيير تاريخ البداية بخيارين: نقل الجدول مع التقدم أو بدء جديد ومسح التقدم.
- [x] Room بدل SharedPreferences للسجل المنظم.
- [x] DataStore لإعدادات التطبيق.
- [x] جرعة كرشمة الفوطة الثانية كعنصر مستقل وتذكير مستقل.
- [x] إزالة قفل Portrait وتطبيق Edge-to-edge/Responsive layout.
- [x] Unit tests لمنطق الخطة والجدول والسياسات.

## Functional cases covered in code/tests

- Day 1 / Day 14 / transition to phase 2.
- Phase 2 confirmation and delayed start.
- Transition to phase 3.
- Program completion after phase 3.
- Future session read-only.
- Rest days.
- 5 vs 6 training days in phase 2.
- Two towel doses required for full completion in phase 1.
- Completed / partial / skipped / stopped-due-to-symptoms states.
- Reopen completed/skipped/stopped sessions.
- Notification permission requested only when the user enables reminders.
- Re-scheduling on boot/time/timezone changes.

## Local verification performed in this workspace

- Pure Kotlin domain compilation: PASS.
- Domain QA runner: PASS (`DOMAIN_QA_OK`).
- XML parse validation: PASS (`XML_QA_OK`).

## Build verification remaining

This workspace does not contain Android SDK/Gradle dependency caches, so a full Android `lintDebug/testDebugUnitTest/assembleDebug` cannot be executed locally here. The included GitHub Actions workflow performs all three in a clean Android-capable CI environment and fails if any step fails.

## Dependency compatibility QA

- Compose BOM intentionally pinned to `2026.06.00` (Compose 1.11.4 line), because Compose 1.12 requires compileSdk 37 / newer AGP.
- Navigation intentionally pinned to `2.9.8`; Navigation 2.10 moved its Compose compileSdk to API 37 and is not compatible with this AGP 8.13/API 36 baseline.
- AGP 8.13.2 + Gradle 8.13 + JDK 17 + Kotlin 2.2.21 stay on the API 36-compatible toolchain.

- [x] Added Compose instrumentation smoke test for the Today screen.
- [x] CI compiles the Android test APK (`assembleDebugAndroidTest`) in addition to unit tests, lint, and app APK assembly.

## Final static QA pass

- [x] No `SharedPreferences` references in app/build code.
- [x] No `INTERNET` permission.
- [x] No portrait orientation lock.
- [x] Room / DataStore / ViewModel / Navigation are present in the source architecture.
- [x] Future-session edit guard is present.
- [x] Explicit phase-transition gate is present.
- [x] Second towel-curl dose is modeled independently.
- [x] Boot and timezone/time-change reminder rescheduling are present.
- [x] No TODO/FIXME/HACK markers remain.
- [x] Kotlin parser heuristic found no syntax/parser diagnostics; full Android symbol resolution still requires the Android/Compose classpath in CI.
