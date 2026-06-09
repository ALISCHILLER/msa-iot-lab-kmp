# راهنمای UI دو زبانه MSA IoT Lab

این نسخه UI مشترک Compose Multiplatform را برای دو زبان انگلیسی و فارسی آماده کرده است. زبان از داخل Shell اصلی تغییر می‌کند و کل رابط کاربری اصلی، مسیرهای ناوبری، داشبورد، پروفایل‌ها، کنسول زنده، تاریخچه، قالب‌ها، تنظیمات و راهنمای داخل اپ از همان وضعیت زبان استفاده می‌کنند.

## قابلیت‌های اضافه‌شده

- `AppLanguage` برای تعریف زبان‌های پشتیبانی‌شده: English و Persian.
- `LocalAppLanguage` برای انتشار زبان فعال در کل UI مشترک `commonMain`.
- `LocalizedText` و تابع‌های `t(...)` برای انتخاب متن انگلیسی/فارسی.
- تغییر خودکار جهت چیدمان با `LocalLayoutDirection`؛ فارسی به‌صورت RTL و انگلیسی به‌صورت LTR نمایش داده می‌شود.
- دکمه تغییر زبان در Sidebar دسکتاپ، Rail تبلت و Navigation موبایل.
- localized title/summary برای `ProtocolType`، `PayloadEncoding` و `ConnectionState`.
- تست smoke برای helperهای localization در `commonTest`.

## نحوه استفاده در کد

برای متن‌های داخل composable از این الگو استفاده کن:

```kotlin
Text(t("Connect", "اتصال"))
```

برای متن‌هایی که خارج از context کامپوزی ساخته می‌شوند، زبان را صریح پاس بده:

```kotlin
t(language, "Connection Profiles", "پروفایل‌های اتصال")
```

## قوانین توسعه UI دو زبانه

1. متن‌های قابل مشاهده کاربر نباید فقط انگلیسی hardcode شوند.
2. متن‌های برند، نام پروتکل‌ها، QoS، TLS، JSON و Base64 می‌توانند همان شکل فنی خود را حفظ کنند.
3. هر صفحه جدید باید از `t(...)` یا helperهای localization استفاده کند.
4. برای فارسی، از layout direction خودکار استفاده شده؛ از force کردن alignment در صفحه‌ها خودداری کن مگر واقعاً لازم باشد.
5. خطاهای domain فعلاً انگلیسی باقی مانده‌اند؛ برای فاز بعدی می‌توان `DomainErrorLocalizer` اضافه کرد.

## چک کیفیت

قبل از تحویل اجرا کن:

```bash
python3 tools/static_audit.py
./gradlew :composeApp:allTests
./gradlew :composeApp:compileKotlinDesktop
```
