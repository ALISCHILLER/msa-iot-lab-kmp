# چک‌لیست نهایی Release

این چک‌لیست برای آخرین بررسی قبل از تحویل ZIP، push به Git یا انتشار نسخه داخلی است.

---

## 1. چک سورس‌کد

- [ ] هیچ `TODO` یا `FIXME` باقی نمانده باشد.
- [ ] هیچ `!!` در Kotlin code باقی نمانده باشد.
- [ ] همه type declarationها KDoc داشته باشند.
- [ ] UI به database/platform مستقیم وابسته نباشد.
- [ ] commonMain هیچ import مستقیم از JVM/iOS API نداشته باشد.
- [ ] فایل‌های بزرگ بیش از حد تقسیم شده باشند.

---

## 2. چک Gradle و Build

- [ ] `gradle/libs.versions.toml` نسخه‌ها را متمرکز نگه داشته باشد.
- [ ] `composeApp/build.gradle.kts` source setها را درست جدا کرده باشد.
- [ ] Android/Desktop/iOS dependencyها فقط در source set درست باشند.
- [ ] Room schema در repository باقی بماند.
- [ ] Gradle wrapper سالم باشد.

---

## 3. چک تست

- [ ] `python3 tools/static_audit.py` پاس شود.
- [ ] `./gradlew :composeApp:allTests` پاس شود.
- [ ] تست‌های console lifecycle پاس شوند.
- [ ] تست‌های import/export پاس شوند.
- [ ] تست‌های protocol validator پاس شوند.
- [ ] تست‌های security parser پاس شوند.

---

## 4. چک UI/UX

- [ ] Dashboard در phone/tablet/desktop خوانا باشد.
- [ ] Profile Editor روی موبایل تک‌ستونه و روی دسکتاپ حرفه‌ای باشد.
- [ ] Console روی دسکتاپ دو پنله باشد.
- [ ] History و Templates روی صفحه بزرگ دو پنله باشند.
- [ ] Guide داخل اپ قابل دسترس باشد.
- [ ] خطاهای validation قابل فهم نمایش داده شوند.

---

## 5. چک Protocol

- [ ] MQTT topic validation درست کار کند.
- [ ] WebSocket header JSON امن parse شود.
- [ ] TCP قبل از اتصال payload نگیرد.
- [ ] UDP broadcast/local bind درست validate شود.
- [ ] unsupported iOS clientها crash نکنند و event واضح بدهند.

---

## 6. چک امنیت

- [ ] Android `allowBackup=false` باشد.
- [ ] export به صورت پیش‌فرض secretها را mask کند.
- [ ] header injection با CR/LF رد شود.
- [ ] import خراب قبل از persistence رد شود.
- [ ] برای production، TLS توصیه شده باشد.

---

## 7. چک بسته‌بندی ZIP

- [ ] `.gradle/` داخل ZIP نباشد.
- [ ] `build/` داخل ZIP نباشد.
- [ ] `.idea/` داخل ZIP نباشد.
- [ ] `local.properties` داخل ZIP نباشد.
- [ ] `__pycache__` داخل ZIP نباشد.
- [ ] `zip -T` پاس شود.
