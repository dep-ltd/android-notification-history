# Notification History

**Офлайн, відкритий код, приватний журнал push-нотіфікацій Android.**

Додаток перехоплює нотіфікації всіх установлених програм (через `NotificationListenerService`), зберігає їх у **зашифрованій** локальній базі даних (SQLCipher + Android Keystore) разом із зображеннями та посиланнями, і показує єдиний хронологічний лог на головному екрані. **Інтернет не використовується** — ні для збору, ні для синхронізації, ні для аналітики.

| | |
|---|---|
| Платформа | Android (мін. API 26+, target SDK 34) |
| UI | Jetpack Compose + Material 3 |
| Дані | Room + SQLCipher, DataStore, локальні медіа |
| Доступ до журналу | Біометрія або 6-значний PIN (власна numpad) |
| Захист від підбору | 10 невдалих спроб (PIN або біометрія) → повне стирання даних |
| Root | Додаток **не працює** на рутованих пристроях |
| Ліцензія | Apache 2.0 |

Повний опис продукту, архітектури та **пофазовий план** — у [`docs/PROJECT_IDEA.md`](docs/PROJECT_IDEA.md).

**Стан імплементації:** фази **0–4** реалізовані в `main`. Далі — фаза 5 (опційно) / фаза 6 (foldables).

---

## Можливості

- **Журнал нотіфікацій** — хронологічний feed, групи з expand/collapse, оновлення за `stableKey`.
- **Детальний перегляд** — текст, час, галерея зображень (Coil), посилання (`ACTION_VIEW` без `INTERNET`).
- **Чорний список додатків** — DataStore + екран вибору з пошуком.
- **Захист журналу** — PIN (6 цифр, custom numpad), опційна біометрія, `FLAG_SECURE`, автоблокування у фоні.
- **Шифрування** — SQLCipher passphrase у EncryptedSharedPreferences / Keystore після створення PIN.
- **Анти-підбір** — 10 невдалих спроб → `WipeAllDataUseCase` (БД, медіа, DataStore, ключі).
- **Без root** — блокуючий екран попередження.

---

## Вимоги

- Android Studio Ladybug / останній stable
- JDK 17+
- Google Play services **не обов’язкові**

---

## Швидкий старт

```bash
git clone https://github.com/dep-ltd/android-notification-history.git
cd android-notification-history
cp .env.example .env
# Опційно: DEBUG_DB_ENCRYPTION_KEY для debug-збірок
./gradlew assembleDebug
```

На пристрої:

1. Встановити APK.
2. **Спеціальний доступ → Доступ до сповіщень** — увімкнути Notification History.
3. Пристрій **не рутований**.
4. Прийняти політику стирання → створити PIN → розблокувати feed.
5. За потреби: **Налаштування → Ignore apps** або біометрія.

---

## Конфігурація секретів (`.env`)

Секрети **не комітяться**. Шаблон: [`.env.example`](.env.example).

| Змінна | Призначення |
|--------|-------------|
| `PLAY_UPLOAD_*` | Upload keystore для release (опційно) |
| `DEBUG_DB_ENCRYPTION_KEY` | Base64 passphrase для **debug** (до створення PIN) |

Release: passphrase генерується на пристрої при створенні PIN.

---

## Дозволи

| Дозвіл / API | Навіщо |
|--------------|--------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Збір нотіфікацій |
| `USE_BIOMETRIC` | Розблокування |
| **Немає** `INTERNET` | Офлайн-політика |

Політика Store: [`docs/PRIVACY.md`](docs/PRIVACY.md).

---

## Розробка

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

CI: [`.github/workflows/android.yml`](.github/workflows/android.yml).

---

## Дорожня карта

| Фаза | Статус |
|------|--------|
| 0 | ✅ Gradle, CI, root guard, `.env` |
| 1 | ✅ Listener, parser, feed, detail |
| 2 | ✅ Групи, медіа, blacklist, settings |
| 3 | ✅ SQLCipher, lock, wipe, біометрія |
| 4 | ✅ Retention, пошук, uk/en, Play docs |
| 4.1 | ✅ Стабілізація: PIN/БД, групи, app picker, empty feed — [§13](docs/PROJECT_IDEA.md#13-чекліст-повторної-валідації-після-фаз-41--6) |
| 5 | ⏳ Опційно (export, widget, Wear) |
| 6 | ✅ Adaptive UI (fold / tablet) |

Повторна валідація: [`docs/PROJECT_IDEA.md` §13](docs/PROJECT_IDEA.md#13-чекліст-повторної-валідації-після-фаз-41--6).

---

## Ліцензія

Apache 2.0 — див. [`LICENSE`](LICENSE).
