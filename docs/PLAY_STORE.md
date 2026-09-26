# Play Store-publisering

Komet kan publiserast på Google Play, men Play-bygget må ikkje bruke den private GitHub-oppdateraren.
Google Play krev at appar som er distribuerte via Play blir oppdaterte via Play, ikkje ved å laste ned
og installere APK-ar sjølve.

## Bygg Play Store-pakke

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1 -PlayStore
```

Dette køyrer einingstestane og lagar:

```text
app/build/outputs/bundle/release/app-release.aab
```

Play Store-bygget set `BuildConfig.PLAY_STORE=true`, gøymer Appoppdateringar på foreldresida og
bruker ikkje Android-løyva `INTERNET` eller `REQUEST_INSTALL_PACKAGES`. Den vanlege private
APK-releasen blir framleis bygd med:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Build-Komet.ps1 -Release
```

## Før innsending

- Appnamn: Komet.
- Pakkenamn: `app.komet`.
- Versjon: auk `versionCode` for kvar Play-opplasting i `app/build.gradle.kts`.
- Mål-API: `targetSdk = 36`.
- Innhald: læringsspel for barn, utan reklame, utan kjøp i appen og utan sporing.
- Personvern: framgang blir lagra lokalt i `files/komet.json`; Play Store-varianten gjer ingen nettverkssjekk.
- Målgruppe: barn 5-9 år. Gå gjennom Families Policy og fyll ut målgruppe, innhaldsvurdering,
  personvernpolicy og Data safety i Play Console.

## Play Console

1. Opprett ein Google Play Developer-konto og ny app i Play Console.
2. Last opp `app-release.aab` i intern test først.
3. Fyll ut Store listing med ikon, skjermbilete, kort og lang skildring.
4. Fyll ut App content: Data safety, Target audience and content, Ads, App access, Content rating og Privacy policy.
5. Køyr intern test på fysisk nettbrett før produksjon.
6. Rull ut til produksjon når intern test er godkjend.

Offisielle sider som er særleg relevante:

- https://support.google.com/googleplay/android-developer/answer/11926878
- https://support.google.com/googleplay/android-developer/answer/12085295
- https://support.google.com/googleplay/android-developer/answer/9893335
- https://support.google.com/googleplay/android-developer/answer/10787469
