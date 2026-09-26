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

Offentleg release, digest, GitHub CI og ekte oppdatering gjennom appen blir dokumenterte etter
publisering, i ein eigen commit utan å flytte releasetaggen.

## Avgrensingar

Lytting, talebalanse og yting på fysisk nettbrett og observasjonar av barn i skulen står att.
