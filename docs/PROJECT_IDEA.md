# Notification History — ідея проєкту та план імплементації

## 1. Візія

**Notification History** — це приватний, повністю офлайн журнал push-нотіфікацій Android. Користувач отримує єдине місце, де можна переглянути, *що саме* приходило з будь-якого додатку: текст, час, зображення, посилання — навіть якщо оригінальне сповіщення вже зникло з шторки.

Принципи:

| Принцип | Реалізація |
|---------|------------|
| Без інтернету | `INTERNET` не в маніфесті; жодних SDK аналітики / crashlytics з мережею |
| Відкритий код | Прозорий код listener + шифрування; аудит приватності |
| Локальність | Усі дані лише на пристрої |
| Захист | Шифрування БД/файлів + біометрія або PIN; 10 невдалих спроб → повне стирання |
| Без root | На рутованому пристрої додаток не працює, лише попередження |
| Сучасний UX | Jetpack Compose, Material 3, dynamic color, адаптивні списки |

---

## 2. Проблема та аудиторія

**Проблема:** системна шторка не зберігає історію; багато додатків «з’їдають» старі нотіфікації при групуванні або оновленні.

**Для кого:** користувачі, які хочуть контролювати вхідні сповіщення (месенджери, банки, пошта) без хмари та без відправки даних третім сторонам.

**Не ціль:** обхід DRM, прихований шпигунський софт, віддалена передача нотіфікацій.

---

## 3. Користувацькі сценарії

### 3.1 Перший запуск

1. **Перевірка root** — якщо пристрій рутований: блокуючий екран «Додаток не підтримує рутовані пристрої»; listener і БД не ініціалізуються.
2. Екран онбордингу: пояснення, навіщо потрібен доступ до сповіщень.
3. Перехід у системний екран `ACTION_NOTIFICATION_LISTENER_SETTINGS`.
4. Створення 6-значного PIN на **кастомній numpad** (без `InputMethod`) + **явна згода**: після 10 невдалих спроб розблокування (PIN або біометрія) усі дані будуть безповоротно знищені.
5. Опційно: увімкнути біометрію (`BiometricPrompt`, weak/strong за політикою).
6. Генерація ключа шифрування БД (Android Keystore) — прозоро для користувача.

### 3.2 Щоденне використання

1. Розблокування (біометрія або PIN); при помилці — інкремент спільного лічильника (залишилось N спроб до стирання).
2. **Головний екран (Feed)** — хронологічний список: іконка, назва додатку, заголовок, прев’ю тексту, відносний час.
3. Тап → **Деталі**: повний текст, package, channel id, час (дата + година), вкладені зображення (BigPicture, MessagingStyle), клікабельні посилання з `PendingIntent` / extras (де доступно).
4. Пошук / фільтр за додатком або датою (фаза 4).

### 3.3 Налаштування

- **Ігнорувати додатки** — мультивибір installed packages (іконка + label + package); зміни застосовуються до нових подій.
- **Безпека** — змінити PIN, увімк/вимк біометрію, автоблокування (immediate / 1 / 5 хв); нагадування про політику 10 спроб.
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

#### 4.2.1 Парсинг і відображення груп (обов’язково з фази 4.1)

**Джерело `groupKey`:** `StatusBarNotification.groupKey`, fallback — `Notification.group`.

**Парсер (`NotificationExtrasReader`)** — title/text не лише з `EXTRA_TITLE` / `EXTRA_TEXT`:

| Джерело в extras | Поле |
|------------------|------|
| `EXTRA_TITLE`, `android.conversationTitle` | title |
| `EXTRA_TEXT`, `EXTRA_BIG_TEXT`, `EXTRA_SUMMARY_TEXT`, `EXTRA_INFO_TEXT`, `EXTRA_SUB_TEXT` | text |
| `EXTRA_TEXT_LINES` (InboxStyle) | text = останній рядок |
| `android.messages[]` (MessagingStyle) | sender → title, text → body (останнє за timestamp) |
| Group summary без тексту | title = `appLabel`; text з subtext/info, якщо є |

