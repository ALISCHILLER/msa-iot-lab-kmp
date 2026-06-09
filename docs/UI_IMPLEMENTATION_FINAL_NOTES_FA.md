# یادداشت‌های پیاده‌سازی UI نهایی

این نسخه روی UI حرفه‌ای، adaptive و دو زبانه تمرکز دارد.

## اصول UI

- Desktop-first برای تست‌های جدی و پرترافیک.
- Responsive برای Phone، Tablet/Foldable و Desktop.
- Command Center جدا از Traffic Intelligence.
- Navigation ثابت در Desktop، rail در Tablet و compact tab/chip در Phone.
- پشتیبانی runtime از English/Persian.
- RTL برای فارسی.
- ذخیره زبان انتخاب‌شده در Room.

## ساختار فایل‌های UI

```text
ui/WorkbenchShell.kt        # shell اصلی adaptive
ui/WorkbenchNavigation.kt   # sidebar/rail/compact navigation
ui/ResponsiveLayout.kt      # breakpointها و layout helperها
ui/DashboardScreen.kt       # شروع سریع و وضعیت workspace
ui/ProfileEditorScreen.kt   # فرم ساخت/ویرایش پروفایل
ui/ConsoleScreen.kt         # Live Console
ui/ConsoleComponents.kt     # command و traffic components
ui/GuideScreen.kt           # راهنمای داخل برنامه
ui/GuideSetupSections.kt    # راهنمای setup و quality gate داخل UI
```

## قوانین توسعه UI

- UI نباید به database یا platform implementation import مستقیم داشته باشد.
- UI نباید ID یا timestamp تولید کند.
- UI باید use-case و repository contract دریافت کند.
- متن‌های UI باید با `t(en, fa)` یا helperهای localization نوشته شوند.
- هر composable بزرگ باید به componentهای کوچک‌تر تقسیم شود.
- فرم‌ها باید در موبایل تک‌ستونه و در دسکتاپ چندپنله باشند.
