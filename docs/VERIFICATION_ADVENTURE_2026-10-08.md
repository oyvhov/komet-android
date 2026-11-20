# Planetane vaknar – lokal verifikasjon

Dato: 8. oktober 2026. Lokal vidareutvikling av 2.1.0, `app.komet.debug`.
Ingen versjonsauke, signert release, push eller publisering.

## Det som er lagt til

- Fire innbyggjarar med tre oppdrag kvar: Pip og krystallfyret, Mira og signalbrua, Rio og
  stjernehagen, Vega og stjernekikkaren.
- Fem oppgåver per oppdrag, frå eksisterande læringsnivå. Valet er tilpassa klassesteg 0–3.
  Dei 128 læringsnivåa og fasitane deira er uendra.
- Ei eiga illustrert vising av kvar stad, med tre varige endringar, takk og avslutning.
- Inngang frå planetkortet og den første innbyggjaren. Andre små innbyggjarar hoppar som før.
- Barnet sender hjelpa sjølv etter kvart løyst eller forklart svar. Ingen tidspress eller straff.
- Historia tel berre rundar som er starta som oppdrag. Vanleg øving endrar ikkje historia.
- Lagring per spelar, også før første svar og etter siste svar før leveringa. Sistnemnde blir
  ferdigstilt ved gjenopptaking utan at svara blir talde ein gong til.
- Nynorsk og bokmål. Opplesing på trykk. Redusert rørsle viser ferdig framgang med ein gong.

## Automatiske kontrollar

- `scripts/Build-Komet.ps1`: bygging og alle **107 einingstestar** bestått, 0 feil / 0 hoppa over.
- Sju nye testar dekkjer alle tolv oppdrag, begge målformer, klassesteg, gyldige læringsnivå,
  rekkjefølgje, feil nivå, vanleg øving, fullføring med hjelp, dobbel fullføring, gjenopptaking,
  spelarskilje, gamle lagringar og ukjende verdiar.
- Endeleg APK: `app/build/outputs/apk/debug/app-debug.apk` (13,5 MB).
- SHA-256: `074c6bbf7d28b871d03c9e887f8cf005a30948a73a135a1bb02095e8bb5973f0`.
- Endeleg `lintDebug` bestått: 0 feil og 15 åtvaringar. Ingen åtvaringar i dei nye eventyrfilene
  eller i endringane for lagring, framgang, planetvising og resultat.
- GitHub CI er ikkje køyrd for dei lokale endringane.

## Funksjons- og skjermkontroll

Eigen `Komet_Adventure_Test` på port 5580, laga frå det allereie installerte Android 16-systembiletet.
Den delte emulatoren på port 5554 vart ikkje endra. Testprofilane har oppdikta namn og innhald.
Den eigne testemulatoren vart stoppa etter kontrollane; den delte emulatoren køyrer vidare.
Svar vart dels styrte gjennom dei eksisterande debug-kommandoane; navigasjon og oppdragsstart vart
prøvde med trykk. Dette er ikkje ein pedagogisk brukartest med barn.

- Mobil ståande, 1080 × 2400 / 420 dpi, nynorsk og små bokstavar: heile Pip-eventyret, frå
  tre mørke krystallar til tre lys og lysstråle. Ingen ny startknapp etter tredje oppdrag.
- Første oppdrag med to bom: fasit/forklaring vart vist, og oppdraget kunne fullførast med hjelp.
- Avbrot etter to lagra svar: lukka prosessen, opna appen og heldt fram. Etter fullføring hadde
  nivået nøyaktig fem registrerte svar og eitt fullført oppdrag. Søskenprofilen var urørt.
- Nettbrett liggjande, 2560 × 1600 / 320 dpi, skrift 2.0 og bokmål: innleiingane til Mira og Rio,
  og den første reparerte delen av brua. Lange detaljar kan rullast, og lukkeknappen står fast.
- Endeleg APK, avbrot etter femte svar i Rio sitt første oppdrag, før leveringa: lagringa viste
  fem av fem svar og ingen fullført historie. Etter at prosessen vart lukka og starta att, gav
  **Fortsett med hjelpen** resultatet med fire av fem rette, nøyaktig fem registrerte svar,
  éi speling og tre nye stjerner. Søskenprofilen var urørt. Første blomsten opna seg.
- Den nye vidareknappen på resultatet var synleg på nettbrett med skrift 2.0 og gjekk tilbake
  til Rio. Kontrollert på den endelege APK-en.
- Trykk på Vega i sjølve planetlandskapet opna rett eventyr. Nettbrett ståande,
  1600 × 2560 / 320 dpi, skrift 2.0: teleskop, innleiing, startknapp og kapittel fekk plass.
- Liggjande mobil, 2400 × 1080 / 420 dpi, skrift 2.0: rulla fram den lange teksten og startknappen.
  Eit trykk starta `space_moon` og lagra null av fem svar. Lukkeknappen heldt seg synleg (64 dp).
- Dei siste kontrollane, inkludert gjenopptakinga til Rio, vart gjorde etter at Wi-Fi og mobildata
  var slått av på den eigne testemulatoren.
- Skjermkontrollen avdekte at vidareknappen kom langt ned på resultatet. Han er flytta opp til
  takken, før dei ekstra løningane. Mira si antenne har fått plass innanfor rullefeltet.

Skjermbilete, testlagringar og emulatorloggar ligg i
`app/build/reports/adventure-2026-10-08/` (Git-ignorert). Nokre tidlege bilete er tekne før dei siste
justeringane av vidareknapp, antenne og innbyggjar på planeten.

## Avgrensingar

Testemulatoren køyrde utan lyd. Tale og musikk er ikkje lytta til. Kaldstart var tidvis treg under
emulatorkøyringa; yting på fysisk nettbrett og pedagogisk testing med barn står att.
Ingen testresultat her er ei godkjenning av ein publisert release.
