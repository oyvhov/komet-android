# Verifisering · Komet 1.2.0

Dato: 14. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` |
| Versjon | 1.2.0 (versionCode 3), minSdk 26, targetSdk 36 |
| APK | `Komet-v1.2.0.apk`, 2 285 318 byte |
| APK SHA-256 | `3a9d5f4335622a52f1fe3a5edde0f236cc58e12009bc3df2f864210d43277ca0` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) – same nøkkel som 1.1.0 |
| Løyve | `INTERNET` og `REQUEST_INSTALL_PACKAGES` (uendra) |
| Debuggable | Nei |

Kommando: `gradlew testDebugUnitTest assembleRelease lintRelease` – grønt.

## Testar

- Einingstestar: **77 av 77**. Nytt sidan 1.1.0:
  - `StrokesTest` (10) og `StrokeSheetExport`: strekgeometri, retning, rekkjefølgje og at kvart av dei
    68 teikna kan skrivast med sporaren; kontrollark i `app/build/reports/strokes`.
  - `TopicsTest` (3): alle nivå har ein oppgåvetype i riktig fag.
  - `EnglishContentTest` (5): ord, fraser, talemerke `{en:…}` og at rett bilete/farge/tal høyrer til ordet.
  - `SpaceContentTest` (4): planetrekkjefølgje, storleik, fakta og storleiksoppgåver.
  - `ProgressionTest`: lagring etter kvart svar, hald fram runde, favorittar og repetisjon.
  - `StateStoreTest`: lagra runde og favorittar, og at øydelagde verdiar blir ignorerte.
- Lint (release): 0 feil, 12 åtvaringar (avhengigheitsversjonar låste til Spole-cachen og
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
- Skriftstorleik 2.0: Heim, Utforsk og ei engelsk fraseoppgåve. «Neste oppdrag» og rangmerket er retta. Flistitlane krympar no automatisk til dei passar, kontrollert med «Verdensrommet» på bokmål ved vanleg skrift. Ved 2.0 fall emulatoren ut før nytt bilete kunne takast, så krympinga ved 2.0 er ikkje sett på skjerm etter rettinga.
- Nettbrett liggjande (2560 × 1600): skriving med biletord til høgre, og Heim med fire fag på rad.

## Release på GitHub

- Release: <https://github.com/oyvhov/komet-android/releases/tag/v1.2.0>, publisert som «latest», ikkje prerelease.
- Kjelde: tag `v1.2.0`, commit `3bcd886383721b91993cb105fdcc8890ad618917`. Koden er identisk med byggjecommiten
  `c3ba0f0`; berre denne rapporten er endra i mellomtida.
- Vedlegg: éin APK (`Komet-v1.2.0.apk`, 2 285 318 byte), `SHA256SUMS.txt`, `mapping-v1.2.0.txt` og `SOURCE_COMMIT.txt`.
- GitHub-API-et utan token (same kall som appen gjer): `draft=false`, APK-en har `state=uploaded` og
  `digest=sha256:3a9d5f4335622a52f1fe3a5edde0f236cc58e12009bc3df2f864210d43277ca0`, lik den lokale hashen.
  `releases/latest` peikar på `v1.2.0`. 1.1.0 (versjonskode 2) ser derfor 1.2.0 (versjonskode 3) som ny versjon.
- CI «Bygg og test» (einingstestar, lint og debug-bygg på Ubuntu): grøn på `main` (køyring 34810559845) og på taggen `v1.2.0` (køyring 34810559866).

## Ikkje testa

- Oppdatering gjennom appen frå 1.1.0 til 1.2.0 på ei eining (skjer når releasen er publisert).
- Fysisk nettbrett: lyd, engelsk og norsk stemme, emoji og skriving med finger på ekte skjerm.
