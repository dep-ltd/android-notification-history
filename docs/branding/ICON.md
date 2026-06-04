# Launcher icon — Notification History

## Concept

| Елемент | Значення |
|---------|----------|
| Фон | Градієнт indigo `#4338CA` → `#312E81` |
| Дві картки | Журнал / історія нотіфікацій |
| Три лінії | Прев’ю тексту в сповіщенні |
| Щит | Приватність, шифрування, офлайн |

## Файли

- `app/src/main/res/drawable/ic_launcher_background.xml` — gradient
- `app/src/main/res/drawable/ic_launcher_foreground.xml` — symbol (vector)
- `app/src/main/res/drawable/ic_launcher_monochrome.xml` — themed icon (Android 13+)
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — adaptive icon
- `app/src/main/res/values/colors.xml` — brand colors

Adaptive icon safe zone: символ у центрі viewport 108×108; перевіряйте в **Image Asset Studio** або на пристрої (кругла / squircle mask).

## Play Store

Для listing потрібен окремий **512×512 PNG** — експорт з Android Studio: *File → New → Image Asset* на базі цих drawable, або `vectorDrawable` → PNG через Asset Studio.
