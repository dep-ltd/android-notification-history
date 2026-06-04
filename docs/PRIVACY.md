# Privacy Policy — Notification History Log

**Last updated:** 2026-06-05  
**Package:** `com.depsoftware.notifhistory`

---

## English

### Summary

Notification History Log does **not collect, transmit, or sell** any of your personal data. All notifications and attachments are stored **only on your device**, encrypted. The app **does not use the internet**.

### What data is stored (on-device only)

The app may store the following data **locally on your device**:

- Notification text and metadata (title, body, timestamp, channel ID)
- Package name and display label of the app that sent the notification
- Images attached to notifications (where present)
- Links explicitly present in notification metadata (for display only)
- Your PIN in hashed form (never stored as plain text)
- App settings (ignored-app list, biometric preference, lock timeout)

**None of this data ever leaves your device.**

### Permissions used

| Permission | Purpose |
|------------|---------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Read notifications from other apps to save a local log. Without this the app cannot function. |
| `USE_BIOMETRIC` | Unlock the local journal using fingerprint or face recognition. |
| `QUERY_ALL_PACKAGES` | Build the ignore-app list showing installed applications. |

The app does **not** request the `INTERNET` permission. No network connections are made.

### Data sharing

**None.** There are no servers, no analytics SDKs, no advertising frameworks, and no developer accounts embedded in the app.

### Data storage and security

- The local database is encrypted using **SQLCipher** with a key stored in Android Keystore.
- Image files are stored in the app's private storage directory.
- Access to the journal is protected by a **6-digit PIN** or **biometric unlock**.
- After **10 consecutive failed unlock attempts** (PIN or biometric combined), the app **permanently and irreversibly deletes** all history, settings, and encryption keys on the device. This is disclosed and confirmed by the user before PIN setup.
- The app **does not run on rooted devices**: it shows a warning and disables the notification listener to reduce the risk of OS-level security bypass.

### Your rights

You may at any time:

- Delete all notification history via **Settings → Clear all history**.
- Remove all app data by uninstalling the app from your device.
- Revoke notification listener access in Android system settings, which stops all future recording.

### Children

This app is not directed at children under 13 and does not knowingly collect data from children.

### Changes to this policy

The current version of this policy is always available at this URL. Material changes will also be noted in the Google Play update description.

### Contact

For privacy-related questions, contact: **daniil.pavenko@gmail.com**

---

## Українська

### Коротко

Notification History Log **не збирає, не передає і не продає** ваші персональні дані. Усі сповіщення та вкладення зберігаються **лише на вашому пристрої**, у зашифрованому вигляді. Додаток **не використовує інтернет**.

### Які дані зберігаються (лише на пристрої)

На пристрої локально можуть зберігатися:

- Текст і метадані сповіщень (заголовок, текст, час, ідентифікатор каналу)
- Package name і назва додатку-відправника
- Зображення зі сповіщень (за наявності)
- Посилання, явно присутні в метаданих сповіщення (лише для відображення)
- Ваш PIN у вигляді криптографічного хешу (не в відкритому тексті)
- Налаштування (список ігнорованих додатків, біометрія, таймаут блокування)

**Жодні дані не залишають ваш пристрій.**

### Дозволи

| Дозвіл | Призначення |
|--------|------------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Читання сповіщень інших додатків для локального журналу. Без цього дозволу додаток не виконує свою функцію. |
| `USE_BIOMETRIC` | Розблокування журналу відбитком або розпізнаванням обличчя. |
| `QUERY_ALL_PACKAGES` | Формування списку встановлених додатків для функції ігнорування. |

Додаток **не запитує** дозвіл `INTERNET`. Жодних мережевих з'єднань не здійснюється.

### Передача даних третім сторонам

**Відсутня.** Немає серверів, аналітики, рекламних SDK або вбудованих облікових записів розробника.

### Зберігання та безпека

- Локальна база шифрується за допомогою **SQLCipher** з ключем в Android Keystore.
- Файли зображень зберігаються у захищеному сховищі додатку.
- Доступ до журналу захищено **6-значним PIN** або **біометрією**.
- Після **10 невдалих спроб** розблокування (разом: невірний PIN або невдала біометрія) додаток **безповоротно видаляє** всю історію, налаштування та ключі шифрування. Це пояснюється і підтверджується користувачем до налаштування PIN.
- Додаток **не працює на рутованих пристроях**: показує попередження і вимикає listener для зниження ризику обходу захисту ОС.

### Ваші права

Ви можете у будь-який момент:

- Видалити всю історію через **Налаштування → Очистити всю історію**.
- Видалити всі дані через деінсталяцію додатку.
- Вимкнути доступ до сповіщень у системних налаштуваннях Android, що зупинить подальший запис.

### Діти

Додаток не спрямований на дітей до 13 років і свідомо не збирає дані дітей.

### Зміни політики

Актуальна версія завжди доступна за цим посиланням. Суттєві зміни відображатимуться в описі оновлення в Google Play.

### Контакт

З питань конфіденційності: **daniil.pavenko@gmail.com**
