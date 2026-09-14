# Verifisering · Komet 2.0.0-beta2 (testutgåve)

Dato: 14. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` |
| Versjon | 2.0.0-beta2 (versionCode 5), minSdk 26, targetSdk 36 |
| APK | `Komet-v2.0.0-beta2.apk`, 2 413 062 byte |
| APK SHA-256 | `e7a6aa687d0a076910388b9c6be1727d2176cd27964c5ab50cd7217f52969625` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) – same nøkkel som 2.0.0-beta1 |
| Løyve | `INTERNET` og `REQUEST_INSTALL_PACKAGES` (uendra) |
| Debuggable | Nei |

Kommando: `gradlew testDebugUnitTest assembleDebug assembleRelease lintRelease` – grønt. Bygd frå commit
`8dc2613`.

## Testar

- Einingstestar: **90 av 90**. Nytt sidan 2.0.0-beta1:
  - `WalletTest` (8): gullklumpar for stjerner, bonus for dagens oppdrag og rekord i rakettløpet, kjøp med
    for lite, kjøp som går og kjøp av noko ein alt har, saldoen går aldri under 0, berre kjøpte ting kan
    takast på, kvar klump på planeten kan plukkast éin gong, startgåva, og at alle 32 varene har unik id,
    pris og namn på nynorsk og bokmål.
  - `StateStoreTest`: gullklumpar, kjøpte ting, kva som er på og plukka klumpar blir lagra og lesne att, og
    ugyldige verdiar blir rydda (negativ saldo, ukjende varer, ting som ikkje er kjøpte).
- Lint (release): 0 feil, 12 åtvaringar (same som 2.0.0-beta1: avhengigheitsversjonar og kjende åtvaringar).

## Visuell kontroll (emulator `Komet_Phone`, debug-bygg med same kode)

- Mobil ståande: gullklumpar på kartet, profilvindauget, butikken med alle åtte hyllene, prøving, kjøp med
  stadfesting (14 → 6), «ikkje nok gullklumpar», resultat med «+3 gullklumpar», klump å plukke på planeten
  (13 → 14), raud X på oppgåver, resultat, avslutt-spørsmål, romkort og ny profil.
- Nettbrett ståande (1600 × 2560): butikken, profilvindauget i to kolonnar og planeten. Nettbrett
  liggjande (2560 × 1600): butikken og profilvindauget.
- Alle 32 varene på korta i butikken, og eit utval på scena: drakekam, regnbogevisir, galaksedrakt, kappe,
  jetpakke, vengjer, propell, stjerneantenne, sløyfe, solbriller, Bolt-fargar og rakettfargar.
- Skriftstorleik 2.0: butikken, profilvindauget og planeten med det lengste namnet («Verdensrommet»).

## Utgåvebygget på emulatoren

Den signerte APK-en er installert over ein gammal lokal 1.0.0 (versionCode 1) på `Komet_Phone` med
`adb install -r` og starta:

- Første start viste «Komet svarar ikkje» etter om lag ti sekund. Stakksporet frå ANR-en viser at
  hovudtråden ventar på RenderThread, som kompilerer GPU-program gjennom emulatorens grafikkrøyr
  (`glCreateProgram` → `qemu_pipe_read`). Ingen av trådane til Komet arbeidde, lydtrådane var ferdige, og
  emulatoren var under tung last rett etter bygget. Dette er shader-kompilering i emulatoren, ikkje kode i
  appen.
- Andre start: ingen ANR og ingen krasj. Kartet, profilvindauget og butikken fungerer i R8-bygget, og den
  gamle profilen fekk startgåva (15 gullklumpar) og standardastronauten.

## Release på GitHub

- Release: <https://github.com/oyvhov/komet-android/releases/tag/v2.0.0-beta2>, publisert som testutgåve
  (prerelease), ikkje «latest».
- Kjelde: tag `v2.0.0-beta2`, commit `725b9a68c78a943d402dd0f3b94ba4b3273a7f4c`. Koden er identisk med
  byggjecommiten `8dc2613`; berre denne rapporten er lagd til.
- Vedlegg: éin APK (`Komet-v2.0.0-beta2.apk`, 2 413 062 byte), `SHA256SUMS.txt`,
  `mapping-v2.0.0-beta2.txt` og `SOURCE_COMMIT.txt`.
- GitHub-API-et utan token (same kall som appen gjer): `draft=false`, `prerelease=true`, APK-en har
  `state=uploaded` og `digest=sha256:e7a6aa687d0a076910388b9c6be1727d2176cd27964c5ab50cd7217f52969625`, lik den
  lokale hashen. `releases/latest` peikar framleis på `v1.2.0`, så appen tilbyr 2.0.0-beta2 berre når
  «Testutgåver» er slått på.
- APK-en er ikkje lasta ned att frå GitHub; sjekksummen frå API-et er samanlikna med den lokale fila.
- CI «Bygg og test» (einingstestar, lint og debug-bygg på Ubuntu): grøn på `main` (køyring 34846152786) og
  på taggen `v2.0.0-beta2` (køyring 34846152815).

## Ikkje testa

- Oppdatering gjennom appen frå 2.0.0-beta1 (krev «Testutgåver» på foreldresida). `adb install -r` er ikkje
  ein test av oppdateringa.
- Første start på ei ekte eining og fysisk nettbrett: yting, lyd, stemmer og berøring på ekte skjerm.
