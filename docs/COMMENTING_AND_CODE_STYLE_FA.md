# راهنمای کامنت‌گذاری و سبک کدنویسی

این پروژه از کامنت‌گذاری هدفمند استفاده می‌کند؛ یعنی کامنت‌ها باید مسئولیت کلاس و تصمیم معماری را توضیح دهند، نه اینکه هر خط کد را بازنویسی کنند.

---

## 1. قانون KDoc بالای کلاس‌ها

هر declaration از نوع‌های زیر باید KDoc داشته باشد:

- class
- data class
- interface
- object
- enum class
- sealed class
- sealed interface

نمونه مناسب:

```kotlin
/**
 * Coordinates profile validation and persistence for the profile editor workflow.
 */
class SaveProfileUseCase(...)
```

نمونه نامناسب:

```kotlin
/** This class saves profile. */
```

---

## 2. کامنت خوب چه ویژگی‌ای دارد؟

- دلیل وجود کلاس را توضیح می‌دهد.
- boundary معماری را مشخص می‌کند.
- درباره تصمیم‌های غیرواضح توضیح می‌دهد.
- کوتاه است و با تغییر کد سریع outdated نمی‌شود.

---

## 3. کجا کامنت نگذاریم؟

- روی کد واضح و ساده
- برای تکرار نام متد
- برای توضیح setter/getterهای بدیهی
- برای پنهان کردن کد پیچیده؛ اول کد را ساده کنید.

---

## 4. قوانین مرتب‌سازی فایل‌ها

- هر فایل یک مسئولیت اصلی داشته باشد.
- screenهای بزرگ باید به componentهای کوچک‌تر شکسته شوند.
- مدل‌های protocol از UI جدا باشند.
- platform implementation در source set خودش بماند.
- test fakeها در `commonTest` باشند.

---

## 5. ابزار کنترل کامنت‌ها

```bash
python3 tools/static_audit.py
```

این ابزار بررسی می‌کند که همه type declarationها KDoc داشته باشند و همچنین boundaryهای معماری رعایت شده باشند.
