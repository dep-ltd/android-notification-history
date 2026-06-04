# Notification History — ідея проєкту та план імплементації

## 1. Візія

**Notification History** — це приватний, повністю офлайн журнал push-нотіфікацій Android. Користувач отримує єдине місце, де можна переглянути, *що саме* приходило з будь-якого додатку: текст, час, зображення, посилання — навіть якщо оригінальне сповіщення вже зникло з шторки.

Принципи:

| Принцип | Реалізація |
|---------|------------|
| Без інтернету | `INTERNET` не в маніфесті; жодних SDK аналітики / crashlytics з мережею |
| Відкритий код | Прозорий код listener + шифрування; аудит приватності |
| Локальність | Усі дані лише на пристрої |
| Захист | Шифрування БД/файлів + біометрія або PIN |
| Сучасний UX | Jetpack Compose, Material 3, dynamic color, адаптивні списки |

---

## 2. Проблема та аудиторія

**Проблема:** системна шторка не зберігає історію; багато додатків «з’їдають» старі нотіфікації при групуванні або оновленні.

**Для кого:** користувачі, які хочуть контролювати вхідні сповіщення (месенджери, банки, пошта) без хмари та без відправки даних третім сторонам.

**Не ціль:** обхід DRM, прихований шпигунський софт, віддалена передача нотіфікацій.

---

## 3. Користувацькі сценарії

### 3.1 Перший запуск

1. Екран онбордингу: пояснення, навіщо потрібен доступ до сповіщень.
2. Перехід у системний екран `ACTION_NOTIFICATION_LISTENER_SETTINGS`.
3. Створення 6-значного PIN на **кастомній numpad** (без `InputMethod`).
4. Опційно: увімкнути біометрію (`BiometricPrompt`, weak/strong за політикою).
5. Генерація ключа шифрування БД (Android Keystore) — прозоро для користувача.

### 3.2 Щоденне використання

1. Розблокування (біометрія або PIN).
2. **Головний екран (Feed)** — хронологічний список: іконка, назва додатку, заголовок, прев’ю тексту, відносний час.
3. Тап → **Деталі**: повний текст, package, channel id, час (дата + година), вкладені зображення (BigPicture, MessagingStyle), клікабельні посилання з `PendingIntent` / extras (де доступно).
4. Пошук / фільтр за додатком або датою (фаза 4).

### 3.3 Налаштування

- **Ігнорувати додатки** — мультивибір installed packages (іконка + label + package); зміни застосовуються до нових подій.
- **Безпека** — змінити PIN, увімк/вимк біометрію, автоблокування (immediate / 1 / 5 хв).
- **Дані** — експорт зашифрованого backup (опційно, фаза 4+), очистити історію, видалити медіа-кеш.
- **Про додаток** — версія, ліцензії open source, політика приватності.

---

## 4. Технічна модель даних

### 4.1 Подія нотіфікації (`NotificationEvent`)

| Поле | Тип | Опис |
|------|-----|------|
| `id` | UUID / Long | Первинний ключ |
| `stableKey` | String | `statusBarNotification.key` + package — для upsert |
| `packageName` | String | |
| `appLabel` | String | Кеш з `PackageManager` |
| `postedAt` | Instant | |
| `updatedAt` | Instant? | При зміні тієї ж нотіфікації |
| `removedAt` | Instant? | Якщо знято з шторки |
| `groupKey` | String? | Android group |
| `isGroupSummary` | Boolean | |
| `channelId` | String? | |
| `title` | String? | |
| `text` | String? | |
| `bigText` | String? | |
| `style` | Enum | Default, BigText, BigPicture, Messaging, Inbox |
| `extrasJson` | String? | Обмежений whitelist extras (без PII надлишку) |
| `clickUri` | String? | Нормалізований URI з intent |
| `mediaPaths` | List\<String\> | Локальні шляхи до зображень у EncryptedFile |
| `importance` | Int? | |
| `eventType` | Enum | POSTED, UPDATED, REMOVED |

