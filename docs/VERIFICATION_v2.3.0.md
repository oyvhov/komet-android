# Verifikasjon av Komet 2.3.0

20. november 2026. Android-pakken er `app.komet`, versjonskoden er 9, og minste Android API er 26. Førre stabile utgåve er 2.2.0 med kode 8.

- 121 einingstestar bestod i releasekontrollen. Signert produksjonsbygg med R8, `lintDebug` og `lintRelease` fullførte.
- På den same spelkoden bestod i tillegg elleve lokale Compose-skjermtestar. Dei omfatta nettbrett i begge retningar, mobil med dobbel skriftstorleik, kaioppdrag og Stjerneposten.
- Oppdateringsprøva tek utgangspunkt i den offentlege APK-en for 2.2.0 med SHA-256 `12781b5f6e3dcf6dfa24aca2a2fa6055161dd86032b1712580b8bf442f476f8c`.

## Publisert utgåve

- [GitHub Release v2.3.0](https://github.com/oyvhov/komet-android/releases/tag/v2.3.0) er den nyaste stabile utgåva. Den offentlege lista inneheld éin universal APK på 2 567 418 byte med SHA-256 `520ab82c0e63e2e6ebe794daa9b94cc08833ad2005c7808862125b1dbe56dedd`.
- Direkte nedlasting og nedlasting frå API-endepunktet som appen brukar, gav identiske byte. Oppdateringsreglane godtok APK-en med same applikasjons-ID og sertifikat som 2.2.0 og med høgare versjonskode.
- GitHub-kontrollane for [hovudgreina](https://github.com/oyvhov/komet-android/actions/runs/44686263354) og [releasetaggen](https://github.com/oyvhov/komet-android/actions/runs/44686263332) bestod.

## Oppdatering gjennom foreldremenyen

På ein separat Android 36-emulator hadde den offentlege 2.2.0-utgåva prøveprofilen Atlas på 2. trinn, med seks stjerner, tre nivåstjerner og eit favorittnivå. Frå foreldremenyen fann **Sjekk no** v2.3.0. **Last ned oppdatering** henta og kontrollerte APK-en. Etter løyve til installasjon frå Komet opna **Installer oppdatering** Android sin installasjonsdialog. Android oppdaterte produksjonspakken frå kode 8 til 9.

Etter ny oppstart viste heimeskjermen Atlas og seks stjerner. Profil-ID, klassesteg, stjerner, nivåresultat og favoritt samsvara med prøveprofilen frå 2.2.0. Eit tidlegare forsøk på ein nesten full emulator stansa ved nedlasting med lagringsmelding.
