# Runbook عملیاتی MSA IoT Lab

این Runbook برای کاربر فنی، تستر IoT، توسعه‌دهنده بک‌اند و تیم پشتیبانی نوشته شده است تا بداند هنگام تست پروتکل‌ها دقیقاً چه مسیری را طی کند.

---

## 1. سناریوی تست سریع MQTT

1. وارد Profiles شوید.
2. پروتکل MQTT را انتخاب کنید.
3. host، port، clientId و topicها را وارد کنید.
4. اگر broker نیاز دارد username/password را وارد کنید.
5. profile را ذخیره کنید.
6. وارد Console شوید.
7. Diagnostics را بخوانید.
8. Connect را بزنید.
9. payload را ارسال کنید.
10. Incoming/Outgoing/System/Error را در Traffic Monitor بررسی کنید.
11. نتیجه را در History ببینید.

چک‌های مهم:

- publish topic نباید wildcard داشته باشد.
- subscribe topic می‌تواند wildcard معتبر داشته باشد.
- QoS باید بین 0 و 2 باشد.
- برای brokerهای غیرمحلی، TLS پیشنهاد می‌شود.

---

## 2. سناریوی تست WebSocket

1. profile از نوع WebSocket بسازید.
2. host و port را تنظیم کنید.
3. اگر endpoint امن است TLS را فعال کنید.
4. headerها را به شکل JSON object وارد کنید.
5. Console را باز کنید و Connect بزنید.
6. پیام text یا JSON ارسال کنید.
7. close/error eventها را در Traffic Monitor بررسی کنید.

نکته امنیتی: header name دارای CR/LF، name خالی، یا duplicate normalized header توسط parser رد می‌شود.

---

## 3. سناریوی تست TCP

1. profile TCP بسازید.
2. IP/host دستگاه یا server را وارد کنید.
3. port را وارد کنید.
4. payload encoding را انتخاب کنید.
5. برای binary payload از HEX استفاده کنید.
6. Connect بزنید و بعد payload ارسال کنید.

نکته: ارسال قبل از Connected شدن عمداً مسدود شده تا transport در state اشتباه payload نگیرد.

---

## 4. سناریوی تست UDP

1. profile UDP بسازید.
2. target host و port را وارد کنید.
3. در صورت نیاز local bind port تنظیم کنید.
4. برای discovery، broadcast را فعال کنید.
5. payload را ارسال کنید.
6. پاسخ‌ها یا packetهای دریافتی را در Console و History بررسی کنید.

نکته: UDP تضمین دریافت ندارد. نبود پاسخ همیشه به معنی خراب بودن اپ نیست؛ firewall، subnet و رفتار device را بررسی کنید.

---

## 5. سناریوی Auto-repeat

Auto-repeat برای heartbeat، polling و تست‌های سبک مناسب است.

چک‌لیست:

- ابتدا Connected شوید.
- payload معتبر وارد کنید.
- interval منطقی انتخاب کنید.
- payload size policy را رعایت کنید.
- هنگام مشاهده خطا، repeat را متوقف کنید.

---

## 6. تحلیل History

History برای پاسخ به این سؤال‌هاست:

- دستگاه چه زمانی disconnect شد؟
- آخرین پیام ورودی چه بود؟
- چند بایت ارسال/دریافت شد؟
- خطاهای اتصال تکرار شده‌اند یا موردی بوده‌اند؟
- payload تست قبلی چه بود؟

---

## 7. Export / Import

برای انتقال workspace:

1. وارد Settings شوید.
2. Export بگیرید.
3. خروجی JSON را ذخیره کنید.
4. در سیستم دیگر Import کنید.

نکته امنیتی: پسورد MQTT به صورت پیش‌فرض mask می‌شود. حتی با masking، host، port، topic و ساختار عملیاتی ممکن است حساس باشند.

---

## 8. الگوی عیب‌یابی سریع

| مشکل | بررسی سریع |
|---|---|
| Connect نمی‌شود | IP، port، firewall، VPN، TLS |
| Send غیرفعال است | state باید Connected باشد |
| MQTT auth fail | username/password/ACL/clientId |
| WebSocket close می‌شود | server policy، header، TLS، path |
| TCP response نمی‌آید | framing، line ending، binary/text encoding |
| UDP response نمی‌آید | subnet، broadcast، firewall، device behavior |
| Import fail می‌شود | schemaVersion، duplicate id، payload/template invalid |
