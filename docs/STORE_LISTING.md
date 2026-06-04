# Google Play — Store listing (draft)

## English (en-US)

**Title:** Notification History — Offline Log

**Short description:** Private offline notification journal with encryption and PIN.

**Full description:**

Notification History saves a local, encrypted log of your Android notifications — including text, images, and links — without ever using the internet. No accounts, no cloud, no analytics.

- Notification Listener access (required) — explained clearly in-app
- Encrypted database (SQLCipher) after you set a PIN
- Biometric or 6-digit PIN unlock (custom keypad, no system keyboard)
- Grouped feed like the shade, with search and per-app filters
- Ignore apps you do not want to log
- 10 failed unlock attempts permanently wipe all data (disclosed before setup)
- Does not run on rooted devices

**Category:** Tools / Productivity

**Privacy policy URL:** link to `docs/PRIVACY.md` on GitHub or hosted page before release.

---

## Українська (uk)

**Назва:** Notification History — офлайн журнал

**Короткий опис:** Приватний офлайн журнал сповіщень із шифруванням та PIN.

**Повний опис:**

Notification History зберігає локальний зашифрований журнал сповіщень Android — текст, зображення та посилання — без використання інтернету. Без облікових записів, хмари та аналітики.

- Доступ до сповіщень (обов’язковий) — з поясненням у додатку
- Шифрована база (SQLCipher) після створення PIN
- Біометрія або 6-значний PIN (власна клавіатура)
- Групи, пошук і фільтр за додатком
- Чорний список додатків
- 10 невдалих спроб розблокування → повне стирання даних
- Не працює на рутованих пристроях

---

## Notification Listener declaration

The app uses `NotificationListenerService` solely to display a private on-device history for the user. Data is not transmitted off-device.

## Data safety (Play Console)

| Field | Answer |
|-------|--------|
| Data collected | None transmitted to developer |
| Data shared | None |
| Encryption in transit | N/A (no network) |
| Encryption at rest | Yes (on device) |
| Deletion | In-app clear history, uninstall, or 10-failed-attempt wipe |

## Content rating

Expected: **Everyone** / low maturity (no user-generated public content). Complete questionnaire in Play Console.
