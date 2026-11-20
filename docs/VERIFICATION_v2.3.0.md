# Verifikasjon av Komet 2.3.0

20. november 2026. Android-pakken er `app.komet`, versjonskoden er 9, og minste Android API er 26. Førre stabile utgåve er 2.2.0 med kode 8.

- 121 einingstestar bestod i releasekontrollen. Signert produksjonsbygg med R8, `lintDebug` og `lintRelease` fullførte.
- På den same spelkoden bestod i tillegg elleve lokale Compose-skjermtestar. Dei omfatta nettbrett i begge retningar, mobil med dobbel skriftstorleik, kaioppdrag og Stjerneposten.
- Oppdateringsprøva tek utgangspunkt i den offentlege APK-en for 2.2.0 med SHA-256 `12781b5f6e3dcf6dfa24aca2a2fa6055161dd86032b1712580b8bf442f476f8c`.
