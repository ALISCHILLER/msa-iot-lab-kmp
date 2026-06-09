# نیازمندی‌های کامل پروژه MSA IoT Lab

این سند، چک‌لیست رسمی نیازمندی‌های اجرای پروژه است. قبل از build یا اجرا، این موارد را آماده کنید تا پروژه بدون خطا روی Android، Desktop و در صورت نیاز iOS بالا بیاید.

---

## 1. نیازمندی‌های عمومی توسعه

| مورد | مقدار پیشنهادی | توضیح |
|---|---:|---|
| سیستم‌عامل | Windows 10/11، Linux، macOS | Android و Desktop روی هر سه قابل توسعه‌اند. iOS فقط روی macOS قابل build است. |
| JDK | 21 پیشنهاد می‌شود | برای Gradle، Android Gradle Plugin و Compose Multiplatform پایدارتر است. |
| IDE | Android Studio یا IntelliJ IDEA Ultimate/Community با KMP support | برای sync، run configuration و debug راحت‌تر. |
| اینترنت | لازم برای اولین sync | Gradle باید pluginها و dependencyها را دانلود کند. |
| RAM | حداقل 8GB، پیشنهادی 16GB+ | KMP + Compose + Room/KSP سنگین‌تر از پروژه‌های ساده است. |
| Disk | حداقل 5GB آزاد | Gradle cache، Android SDK، build outputs و emulators فضا می‌گیرند. |

---

## 2. نیازمندی‌های Android

- Android Studio جدید با Android SDK نصب‌شده
- Android SDK Platform مطابق `compileSdk` پروژه
- Android Emulator یا دستگاه واقعی
- USB debugging برای تست روی دستگاه واقعی
- دسترسی شبکه برای تست protocolها

دستورات مهم:

```bash
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug
```

نکته امنیتی: اپ برای تست IoT محلی از network security config استفاده می‌کند. برای production، TLS و سیاست‌های شبکه باید سخت‌گیرانه‌تر شوند.

---

## 3. نیازمندی‌های Desktop

- JDK 21
- Gradle wrapper موجود در پروژه
- دسترسی شبکه local/LAN برای تست TCP/UDP/MQTT/WebSocket

اجرای دسکتاپ:

```bash
./gradlew :composeApp:run
```

ساخت package دسکتاپ:

```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

خروجی native distribution به سیستم‌عامل بستگی دارد؛ مثل DMG برای macOS، MSI برای Windows و DEB برای Linux.

---

## 4. نیازمندی‌های iOS

- فقط macOS
- Xcode نصب‌شده
- Apple Developer account برای اجرای روی دستگاه واقعی
- Team ID تنظیم‌شده در Xcode

اجرای iOS از IDE انجام می‌شود. source set مربوط به iOS داخل پروژه وجود دارد، اما MQTT/TCP/UDP خام فعلاً به‌صورت unsupported client امن پیاده شده‌اند. WebSocket، Room و shared UI آماده هستند.

---

## 5. نیازمندی‌های شبکه برای تست IoT

برای تست واقعی، حداقل یکی از این endpointها را داشته باشید:

- MQTT broker مانند Mosquitto، HiveMQ broker یا broker داخلی شرکت
- WebSocket backend تستی
- TCP server یا device با raw socket
- UDP listener/device برای discovery یا command packet

چک‌لیست شبکه:

- دستگاه موبایل/کامپیوتر و device در یک subnet باشند.
- VPN یا firewall پورت‌ها را نبسته باشد.
- برای UDP broadcast، broadcast address و local bind port را درست تنظیم کنید.
- برای TLS، certificate و hostname با هم سازگار باشند.

---

## 6. نیازمندی‌های کیفیت و تست

قبل از تحویل یا merge، این دستورات را اجرا کنید:

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
```

`static_audit.py` بدون نیاز به دانلود dependency اجرا می‌شود و مواردی مثل KDoc، package/path، boundaryهای معماری، TODO/FIXME، non-null assertion و importهای حساس UI را بررسی می‌کند.

---

## 7. نسخه‌های مهم استفاده‌شده در پروژه

نسخه‌ها در `gradle/libs.versions.toml` متمرکز شده‌اند. قبل از upgrade، compatibility جدول Kotlin/Compose/AGP را بررسی کنید و upgrade را در یک branch جدا انجام دهید.

فایل‌های مهم:

```text
gradle/libs.versions.toml
settings.gradle.kts
build.gradle.kts
composeApp/build.gradle.kts
gradle.properties
gradle/wrapper/gradle-wrapper.properties
```
