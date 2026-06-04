# Google Play — графіка для сторінки додатку

Готові PNG для [Play Console → Store listing](https://play.google.com/console) (розділ **Main store listing**).

## Файли

| Файл | Розмір | Куди в Console |
|------|--------|----------------|
| `store-icon-512.png` | 512×512 | **App icon** (high-res icon) |
| `feature-graphic-1024x500.png` | 1024×500 | **Feature graphic** (банер зверху сторінки) |
| `phone-screenshot-01-feed-1080x1920.png` | 1080×1920 | **Phone screenshots** (мін. 2) |
| `phone-screenshot-02-detail-1080x1920.png` | 1080×1920 | ↑ |
| `phone-screenshot-03-pin-1080x1920.png` | 1080×1920 | ↑ |
| `phone-screenshot-04-settings-1080x1920.png` | 1080×1920 | ↑ |

Бренд узгоджений з іконкою додатку: індиго `#4338CA` → `#312E81`, стопка карток нотіфікацій + щит.

## Рекомендації перед публікацією

1. **Скріншоти** — для продакшену краще зняти **реальні** кадри з пристрою (emulator або Nothing/Pixel), щоб UI 1:1 збігався з APK. Ці mockup-и підходять для чернетки лістингу або A/B тесту опису.
2. **Тексти в Console** (окремо від картинок): короткий опис, повний опис, privacy policy URL — у `docs/PROJECT_IDEA.md` / README.
3. **Планшети** (опційно): 7″ ≥1024×600, 10″ ≥1280×800 — за потреби додайте ще 2–4 кадри з foldable/tablet layout (фаза 6).
4. **Promo video** — YouTube URL, якщо є.

## Повторна генерація (Gemini)

Скрипт `generate_assets.sh` викликає `gemini-2.5-flash-image` (потрібен `GEMINI_API_KEY` у `~/.claude/.env`):

```bash
set -a && source ~/.claude/.env && set +a
./marketing/google-play/generate_assets.sh
```

При ліміті API (429) зачекайте кілька хвилин і повторіть. Поточний набір згенеровано через Cursor image gen і підігнано `sips` під розміри Play.

## Тексти для лістингу (чернетка)

**Назва (до 30 символів):** Notification History  

**Короткий опис (до 80):** Offline encrypted journal of your Android notifications.  

**Повний опис:** див. README проєкту + акцент на offline, SQLCipher, PIN/biometric, no root, no internet.
