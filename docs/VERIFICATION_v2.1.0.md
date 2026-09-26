# Verifikasjon av Komet 2.1.0

Dato: 26. september 2026. Versjonskode 7, pakkenamn `app.komet`, minste Android API 26.

## Før publisering

- 98 lokale einingstestar bestått under utviklinga, inkludert Montessori-materiale og nye lydar.
- Mobil og nettbrett i begge retningar, skriftstorleik 2.0, nynorsk, bokmål og avslåtte
  animasjonar kontrollerte. Sjå `VERIFICATION_UI_2026-09-26.md` for detaljar.
- `testDebugUnitTest`, `assembleRelease`, `lintRelease` og `lintDebug`: BUILD SUCCESSFUL.
- APK: 2 445 826 byte, SHA-256 `95e8d640dc424d2cfbf6445d6037f3d3269fe2de776a0e7b2a30b1bd842089ce`.
- Sertifikat SHA-256: `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b`.
- Pakkedata kontrollerte: `app.komet`, versjon `2.1.0`, kode 7, minSdk 26 og ikkje debuggable.
- APK-en har INTERNET og REQUEST_INSTALL_PACKAGES; GitHub-oppdateraren er aktiv i denne utgåva.
- Signeringsfiler og emulatordata er Git-ignorerte og blir ikkje publiserte.

## Etter publisering

- Publisert stabil release: [v2.1.0](https://github.com/oyvhov/komet-android/releases/tag/v2.1.0), merkt latest, ikkje kladd eller prerelease.
- Taggen viser til `dede3963b6189b082f262da0e62507b0b7e3753c`. APK, R8-mapping,
  `SHA256SUMS.txt` og `SOURCE_COMMIT.txt` er vedlagde.
- GitHub CI bestått for både [main](https://github.com/oyvhov/komet-android/actions/runs/36224777453)
  og [v2.1.0](https://github.com/oyvhov/komet-android/actions/runs/36224777292), på same releasecommit.
- Release-lista henta utan token: éin APK, 2 445 826 byte og GitHub-digest
  `sha256:95e8d640dc424d2cfbf6445d6037f3d3269fe2de776a0e7b2a30b1bd842089ce`.
- APK-en lasta ned frå den offentlege lenkja til ei eiga fil. SHA-256 var identisk med bygget.
- Ekte oppdatering testa på `Komet_Phone`, `emulator-5580`, frå den tidlegare publiserte
  produksjonsappen 2.0.0 (kode 6) til 2.1.0 (kode 7), med same signatur.
  Foreldre → Innstillingar → Sjekk no fann 2.1.0. Last ned oppdatering fullførte kontrollen,
  Installer oppdatering opna Android-installasjonen. Play Protect skanna appen og tillét
  installasjon; Android viste «App installed». Den nye APK-en vart ikkje installert med adb.
- Testprofilen TTee var bevart med 3 stjerner og 18 gullklumpar. Ein fullført runde,
  100 % rett første gong, 1 minutt og Blanda og tekst: 1 av 6 nivå fullført var identiske
  før og etter oppdateringa. Nytt Montessori-val vart vist på stjernekartet; verkstaden
  opna med alle tre arbeid i det signerte release-bygget. Profil, stjerner og gull var
  òg bevarte etter omstart av emulatoren.
- Lokale skjermbilete og UI-dumpar ligg i `app/build/reports/release-v2.1.0/` (Git-ignorert).
- Automatisk sjekk er framleis aktiv i GitHub-utgåva. Appen sjekkar når han kjem fram,
  høgst éin gong kvar tolvte time. Ein vaksen vel nedlasting og godkjenner installasjonen.

Denne rapporten blir oppdatert i ein eigen commit etter publisering; releasetaggen blir ikkje flytta.

## Avgrensingar

Lytting, talebalanse og yting på fysisk nettbrett og observasjonar av barn i skulen står att.
