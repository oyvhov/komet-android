# Bolt som følgjesven i oppgåvene

Dato: 8. oktober 2026. Lokal vidareutvikling av 2.1.0, pakkenamn `app.komet.debug`.
Ingen ny versjonskode, signert release eller publisering.

## Endringa

- Bolt er ein 64 dp hjelpeknapp på mobil og står ved astronauten med 80 dp trykkflate i det breie oppsettet.
- Han jublar etter rett svar, og viser eit roleg smil og ei hjelpande hand ved bom, hjelp og forklart svar.
- Eit trykk opnar hjelp med nynorsk/bokmål og opplesing gjennom den eksisterande talemotoren.
  Engelskmerking blir bevart for tale og fjerna frå teksten som blir vist.
- Hjelpa bruker berre oppgåveteksten før svaret er løyst eller forklart. Då kan også forklaringa lesast.
- Automatisk overgang til neste oppgåve ventar medan hjelpevinduet er ope. Lukking stoppar opplesinga.
- Same respons gjeld med avslått tale eller animasjon. Ny oppgåve nullstiller respons og «Prøv igjen».
- `KometDialog` kan få større lukkeknapp; Bolt-vindauget bruker 64 dp.
- Emulatorstarten behandlar resultatet frå `adb devices` som ei liste også når berre éin emulator finst.
  `-GpuMode host` kan velje PC-grafikk ved ny oppstart; standarden er framleis `auto`.

## Automatiske kontrollar

- `scripts/Build-Komet.ps1`: `testDebugUnitTest` og `assembleDebug` bestått.
- **100 testar**, ingen feil eller hoppa over. Fem nye testar kontrollerer fasitvern, begge målformer,
  rett svar etter bom, forklaring etter vist svar, synleg hjelp og nullstilling mellom oppgåver.
- Debug-APK: `app/build/outputs/apk/debug/app-debug.apk`.
- SHA-256: `833b3d061b1b39e2d06457a25e12f1372ab3c41d6a95e3227edae6795526e373`.
- `lintDebug` bestått også etter siste kodeendring: 0 feil og 15 åtvaringar;
  ingen åtvaring i filene som er endra for Bolt.
- GitHub CI er ikkje køyrd for dei lokale endringane.
- PowerShell-parseren godkjenner emulatorskriptet. Oppstart og gjenbruk med éin emulator er prøvde;
  PC-grafikk er prøvd med dei tilsvarande argumenta direkte til emulatoren.

## Skjermkontroll

På `Tunet_Ascii`, Android 16, med ein eigen lokal testprofil:

- Mobil ståande, 1080 × 2400 / 420 dpi: normal oppgåve, Bolt-vindauge, «Vis hjelpa», tiarramme,
  støtte etter eitt bom og jubel etter rett svar på same oppgåve.
- Rett svar på `m_add10`: opna Bolt med ein gong, venta minst tre sekund og kontrollerte at same
  løyste oppgåve og forklaring stod framme. Automatisk overgang vart dermed halden igjen.
- Mobil med skrift 2.0, bokmål og små bokstavar: `m_mission_collect`, to bom, markert rett svar,
  forklaring, «Send hjelpen!» og Bolt-vindauget. Tekst og knappar fekk plass.
- Nettbrett liggjande, 2560 × 1600 / 320 dpi, skrift 2.0, bokmål, avslått tale og animasjon:
  oppgåve med eitt bom, Bolt-vindauge, lukking og fullført runde. Resultatet viste 7 av 8 på første
  forsøk, tre stjerner, tre gullklumpar og eit nytt romkort. Tekst og knappar fekk plass.
  UI-hierarkiet stadfestar 80 dp Bolt-knapp og 64 dp lukkeknapp i hjelpa.
- Nettbrett ståande, 1600 × 2560 / 320 dpi, skrift 2.0 og nynorsk: `e_animals_listen`.
  Bolt viste «Finn pig.» utan teknisk engelskmerking, med lesbar tekst og synleg lukkeknapp.
- Mobil liggjande, 2400 × 1080 / 420 dpi, skrift 2.0 og nynorsk: `m_mission_cargo`.
  Oppgåva og opning av Bolt vart kontrollerte. Lang hjelpetekst krev rulling; lukkeknappen er synleg.
  Den siste rullekontrollen vart avbroten då ein annan app tok over den felles emulatoren, og er
  derfor ikkje rekna som bestått.
- Skjermbilete og lokale diagnostikkfiler ligg i `app/build/reports/bolt-2026-10-08/` (Git-ignorert).
  Nettbrettkontrollane og liggjande mobil bruker den endelege APK-en med 64 dp lukkeknapp.

## Avgrensingar i testmiljøet

Emulatoren gav ANR ved oppstart, også i Android sine eigne appar før Komet vart installert.
Den innsamla Komet-loggen viser at hovudtråden venta på `HardwareRenderer.setStopped`, medan
`RenderThread` venta i `libEGL_emulation` / `qemu_pipe_read` under oppretting av eit OpenGL-program.
Det var samtidig stor CPU- og I/O-venting. Dette peikar på emulatorgrafikken, men er ikkje ei
godkjenning av ytinga på fysiske einingar.

Etter omstart med `-gpu host` (NVIDIA GeForce RTX 3060) kunne dei siste funksjonskontrollane
gjennomførast utan Komet-ANR. Android System UI gav likevel ANR under oppstarten, så kaldstart og
yting er ikkje friskmelde. Den endelege lint-køyringa var ferdig før desse kontrollane.

Emulatoren hadde om lag 306 MB ledig og avviste ei oppdateringsinstallering. Android si
cache-opprydding frigjorde ikkje plass. Lågplassgrensa vart mellombels sett til 1 % for denne
13,5 MB test-APK-en, og den opphavlege innstillinga vart gjenoppretta straks etter vellykka installering.

Emulatoren vart ståande då eit anna prosjekt tok han i bruk. Ved siste lesekontroll køyrde ein annan
app med 1200 × 1920 / 240 dpi, skrift 2.0 og animasjonsskala 1. Desse delte innstillingane vart ikkje
overstyrte eller nullstilte, og emulatoren vart ikkje stoppa. Lågplassgrensa var tilbake på standarden.

Fysisk nettbrett, lytting til tale og musikk og ytingsmåling står att.