### 4.2 Групи

- При `posted` з однаковим `groupKey`: зв’язати дочірні записи з `summaryId` (nullable FK).
- Summary рядок у feed — згортання дітей (expand/collapse у UI).
- **Оновлення:** той самий `stableKey` → `UPDATE` замість нового рядка; оновити `updatedAt`, текст, медіа.
- **Видалення:** `onNotificationRemoved` → позначити `removedAt`, не обов’язково стирати одразу (політика retention у налаштуваннях).

### 4.3 Зображення та посилання

- **Зображення:** `Notification.largeIcon`, `MessagingStyle` photos, `PictureAttachment` — копія в app-private storage (`EncryptedFile` або директорія під SQLCipher-модулем).
- **Посилання:** парсинг `extras` (`android.intent.extra.TEXT`, `Notification.EXTRA_TEXT_LINES`), `RemoteInput` не зберігати без згоди; `PendingIntent` не виконувати — лише серіалізувати безпечний URI, якщо доступний через `intent` clone (обережно з security — тільки `toUri(0)` для перегляду).

---

## 5. Компоненти Android (стандартний стек)

### 5.1 Збір нотіфікацій

```text
NotificationListenerService (системний)
    → NotificationParser (extras, style, icons)
    → NotificationRepository.upsert()
    → Room DAO (transaction)
```

- Реєстрація в `AndroidManifest.xml` з `android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"`.
- `meta-data` для системного binding.
- Обробники: `onListenerConnected`, `onNotificationPosted`, `onNotificationRemoved`, `onNotificationRankingUpdate` (за потреби для сортування).

### 5.2 UI (Compose)

| Екран | Composable / pattern |
|-------|----------------------|
| Lock | `LockScreen` + custom `Numpad` + `BiometricPrompt` |
| Feed | `LazyColumn` + `PullToRefresh` (локальний reload) + sticky date headers |
| Detail | `Scaffold` + zoomable image (`Modifier.pointerInput`) |
| Settings | `Preference`-like M3 screens, `LazyColumn` switches |
| App picker | `LazyColumn` + search + checkbox per package |

**Дизайн:** Material 3 (`MaterialTheme`, `TopAppBar`, `NavigationBar` за потреби), типографіка M3, **dynamic color** (Monet), підтримка dark/light, **edge-to-edge** (`WindowInsets`), ripple, `AnimatedVisibility` для expand груп.

### 5.3 Зберігання

- **Room** 2.x, KSP, Flow для feed.
- **SQLCipher** (або `androidx.security:security-crypto` + Room encryption experimental) — passphrase з Keystore.
- **DataStore Preferences** — blacklist packages, flags біометрії, timeout lock.
- **Coil** — `file://` для локальних прев’ю.

### 5.4 Безпека

| Шар | Технологія |
|-----|------------|
| PIN | 6 цифр, PBKDF2/Argon2 hash у EncryptedSharedPreferences або Keystore-bound |
| Біометрія | `BiometricPrompt` + `CryptoObject` для ключа БД |
| БД | SQLCipher passphrase / `SupportFactory` |
| Файли | `EncryptedFile` (AES256-GCM) |
| Захист від скріншотів | `FLAG_SECURE` на lock і detail (опційно в налаштуваннях) |

**Кастомна клавіатура:** `Grid` з цифрами 0–9, backspace, confirm; жодного `TextField` з системним IME; PIN лише в пам’яті до хешування.

### 5.5 DI та архітектура

- **Hilt** — `SingletonComponent`, модулі `DatabaseModule`, `SecurityModule`, `CoroutineModule`.
- **Use cases:** `SaveNotificationUseCase`, `ApplyBlacklistUseCase`, `UnlockAppUseCase`, `PurgeOldMediaUseCase`.
- **Тести:** unit для parser/grouping; instrumented для DAO (in-memory / test SQLCipher).

---

## 6. Безпека та ключі (`.env` і Google Play)

### 6.1 Політика

