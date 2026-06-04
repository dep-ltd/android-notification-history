# Google Play — ASO Materials & Store Listing
# Package: com.depsoftware.notifhistory · Version: 1.0 (versionCode 1)

---

## APP IDENTITY

| Field | Value |
|-------|-------|
| Package name | `com.depsoftware.notifhistory` |
| Category | Tools |
| Content rating | Everyone |
| Price | Free |
| Contains ads | No |
| In-app purchases | No |

---

## ENGLISH (en-US) — PRIMARY

### App title (≤ 30 chars)
```
Notification History Log
```

### Short description (≤ 80 chars)
```
Private offline notification log · encrypted · no internet needed
```

### Full description (≤ 4000 chars)
```
Never lose a notification again. Notification History Log captures every push 
notification you receive and stores it privately on your device — encrypted, 
offline, and completely under your control.

── WHAT IT DOES ──
• Saves every notification with full text, images, and timestamps
• Groups notifications the same way your shade does
• Search by keyword or filter by app
• View the full message even after it was dismissed

── PRIVACY & SECURITY ──
• Zero internet — no data ever leaves your device
• No accounts, no cloud sync, no analytics SDK
• Database encrypted with SQLCipher (AES-256)
• Unlock with 6-digit PIN or biometric (fingerprint / face)
• Custom PIN keypad — system keyboard never sees your PIN
• 10 consecutive failed unlock attempts permanently wipe all data
  (disclosed and confirmed before setup)
• Does not work on rooted devices

── DAILY USE ──
• Chronological feed with sticky date headers
• Expand grouped conversations to see each message
• Tap any notification to read the full text, metadata, and images
• Per-app ignore list — block apps you never want to log
• Automatic retention cleanup (configurable: 7 – 365 days)
• Foreground service keeps the listener alive on all OEM ROMs

── ADAPTIVE UI ──
• Material 3 design with dynamic color (Android 12+)
• Full dark mode support
• List + detail side-by-side layout on tablets and foldables
• Navigation rail on medium / large screens
• Edge-to-edge display

── OPEN & HONEST ──
• Open source — audit the notification listener and encryption logic
• Notification Listener access explained in plain language before granting
• No hidden permissions: only Notification Listener + optional biometric

Notification History Log is designed for people who want full control over 
their own notification data — journalists, professionals, privacy advocates, 
or anyone tired of losing important messages.
```

### Keywords / Tags (for internal search strategy)
```
notification history, notification log, notification tracker, notification saver,
notification backup, privacy, encrypted, offline, PIN lock, secure, no internet,
notification reader, push notification history, missed notifications
```

---

## UKRAINIAN (uk) — SECONDARY

### Назва (≤ 30 символів)
```
Журнал сповіщень
```

### Короткий опис (≤ 80 символів)
```
Приватний офлайн журнал сповіщень · шифрування · без інтернету
```

### Повний опис (≤ 4000 символів)
```
Більше жодного пропущеного сповіщення. Журнал сповіщень зберігає кожне 
push-сповіщення на вашому пристрої — зашифровано, офлайн і повністю під 
вашим контролем.

── ЩО РОБИТЬ ДОДАТОК ──
• Зберігає кожне сповіщення з повним текстом, зображеннями та часом
• Групує сповіщення так само, як системна шторка
• Пошук за ключовим словом або фільтр за додатком
• Читайте повний текст навіть після видалення з шторки

── ПРИВАТНІСТЬ І БЕЗПЕКА ──
• Без інтернету — жодні дані не залишають пристрій
• Без облікових записів, хмари, аналітики та SDK стеження
• БД зашифрована SQLCipher (AES-256)
• Розблокування 6-значним PIN або біометрією (відбиток / обличчя)
• Власна клавіатура — системна IME ніколи не бачить PIN
• 10 невдалих спроб підряд → безповоротне знищення всіх даних
  (вимагає явної згоди перед налаштуванням)
• Не працює на рутованих пристроях

── ЩОДЕННЕ ВИКОРИСТАННЯ ──
• Хронологічний журнал зі sticky-заголовками дат
• Розгортання груп для перегляду кожного повідомлення
• Натисніть на сповіщення — повний текст, метадані та зображення
• Чорний список додатків — блокуйте те, що не хочете зберігати
• Автоматичне видалення старих записів (7 – 365 днів, за вибором)
• Foreground-сервіс для стабільної роботи на всіх OEM-прошивках

── АДАПТИВНИЙ UI ──
• Material 3 з динамічним кольором (Android 12+)
• Повна підтримка темного режиму
• Список + деталі поруч на планшетах і складаних пристроях
• Навігаційна рейка на середніх / великих екранах
• Edge-to-edge відображення

── ВІДКРИТО І ЧЕСНО ──
• Відкритий код — аудит listener і шифрування доступний
• Доступ до сповіщень пояснюється зрозумілою мовою до видачі дозволу
• Жодних прихованих дозволів: лише Notification Listener + опційна біометрія
```