**Заборонено в UI:** показувати порожній group summary як картку з «No title / No text».

**Feed (`NotificationFeedGrouper` + `FeedScreen`):**

- Summary без `title` і `text` **не** використовується як заголовок групи.
- Заголовок групи: summary з контентом → інакше найновіше дитяче з контентом → інакше placeholder («{app} · N сповіщень у групі»).
- Кожне дочіче сповіщення (`!isGroupSummary`) — окремий рядок у розгорнутій групі з власним title/text.
- Група з одним дочірнім повідомленням лишається **групою**, не зливається в `Single`.
- Unit-тести: `NotificationExtrasReaderTest`, `NotificationFeedGrouperTest`.

**Примітка:** записи в БД, збережені до оновлення парсера, можуть лишатися з порожніми полями; нові нотіфікації мають парситися коректно.

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
| Root blocked | `RootWarningScreen` — повноекранне M3, без навігації в feed |
| Lock | `LockScreen` + custom `Numpad` + `BiometricPrompt` + лічильник спроб |
| Feed | `LazyColumn` + `PullToRefresh` (локальний reload) + sticky date headers |
| Detail | `Scaffold` + zoomable image (`Modifier.pointerInput`) |
| Settings | `Preference`-like M3 screens, `LazyColumn` switches |
| App picker | `LazyColumn` + search + checkbox per package |

**Дизайн:** Material 3 (`MaterialTheme`, `TopAppBar`, `NavigationBar` за потреби), типографіка M3, **dynamic color** (Monet), підтримка dark/light, **edge-to-edge** (`WindowInsets`), ripple, `AnimatedVisibility` для expand груп.

#### 5.2.1 Адаптивний UI (foldable, планшети) — фаза 6