- Репозиторій **не містить** ключів шифрування, паролів keystore, API keys.
- Розробник копіює `.env.example` → `.env` (в `.gitignore`).
- CI (GitHub Actions) — secrets у vault, не в логах.

### 6.2 Змінні `.env` (build-time)

| Змінна | Використання |
|--------|----------------|
| `PLAY_UPLOAD_STORE_FILE` | Шлях до upload keystore |
| `PLAY_UPLOAD_STORE_PASSWORD` | |
| `PLAY_UPLOAD_KEY_ALIAS` | |
| `PLAY_UPLOAD_KEY_PASSWORD` | |
| `APP_DB_ENCRYPTION_KEY_BASE64` | **Лише debug** — фіксований ключ для емулятора; release генерує Keystore на пристрої |

Gradle (`build.gradle.kts`):

```kotlin
// Псевдокод фази 0: читання .env → BuildConfig або resValue
// release: не підставляти APP_DB_ENCRYPTION_KEY — passphrase з Android Keystore
```

### 6.3 Google Play

- **Play App Signing** обов’язково.
- Data safety form: «No data collected» / усі дані on-device.
- Declarations: Special app access (Notification listener) — чітка justification у описі Store.
- Перевірка на **відсутність** `INTERNET` у merged manifest.
- Privacy policy URL (статична сторінка в repo `docs/PRIVACY.md`).

---

## 7. Нефункціональні вимоги

| Вимога | Ціль |
|--------|------|
| Продуктивність | < 50 ms на збереження простої нотіфікації (фоновий dispatcher) |
| Обсяг | Retention 90 днів за замовчуванням (налаштовується) |
| Батарея | Без періодичного polling; лише callback listener |
| OEM | Foreground service notification «Журнал активний» — toggle в налаштуваннях |
| Доступність | TalkBack для feed; contentDescription на іконках |

---

## 8. Обмеження платформи (чесно)

- Потрібен ручний дозвіл «Доступ до сповіщень» — без цього додаток не працює.
- Деякі додатки використовують `secret` / custom layout — парсинг може бути неповним.
- На Android 13+ власні notification channels для статусу сервісу.
- **Неможливо** відновити нотіфікації до встановлення listener — лише з моменту активації.

---

## 9. Фази імплементації

### Фаза 0 — Фундамент репозиторію (1–2 тижні)

**Ціль:** збірний скелет, політика секретів, без мережі.

- [ ] Gradle Kotlin DSL: `app`, опційно `core:model`, `core:database`
- [ ] Package `com.<org>.notificationhistory`
- [ ] Min SDK 26, target / compile — latest stable
- [ ] Jetpack Compose BOM, Material 3, Hilt, Room KSP, Coroutines
- [ ] `.env` + `dotenv` / `BuildConfig` pipeline; `.env.example`, `.gitignore`
- [ ] `AndroidManifest`: **без** `INTERNET`; declaration `NotificationListenerService`
- [ ] ProGuard rules для Room / SQLCipher
- [ ] `LICENSE` (Apache 2.0), `docs/PRIVACY.md`, baseline `README`
- [ ] CI: `assembleDebug`, `lint`, `test` (GitHub Actions)
- [ ] Signing config для release з `.env` / `upload-keystore.properties`

**Критерій готовності:** `./gradlew assembleDebug` проходить; порожній Compose `MainActivity` з темою M3.

---

### Фаза 1 — Listener і базовий журнал (2–3 тижні)

**Ціль:** збереження простих нотіфікацій і список на головному екрані.

- [ ] `NotificationListenerService` + reconnect handling
- [ ] `NotificationParser`: title, text, package, posted time, small icon → bitmap cache
- [ ] Room: entities, DAO, `NotificationRepository`
- [ ] Foreground / status UX: екран «Увімкніть доступ», якщо `!isNotificationListenerEnabled`
- [ ] Compose **Feed**: `ViewModel` + `StateFlow`, `LazyColumn`, empty state
- [ ] Navigation: Feed → Detail (базовий текст + іконка)
- [ ] Unit-тести parser для `EXTRA_TITLE`, `EXTRA_TEXT`

