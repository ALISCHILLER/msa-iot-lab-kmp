# راهنمای کامل کاربری و عملیاتی MSA IoT Lab

این سند، راهنمای اصلی استفاده از پروژه **MSA IoT Lab** است؛ یک ابزار Kotlin Multiplatform و Compose Multiplatform برای تست حرفه‌ای پروتکل‌های IoT و بک‌اندهای real-time. هدف پروژه این است که برای MQTT، WebSocket، TCP و UDP تجربه‌ای شبیه Postman، اما مخصوص دنیای IoT فراهم کند.

---

## 1. خلاصه پروژه

MSA IoT Lab برای این سناریوها ساخته شده است:

- تست brokerهای MQTT و topicهای publish/subscribe
- تست WebSocket gatewayهای بک‌اند
- ارسال/دریافت raw TCP برای دستگاه‌ها، gatewayها یا سرویس‌های صنعتی
- ارسال UDP، listen روی local port و broadcast برای discovery دستگاه‌ها
- ذخیره connection profileها برای endpointهای پرتکرار
- مشاهده live traffic شامل IN / OUT / SYSTEM / ERROR
- ذخیره sessionها و message logها در Room KMP
- ساخت payload templateهای reusable
- export/import کردن workspace برای انتقال بین سیستم‌ها
- اجرای auto-repeat برای heartbeat، status request یا stress سبک

---

## 2. معماری اجرایی اپلیکیشن

ساختار runtime پروژه به شکل زیر است:

```text
Compose UI
  -> Controllers / UseCases / Repository Contracts / Validators
      -> Domain Models / Runtime Providers / Protocol Contracts
          -> Room Implementations / Platform Protocol Clients
```

نکته‌های مهم:

- UI فقط render و delegate می‌کند.
- validation در کلاس‌های domain و use-caseها انجام می‌شود.
- ذخیره‌سازی با Room KMP در لایه repository انجام می‌شود.
- پروتکل‌ها از طریق `ProtocolClient` abstraction پیاده‌سازی شده‌اند.
- Android و Desktop پیاده‌سازی کامل MQTT/TCP/UDP/WebSocket دارند.
- iOS فعلاً Room + UI + WebSocket دارد و برای MQTT/TCP/UDP، unsupported client امن برمی‌گرداند.

---

## 3. اجرای پروژه

### 3.1 اجرای دسکتاپ

```bash
./gradlew :composeApp:run
```

اگر wrapper آماده نبود:

```bash
bash tools/bootstrap_gradle_wrapper.sh
```

سپس دوباره دستور اجرا را بزنید.

### 3.2 اجرای Android

پروژه را در Android Studio یا IntelliJ IDEA باز کنید و target مربوط به `composeApp` را اجرا کنید.

### 3.3 اجرای تست‌ها

```bash
./gradlew :composeApp:allTests
```

### 3.4 اجرای چک کیفیت مستقل

```bash
python3 tools/static_audit.py
bash tools/run_quality_checks.sh
```

---

## 4. راهنمای UI برای همه دستگاه‌ها

### 4.1 دسکتاپ

در دسکتاپ، اپلیکیشن به شکل یک workbench حرفه‌ای طراحی شده است:

- sidebar ثابت برای navigation
- header شامل title، subtitle و metricهای مهم
- dashboard با کارت‌های protocol و workspace health
- console دو پنله شامل Command Center و Traffic Intelligence
- history، template و settings با layout دو پنله

### 4.2 تبلت و Foldable

در عرض‌های متوسط:

- navigation rail نمایش داده می‌شود
- کارت‌ها دو ستونه می‌شوند
- فرم‌ها فشرده‌تر اما خوانا باقی می‌مانند
- صفحات اصلی هنوز حس workbench دارند

### 4.3 موبایل

در موبایل:

- navigation به chipهای افقی تبدیل می‌شود
- فرم‌ها تک‌ستونه هستند
- دکمه‌ها full-width یا stacked هستند
- console به صورت stacked command + traffic نمایش داده می‌شود

---

## 5. Workflow اصلی استفاده

### مرحله 1: ساخت Profile

از Dashboard یا صفحه Profiles، یک profile بسازید.

فیلدهای عمومی:

- نام profile
- نوع protocol
- host یا IP
- port
- TLS enabled/disabled
- timeout
- auto reconnect
- payload encoding

