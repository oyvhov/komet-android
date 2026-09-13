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

## Oppdateringstest

Blir fylt ut når releasen er publisert, i ein eigen commit utan å flytte taggen.
