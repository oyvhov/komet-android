# AI-instruksar for Komet

Arbeidsmanual for ein AI-agent som skal vidareutvikle, teste og byggje Komet. Oppsettet følgjer
same mønster som Spole (`C:\JellyBin\reelstack-android`), men appen er eit eige prosjekt med eigen
pakke, nøkkel og emulator.

## 1. Produktet og faste føringar

Komet er ein app der barn øver på matte, norsk, engelsk og verdsrommet. Temaet er verdsrommet:
planetar er kapittel, stjerner er framgang, og romkort er premien.

- **Som eit kult spel, ikkje ein barnsleg app – og ikkje HUD.** Mørkt romtema, planetar og blanke 3D-knappar
  (`PressSurface` + `gloss`), djupe fargar per fag (`Tones`), kvit spelskrift med mørk kontur (`GameText`).
  Ingen flate pastellflater, ingen sci-fi-grensesnitt med kanta panel. Ingen maskot som snakkar.
- **Lett å bruke utan å kunne lese.** Oppgåvene blir lesne høgt (norsk talemotor), knappane er minst
  64 dp og nesten alle svar er eitt trykk.
- **Aldri straff.** Feil svar gir «Prøv igjen». Etter to bom blir rett svar vist med ei forklaring.
  Ingen liv, ingen nedteljing i vanlege rundar.
- **Personvern.** Ingen reklame, ingen kjøp og ingen sporing. Einaste nettkontakt er
  oppdateringssjekken mot GitHub (`update/`), som kan slåast av. Framgang ligg berre i
  `files/komet.json` på eininga. Legg aldri til analyse, reklame-SDK eller andre nettkall.
- **Oppdateringar** følgjer [RELEASE_WORKFLOW.md](RELEASE_WORKFLOW.md). Komet vel berre éin signert
  APK frå `oyvhov/komet-android`, og installerer aldri utan at ein vaksen vel det.
- **Målform per profil.** All tekst barnet ser, finst som `Txt(nn, nb)`. Lesestoff (ord, setningar)
  følgjer profilen sitt val av STORE eller små bokstavar; setningar held vanleg skrivemåte i
  små-modus (namn og ny setning får stor forbokstav).
- **Foreldresida** ligg bak eit gongestykke (6–9 × 6–9) som eit barn på 6 år ikkje løyser.
- **Stor skrift:** test på skriftstorleik 2.0. Ingen tekst skal klippast, og ingen knapp skal bryte
  eit ord over fleire linjer.

## 2. Kodestruktur

| Mappe | Innhald |
| --- | --- |
| `domain/` | Rein Kotlin utan Android: oppgåvemodell, generatorar per fag (`MathSkills`, `ReadingSkills`, `EnglishSkills`, `SpaceSkills`), ordbank, oppgåvetypar (`Topics`), strekdata for skriving (`Strokes`), framgang, romkort. |
| `data/` | `StateStore` – éi JSON-fil, atomisk skriving, knekt fil blir lagt til side. |
| `audio/` | `Speaker` (norsk TTS, og engelsk for tekst merkt `{en:…}`), `Synth` (lydeffektar laga i kode) og `SoundFx`. |
| `ui/` | ViewModel, navigasjon (`Screen`), tekstar (`Strings.kt`), tema, komponentar og skjermar. |

Dataflyt: `Skill.generate(QuestionContext)` → `Curriculum.round()` → `RoundState` i
`KometViewModel` → `PlayScreen`. Når runden er ferdig: `Progression.applyRound()` → `StateStore`.

### Leggje til eit nytt nivå

1. Skriv generatoren i fagfila (`MathSkills.kt`, `ReadingSkills.kt`, `EnglishSkills.kt`, `SpaceSkills.kt`).
2. Plasser han i rett kapittel. Klassesteget (`grade`) må aldri gå ned innanfor eit kapittel.
3. Bruk berre visuelle typar som finst i `Visual`, eller legg til ein ny og teikn han i `TaskVisuals.kt`.
4. Gi nivået ein oppgåvetype i `Topics.kt` (`TopicsTest` feilar elles), så det finst i Utforsk.
5. Utvid innhaldstesten for faget slik at testen reknar ut svaret sjølv.
6. Oppdater `docs/INNHALD.md`.

## 3. Bygging

Brukarmappa har «Ø» i namnet. Android-verktøya tolar ikkje det i TEMP, Gradle-heimen eller
emulatorstien. Difor:

- `TEMP`/`TMP` = `C:\LeseApp\.gradle-tmp`
- Gradle-heim = `C:\JellyBin\.gradle-home` (delt cache med Spole, same versjonar)
- SDK til emulator og adb = `C:\Android\sdk` (koplingspunkt til den ekte SDK-en)

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1            # testar + debug-APK
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1 -Release   # testar + signert release
```

- Debug: `app.komet.debug`, `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app.komet`, `app/build/outputs/apk/release/app-release.apk` (R8 og ressurskrymping)
- Sertifikat SHA-256: `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b`
- Ny versjon: auk `versionCode` og `versionName` i `app/build.gradle.kts`.

## 4. Emulator og visuell kontroll

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Start-KometEmulator.ps1 -Headless
```

Debug-bygget tek imot kommandoar som hoppar rett til ein skjerm eller eit nivå. Dei finst ikkje i
release-bygget.

```bash
adb -s emulator-5580 shell am start -f 0x20000000 -n app.komet.debug/app.komet.MainActivity --es skill m_clock_half
```

| Ekstra | Verknad |
| --- | --- |
| `--es skill <id>` | Startar nivået (id-ar i `docs/INNHALD.md`) |
| `--es screen home\|math\|reading\|cards\|race\|parent` | Opnar skjermen (foreldresida utan lås) |
| `--ei solve <n>` | Svarar rett på dei neste n oppgåvene |
| `--ei miss <n>` | Bommar n gonger på gjeldande oppgåve |
| `--ei stars <n>` | Set stjerner (for romkort og rang) |
| `--ez unlockAll true` | Opnar alle nivå |
| `--es maalform nn\|nb`, `--es case upper\|lower` | Byter målform og bokstavar |
| `--es speech on\|off` | Slår opplesing av/på |

Skjermbilete: `adb -s emulator-5580 exec-out screencap -p > bilete.png`. Første oppstart av
debug-bygget på emulator utan GPU tek 10–15 sekund; vent før du tek bilete.

Kontroller ved endringar i UI:

- telefon stående, nettbrett liggjande (`adb shell wm size 2560x1600` + `wm density 320`, nullstill etterpå)
- skriftstorleik 2.0 (`adb shell settings put system font_scale 2.0`, set tilbake til 1.0)
- bokmål + små bokstavar
- rett svar, feil svar, to feil (vis svaret), hjelp-pæra og resultatskjermen

## 5. Ferdig-kriterium

- `testDebugUnitTest` er grøn (sjå talet i den nyaste `docs/VERIFICATION_v*.md`)
- CI-arbeidsflyten «Bygg og test» på GitHub er grøn
- nytt innhald har test som reknar ut svaret sjølv
- nynorsk og bokmål er på plass
- skjermane er sjekka på emulator, også med stor skrift
- ingen persondata, passord eller signeringsfiler i Git
