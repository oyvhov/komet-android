# Verifikasjon av Komet 2.0.0

Dato: 15. september 2026. Versjonskode 6, pakkenamn app.komet, minste Android API 26.

## Før publisering

- 94 einingstestar bestått, inkludert 12 000 uavhengig kontrollerte romoppgåver på begge målformer.
- Nivågrenser, synleg bilethjelp, svaralternativ, variasjon og anbefaling etter klassesteg testa.
- testDebugUnitTest, assembleDebug, assembleRelease og lintRelease: BUILD SUCCESSFUL.
- Sertifikat SHA-256: e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b.
- APK SHA-256: ebb25cea9ae03ef9ee34ab10fd9a3c486f41cb522d4bdc5dedc5faca5c6bd8cf.
- Produksjonspakke og versjon kontrollert med aapt2; APK-en er ikkje debuggable.
- Eiga Komet-emulator på port 5580. Mobil og nettbrett, skrift 2.0, begge målformer, hjelp,
  rette svar, to feil, utsending og resultat kontrollert under arbeidet.
- Redusert rørsle: utsending fullført og neste oppgåve med baselys kontrollert.
- Nye landskap og marknadsføringsbilete kontrollerte visuelt. Bileta viser ein testprofil.
- Ingen signeringsfiler eller persondata i release eller Git.

## Etter publisering

- Publisert stabil release [v2.0.0](https://github.com/oyvhov/komet-android/releases/tag/v2.0.0), merka latest, med éin universal APK og tre kontrollfiler.
- Kjeldecommit: `ee907a5893a46c9e34e55df240139b24d6141e3f`. Releasetaggen er uendra.
- GitHub CI bestått: [Android CI](https://github.com/oyvhov/komet-android/actions/runs/34994112973) og [andre releasekøyring](https://github.com/oyvhov/komet-android/actions/runs/34994112825).
- Offentleg API utan token viste rett release og digest. Separat nedlasting av APK-en (2 413 058 byte) gav same SHA-256 som byggjefila over.
- Ekte oppdatering på Komet-emulatoren: installert produksjonsutgåve 2.0.0-beta2 (kode 5) → foreldresida → Sjekk no → Last ned oppdatering → Installer oppdatering → Android Update → App installed.
- Android-løyvet «Allow from this source» vart gitt til Komet. Første forsøk feila med INSTALL_FAILED_VERIFICATION_FAILURE; nytt forsøk gjennom same appflyt fullførte, utan å slå av verifikasjon eller bruke adb install.
- Installert pakkenamn app.komet, versionName 2.0.0 og versionCode 6 kontrollert etterpå. Appen opna med same testprofil, klassesteg 1, 0 stjerner og 15 gull. Profilen hadde ingen fullførte nivå før testen; bevaring av ikkje-tom nivåhistorikk er derfor ikkje stadfesta av denne testen.
- Lokale testbevis: app/build/reports/mission-check/update-before.png og update-after.png (Git-ignorerte).

## Avgrensingar

Fysisk nettbrett, faktisk tale-/lydavspeling og observasjon av barn som spelar er ikkje verifisert.
Klassesteg er eit foreslått startpunkt. Oppdragsbok og varige landskapsendringar står att.