Орієнтир: [Large screens & foldables](https://developer.android.com/develop/ui/compose/layouts/adaptive), [List-detail](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail), Material 3 **canonical layouts**.

| Клас вікна (`WindowSizeClass`) | Ширина (typ.) | Навігація | Feed + Detail | Налаштування |
|-------------------------------|---------------|-----------|---------------|--------------|
| **Compact** | телефон, cover display | `NavigationBar` або top-level tabs | Один стек: Feed → Detail (push) | Один стек: категорії → екран опції |
| **Medium** | fold unfolded, малі планшети | `NavigationRail` (опційно) | **List-detail**: список ~40%, detail ~60% | Master-detail: список розділів + контент |
| **Expanded** | планшет, desktop mode | `NavigationRail` + більші відступи | List-detail; detail з двоколонковим контентом (мета + медіа) | Master-detail; app picker у другій панелі |

**Стек бібліотек (фаза 6):**

- `androidx.compose.material3.adaptive` — `ListDetailPaneScaffold`, `SupportingPaneScaffold` (де доречно)
- `androidx.compose.material3.adaptive.navigation` — `NavigationSuiteScaffold` (bar ↔ rail за шириною)
- `androidx.window:window` + `WindowInfoTracker` — fold posture, `FoldingFeature` (hinge)
- `WindowSizeClass` з `currentWindowAdaptiveInfo()` — єдине джерело правди для layout

**Feed (список нотіфікацій):**

- Compact: повноширинний `LazyColumn`, sticky date headers, групи expand/collapse як зараз.
- Medium/Expanded: той самий список у **primary pane**; при виборі елемента — detail у **secondary pane** (без втрати scroll position).
- Порожній detail: placeholder «Оберіть сповіщення» (M3 empty state).
- Hinge: `Modifier.padding()` від `FoldingFeature.occlusionBounds` — не класти FAB/важливі кнопки на згин.

**Detail (детальна сторінка):**

- Compact: повноекранний `Scaffold` + `TopAppBar` з back.
- Medium+: detail у secondary pane; top bar без back (вибір у списку); на Expanded — рядок метаданих (іконка, package, час) + нижче текст/галерея в `FlowRow` або дві колонки.
- Зображення: `HorizontalPager` на compact; на expanded — сітка прев’ю + zoom.

**Налаштування:**

- Compact: `LazyColumn` секцій (Безпека, Додатки, Дані, Про).
- Medium+: **ліва панель** — секції/пункти; **права** — вміст (PIN, біометрія, blacklist, retention).
- App picker (blacklist): на Expanded — пошук + список у supporting pane без перекриття всього екрана.

**Маніфест / activity:**

- `android:resizeableActivity="true"`, `configChanges` з `screenSize|smallestScreenSize|screenLayout` (мінімально необхідне).
- Тест: Foldable emulator (7.6" Fold-in), планшет 10", **Desktop window** на fold.

**Не ціль фази 6:** окремий UX для Wear OS (залишається в фазі 5 як опція).

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
| Root | `RootDetector` — блокування до будь-якого доступу до даних |
| Анти-підбір | Спільний лічильник невдалих спроб → `WipeAllDataUseCase` на 10-й |

**Кастомна клавіатура:** `Grid` з цифрами 0–9, backspace, confirm; жодного `TextField` з системним IME; PIN лише в пам’яті до хешування.

#### 5.4.1 Захист від підбору (10 невдалих спроб)

**Політика:** один спільний лічильник `failedUnlockAttempts` (0–10) для **будь-якої** невдалої спроби розблокування:

| Подія | Лічильник |
|-------|-----------|
| Невірний PIN (підтвердження на numpad) | +1 |
| `BiometricPrompt` — `ERROR_*` / скасування після невдалої аутентифікації | +1 |
| Успішний unlock (PIN або біометрія) | скидання в 0 |

**На 10-й невдалій спробі** (атомарно, у фоні, перед показом UI):

1. Зупинити / не запускати `NotificationListenerService`.
2. Видалити файл SQLCipher, каталог медіа, DataStore, EncryptedSharedPreferences.
3. Видалити ключі Android Keystore, пов’язані з додатком (alias app-specific).
4. Скинути `failedUnlockAttempts` і стан сесії.
5. Перейти на екран «Дані знищено з міркувань безпеки» → онбординг (новий PIN), **без** відновлення старої історії.

**UX:**

- Під час онбордингу та в налаштуваннях — чекбокс/діалог згоди з політикою стирання.
- На `LockScreen` — текст «Залишилось N спроб» (N = 10 − attempts).
- Після стирання — не розкривати, чи був PIN правильним на 10-й спробі (захист від oracle).

**Зберігання лічильника:** EncryptedSharedPreferences або захищений DataStore; інкремент лише після підтвердженої невдачі (не на випадковий tap).

#### 5.4.2 Рутовані пристрої (не підтримуються)

**Вимога:** на рутованому пристрої додаток **не працює** — не збирає нотіфікації, не показує журнал, не зберігає нові дані.

**Детекція** (комбінація сигналів, консервативно — false positive краще, ніж пропуск root):

- `Build.TAGS` містить `test-keys`
- Наявність `su`, `magisk`, типових root-шляхів (`/system/xbin/su`, …)
- Встановлені пакети: Magisk, SuperSU, KernelSU (whitelist оновлюється)
- `RootBeer` або власний `RootDetector` (release); **debug** — `BuildConfig.ALLOW_ROOT_FOR_DEBUG` лише для розробки

**Поведінка:**

- Перевірка в `Application.onCreate` і при `onResume` (root можуть увімкнути під час сесії).
- Якщо root виявлено → `RootWarningScreen` (M3, іконка warning, пояснення uk/en, кнопка «Вийти» / `finish()`).
- `NotificationListenerService` не реєструється активно; якщо дозвіл уже був — показати інструкцію вимкнути listener в системних налаштуваннях.
- Опис у Google Play: «Не підтримує рутовані пристрої».

**Чесне обмеження:** визначені приховані root / custom ROM можуть обійти евристику; мета — стандартні сценарії Magisk/su, не війна з усіма модами.

### 5.5 DI та архітектура

- **Hilt** — `SingletonComponent`, модулі `DatabaseModule`, `SecurityModule`, `CoroutineModule`.
- **Use cases:** `SaveNotificationUseCase`, `ApplyBlacklistUseCase`, `UnlockAppUseCase`, `PurgeOldMediaUseCase`, `WipeAllDataUseCase`, `CheckRootStatusUseCase`.
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
| Анти-підбір | Максимум 10 невдалих unlock (PIN + біометрія разом), далі secure wipe |
| Root | Блокування UI + відсутність збору нотіфікацій |

---

## 8. Обмеження платформи (чесно)

- **Рутовані пристрої не підтримуються** — додаток показує попередження і не веде журнал.
- Потрібен ручний дозвіл «Доступ до сповіщень» — без цього додаток не працює.
- Деякі додатки використовують `secret` / custom layout — парсинг може бути неповним.
- На Android 13+ власні notification channels для статусу сервісу.
- **Неможливо** відновити нотіфікації до встановлення listener — лише з моменту активації.

---

## 9. Фази імплементації

### Фаза 0 — Фундамент репозиторію (1–2 тижні)

**Ціль:** збірний скелет, політика секретів, без мережі.

- [x] Gradle Kotlin DSL: `app`, опційно `core:model`, `core:database`
- [x] Package `com.notificationhistory`
- [x] Min SDK 26, target / compile — latest stable
- [x] Jetpack Compose BOM, Material 3, Hilt, Room KSP, Coroutines
- [x] `.env` + `dotenv` / `BuildConfig` pipeline; `.env.example`, `.gitignore`
- [x] `AndroidManifest`: **без** `INTERNET`; declaration `NotificationListenerService`
- [x] ProGuard rules для Room / SQLCipher
- [x] `LICENSE` (Apache 2.0), `docs/PRIVACY.md`, baseline `README`
- [x] CI: `assembleDebug`, `lint`, `test` (GitHub Actions)
- [x] Signing config для release з `.env` / `upload-keystore.properties`
- [x] `RootDetector` + `RootWarningScreen`; `ALLOW_ROOT_FOR_DEBUG` лише в debug

**Критерій готовності:** `./gradlew assembleDebug` проходить; на емуляторі без root — Compose `MainActivity`; з root (тестовий образ) — лише warning screen.

---

### Фаза 1 — Listener і базовий журнал (2–3 тижні)

**Ціль:** збереження простих нотіфікацій і список на головному екрані.

- [x] `NotificationListenerService` + reconnect handling
- [x] `NotificationParser`: title, text, package, posted time, small icon → bitmap cache
- [x] Room: entities, DAO, `NotificationRepository`
- [x] Foreground / status UX: екран «Увімкніть доступ», якщо `!isNotificationListenerEnabled`
- [x] Compose **Feed**: `ViewModel` + `StateFlow`, `LazyColumn`, empty state
- [x] Navigation: Feed → Detail (базовий текст + іконка)
- [x] Unit-тести parser для `EXTRA_TITLE`, `EXTRA_TEXT`
- [x] *(4.1)* Розширений парсер: MessagingStyle, Inbox lines, group summary (див. §4.2.1)

**Критерій:** після дозволу нові push з Telegram/Email з’являються в списку після перезапуску.

---

### Фаза 2 — Групи, оновлення, медіа (2–3 тижні)

**Ціль:** коректна поведінка як у системної шторки.

- [x] `stableKey` upsert: POSTED vs UPDATED
- [x] `onNotificationRemoved` → `removedAt`
- [x] Group key: summary + children, UI expand/collapse
- [x] *(4.1)* Групи без «No title» на summary; діти окремими рядками (див. §4.2.1)
- [x] Збереження `largeIcon` / BigPicture (стиснення, ліміт розміру)
- [x] Detail: галерея зображень (Coil), показ `clickUri` (відкриття через `CustomTabs` **без** INTERNET permission — використати `Intent.ACTION_VIEW` лише на user tap, optional feature flag)
- [x] Blacklist: DataStore + filter у repository
- [x] Settings screen: список installed apps + search
- [x] *(4.1)* Повний список installed apps для blacklist (`QUERY_ALL_PACKAGES` + `getInstalledApplications`, див. §4.1.3)

**Критерій:** груповий чат не дублює 50 рядків; оновлення одного message id оновлює запис.

---

### Фаза 3 — Шифрування та захист доступу (2 тижні)

**Ціль:** дані at rest і lock screen.

- [x] SQLCipher інтеграція + міграція Room
- [x] Android Keystore: генерація passphrase при першому PIN
- [x] PIN setup / verify UI (custom numpad)
- [x] `BiometricPrompt` для unlock і опційно re-key
- [x] `LockScreen` gate: NavHost з start destination = Lock until unlocked
- [x] Timeout background lock (Lifecycle + `ProcessLifecycleOwner`)
- [x] Optional `FLAG_SECURE`
- [x] `failedUnlockAttempts`: спільний лічильник PIN + біометрія; UI «N спроб залишилось»
- [x] `WipeAllDataUseCase` на 10-й невдачі (БД, файли, prefs, Keystore)
- [x] Онбординг: згода з політикою стирання; екран після wipe

- [x] *(4.1)* `MainActivity` extends `FragmentActivity` (вимога `BiometricPrompt`)
- [x] *(4.1)* БД недоступна до PIN; `setupPin` → `recreateEncrypted()` без race з plain SQLite (див. §4.1.2)

**Критерій:** 10 невірних PIN підряд знищують дані і повертають на setup; біометрія теж інкрементує лічильник; на root-пристрої feed недоступний.

---

### Фаза 4 — Полірування та Google Play (2–3 тижні)

**Ціль:** реліз у Store.

- [x] Retention job (`WorkManager`) — prune старих записів і файлів
- [x] Пошук у feed, фільтр за package
- [x] Онбординг illustrations, accessibility pass (включно з root warning і 10-attempt policy)
- [x] Локалізація: uk, en (strings.xml)
- [x] `docs/PRIVACY.md` + Store listing (uk/en)
- [x] Data safety, content rating, notification listener declaration
- [x] Release build, internal testing track, bugfix OEM (Xiaomi, Samsung)

- [x] *(4.1)* `HiltWorkerFactory`: вимкнено `WorkManagerInitializer` у manifest (див. §4.1.2)
- [x] *(4.1)* `RetentionWorker` не звертається до БД до встановлення PIN
- [x] *(4.1)* Empty state feed з `weight(1f)` і окремими текстами для фільтра (див. §4.1.4)

**Критерій:** успішна internal testing, відсутність INTERNET у merged manifest, пройдений checklist Play.

---

### Фаза 4.1 — Стабілізація та доопрацювання (реалізовано; для повторної валідації)

**Ціль:** закрити краші онбордингу, коректні групи нотіфікацій, UX feed/settings, адаптивний layout (фаза 6).  
**Статус:** реалізовано в `main` (коміти після v1.0); використовуй §13 для регресійної перевірки.

#### 4.1.1 WorkManager і життєвий цикл UI

| Вимога | Реалізація / файл |
|--------|------------------|
| `RetentionWorker` через `@HiltWorker` + `HiltWorkerFactory` | `NotificationHistoryApp`, `RetentionWorker.kt` |
| Вимкнути дефолтний `WorkManagerInitializer` | `AndroidManifest` — `tools:node="remove"` на `androidx.work.WorkManagerInitializer` |
| Retention не чіпає БД без PIN | `RetentionWorker`: `if (!authManager.isPinSet()) return success` |
| `FeedViewModel` не створюється на consent/setup/lock | `hiltViewModel()` лише в `composable("feed")`, не в `MainActivity` |
| Repository не відкриває SQLCipher до PIN | `NotificationRepository.isDatabaseReady()` |

**Валідація:** чистий install → consent → PIN → feed без FATAL; logcat без `NoSuchMethodException` для `RetentionWorker` і без `Database is not available until a PIN is configured` на старті.

#### 4.1.2 SQLCipher і створення PIN

| Вимога | Реалізація |
|--------|------------|
| Не викликати `ensureDatabasePassphrase()` **до** видалення старого файлу БД | `AuthManager.setupPin()` → лише `databaseHolder.recreateEncrypted()` |
| Ніколи відкривати незашифровану persistent Room | `DatabaseHolder.openDatabase()` вимагає passphrase |
| Повне видалення `.db` + `-wal` / `-shm` / journal | `DatabaseHolder.wipeDatabaseFiles()` |
| Recovery при plain/corrupt файлі | catch `file is not a database` → wipe + reopen |
| Passphrase в secure prefs після PIN; debug key не маскує `hasDatabasePassphrase()` | `SecurityManager` |

**Валідація:** `pm clear` → онбординг → PIN → feed; повторний запуск; немає `SQLiteException: file is not a database`.

#### 4.1.3 Blacklist — список додатків

| Вимога | Реалізація |
|--------|------------|
| Не лише launcher apps | `InstalledAppsLoader`: `getInstalledApplications` + launcher query + пакети з історії |
| `QUERY_ALL_PACKAGES` + `<queries>` для MAIN/LAUNCHER | `AndroidManifest` |
| Асинхронне завантаження, loading, empty search | `AppPickerViewModel`, `AppPickerScreen` |
| Master-detail: app picker на medium+ у правій панелі з прокруткою | `SettingsAdaptive` + `weight(1f)` на списку |

**Валідація:** Settings → Ignore apps — сотні пакетів (не 10–20); пошук за назвою/package; toggle зберігається.

#### 4.1.4 Feed — порожній стан і фільтри

| Вимога | Реалізація |
|--------|------------|
| Empty state займає область під search/chips | `Modifier.weight(1f)` на `FeedEmptyState` / `LazyColumn` |
| Окремі рядки: немає записів / немає за фільтром | `feed_empty`, `feed_empty_hint`, `feed_empty_filtered` |
| Іконка + центрування (M3 empty state) | `FeedEmptyState` composable |

**Валідація:** порожній журнал — видимий текст по центру; пошук без збігів — інше повідомлення.

#### 4.1.5 Listener — стійкість

| Вимога | Реалізація |
|--------|------------|
| Іконка від видаленого/відсутнього package не крашить service | `extractSmallIconBitmap` try/catch → `null` |
| Збереження лише після PIN | `handleNotification`: `if (!authManager.isPinSet()) return` |

---

### Фаза 5 — Опційно після v1 (не fold)

- Зашифрований локальний export/import (файл + QR passphrase offline)
- Віджет «останні 3»
- Wear OS companion (мінімальний перегляд)
- Aggressive OEM battery whitelist guide в UI

---

### Фаза 6 — Foldable та великі екрани (реалізовано)

**Ціль:** адаптивний layout за офіційними гайдлайнами Android / Material 3 для **feed**, **detail** і **settings** (див. §5.2.1).

- [x] Залежності: `material3-adaptive`, `material3-window-size-class`, `material3-adaptive-navigation-suite`, `androidx.window`
- [x] `calculateWindowSizeClass` + `NavigationSuiteScaffold` (Feed / Settings) — `MainAppScaffold`
- [x] **Feed + Detail:** compact = push `detail/{id}`; medium+ = `Row` list ~42% + detail ~58%, `rememberSaveable` selected id
- [x] Placeholder у detail pane — «Оберіть сповіщення» (`detail_select_prompt`)
- [x] **Detail:** двоколонковий layout на `WindowWidthSizeClass.Expanded`
- [x] **Settings:** master-detail на medium+ (`SettingsSection` + `SettingsPanel` / embedded `AppPickerScreen`)
- [x] **App picker:** full screen на compact; embedded у правій панелі на medium+
- [x] Hinge: `adaptiveHingePadding()` через `WindowInfoTracker` (спрощено — padding при half-opened)
- [x] `resizeableActivity="true"`, `configChanges` screenSize|smallestScreenSize|screenLayout|orientation
- [ ] Preview: `@Preview(device = Devices.FOLDABLE)` + планшет (опційно)
- [ ] Instrumented screenshot tests (опційно)

**Примітка:** замість `ListDetailPaneScaffold` (API 1.0) використано `Row` + `WindowSizeClass` — достатньо для критерію list-detail на medium+.

**Критерій:** на unfolded fold / планшеті — список і деталь одночасно; на телефоні — стек; settings у дві колонки; немає регресії compact.

**Визначення готовності v1.1 (large screens):** пункти 1–8 з §12 збережені + list-detail на width ≥ medium для feed/detail/settings.

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
| Підбір PIN офлайн | 10 спроб → secure wipe; лічильник у захищеному сховищі |
| Root обходить шифрування | Відмова в роботі на root; повторна перевірка on resume |
| Випадкове стирання | Явна згода при онбордингу; показ залишку спроб на lock |
| Поганий UX на fold | Фаза 6: `ListDetailPaneScaffold`, тести на Fold emulator |
| Стан при згині/повороті | `rememberSaveable` selected id; ViewModel не скидає feed |

---

## 12. Визначення готовності v1.0

1. Користувач з дозволом listener бачить історію з іконками та деталями.
2. Групи та оновлення не ламають feed.
3. Blacklist працює для нових подій.
4. Без unlock немає доступу до feed.
5. Після 10 невдалих спроб (PIN або біометрія) — повне стирання, без відновлення.
6. На рутованому пристрої — блокуючий екран, listener не збирає дані.
7. БД і файли зашифровані; секрети не в git.
8. Додаток проходить lint, тести, internal Play track.

---

## 13. Чекліст повторної валідації (після фаз 4.1 + 6)

Виконуй на **debug build** після `pm clear` (або fresh install), пристрій **без root**, notification access увімкнено.

### A. Онбординг і безпека

- [ ] Запуск без крашу до PIN (немає FATAL у logcat)
- [ ] Consent → PIN (6 цифр) → feed
- [ ] Біометрія в Settings (якщо доступна) — unlock працює
- [ ] Background → foreground з lock timeout
- [ ] 10 невдалих спроб → wipe screen → знову consent (опційно, тестовий PIN)

### B. Журнал (feed)

- [ ] Порожній журнал: іконка + текст + підказка **видимі** (не згорнуті в 0px)
- [ ] Пошук без збігів — `feed_empty_filtered`
- [ ] Нова нотіфікація з’являється після unlock
- [ ] Група з месенджера: заголовок **не** «No title / No text»
- [ ] Розгортання групи — кожне повідомлення з власним title/text
- [ ] Compact: tap → detail; Medium+: list + detail без back на detail pane

### C. Налаштування

- [ ] Ignore apps — повний список + пошук
- [ ] Blacklist: нові нотіфікації від обраного package **не** зберігаються
- [ ] Retention slider / clear history (за бажанням)

### D. Адаптивність (фаза 6)

- [ ] Телефон: bottom nav (Журнал / Налаштування)
- [ ] Fold unfolded / емулятор планшета: list-detail feed, settings у 2 колонки
- [ ] Поворот / half-fold — без крашу, стан списку зберігається

### E. Автотести

```bash
./gradlew testDebugUnitTest assembleDebug
```

Очікується: success; ключові тести — `NotificationExtrasReaderTest`, `NotificationFeedGrouperTest`, `NotificationParserTest`.

### F. Play / manifest (регресія)

- [ ] Merged manifest: **немає** `INTERNET`
- [ ] `QUERY_ALL_PACKAGES` — задекларовано в Play Console (якщо публікуєте в Store)
- [ ] `WorkManagerInitializer` removed у merged manifest

---

*Документ є живим: оновлюйте чеклісти фаз після завершення кожної ітерації.*