---

## GRAPHIC ASSETS CHECKLIST

| Asset | Size | Notes |
|-------|------|-------|
| App icon (hi-res) | 512 × 512 px | PNG, no alpha, no rounded corners (Play adds mask) |
| Feature graphic | 1024 × 500 px | Key visual / banner shown on store page |
| Phone screenshots | 320–3840 px (min 2, max 8) | 16:9 or 9:16 portrait recommended |
| 7-inch tablet screenshots | optional | Required for tablet rating |
| 10-inch tablet screenshots | optional | Required for tablet rating |

### Screenshot subjects (recommended order for phones)
1. Feed with grouped notifications and search bar
2. Unlock screen with PIN numpad
3. Detail screen with full notification text
4. Settings — ignore apps / blacklist
5. Adaptive layout on folded / tablet (if screenshot available)

---

## DATA SAFETY FORM (Play Console answers)

**Does your app collect or share any of the required user data types?**
→ No (all data stays on device; no user data is collected, stored externally, or shared)

**Is all of the user data collected by your app encrypted in transit?**
→ Not applicable — the app does not transmit data

**Do you provide a way for users to request that their data is deleted?**
→ Yes — "Clear all history" in Settings, plus uninstall removes all data; 10-failed-attempt wipe also destroys all data

**Security practices:**
- Data encrypted at rest: ✓ (SQLCipher AES-256)
- Committed to Google Play Families Policy: No (app not designed for children)

---

## NOTIFICATION LISTENER DECLARATION (required by Play policy)

**Declaration text for Play Console:**
```
This app uses the Notification Listener Service to capture and display a 
private on-device history of the user's own notifications. The feature is 
activated only after the user explicitly grants access in system settings 
and creates a PIN. Notification data is stored exclusively on the user's 
device using AES-256 encryption. No notification data is transmitted 
off-device, shared with third parties, or used for any purpose other than 
displaying the private history to the user.
```

---

## CONTENT RATING QUESTIONNAIRE

Answer all questions as follows (for IARC / Google Play rating):

| Question | Answer |
|----------|--------|
| Violence | None |
| Sexual content | None |
| Language | None |
| Controlled substances | None |
| User-generated content | No (all content is user's own notifications) |
| Social features | None |

Expected rating: **Everyone / PEGI 3 / USK 0**

---

## PRIVACY POLICY

Host `docs/PRIVACY.md` at a stable URL before submitting (e.g., GitHub Pages or a static host).

Suggested URL pattern: `https://[username].github.io/android-notification-history/privacy`

---

## RELEASE NOTES (What's New) — v1.0

### English
```
First release.

• Private, encrypted notification history — fully offline
• SQLCipher AES-256 database with PIN or biometric unlock
• Grouped feed with search and per-app filters
• 10-failed-attempt wipe policy (confirmed before setup)
• Adaptive layout for tablets and foldables
• Material 3 with dynamic color
```

### Ukrainian
```
Перший реліз.

• Приватний зашифрований журнал сповіщень — повністю офлайн
• SQLCipher AES-256 з PIN або біометрією
• Групований журнал із пошуком і фільтром за додатком
• Знищення даних після 10 невдалих спроб (підтверджується до налаштування)
• Адаптивний layout для планшетів і складаних пристроїв
• Material 3 з динамічним кольором
```

---

## KEYSTORE INFO (keep safe — never commit)

| Field | Value |
|-------|-------|
| File | `upload-keystore.jks` (project root, git-ignored) |
| Alias | `upload` |
| Algorithm | RSA 4096-bit |
| Valid until | 2053-10-21 |
| SHA-256 fingerprint | `03:93:C6:F6:8F:44:34:93:B6:1F:DB:33:A5:B7:AC:5A:B8:2C:D5:2C:16:71:69:75:D2:C9:C2:8A:8E:37:8F:B1` |
| Passwords | see `.env` (never commit) |

**Play App Signing flow:**
1. Upload AAB — Play Console will re-sign with its own app signing key
2. The upload keystore is used ONLY to authenticate uploads to Play
3. Register the upload key SHA-256 fingerprint in Play Console → Setup → App signing

---

## BUILD COMMANDS REFERENCE

```bash
# Debug APK (install on device)
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Release AAB (upload to Play)
./gradlew bundleRelease
# output: app/build/outputs/bundle/release/app-release.aab

# Unit tests
./gradlew testDebugUnitTest

# Lint
./gradlew lint

# Verify no INTERNET in manifest
./gradlew :app:processReleaseMainManifest
grep -i "uses-permission.*INTERNET" app/build/intermediates/merged_manifests/release/*/AndroidManifest.xml && echo FAIL || echo OK
```
