# Verifisering · Komet 2.0.0-beta1 (testutgåve)

Dato: 14. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` |
| Versjon | 2.0.0-beta1 (versionCode 4), minSdk 26, targetSdk 36 |
| APK | `Komet-v2.0.0-beta1.apk`, 2 383 630 byte |
| APK SHA-256 | `0b848ae340fa789f63a28631f12d3344172a3a1fae78cfbf06d5e75ae79f59aa` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) – same nøkkel som 1.2.0 |
| Løyve | `INTERNET` og `REQUEST_INSTALL_PACKAGES` (uendra) |
| Debuggable | Nei |

Kommando: `gradlew testDebugUnitTest assembleDebug assembleRelease lintRelease` – grønt. Bygd frå commit
`a0be3f0`.

## Testar

- Einingstestar: **81 av 81**. Nytt sidan 1.2.0:
  - `MusicComposerTest` (3): kvar melodi er ein sømlaus loop utan klipping, og komponeringa er lik kvar gong.
  - `StateStoreTest`: musikkbrytaren, astronauten (også rydding av ugyldige verdiar og utsjånad for gamle
    profilar) og kva planetar Bolt har ønskt velkomen til.
- Lint (release): 0 feil, 12 åtvaringar (same som 1.2.0: avhengigheitsversjonar og kjende åtvaringar).

## Visuell kontroll (emulator `Komet_Phone`, debug-bygg med same kode)

- Stjernekartet i mobil ståande og nettbrett liggjande (2560 × 1600): sola, planetane med eigne overflater,
  rakettbana, romkort, Utforsk, rakettflyging med hale og drag utan at draget blir eit trykk.
- Alle fire planetane: Talplaneten, Bokstavplaneten, Engelskplaneten og rombasen, med sti, skilt, verkstad,
  innbyggjarar, astronaut og Bolt. Astronauten går til valt nivå og vidare etter ei fullført runde.
- Nivåkortet nede på mobil og til høgre på nettbrett, låst nivå, favoritt og opplesing.
- Astronautbyggjaren i oppstarten og frå profilmenyen, og astronauten på foreldresidene.
- Oppgåver med planetbakgrunn, astronauten på framdriftslinja, Bolt i svarbanneret og astronaut og Bolt ved
  svara på nettbrett. Resultatet som feiring på mobil og nettbrett, med «Til planeten».
- Bolt si velkomst første gong på ein planet.
- Skriftstorleik 2.0: stjernekartet og planeten med nivåkort.
- Musikk: AudioTrack startar for kartet, alle seks melodiane blir lagra i `cache/music`, og seinare start
  les filene.

## Release på GitHub

- Release: <https://github.com/oyvhov/komet-android/releases/tag/v2.0.0-beta1>, publisert som testutgåve
  (prerelease), ikkje «latest».
- Kjelde: tag `v2.0.0-beta1`, commit `eb07cfca28e280820c06b6fe5555317248d04e66`. Koden er identisk med
  byggjecommiten `a0be3f0`; berre denne rapporten er lagd til.
- Vedlegg: éin APK (`Komet-v2.0.0-beta1.apk`, 2 383 630 byte), `SHA256SUMS.txt`,
  `mapping-v2.0.0-beta1.txt` og `SOURCE_COMMIT.txt`.
- GitHub-API-et utan token (same kall som appen gjer): `draft=false`, `prerelease=true`, APK-en har
  `state=uploaded` og `digest=sha256:0b848ae340fa789f63a28631f12d3344172a3a1fae78cfbf06d5e75ae79f59aa`, lik den
  lokale hashen. `releases/latest` peikar framleis på `v1.2.0`, så appen tilbyr 2.0.0-beta1 berre når
  «Testutgåver» er slått på.
- APK-en er ikkje lasta ned att frå GitHub; sjekksummen frå API-et er samanlikna med den lokale fila.
- CI «Bygg og test» (einingstestar, lint og debug-bygg på Ubuntu): grøn på `main` (køyring 34829412989) og
  på taggen `v2.0.0-beta1` (køyring 34829413034).

## Ikkje testa

- Utgåvebygget (R8) er ikkje starta på emulator eller eining før publisering; emulatoren var stoppa, og
  testutgåva skulle ut utan venting. Debug-bygget med same kode er kontrollert som over.
- Oppdatering gjennom appen frå 1.2.0 til 2.0.0-beta1 (krev «Testutgåver» på foreldresida).
- Fysisk nettbrett: yting, lyd og musikk, stemmer og berøring på ekte skjerm. Musikken er ikkje lytta til
  på eining, berre kontrollert i lydsystemet og som WAV-filer frå testen.
