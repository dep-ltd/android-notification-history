# Google Play release checklist (v1.0)

## Build

- [ ] `./gradlew assembleRelease` succeeds with upload keystore (`.env` / `PLAY_UPLOAD_*`)
- [ ] `./gradlew lint test` clean on `main`
- [ ] Merged manifest has **no** `INTERNET` permission:

```bash
./gradlew :app:processReleaseMainManifest
grep INTERNET app/build/intermediates/merged_manifests/release/AndroidManifest.xml && echo FAIL || echo OK
```

## Policy & Store

- [ ] Privacy policy URL live (from [`PRIVACY.md`](PRIVACY.md))
- [ ] Store listing text ([`STORE_LISTING.md`](STORE_LISTING.md)) uk + en
- [ ] Data safety form completed (see STORE_LISTING)
- [ ] Notification Listener declaration submitted
- [ ] Content rating questionnaire
- [ ] Play App Signing enabled; upload key only local

## Device QA

- [ ] Fresh install: permission → consent → PIN → feed
- [ ] Notifications appear after listener enabled
- [ ] Background lock + timeout (0 / 1 / 5 min)
- [ ] 10 failed PIN → wipe screen → new setup
- [ ] Root device shows block screen only
- [ ] OEM smoke: Samsung / Xiaomi / Nothing (if available)

## Internal testing

- [ ] Upload AAB to **Internal testing** track
- [ ] At least 1 tester device verified
