# Verifisering · Komet 1.0.0

Dato: 13. september 2026

## Bygg

| | |
| --- | --- |
| Pakke | `app.komet` (debug: `app.komet.debug`) |
| Versjon | 1.0.0 (versionCode 1), minSdk 26, targetSdk 36 |
| APK | `dist/Komet-1.0.0.apk`, 1 482 670 byte |
| APK SHA-256 | `3732dec3fee83f82a15db8f4a669eb106320d3abb7e757ec8dd7e3f81d34216f` |
| Sertifikat SHA-256 | `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b` (CN=Komet) |
| R8 | På, med ressurskrymping. `dist/Komet-1.0.0-mapping.txt` er teken vare på. |
| Løyve | Ingen brukarløyve. Berre det interne `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` frå androidx. |

Kommando: `gradlew clean testDebugUnitTest assembleDebug assembleRelease lintRelease` – grønt.

## Testar

- Einingstestar: **40 av 40** i 6 klassar (`CurriculumTest`, `MathContentTest`, `ReadingContentTest`,
  `ProgressionTest`, `StateStoreTest`, `SynthTest`).
- Kvart av dei 76 nivåa lagar 600 oppgåver (150 × fire kombinasjonar av målform og bokstavtype), og
  testane reknar ut svaret sjølve: likningar, tiarstavar, pengar, klokke, vekt, rim, første/siste lyd,
  stavingar, ordbygging og sant/usant.
- Lint (release): 0 feil, 10 åtvaringar. Fem gjeld nyare avhengigheitsversjonar – versjonane er med
  vilje dei same som Spole brukar, så begge prosjekta deler éin Gradle-cache.

## Emulator

`Komet_Phone` (Pixel 7-profil, Android 16 / API 36, Google Play-image), port 5580, utan vindauge.

Kontrollert med skjermbilete:

- Oppsett: målform, namn, figur, klassesteg, bokstavar, «Skyt opp!»
- Heim, kart med stiar og låste nivå, resultat med tre stjerner, nytt romkort og ny rang
- Rundar: tel, tiarramme som hjelp, tastatur, avsløring etter to bom, klokke, pengar, vekt,
  tiarstavar, mønster, tekstoppgåve, bygg ord, ordne orda, første lyd, stavingar, rim,
  setning → bilete og lesetekst
- Romkort, rakettløp (nedteljing, tid, rakett mot månen), foreldrelås, foreldresida
- Bokmål med små bokstavar, skriftstorleik 2.0, nettbrett liggjande (2560 × 1600)
- Release-APK: frå tomt oppsett til første runde utan krasj
- Norsk stemme: talemotoren melde «Klar · nb-no-x-tfs-local»

## Ikkje testa

- Fysisk telefon eller nettbrett. Emulatoren køyrde utan lyd, så lydeffektar og opplesing er ikkje
  høyrde – berre at talemotoren fann norsk stemme.
- Emoji på Android 8–10 og på Samsung, som teiknar emoji annleis.
- Instrumenterte UI-testar finst ikkje endå. Flyten er sjekka manuelt med debug-kommandoane.

## Kjende avgrensingar

- Systemskrifta (Roboto) teiknar liten «l» nesten som stor «I». Ei skrift laga for lesing (t.d. Andika)
  ville vore betre for små bokstavar.
- Opplesing krev norsk stemme i talemotoren på eininga.
- Framgang blir ikkje synkronisert mellom einingar. Android-backup tek med fila.
