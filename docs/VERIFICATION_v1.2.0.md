# Verifisering · Komet 1.2.0

Dato: 14. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` |
| Versjon | 1.2.0 (versionCode 3), minSdk 26, targetSdk 36 |
| APK | `Komet-v1.2.0.apk`, APK_SIZE byte |
| APK SHA-256 | `APK_SHA256` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) – same nøkkel som 1.1.0 |
| Løyve | `INTERNET` og `REQUEST_INSTALL_PACKAGES` (uendra) |
| Debuggable | Nei |

Kommando: `gradlew testDebugUnitTest assembleRelease lintRelease` – grønt.

## Testar

- Einingstestar: **TEST_COUNT av TEST_COUNT**. Nytt sidan 1.1.0:
  - `StrokesTest` (10) og `StrokeSheetExport`: strekgeometri, retning, rekkjefølgje og at kvart av dei
    68 teikna kan skrivast med sporaren; kontrollark i `app/build/reports/strokes`.
  - `TopicsTest` (3): alle nivå har ein oppgåvetype i riktig fag.
  - `EnglishContentTest` (5): ord, fraser, talemerke `{en:…}` og at rett bilete/farge/tal høyrer til ordet.
  - `SpaceContentTest` (4): planetrekkjefølgje, storleik, fakta og storleiksoppgåver.
  - `ProgressionTest`: lagring etter kvart svar, hald fram runde, favorittar og repetisjon.
  - `StateStoreTest`: lagra runde og favorittar, og at øydelagde verdiar blir ignorerte.
- Lint (release): 0 feil, LINT_WARNINGS åtvaringar (avhengigheitsversjonar låste til Spole-cachen og
  kjende åtvaringar frå 1.1.0).

## Visuell kontroll (emulator `Komet_Phone`, debug-bygg)

- Heim med nytt utsjånad, «Uferdig runde», favorittar og alle fire fag.
- Utforsk, oppgåvetype «Pluss» med hjarte, og favoritt som snarveg på Heim.
- Skriv med kometen: store bokstavar (A skriven med tre fingerdrag, gull og gnistar til slutt),
  små bokstavar (p med nedrom) og tal (2).
- Engelsk: lytt og finn farge, fraser med situasjonsbilete, verdskart.
- Verdsrommet: planetbilete, storleiksoppgåve og sant/usant.
- Repetisjon: kort på Matte-kartet, blanda runde og resultat.
- Romkort med sjeldanheitsrammer, rakettløp og kartnodar med ny skrift.
- Skriftstorleik 2.0: FONT_CHECK.

## Ikkje testa

- Oppdatering gjennom appen frå 1.1.0 til 1.2.0 på ei eining (skjer når releasen er publisert).
- Fysisk nettbrett: lyd, engelsk og norsk stemme, emoji og skriving med finger på ekte skjerm.
