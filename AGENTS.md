# Komet: lokal arbeidsrettleiing

Komet er ein læringsapp for barn (5–9 år) med matte, norsk, engelsk og verdsrommet, laga for Android med Kotlin og Jetpack Compose.
Les `docs/AI_INSTRUCTIONS.md` før arbeid i prosjektet. Innhaldslista med alle nivå ligg i
`docs/INNHALD.md`.

- Bygg og test: `powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1` (legg til `-Release` for signert APK).
- Emulator: `powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Start-KometEmulator.ps1`. Komet har sin eigen
  AVD (`Komet_Phone`, port 5580, `C:\LeseApp\.avd`). Rør aldri Spole sine emulatorar (5560, 5562, 5564).
- Signeringsnøkkelen ligg i `.signing/komet-release.jks` med passord i `signing.properties`. Begge er
  Git-ignorerte. **Lag aldri ein ny nøkkel** – då kan appen ikkje lenger oppdaterast på nettbrettet.
- Ny APK-release som appen kan oppdatere frå: følg `docs/RELEASE_WORKFLOW.md`. Same nøkkel, høgare
  versjonskode og éin universal APK i ein publisert GitHub Release på `oyvhov/komet-android`;
  verifiser digest og oppdateringsflyten.
- Alt barnet ser, skal finnast på både nynorsk og bokmål (`Txt(nn, nb)`).
- Nytt innhald skal ha einingstest som viser at rett svar faktisk er rett.
- Design: same retning som appen har (romtema, planetar, blanke 3D-knappar, `GameText`). Ikkje HUD/sci-fi-grensesnitt.
- Prioriteringar og status ligg i `ROADMAP.md`.
