# Lokal verifikasjon: romoppdrag, 15. september 2026

## Endringar
- Ti nye genererte mattenivå i eige kapittel og eigen kategori i Utforsk.
- Ny leveringsscene med stjerner, måne, romskip, flygeboge, lyshale og landingsglitter.
- Manuell utsending etter svar, med forklaring og lyd. Ingen automatisk framdrift i romoppdraga.
- Eksisterande framgang og lagring brukast; ingen datamigrering.

## Kontroller
- Build-Komet.ps1: grønt, 92 testar, ingen feil.
- Ny innhaldstest reknar ut svaret frå teksten barnet ser: 500 oppgåver per nivå per målform,
  totalt 10 000. Sjekkar òg variasjon i 20 rundar per nivå.
- Komet-emulator 5580: telefon ståande, nettbrett 2560 × 1600 / 320 dpi, skrift 2.0.
- Nynorsk og bokmål, små bokstavar; hjelp, rett svar, to feil med forklaring og utsending.
- Verifisert at ei utsending går vidare til neste oppgåve og tenner eitt baselys.
- Redusert rørsle: animator_duration_scale=0, appen starta på nytt; utsending går vidare og
  tilgjengelegheitsteksten viser «Rombasen har fått 1 av 8 leveranser».
- Skjermbilete og UI-kontroll ligg lokalt under app/build/reports/mission-check/.

## Leveranse
Debug-APK: app/build/outputs/apk/debug/app-debug.apk
SHA-256: ae478053db2fdca13e017c92044667e460fccaa391a86997fad097090eb0aad5

Ingen publisering eller push. GitHub CI, fysisk nettbrett og faktisk tale-/lydavspeling er ikkje
verifiserte i denne økta. Oppdragsbok og varige landskapsendringar er framleis framtidig arbeid.
