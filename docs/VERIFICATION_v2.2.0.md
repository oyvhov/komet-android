# Verifikasjon av Komet 2.2.0

19. november 2026. Versjonskode 8, pakkenamn `app.komet`, minste Android API 26.

## Lokal kontroll

- 98 einingstestar bestod. Dei omfattar klokkeoppgåvene, overgangar ved tolv og midnatt og reglane for appoppdatering.
- Debug-bygg, signert release-bygg og `lintRelease` fullførte med `BUILD SUCCESSFUL`.
- Den signerte APK-en inneheld Klokkeverkstaden. Montessori-skjermen, inngangen, tekstane og musikktemaet er fjerna.
- APK-en er 2 445 826 byte. SHA-256 er `12781b5f6e3dcf6dfa24aca2a2fa6055161dd86032b1712580b8bf442f476f8c`.
- Sertifikat SHA-256 er `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b`, det same som i den publiserte APK-en for 2.1.0.
- Pakken har versjon 2.2.0 og tilgangane oppdateraren treng. Han er bygd med release-konfigurasjonen.
- Kontrollerte fargepara for tekst og tal har kontrast på minst 4,67:1. Dei to klokkevisarane har kontrast på 3,52:1 og 4,90:1 mot den mørkaste delen av urskiva.

## Etter publisering

- Bygg, einingstestar og lint bestod for [main](https://github.com/oyvhov/komet-android/actions/runs/44530627733) og [releasetaggen](https://github.com/oyvhov/komet-android/actions/runs/44530627779). Taggen viser til `837656cf8e125a78851916f239d3dcff0302dd1d`.
- Den offentlege release-lista viser v2.2.0 som nyaste stabile utgåve, med éin APK og den venta storleiken og kontrollsummen. [Komet 2.2.0](https://github.com/oyvhov/komet-android/releases/tag/v2.2.0)
- Den direkte nedlastinga og det offentlege API-endepunktet appen brukar leverte same APK som det lokale bygget. Appen sin eigen kode for å velje og validere oppdateringar godtok den offentlege releasen for ein installasjon av 2.1.0.

## Kontroll på eining

- Android godtok den offentlege APK-en som ei oppdatering av den installerte produksjonspakken. `adb install -r` endra versjonskoden frå 7 til 8.
- Windows-emulatoren feila ved oppstart. Oppstarten av debug-bygget i reserveemulatoren gjekk ut på tid. Visuell kontroll av grensesnittet og ein full installasjonstest gjennom oppdateringsmenyen står att.
