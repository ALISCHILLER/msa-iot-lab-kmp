# گزارش نهایی نسخه UI و راه‌اندازی حرفه‌ای

این نسخه برای تحویل عملیاتی آماده‌سازی شده است و تمرکز آن روی سه محور بوده است:

1. UI حرفه‌ای‌تر برای Desktop، Tablet و Phone.
2. پشتیبانی دو زبانه English/Persian با RTL برای فارسی و ذخیره زبان انتخاب‌شده در Room.
3. راهنمای کامل راه‌اندازی و بهره‌برداری داخل پروژه و داخل خود اپلیکیشن.

## تغییرات کلیدی

- اضافه شدن `AppSettingsRepository` و پیاده‌سازی Room-backed برای ذخیره زبان.
- اضافه شدن `GuideSetupSections.kt` برای نمایش setup runbook، platform requirements و quality gate داخل اپ.
- کامل شدن مستندات فارسی و انگلیسی راه‌اندازی.
- حفظ KDoc کامل برای تمام class/interface/object/enum/data/sealed declarationها.
- عبور کامل از static audit.

## خروجی audit

```text
Kotlin files: 140
Type declarations: 177
KDoc-covered declarations: 177
Errors: 0
```

## مسیر راه‌اندازی پیشنهادی

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:run
```

اگر Gradle wrapper روی سیستم آماده نبود:

```bash
bash tools/bootstrap_gradle_wrapper.sh
chmod +x gradlew
```