### مرحله 2: تنظیم optionهای پروتکل

هر protocol optionهای مخصوص خودش را دارد:

- MQTT: clientId، username، password، topic، QoS، retain، keepAlive
- WebSocket: headers JSON، TLS، endpoint
- TCP: buffer، line ending و encoding behavior
- UDP: local bind port، broadcast mode و target endpoint

### مرحله 3: ورود به Live Console

بعد از ذخیره profile وارد Console شوید. Console دو بخش اصلی دارد:

- **Command Center:** اتصال، ارسال payload، format کردن JSON، auto-repeat و template picker
- **Traffic Intelligence:** رویدادها، counters، خطاها، system events و آخرین وضعیت runtime

### مرحله 4: ارسال payload

ابتدا Connect را بزنید. ارسال payload فقط وقتی مجاز است که state برابر Connected باشد. این guard از ارسال اشتباه روی transport قطع‌شده جلوگیری می‌کند.

### مرحله 5: بررسی History

پس از تست، به History بروید و sessionها و message logها را ببینید. sessionها برای تحلیل رفتار دستگاه و مقایسه تست‌های قبلی مفید هستند.

---

## 6. راهنمای MQTT

### تنظیمات پیشنهادی

- broker host و port را دقیق وارد کنید.
- برای brokerهای عمومی یا production از TLS استفاده کنید.
- clientId را unique نگه دارید.
- topicهای publish نباید wildcard داشته باشند.
- topicهای subscribe می‌توانند wildcard معتبر `+` یا `#` داشته باشند.

### مثال payload

```json
{
  "cmd": "status",
  "ts": {timestamp},
  "requestId": "{uuid}"
}
```

### خطاهای رایج

- authentication failure: username/password یا ACL اشتباه است.
- topic rejected: wildcard یا topic format نامعتبر است.
- connection timeout: host/port/firewall/TLS را چک کنید.

---

## 7. راهنمای WebSocket

### تنظیمات پیشنهادی

- برای محیط local از `ws://` و برای محیط امن از `wss://` استفاده کنید.
- headerها باید JSON object معتبر باشند.
- header name خالی، duplicate یا دارای CR/LF پذیرفته نمی‌شود.

### مثال headers

```json
{
  "Authorization": "Bearer token",
  "X-Device-Id": "lab-device-01"
}
```

### خطاهای رایج

- invalid header JSON: فرمت JSON خراب است.
- close frame: سمت سرور اتصال را بسته است.
- TLS error: certificate یا scheme را بررسی کنید.

---

## 8. راهنمای TCP

TCP برای دستگاه‌هایی مناسب است که raw socket دارند.

### نکته‌های مهم

- TCP connection-oriented است؛ پس قبل از send باید Connected باشید.
- اگر دستگاه line-based است، line ending را در payload رعایت کنید.
- برای payloadهای binary از HEX استفاده کنید.
- پاسخ‌ها در Traffic Intelligence و History ذخیره می‌شوند.

### مثال HEX

```text
AA 01 00 FF
```

---

## 9. راهنمای UDP

UDP برای discovery، broadcast و پیام‌های سبک مناسب است.

### نکته‌های مهم

- UDP connectionless است و تضمین دریافت ندارد.
- برای discovery، broadcast را فعال کنید.
- local bind port را فقط وقتی نیاز دارید مشخص کنید.
- firewall و subnet را بررسی کنید.

### سناریوی رایج

1. target را broadcast subnet بگذارید.
2. payload discovery بفرستید.
3. روی local port مناسب listen کنید.
4. پاسخ‌های deviceها را در Traffic Intelligence ببینید.

---

## 10. Payload encoding

پشتیبانی فعلی:

- TEXT
- JSON
- HEX
- BASE64

### JSON

- Pretty Print برای خوانایی
- Minify برای ارسال فشرده
- validation قبل از ارسال

### HEX

- مناسب binary protocolها
- فاصله بین byteها قابل قبول است

### Variables

در payloadها می‌توانید از این placeholderها استفاده کنید:

```text
{timestamp}
{uuid}
{counter}
```

---

## 11. Templateها

Templateها برای payloadهای پرتکرار هستند، مثل:

- heartbeat
- status request
- device reset command
- MQTT command
- UDP discovery

