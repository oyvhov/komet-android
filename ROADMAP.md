# Komet — vegkart

Oppdatert 14. september 2026. Publisert: **1.2.0** (versjonskode 3).

Retning frå eigaren: appen skal sjå ut som eit skikkeleg kult spel og ikkje som ein barnsleg, fargerik
barneapp. Han skal passe for litt større barn. Barnet skal kunne finne oppgåvetypane han likar, uavhengig
av trinn. Alt skal lagrast etter kvart, og appen kan få fleire modular enn norsk og matte.

Rekkjefølgja under er prioritert. Kryss av når eit punkt er ferdig og testa.

## 1.2.0

- [x] **Barneskrift.** Andika (SIL, OFL) for all lesetekst, med «a» og «g» slik skulen lærer dei.
- [x] **Strekdata for skriving.** Stavskrift-rekkjefølgje for A–Å, a–å og 0–9, med testar og kontrollark.
- [x] **Design som eit kult spel, i same retning som før (IKKJE HUD/sci-fi-grensesnitt).** *Godkjent av eigaren.* Behald romtema,
      planetar, rakett, runde former og 3D-knappar. Byt flate pastellfargar med djupe, metta gradientar
      med lys og skugge, feit kvit tekst med mørk kontur, glans på knappar og stjerner og meir liv i
      løningar (stjerner, romkort). Vis heimeskjermen til eigaren før resten blir endra.
- [x] **Lagring etter kvart svar.** Kvart svar blir lagra med ein gong, og ein avbroten runde kan haldast fram.
- [x] **Utforsk og favorittar.** Bla i oppgåvetypar (pluss, minus, klokka, rim …) på tvers av trinn og
      merk nivå som favorittar.
- [x] **Skriv med kometen.** Spor bokstavar og tal med fingeren: nytt kapittel i lesing og tal i matte.
- [x] **Engelsk.** Ord og fraser med engelsk opplesing: fargar, tal, dyr, mat, kropp, helsingar.
- [x] **Verdsrommet.** Kunnskapsmodul om sola, planetane, månen, romfart og stjernene.
- [x] **Repetisjon.** Ein runde med det barnet har bomma mest på.
- [x] Testar, lint, signert APK, dokumentasjon og verifikasjon. Publisert 14. september 2026 etter ja frå eigaren.

## Milepæl 2.0 · Komet-eventyret

**Mål:** Komet blir eit eventyrspel i same ånd som Mummidalen og Albert Junior. Barnet reiser gjennom
eit levande, illustrert verdsrom saman med ein helt og ein ven, hjelper innbyggjarane på planetane og
lærer undervegs. Alt fyller skjermen på både mobil og nettbrett, med grafikk, animasjon, musikk og lydar.

**Prinsipp**

- Barnet utforskar ei verd, ikkje menyar.
- Kvar oppgåve har ein grunn i historia («Roboten treng fem skruar for å fikse raketten»).
- Heile skjermen blir brukt: scener frå kant til kant i ståande og liggjande format, laga for nettbrett
  og skalerte ned til mobil – ingen smale kolonnar med tomrom på sidene.
- Vakkert og roleg: lag på lag med parallakse, mjukt lys, partiklar og musikk som passar kvar stad.
- Framleis same læringsinnhald, nynorsk og bokmål, ingen reklame, ingen kjøp og fungerer utan nett.

**Fasar**

- [x] **1. Fullskjerm og scenemotor.** Scener i lag med parallakse, animasjon og overgangar. Felles
      oppsett for mobil og nettbrett i ståande og liggjande format.
      *Gjort:* `ui/scene/Scene.kt` – kamera i scene-einingar, drag og kast, parallakse, tolerer stor
      skrift og avslåtte animasjonar.
- [ ] **2. Helten og venen.** Hovudfigur og ein følgjesven som guidar, snakkar (opplesing) og reagerer
      på svar, med animasjonar for kvile, gå, hoppe og juble.
      *Gjort:* astronauten (kvile, gå, vinke, juble) med astronautbyggjar i oppstarten og roboten Bolt
      som les nivånamn høgt. *Att:* Bolt reagerer på svar (kjem med fase 7).
- [x] **3. Stjernekartet.** Eit stort, levande solsystem å dra rundt i. Raketten flyg mellom planetane,
      og låste område ligg i tåke.
      *Gjort:* sola, fire fag-planetar, rakettbana, romkort og Utforsk; raketten flyg med hale. Ingen
      område er låste i dag (første nivå i kvart kapittel er alltid ope), så tåka ventar til historia.
- [ ] **4. Planetane som stader.** Kvar fag-planet får ein illustrert overflate med landemerke,
      innbyggjarar med oppdrag og nivå som ting i verda (steinar, dører, skilt).
      *Gjort:* fire landskap (Talplaneten, Bokstavplaneten, Engelskplaneten, Rombasen) med sti, skilt for
      kapittel, øvingsverkstad, landingsplass, mål og innbyggjarar som hoppar. *Att:* oppdrag frå
      innbyggjarane.
- [ ] **5. Historia.** Hovudforteljing i kapittel, oppdragsbok, korte animerte mellomscener med
      opplesing, og løningar som endrar verda.
- [x] **6. Musikk og lydar.** Eigen musikk for kvart område, lydar for det barnet trykkjer på, stemmer til
      figurane og innstilling for musikk av/på.
      *Gjort:* seks melodiar laga i kode, sus, steg, boing, pip og glitter, demping under opplesing og
      brytar på foreldresida.
- [ ] **7. Oppgåvene inne i eventyret.** Oppgåveskjermen blir ei scene med figurane til stades og animert
      respons; på nettbrett står scena og oppgåva side om side.
- [ ] **8. Heimebasen.** Ein eigen base å pynte med ting barnet vinn.
- [ ] **9. Pussing og test.** Yting på eldre nettbrett, test på fysisk nettbrett, release 2.0.

Første leveranse er ein heil smakebit: stjernekartet i fullskjerm med rakett, éin planet ferdig
illustrert med figurar og oppdrag, og musikk – på både mobil og nettbrett.

## Seinare

- Rakettverkstad: stjerner låser opp delar og fargar til raketten.
- Meir matte: tallinje, måling, vekedagar og månader, tal til 1000.
- Dagleg skjermtidgrense styrt av foreldra.
- Test på fysisk nettbrett: lyd, tale, emoji og skriving med finger.

## Faste reglar

- Ingen reklame, ingen kjøp og inga sporing. Einaste nettkontakt er oppdateringssjekken.
- Ny GitHub-release, push og nedlasting av filer skjer berre etter tydeleg ja frå eigaren.
- Lag aldri ny signeringsnøkkel. Ingen persondata i repoet.
