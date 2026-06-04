# Notification History

**Офлайн, відкритий код, приватний журнал push-нотіфікацій Android.**

Додаток перехоплює нотіфікації всіх установлених програм (через `NotificationListenerService`), зберігає їх у **зашифрованій** локальній базі даних разом із зображеннями та посиланнями, і показує єдиний хронологічний лог на головному екрані. **Інтернет не використовується** — ні для збору, ні для синхронізації, ні для аналітики.

| | |
|---|---|
| Платформа | Android (мін. API 26+, target — останній stable SDK) |
| UI | Jetpack Compose + Material 3 |
| Дані | Room + SQLCipher (або Room + EncryptedFile для вкладень) |
| Доступ до журналу | Біометрія або 6-значний PIN (власна клавіатура) |
| Захист від підбору | 10 невдалих спроб (PIN або біометрія) → повне стирання даних |
| Root | Додаток **не працює** на рутованих пристроях; показує попередження |
| Ліцензія | Apache 2.0 (рекомендовано для Play Store) |

Повний опис продукту, архітектури та **пофазовий план імплементації** — у [`docs/PROJECT_IDEA.md`](docs/PROJECT_IDEA.md).

---

## Можливості

- **Журнал нотіфікацій** — список усіх збережених подій з фільтрами за датою та додатком.
- **Детальний перегляд** — іконка додатку, назва, package name, заголовок/текст, час, вкладення (картинки), deep links / intent URI.
- **Групи та оновлення** — коректна обробка `groupKey`, summary vs children, `onNotificationPosted` / `onNotificationRemoved` / зміни через той самий `key`.
- **Чорний список додатків** — у налаштуваннях вимкнути збереження для обраних package.
- **Захист журналу** — BiometricPrompt + PIN на кастомній numpad (без системної клавіатури).
- **Анти-підбір** — спільний лічильник невдалих спроб PIN і біометрії; після **10** помилок — безповоротне видалення БД, медіа, налаштувань і ключів (див. [`docs/PROJECT_IDEA.md` §5.4.1](docs/PROJECT_IDEA.md)).
- **Без root** — на рутованому пристрої додаток не запускає listener і не показує журнал; лише екран попередження з поясненням.
- **Шифрування at rest** — БД і чутливі файли; ключі не вшиваються в репозиторій (див. `.env`).

---

## Вимоги

- Android Studio Ladybug / останній stable
- JDK 17+
- Пристрій або емулятор з Google Play services **не обов’язкові** (додаток автономний)

---

## Швидкий старт (після появи модулів Gradle)

```bash
git clone https://github.com/<your-org>/android-notification-history.git
cd android-notification-history
cp .env.example .env
# Заповніть .env (див. docs/PROJECT_IDEA.md)
./gradlew assembleDebug
```

На пристрої:

1. Встановити APK.
2. **Налаштування → Спеціальний доступ → Доступ до сповіщень** — увімкнути Notification History.
3. Пристрій **не повинен бути рутований** — інакше додаток залишиться на екрані попередження.
4. При першому запуску — створити PIN або увімкнути біометрію (з попередженням про стирання після 10 невдалих спроб).
5. За потреби — виключити додатки в **Налаштування → Ігнорувати додатки**.

---

## Конфігурація секретів (`.env`)

Секрети **не комітяться**. Шаблон: [`.env.example`](.env.example).

| Змінна | Призначення |
|--------|-------------|
| `PLAY_UPLOAD_*` | Локальний upload keystore для релізних збірок (опційно) |
| `APP_DB_ENCRYPTION_KEY_BASE64` | Ключ шифрування БД для dev/debug (production — Android Keystore + SQLCipher passphrase) |

Gradle читає `.env` через `dotenv` plugin або `local.properties` fallback — деталі в фазі 0 плану в `docs/PROJECT_IDEA.md`.

> **Google Play:** використовуйте [Play App Signing](https://support.google.com/googleplay/android-developer/answer/9842756); upload key зберігайте лише локально в `.env` / `upload-keystore.properties`.

---

## Дозволи та приватність

| Дозвіл / API | Навіщо |
|--------------|--------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Єдиний легальний спосіб читати нотіфікації інших додатків |
| `USE_BIOMETRIC` | Розблокування журналу |
| `POST_NOTIFICATIONS` (API 33+) | Власні сповіщення (напр. «сервіс активний») — опційно |
| `FOREGROUND_SERVICE` | Стабільний listener на OEM з агресивним kill — за потреби |
| **Немає** `INTERNET` | Політика офлайн |

Політика конфіденційності для Store: дані не покидають пристрій; видалення — через «Очистити історію» / деінсталяцію.

---

## Архітектура (коротко)

```
app/
├── data/          # Room, DAO, SQLCipher, репозиторії
├── domain/        # Use cases, моделі
├── service/       # NotificationListenerService
├── security/      # PIN, Keystore, Crypto, root check, secure wipe
└── ui/            # Compose: feed, detail, settings, lock
```

Стек: **Kotlin**, **Coroutines + Flow**, **Hilt**, **Compose Navigation**, **Coil** (локальні URI), **DataStore** (налаштування).

---

## Розробка

```bash
./gradlew test
./gradlew lint
./gradlew assembleRelease   # після налаштування signing
```

Стиль: офіційні [Kotlin conventions](https://kotlinlang.org/docs/coding-conventions.html), [Architecture Samples](https://github.com/android/architecture-samples) (Clean-ish layers).

---

## Дорожня карта

Див. таблицю фаз у [`docs/PROJECT_IDEA.md`](docs/PROJECT_IDEA.md#фази-імплементації).

| Фаза | Зміст |
|------|--------|
| 0 | Gradle, модулі, `.env`, CI, політика INTERNET-free |
| 1 | Listener + Room + базовий feed |
| 2 | Групи, оновлення, медіа |
| 3 | Шифрування, lock, 10-attempt wipe, root block |
| 4 | Налаштування, polish, Play |

---

## Ліцензія та внесок

Проєкт відкритий (Apache 2.0 — файл `LICENSE` додається у фазі 0). Issues та PR вітаються.

---

## Контакти

Замініть на свій репозиторій / email після публікації.
