#!/usr/bin/env bash
# Generate Google Play assets via Gemini (requires GEMINI_API_KEY in ~/.claude/.env)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$ROOT/marketing/google-play"
RAW="$OUT/raw"
GEN="$HOME/.claude/skills/gemini-image-gen/scripts/generate.py"

source ~/.claude/.env 2>/dev/null || true
if [[ -z "${GEMINI_API_KEY:-}" ]]; then
  echo "GEMINI_API_KEY not set. Add to ~/.claude/.env"
  exit 1
fi

run() {
  local name="$1"
  local prompt="$2"
  echo "=== $name ==="
  python3 "$GEN" --prompt "$prompt" --output "$RAW/${name}.png"
  sleep 8
}

BRAND="Android app Notification History: offline private encrypted notification journal. Brand: deep indigo gradient #4338CA to #312E81, white and soft lavender #C7D2FE accents, Material Design 3. Visual motif: two stacked rounded notification cards with lines of text and a small shield with checkmark (privacy). Modern, trustworthy, minimal. No Google Play badge, no third-party logos, no watermarks."

run "01-store-icon" "${BRAND} Square app icon for Google Play, 512x512 style: centered icon on smooth indigo gradient background, stacked cards and shield, crisp flat vector-like illustration, high contrast, professional store listing quality."

run "02-feature-graphic" "${BRAND} Wide horizontal banner 1024x500 aspect ratio for Google Play feature graphic: left side bold title Notification History and tagline Private offline notification journal, right side stylized phone showing notification feed list, indigo gradient background, clean marketing layout, no device bezels required."

run "03-screenshot-feed" "${BRAND} Vertical phone screenshot mockup 9:16 portrait: Material 3 notification feed screen with grouped items (Telegram, Gmail, Slack style generic icons), search bar at top, indigo top app bar titled Notification History, realistic Android UI, light theme."

run "04-screenshot-detail" "${BRAND} Vertical phone screenshot 9:16: detail screen of one notification with title, body text, timestamp, image thumbnail strip, Open link button, back arrow, Material 3 indigo accents."

run "05-screenshot-security" "${BRAND} Vertical phone screenshot 9:16: PIN unlock screen with 6-dot indicator and custom numeric keypad, subtitle Unlock your journal, shield icon, dark indigo background, secure calm aesthetic."

run "06-screenshot-settings" "${BRAND} Vertical phone screenshot 9:16: settings screen list items: Biometric unlock, Ignore apps, Retention, Wipe data, About, Material 3 settings style with indigo theme."

echo "Resizing with sips..."
finalize() {
  local src="$1" w="$2" h="$3" dst="$4"
  cp "$src" "$dst"
  sips -z "$h" "$w" "$dst" >/dev/null
}

finalize "$RAW/01-store-icon.png" 512 512 "$OUT/store-icon-512.png"
finalize "$RAW/02-feature-graphic.png" 1024 500 "$OUT/feature-graphic-1024x500.png"
finalize "$RAW/03-screenshot-feed.png" 1080 1920 "$OUT/phone-screenshot-01-feed-1080x1920.png"
finalize "$RAW/04-screenshot-detail.png" 1080 1920 "$OUT/phone-screenshot-02-detail-1080x1920.png"
finalize "$RAW/05-screenshot-security.png" 1080 1920 "$OUT/phone-screenshot-03-pin-1080x1920.png"
finalize "$RAW/06-screenshot-settings.png" 1080 1920 "$OUT/phone-screenshot-04-settings-1080x1920.png"

echo "Done. Files in $OUT"
