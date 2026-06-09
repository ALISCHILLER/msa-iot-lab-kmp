# راهنمای کامل راه‌اندازی و بهره‌برداری MSA IoT Lab

این فایل برای زمانی نوشته شده که پروژه را از ZIP خارج کرده‌اید و می‌خواهید آن را روی Desktop، Android یا iOS اجرا کنید. پروژه با Kotlin Multiplatform، Compose Multiplatform و Room KMP ساخته شده و UI مشترک در `composeApp/src/commonMain` قرار دارد.

## 1. نیازمندی‌های عمومی

- سیستم‌عامل توسعه: Windows، Linux یا macOS برای Android/Desktop؛ macOS برای iOS.
- JDK پیشنهادی: JDK 21.
- IDE پیشنهادی: Android Studio یا IntelliJ IDEA با پشتیبانی Kotlin Multiplatform.
- اینترنت برای اولین Gradle sync و دانلود dependencyها.
- Python 3 برای اجرای `tools/static_audit.py`.
- دسترسی شبکه به broker/backend/deviceهایی که قرار است تست شوند.

## 2. باز کردن پروژه

پروژه را از ریشه‌ای باز کنید که فایل‌های زیر را دارد:

```text
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
composeApp/build.gradle.kts
```

اگر فقط فولدر `composeApp` را باز کنید، Gradle پروژه کامل را نمی‌بیند و sync ناقص می‌شود.

## 3. Sync و بررسی اولیه

از ریشه پروژه اجرا کنید:

```bash
./gradlew --version
python3 tools/static_audit.py
```

اگر `gradlew` موجود نبود یا قابل اجرا نبود:

```bash
bash tools/bootstrap_gradle_wrapper.sh
chmod +x gradlew
```

در Windows می‌توانید از `gradlew.bat` استفاده کنید.

## 4. اجرای Desktop

Desktop بهترین target برای تست مهندسی و بررسی ترافیک زیاد است، چون UI عریض، sidebar ثابت، Command Center و Traffic Intelligence را کامل نشان می‌دهد.

```bash
./gradlew :composeApp:run
```

چک‌های پیشنهادی بعد از اجرا:

1. زبان را بین English و فارسی تغییر دهید.
2. یک profile برای MQTT یا WebSocket بسازید.
3. وارد Live Console شوید.
4. payload تستی ارسال کنید.
5. traffic cards، error/system events و history را بررسی کنید.

## 5. اجرای Android

نیازمندی‌ها:

- Android SDK نصب باشد.
- emulator یا دستگاه واقعی آماده باشد.
- دستگاه در صورت تست IoT بهتر است در همان شبکه محلی backend/device باشد.

دستور build:

```bash
./gradlew :composeApp:assembleDebug
```

یا در Android Studio، target مربوط به `composeApp` را اجرا کنید.

## 6. اجرای iOS

برای iOS حتماً macOS و Xcode لازم است. در این پروژه shared UI، Room و WebSocket آماده هستند. MQTT/TCP/UDP خام برای iOS فعلاً به‌صورت unsupported safe client مدیریت شده‌اند تا UI crash نکند. اگر iOS raw IoT testing لازم شد، باید engineهای native پشت `ProtocolClient` پیاده‌سازی شوند.

## 7. اجرای تست‌ها

```bash
./gradlew :composeApp:allTests
```

تست‌ها بخش‌های زیر را پوشش می‌دهند:

- validation پروفایل‌ها و templateها
- payload codec و JSON formatter
- protocol diagnostics و traffic analyzer
- import/export workspace
- console controller و retry flow
- localization و language code mapping
- security helpers مثل header validation و secret masking

## 8. Quality gate قبل از تحویل

قبل از تحویل پروژه به تستر یا کاربر نهایی این‌ها را اجرا کنید:

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
```

## 9. راهنمای استفاده از نرم‌افزار

مسیر پیشنهادی کاربر:

1. از Dashboard پروتکل را انتخاب کند.
2. Profile بسازد و host/port/options را وارد کند.
3. از Profiles وارد Console شود.
4. Diagnostics را بخواند و Connect بزند.
5. Payload را با TEXT/JSON/HEX/Base64 ارسال کند.
6. Traffic Intelligence را برای IN/OUT/SYSTEM/ERROR بررسی کند.
7. session history را ذخیره و مرور کند.
8. در صورت نیاز workspace را از Settings export/import کند.

## 10. نکات مخصوص پروتکل‌ها

### MQTT

- host و port بروکر را دقیق وارد کنید.
- publish topic نباید wildcard داشته باشد.
- subscribe topic می‌تواند `+` و `#` معتبر داشته باشد.
- برای محیط production از TLS و credential استفاده کنید.

### WebSocket

- برای lab از `ws://` و برای محیط امن از `wss://` استفاده کنید.
- headers باید JSON object معتبر باشد.
- header name نباید خالی یا دارای CR/LF باشد.

### TCP

- line ending و encoding را با پروتکل دستگاه هماهنگ کنید.
- برای payloadهای binary از HEX استفاده کنید.
- responseها را در history مقایسه کنید.

### UDP

- local bind port را فقط وقتی لازم است تنظیم کنید.
- broadcast را فقط برای discovery فعال کنید.
- UDP قابل اطمینان نیست و packet loss طبیعی است.

## 11. خطاهای رایج

- `Gradle distribution download failed`: اینترنت یا proxy را بررسی کنید.
- `JDK incompatible`: JDK 21 را فعال کنید.
- `Send disabled`: ارسال فقط بعد از Connected شدن فعال است.
- `MQTT publish fails`: topic، QoS، credential و ACL بروکر را بررسی کنید.
- `UDP discovery fails`: subnet، firewall و broadcast را بررسی کنید.

## 12. نکات UI دو زبانه

- زبان از داخل shell قابل تغییر است.
- فارسی با RTL نمایش داده می‌شود.
- زبان انتخاب‌شده در Room ذخیره می‌شود و بعد از اجرای دوباره باقی می‌ماند.
- متن‌های اصلی UI از `LocalizedText` و `t(...)` استفاده می‌کنند.

## 13. مسیر توسعه بعدی

- مهاجرت localization به Compose Multiplatform string resources برای پروژه بزرگ‌تر.
- افزودن file picker برای import/export.
- افزودن native TCP/UDP/MQTT برای iOS.
- افزودن secure storage برای passwordها.
- افزودن integration test با broker و socket server محلی.
