# راهنمای کامل راه‌اندازی، اجرا و Build پروژه

این راهنما برای زمانی است که پروژه را از ZIP دریافت کرده‌اید و می‌خواهید آن را بدون سردرگمی در Android Studio، IntelliJ IDEA یا terminal اجرا کنید.

---

## 1. باز کردن پروژه

1. فایل ZIP را extract کنید.
2. فولدر ریشه پروژه را باز کنید؛ همان جایی که فایل‌های زیر دیده می‌شوند:

```text
settings.gradle.kts
build.gradle.kts
gradlew
gradle/libs.versions.toml
composeApp/
iosApp/
docs/
tools/
```

3. پروژه را در Android Studio یا IntelliJ IDEA باز کنید.
4. اجازه دهید Gradle sync کامل شود.

اگر Gradle sync خطا داد، اول بخش Troubleshooting همین سند را بخوانید.

---

## 2. اجرای Audit قبل از Gradle

این دستور سریع‌ترین راه برای فهمیدن سلامت ساختار پروژه است:

```bash
python3 tools/static_audit.py
```

این ابزار dependency دانلود نمی‌کند و حتی قبل از sync کامل هم قابل اجراست.

---

## 3. اجرای Desktop

```bash
./gradlew :composeApp:run
```

برای build package دسکتاپ:

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

پیشنهاد برای تست desktop:

- یک profile WebSocket بسازید و به endpoint تست وصل شوید.
- یک profile TCP بسازید و به server داخلی وصل شوید.
- برای UDP، اول listen/bind را با port ساده تست کنید.
- از History بررسی کنید که session و message log ذخیره شده‌اند.

---

## 4. اجرای Android

روش پیشنهادی:

1. پروژه را در Android Studio باز کنید.
2. device یا emulator را انتخاب کنید.
3. configuration مربوط به `composeApp` را اجرا کنید.

روش terminal:

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug
```

برای تست روی device واقعی:

- USB debugging فعال باشد.
- گوشی و device IoT در یک شبکه باشند.
- برای تست hostهای local، IP واقعی کامپیوتر را وارد کنید، نه `localhost`.

---

## 5. اجرای iOS

برای iOS نیاز به macOS و Xcode دارید.

1. پروژه را روی macOS باز کنید.
2. Xcode را نصب و حداقل یک بار اجرا کنید.
3. Team ID را برای `iosApp` تنظیم کنید.
4. از IDE، target iOS را اجرا کنید.

وضعیت پشتیبانی iOS در این نسخه:

| قابلیت | وضعیت |
|---|---|
| Shared Compose UI | آماده |
| Room KMP | آماده |
| WebSocket | آماده |
| MQTT خام | unsupported امن |
| TCP خام | unsupported امن |
| UDP خام | unsupported امن |

---

## 6. اجرای تست‌ها

```bash
./gradlew :composeApp:allTests
```

تست‌های commonTest این بخش‌ها را پوشش می‌دهند:

- validation پروفایل‌ها
- validation templateها
- codecهای payload
- JSON formatter/minifier
- MQTT topic validation
- WebSocket header parser security
- console lifecycle
- reconnect behavior
- import/export workspace
- secret masking
- protocol diagnostics
- traffic analysis

---

## 7. اجرای Quality Script کامل

```bash
bash tools/run_quality_checks.sh
```

این script اول audit مستقل را اجرا می‌کند و بعد اگر Gradle wrapper قابل اجرا باشد، تست‌ها و buildهای اصلی را اجرا می‌کند.

---

## 8. اگر Gradle Wrapper مشکل داشت

اگر `gradlew` نبود یا خراب بود:

```bash
bash tools/bootstrap_gradle_wrapper.sh
```

یا از Android Studio/IntelliJ گزینه Gradle wrapper generation را اجرا کنید.

---

## 9. Troubleshooting رایج

### Gradle dependency download fail

علت‌های رایج:

- اینترنت یا proxy مشکل دارد.
- repositoryها در دسترس نیستند.
- Gradle cache خراب شده است.

راه‌حل‌ها:

```bash
./gradlew --refresh-dependencies
```

در صورت نیاز، cache محلی Gradle را پاک کنید و دوباره sync بزنید.

### Android build fail به خاطر SDK

- Android SDK را از SDK Manager نصب کنید.
- `compileSdk` و `targetSdk` را با SDK نصب‌شده هماهنگ نگه دارید.

### Desktop run fail

- JDK را چک کنید:

```bash
java -version
```

- مطمئن شوید JDK 21 یا حداقل نسخه سازگار با Gradle/Compose فعال است.

### Network test جواب نمی‌دهد

- host/port را چک کنید.
- firewall را بررسی کنید.
- برای موبایل، `localhost` به خود گوشی اشاره می‌کند؛ IP واقعی backend/device را وارد کنید.
- برای UDP broadcast، subnet و broadcast address را درست تنظیم کنید.

---

## 10. چک‌لیست قبل از تحویل

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
zip -T <release-file>.zip
```

قبل از انتشار، فایل‌های generated مثل `.gradle/`, `build/`, `.idea/`, `local.properties` و cacheها را وارد ZIP نهایی نکنید.
