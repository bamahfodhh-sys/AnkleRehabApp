# تأهيل الكاحل — Android v1.1

تطبيق Android أصلي، Offline-first، لمتابعة برنامج منزلي متدرج لتأهيل الكاحل والشظية على مدى 6 أسابيع تدريبية. إذا تأخر تأكيد الانتقال بين المراحل، تتأخر تواريخ المرحلة التالية بدل أن يقفز التطبيق تلقائيًا للأمام.

## ما تم تنفيذه

- Kotlin + Jetpack Compose + Material 3.
- Room Database لسجل الجلسات والـChecklist والملاحظات.
- DataStore لإعدادات الجدول والتذكيرات.
- ViewModel + Repository للفصل بين الواجهة والبيانات.
- 3 مراحل × أسبوعين، مع 42 يومًا برمجيًا.
- 34 جلسة تدريب افتراضيًا عند اختيار 6 أيام في الأسبوعين 3–4، أو 32 جلسة عند اختيار 5 أيام.
- انتقال المرحلة 1 → 2 والمرحلة 2 → 3 لا يحدث تلقائيًا لمجرد مرور الوقت؛ يحتاج تأكيد المستخدم، وتبدأ المرحلة الجديدة من يوم التأكيد.
- الجلسات المستقبلية Preview فقط ولا يمكن تسجيلها.
- حالات الجلسة: مجدولة، قيد التنفيذ، مكتملة، جزئية، تم التخطي، أوقفت بسبب ألم/أعراض.
- جرعتان منفصلتان لكرشمة الفوطة في المرحلة الأولى، مع تذكير اختياري مستقل للجرعة الثانية.
- مؤقت 1 دقيقة للتهيئة و10 دقائق لرفع القدم.
- تذكير الجلسة باستخدام AlarmManager كمنبه مرن غير دقيق بالدقيقة، مع إعادة الجدولة بعد إعادة تشغيل الهاتف وتغيير الوقت أو المنطقة الزمنية.
- لا Internet permission، لا حساب، لا Analytics، لا Cloud backend.
- الملاحظات وسجل الجلسات مستبعدان من Android cloud backup/device transfer.
- واجهة RTL عربية، Edge-to-edge، وتخطيط متكيف مع الهاتف والشاشات الكبيرة.

## هيكل المشروع

```text
app/src/main/java/com/sanad/anklerehab/
├── MainActivity.kt
├── RehabApplication.kt
├── AppContainer.kt
├── data/
│   ├── RehabRepository.kt
│   ├── db/
│   │   ├── Entities.kt
│   │   ├── RehabDao.kt
│   │   └── RehabDatabase.kt
│   └── settings/
│       └── SettingsRepository.kt
├── domain/
│   ├── Models.kt
│   ├── PlanCatalog.kt
│   ├── ScheduleEngine.kt
│   └── SessionPolicy.kt
├── reminder/
│   ├── NotificationChannels.kt
│   ├── ReminderReceiver.kt
│   ├── ReminderScheduler.kt
│   └── SystemEventReceiver.kt
└── ui/
    ├── MainViewModel.kt
    ├── RehabApp.kt
    ├── Screens.kt
    └── theme/Theme.kt
```

## البناء

المشروع مضبوط على:

- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Kotlin 2.2.21
- JDK 17
- compileSdk / targetSdk 36
- Compose BOM 2026.06.00 (Compose 1.11.4-compatible line)
- Room 2.8.5
- DataStore 1.2.1

### GitHub Actions

ارفع محتويات المشروع إلى مستودع GitHub ثم شغّل workflow باسم **Android QA and APK**. سيقوم بالترتيب:

1. Unit tests
2. Android Lint
3. assembleDebug
4. رفع `app-debug.apk` كـArtifact قابل للتنزيل

## حدود النسخة

- لا يوجد Backend سحابي عمدًا؛ البيانات محلية فقط.
- التنبيه مرن وليس Exact Alarm، لذلك قد يؤخر Android الإشعار قليلًا لتوفير البطارية.
- الانتقال بين المراحل يحتاج تأكيدًا من المستخدم، ولا يمثل تقييمًا طبيًا أو تصريحًا تلقائيًا بزيادة التحميل.