**Критерій:** після дозволу нові push з Telegram/Email з’являються в списку після перезапуску.

---

### Фаза 2 — Групи, оновлення, медіа (2–3 тижні)

**Ціль:** коректна поведінка як у системної шторки.

- [ ] `stableKey` upsert: POSTED vs UPDATED
- [ ] `onNotificationRemoved` → `removedAt`
- [ ] Group key: summary + children, UI expand/collapse
- [ ] Збереження `largeIcon` / BigPicture (стиснення, ліміт розміру)
- [ ] Detail: галерея зображень (Coil), показ `clickUri` (відкриття через `CustomTabs` **без** INTERNET permission — використати `Intent.ACTION_VIEW` лише на user tap, optional feature flag)
- [ ] Blacklist: DataStore + filter у repository
- [ ] Settings screen: список installed apps + search

**Критерій:** груповий чат не дублює 50 рядків; оновлення одного message id оновлює запис.

---

### Фаза 3 — Шифрування та захист доступу (2 тижні)

**Ціль:** дані at rest і lock screen.

- [ ] SQLCipher інтеграція + міграція Room
- [ ] Android Keystore: генерація passphrase при першому PIN
- [ ] PIN setup / verify UI (custom numpad)
- [ ] `BiometricPrompt` для unlock і опційно re-key
- [ ] `LockScreen` gate: NavHost з start destination = Lock until unlocked
- [ ] Timeout background lock (Lifecycle + `ProcessLifecycleOwner`)
- [ ] Optional `FLAG_SECURE`

**Критерій:** `adb backup` / raw DB на root не показує plaintext title/body.

---

### Фаза 4 — Полірування та Google Play (2–3 тижні)

**Ціль:** реліз у Store.

- [ ] Retention job (`WorkManager`) — prune старих записів і файлів
- [ ] Пошук у feed, фільтр за package
- [ ] Онбординг illustrations, accessibility pass
- [ ] Локалізація: uk, en (strings.xml)
- [ ] `docs/PRIVACY.md` + Store listing (uk/en)
- [ ] Data safety, content rating, notification listener declaration
- [ ] Release build, internal testing track, bugfix OEM (Xiaomi, Samsung)

**Критерій:** успішна internal testing, відсутність INTERNET у merged manifest, пройдений checklist Play.

---

### Фаза 5 — Опційно після v1

- Зашифрований локальний export/import (файл + QR passphrase offline)
- Віджет «останні 3»
- Wear OS / планшет dual pane
- Aggressive OEM battery whitelist guide в UI

---

## 10. Структура модулів (цільова)

```text
android-notification-history/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/.../service/NotificationHistoryListenerService.kt
│       ├── java/.../ui/feed|detail|settings|lock/
│       ├── java/.../data/local|repository/
│       └── java/.../security/
├── docs/
│   ├── PROJECT_IDEA.md      ← цей файл
│   └── PRIVACY.md           ← фаза 4
├── .env.example
├── README.md
└── LICENSE
```

---

## 11. Ризики та мітигація

| Ризик | Мітигація |
|-------|-----------|
| Відхилення в Play через listener | Прозорий опис, privacy policy, no INTERNET |
| Великий обсяг медіа | Ліміт розміру, retention, стиснення JPEG/WebP |
| Витік PIN через accessibility | Custom numpad, `FLAG_SECURE`, не логувати PIN |
| SQLCipher + Room міграції | Тести міграцій, версіонування schema |

---

## 12. Визначення готовності v1.0

1. Користувач з дозволом listener бачить історію з іконками та деталями.
2. Групи та оновлення не ламають feed.
3. Blacklist працює для нових подій.
4. Без unlock немає доступу до feed.
5. БД і файли зашифровані; секрети не в git.
6. Додаток проходить lint, тести, internal Play track.

---

*Документ є живим: оновлюйте чеклісти фаз після завершення кожної ітерації.*
