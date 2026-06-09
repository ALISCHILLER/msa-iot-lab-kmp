# راهنمای توسعه‌دهنده MSA IoT Lab

این سند برای کسی است که می‌خواهد پروژه را توسعه دهد، پروتکل جدید اضافه کند، UI را گسترش دهد یا تست‌های حرفه‌ای‌تر بنویسد.

---

## 1. اصل معماری

قاعده اصلی پروژه:

```text
UI نباید transport، database، timestamp، id generation یا validation خام را مستقیم انجام دهد.
```

مسیر وابستگی مجاز:

```text
Compose UI
  -> UseCases / Controllers / Repository Contracts / Validators
      -> Domain Models / Runtime Providers / Protocol Contracts
          -> Room Implementations / Platform Protocol Clients
```

---

## 2. اضافه کردن پروتکل جدید

برای اضافه کردن پروتکل جدید:

1. مقدار جدید را به `ProtocolType` اضافه کنید.
2. options مخصوص را در domain model تعریف کنید.
3. validator/inspector را برای profile جدید کامل کنید.
4. `ProtocolClient` implementation بنویسید.
5. factoryهای platform را آپدیت کنید.
6. UI editor section اضافه کنید.
7. تست‌های validator، factory و console behavior بنویسید.

---

## 3. قوانین UI

- UI فقط state را نمایش دهد و action را delegate کند.
- UI نباید مستقیم Room DAO یا platform client را import کند.
- UI نباید ID یا timestamp بسازد.
- screenهای بزرگ باید به componentهای کوچک‌تر تقسیم شوند.
- همه layoutها باید compact/medium/expanded را پشتیبانی کنند.

---

## 4. قوانین Domain و UseCase

- هر use-case یک مسئولیت مشخص داشته باشد.
- validation قبل از persistence انجام شود.
- خروجی خطا باید قابل نمایش و قابل تست باشد.
- برای زمان و ID از `TimeProvider` و `IdProvider` استفاده شود.
- از `!!` استفاده نشود.

---

## 5. قوانین Protocol Layer

- هر client باید `ProtocolClient` را رعایت کند.
- state باید از طریق `StateFlow` منتشر شود.
- eventها باید از طریق `Flow<ProtocolEvent>` منتشر شوند.
- خطاها باید تبدیل به event شوند، نه crash خام.
- close/cleanup در مسیر failure ضروری است.

---

## 6. قوانین تست

برای هر تغییر مهم حداقل یکی از این تست‌ها لازم است:

- unit test برای validator/use-case
- fake protocol client برای console behavior
- in-memory repository برای persistence behavior
- deterministic TimeProvider/IdProvider برای assert دقیق
- regression test برای bugهای lifecycle

---

## 7. قوانین مستندات و کامنت‌گذاری

- بالای هر `class`, `interface`, `object`, `enum`, `data class`, `sealed` یک KDoc کوتاه و مفید باشد.
- KDoc باید توضیح دهد کلاس چه مسئولیتی دارد؛ نه اینکه کد را خط‌به‌خط تکرار کند.
- اسناد فارسی کاربری در `docs/` نگهداری شوند.
- تغییرات release در `docs/FINAL_DELIVERY_NOTES.md` ثبت شود.

---

## 8. چک کیفیت قبل از commit

```bash
python3 tools/static_audit.py
./gradlew :composeApp:allTests
```

اگر UI یا build scripts تغییر کرده‌اند:

```bash
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
```
