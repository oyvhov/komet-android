# Release-flyt for APK og oppdatering i Komet

Same kontrakt som Spole. Publiser berre når brukaren ber om ein ny release.

## Kontrakten appen forventar

- Offentleg repo: `oyvhov/komet-android`. Appen les
  `https://api.github.com/repos/oyvhov/komet-android/releases?per_page=100` utan token.
- Produksjonspakke: `app.komet`, aldri `.debug`.
- `versionName` og tag må samsvare: `1.2.0` og `v1.2.0`. Testutgåver: `1.2.0-beta1` og `v1.2.0-beta1`.
- `versionCode` må vere høgare enn alle APK-ar som er delte, også lokale filer. 1.0.0 = 1, 1.1.0 = 2, 1.2.0 = 3, 2.0.0-beta1 = 4, 2.0.0-beta2 = 5.
- Same Komet-signatur. Sertifikat SHA-256:
  `e914f8b6b01f9b7105104b1f1c0dde9f554043fef81a44444dd963ee6859195b`.
- Releasen må vere publisert (ikkje kladd) og ha **nøyaktig éin APK**. Legg òg ved `SHA256SUMS.txt`,
  R8-mappinga og `SOURCE_COMMIT.txt` – dei er ikkje APK-ar og påverkar ikkje utvalet.
- GitHub må oppgi `digest: sha256:…` for APK-en. Appen kontrollerer storleik, hash, pakkenamn,
  minste Android-versjon, versjon og signatur før Android får spørsmål om installasjon.
- Testutgåver skal merkast som prerelease og ikkje som «latest».

Automatisk sjekk skjer når appen kjem fram, høgst éin gong per tolv timar. «Sjekk no» på foreldresida
går utanom ventetida. Ingenting blir lasta ned eller installert utan at ein vaksen vel det, og Android
godkjenner sjølve installasjonen.

## 1. Frys kjelda

1. `git status` og `git diff`. Ta vare på alt arbeid; ingen reset eller clean for å få eit reint tre.
2. Vel ein ubrukt, høgare versjon og versjonskode i `app/build.gradle.kts`.
3. Oppdater `CHANGELOG.md`, `docs/release-vX.Y.Z.md` og `docs/VERIFICATION_vX.Y.Z.md`.
4. Ingen signeringsfiler, passord, emulatordata eller skjermbilete med ekte persondata i Git eller release.

## 2. Bygg og test

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1 -Release
```

eller manuelt:

```powershell
$env:TEMP='C:\LeseApp\.gradle-tmp'; $env:TMP=$env:TEMP
./gradlew.bat --gradle-user-home C:\JellyBin\.gradle-home testDebugUnitTest assembleDebug assembleRelease lintRelease --console=plain
if ($LASTEXITCODE -ne 0) { throw 'Bygget feila; ikkje publiser.' }
```

Vent på BUILD SUCCESSFUL. Kontroller skjermane på `Komet_Phone` (sjå `docs/AI_INSTRUCTIONS.md`).

## 3. Kontroller og arkiver artefaktane

```powershell
$version='2.0.0-beta2' # endre ved neste release
$tag="v$version"
$out="dist/release-$tag"
New-Item -ItemType Directory -Path $out -Force | Out-Null
$apk="$out/Komet-$tag.apk"
$mapping="$out/mapping-$tag.txt"
Copy-Item app/build/outputs/apk/release/app-release.apk $apk
Copy-Item app/build/outputs/mapping/release/mapping.txt $mapping
$bt='C:\Android\sdk\build-tools\36.1.0'
& "$bt\apksigner.bat" verify --print-certs $apk
& "$bt\aapt2.exe" dump badging $apk
$hash=(Get-FileHash $apk -Algorithm SHA256).Hash.ToLower()
"$hash  $(Split-Path $apk -Leaf)" | Set-Content "$out/SHA256SUMS.txt" -Encoding ascii
```

Kontroller sertifikatet over, pakken `app.komet`, versjon og versjonskode, og at
`application-debuggable` ikkje finst. `dist/` er Git-ignorert.

## 4. Commit, tag og kladd

```powershell
git diff --check
git add <gjennomgåtte filer>
git commit -m "Release Komet $version"
$commit=git rev-parse HEAD
$commit | Set-Content "$out/SOURCE_COMMIT.txt" -Encoding ascii
git tag -a $tag -m "Komet $version"
git push --atomic origin main $tag
gh release create $tag $apk "$out/SHA256SUMS.txt" $mapping "$out/SOURCE_COMMIT.txt" --repo oyvhov/komet-android --verify-tag --draft --latest --title "Komet $version" --notes-file "docs/release-$tag.md"
```

Testutgåve: bruk `--prerelease --latest=false`. Flytt aldri ein publisert tag og byt aldri APK bak same
versjon – lag ny versjon ved feil.

Kontroller kladden: `gh release view $tag --repo oyvhov/komet-android --json isDraft,assets` – éin APK,
`state=uploaded`, rett storleik og `digest` lik `sha256:$hash`.

## 5. Publiser og prøv den ekte oppdateringa

```powershell
gh release edit $tag --repo oyvhov/komet-android --draft=false --latest
```

1. Hent release-lista **utan token** og stadfest utgåva, `draft=false` og rett digest.
2. Last ned `browser_download_url` til ei eiga fil og samanlikn SHA-256 med den bygde APK-en.
3. Byggje ei eldre lokal utgåve av same kjelde som den nye releasen skal erstatte, og installer
   henne på `Komet_Phone`:

   ```powershell
   ./gradlew.bat --gradle-user-home C:\JellyBin\.gradle-home assembleRelease "-PkometVersionCode=1" "-PkometVersionName=1.0.1"
   ```

   Hermeteikna trengst: PowerShell deler elles `1.0.1` ved punktumet.

   Denne APK-en skal aldri delast. Bygg den ekte releasen på nytt etterpå dersom `app/build` vart overskriven.
4. Opne Foreldre → Innstillingar → Appoppdateringar → **Sjekk no**. Stadfest versjonen, vel
   **Last ned oppdatering**, vent på kontrollen og vel **Installer oppdatering**. Gi løyve til å
   installere frå Komet dersom Android spør, gå tilbake og vel Installer igjen.
5. Opne appen og stadfest ny versjon og at profilar og stjerner er bevarte.

Ein `adb install -r`-test er ikkje ein test av oppdateringa gjennom appen.

## 6. Sluttrapport

Direkte APK-lenkje, release-lenkje, versjon og versjonskode, commit, endringsliste, testresultat og
SHA-256. Oppdater verifikasjonsrapporten med resultatet frå oppdateringstesten i ein eigen commit
utan å flytte taggen.