در صفحه Templates می‌توانید template بسازید، ذخیره کنید و در Console آن را load کنید.

---

## 12. Import / Export

در Settings می‌توانید workspace را export/import کنید.

### Export شامل چیست؟

- profiles
- templates
- sessions
- messages

### نکته امنیتی

پسورد MQTT به صورت پیش‌فرض mask می‌شود. با این حال export می‌تواند host، port، topic و structure سیستم شما را نشان دهد؛ پس آن را مثل داده حساس مدیریت کنید.

---

## 13. Troubleshooting سریع

### اتصال برقرار نمی‌شود

- host/IP را چک کنید.
- port را چک کنید.
- firewall/VPN را بررسی کنید.
- TLS/plain mode را درست انتخاب کنید.
- برای Android، permission اینترنت در manifest وجود دارد.

### Send غیرفعال است

- اتصال هنوز Connected نشده است.
- payload validation خطا دارد.
- protocol فعلی روی platform اجراشده send را پشتیبانی نمی‌کند.

### Auto-repeat کار نمی‌کند

- باید Connected باشید.
- delay باید عدد معتبر باشد.
- payload باید از نظر encoding معتبر باشد.

### UDP پاسخ نمی‌دهد

- subnet و broadcast address را چک کنید.
- device و laptop/mobile باید در یک شبکه باشند.
- local firewall ممکن است packetها را block کند.

---

## 14. تست و کیفیت

پروژه شامل تست برای این بخش‌هاست:

- payload codec
- payload size policy
- MQTT topic validation
- WebSocket header validation
- profile validation
- template validation
- import/export validation
- console controller connection guard
- reconnect behavior
- session lifecycle
- protocol traffic analyzer
- protocol profile inspector
- secret masking

چک کیفیت مستقل:

```bash
python3 tools/static_audit.py
```

این ابزار موارد زیر را بررسی می‌کند:

- وجود package declaration
- تطابق package با path
- brace balance
- KDoc بالای همه type declarationها
- نبود TODO/FIXME
- نبود non-null assertion خطرناک `!!`
- رعایت boundary بین UI و database/platform
- وجود importهای ضروری برای symbolهای رایج UI

---

## 15. مسیر توسعه بعدی

پیشنهادهای بعدی برای تبدیل پروژه به محصول کامل‌تر:

- file picker واقعی برای import/export
- secure storage برای پسوردها
- TLS custom trust store
- native iOS TCP/UDP/MQTT engine
- paging برای message logهای خیلی بزرگ
- integration test با broker و socket server واقعی
- charts برای latency و throughput
- device discovery dashboard
- profile grouping و tagging

---

## 16. چک‌لیست قبل از Release

```text
[ ] static_audit.py پاس شود
[ ] allTests پاس شود
[ ] Desktop run تست شود
[ ] Android assembleDebug تست شود
[ ] حداقل یک MQTT broker تست شود
[ ] یک WebSocket echo server تست شود
[ ] TCP local server تست شود
[ ] UDP listener/broadcast تست شود
[ ] import/export روی workspace نمونه تست شود
[ ] Android network security config بازبینی شود
[ ] README و docs به‌روز باشند
```

این راهنما باید همراه پروژه نگهداری شود و هر feature جدیدی که به app اضافه می‌شود، در همین فایل هم مستند شود.
---

## 18. مسیر پیشنهادی مطالعه مستندات

برای راه‌اندازی کامل پروژه، این ترتیب مطالعه پیشنهاد می‌شود:

1. `docs/REQUIREMENTS_FA.md` برای آماده‌سازی سیستم و ابزارها
2. `docs/SETUP_AND_RUN_FA.md` برای اجرای Android/Desktop/iOS
3. `docs/OPERATOR_RUNBOOK_FA.md` برای سناریوهای واقعی تست پروتکل‌ها
4. `docs/TESTING_STRATEGY.md` برای اجرای تست‌ها و فهم coverage
5. `docs/FINAL_RELEASE_CHECKLIST_FA.md` برای کنترل نهایی قبل از تحویل

اگر هدف شما توسعه پروژه است، بعد از این‌ها `docs/DEVELOPER_GUIDE_FA.md` و `docs/SOLID_ARCHITECTURE_NOTES.md` را بخوانید.

