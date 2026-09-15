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

GitHub CI, offentleg digest, nedlasting og oppdateringsflyt blir førte inn i ein oppfølgjande
verifikasjonscommit etter publisering, utan å flytte releasetaggen.

## Avgrensingar

Fysisk nettbrett, faktisk tale-/lydavspeling og observasjon av barn som spelar er ikkje verifisert.
Klassesteg er eit foreslått startpunkt. Oppdragsbok og varige landskapsendringar står att.
