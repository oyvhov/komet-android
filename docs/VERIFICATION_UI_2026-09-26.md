# Lokal kontroll av Montessori og visuell pussing

Dato: 26. september 2026. Dette er ein lokal prøveversjon; ingen ny release er publisert.

## Endringar

- Montessori-verkstad med talstenger 1–5, teljepinnar 0–5 og talsporing 1–5.
- Sju musikktema, inkludert eit roleg Montessori-tema som blir spelt på lågare volum.
- Nye trykk-, opnings- og plasseringslydar; mjukare knappar, dialogar og skjermovergangar.
- Rakettløp med lysande spor og motorflamme. Nettbrett får illustrert løpsmeny med fire valkort
  og oppgåve og svar ved sida av kvarandre i liggjande format.
- Metalliske kort, lys og sveving; brei kortframvising med illustrasjon og lesetekst side om side.
- Montessori på liggjande nettbrett har tre illustrerte materialkort. Ståande nettbrett har
  større materialillustrasjonar. I begge format får arbeidsmatta og rettleiinga eigne plassar.

## Bygg og automatiske kontrollar

`scripts/Build-Komet.ps1` fullførte med 98 testar, 0 feil. Debug-APK blei bygd.

Nye innhaldstestar sjekkar talstengene og alle teljepinnane, inkludert den tomme nullboksen.
Ein test fangar opp lister som har identitetsbasert likskap: talstengene skal kontrollerast
element for element. Dette rettar eit tilfelle der ei rett trapp blei merkt som feil i skjermen.

Lydtestane sjekkar WAV-format, varigheit, signalnivå og mjuk avslutning for dei nye lydane.
Alle musikktema blir sjekka for klipping og overgangen når sløyfa byrjar på nytt.

APK: `app/build/outputs/apk/debug/app-debug.apk`.
SHA-256: `b59497a8144d3415d383d9415e42df295adf934629dcc7b2883b43ccd970663b`.

## Visuell kontroll

Berre Komet-emulatoren, `emulator-5580`, blei brukt.

| Oppsett | Kontroll |
| --- | --- |
| Mobil ståande | Rakettløp, svar og framgang; kortsamling og opne kort. |
| Mobil med skrift 2.0 og animasjonar av | Rakettløp og kort; bokmål og små bokstavar. |
| Nettbrett liggjande, 2560 × 1600, tettleik 320 | Montessori-hylle, stenger, løpsmeny, løp og brei kortframvising. Rett stengerekkjefølgje gav grøn stadfesting. |
| Nettbrett ståande, 1600 × 2560, tettleik 320 | Teljepinnar med eigne arbeids- og rettleiingsfelt; leggje til pinnar og kontroll av restmengda. |
| Nettbrett ståande med skrift 2.0 og animasjonar av | Store materialval og talsporingsflate, bokmål. Ord og knappar blir viste utan klipping. |

Representative skjermbilete ligg i `app/build/reports/ui/` etter denne lokale kontrollen.
Skjermstorleik, tettleik, skriftstorleik og animasjonsinnstillingar blir nullstilte etter kontrollen.

## Att før publisering

Lydane er kontrollerte som signal, men lytting, talebalanse, berøring og yting på det fysiske
nettbrettet står att. Skulepiloten treng observasjonar frå barn og lærarar, som skildra i
`MONTESSORI_VERKSTAD.md`. GitHub CI og releaseflyten er ikkje køyrde for desse lokale endringane.
