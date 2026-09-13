# Verifisering · Komet 1.1.0

Dato: 13. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` |
| Versjon | 1.1.0 (versionCode 2), minSdk 26, targetSdk 36 |
| APK | `Komet-v1.1.0.apk`, 1 515 938 byte |
| APK SHA-256 | `b1657b150183a5d734d68b37e591a884129e26a1f84035d2c3371761e568ccd9` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) |
| Løyve | `INTERNET` og `REQUEST_INSTALL_PACKAGES` (nye, berre for oppdatering) |
| Debuggable | Nei |

Kommando: `gradlew testDebugUnitTest assembleDebug assembleRelease lintRelease` – grønt.

## Testar

- Einingstestar: **47 av 47** i 7 klassar. Nytt: `ReleasePolicyTest` (7) dekkjer versjonsjamføring,
  val av nyaste utgåve, at kladdar, testutgåver, debug-bygg og tvetydige APK-sett blir hoppa over,
  lagring av venta utgåve, godkjende adresser og kompatibilitet (pakke, versjonskode, signatur, debug, minSdk).
- Lint (release): 0 feil, 12 åtvaringar (avhengigheitsversjonar låste til Spole-cachen, `UsableSpace`
  og `UseKtx` i oppdateringskoden – same mønster som Spole).

## Release på GitHub

- Release: <https://github.com/oyvhov/komet-android/releases/tag/v1.1.0>, publisert som «latest», ikkje prerelease.
- Kjelde: tag `v1.1.0`, commit `ab0e67318a7723073eb57e493d3c7ca808065f04`.
- Vedlegg: éin APK (`Komet-v1.1.0.apk`, 1 515 938 byte), `SHA256SUMS.txt`, `mapping-v1.1.0.txt` og
  `SOURCE_COMMIT.txt`.
- GitHub-API-et utan token (same kall som appen gjer): `draft=false`, APK-en har `state=uploaded` og
  `digest=sha256:b1657b150183a5d734d68b37e591a884129e26a1f84035d2c3371761e568ccd9`, lik den lokale hashen.
  `releases/latest` peikar på `v1.1.0`.
- CI «Bygg og test» (einingstestar, lint og debug-bygg på Ubuntu): grøn på `main` (køyring 34781154657, commit `ab0e673`).

## Oppdateringstest

**Ikkje køyrd.** Den ekte oppdateringa gjennom appen (eldre lokal utgåve → «Sjekk no» → nedlasting →
installasjon) vart hoppa over etter ønske frå eigaren. Val og kontroll av utgåve er dekt av
`ReleasePolicyTest`, men nedlasting, kontroll av hash og signatur og sjølve installasjonen er ikkje prøvde
på ei eining. Første ekte prøve blir når neste versjon kjem ut og 1.1.0 skal oppdatere seg.

Komet 1.0.0 har ikkje oppdateringsfunksjonen og må erstattast manuelt med 1.1.0 éin gong.
