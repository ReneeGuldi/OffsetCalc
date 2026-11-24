# Offset Calc

An Android calculator for sheet-metal duct offsets. It's built from the shop's `offset_calc.xlsx` spreadsheet: same inputs, same formulas, same letter legend. It also draws each fitting to scale with every dimension marked.

**[⬇ Download the latest APK](../../releases/latest)**

---

## What it does

The app has four tabs, one for each calculator in the spreadsheet.

| Tab | You enter | You get |
|---|---|---|
| **Offset** | (A) Offset, (I) Angle, (B) ℄ rise multiplier, (H) ø or cheek | (C) **Cut piece**, (D) ℄ rise of elbow, (E) offset length makeups, (F) offset length ℄s, (G) ℄ to ℄ at angle |
| **Compound** | A (rise) and B (run) | C, the true offset, to use as (A) on the Offset tab |
| **O.W.L.** | Offset, Width, Length | Miter for square mitered offsets |
| **Resize** | W1, H1, W2 | H2, a new size with the same square inches |

- **Results update as you type**, shown as decimals and rounded to the nearest 1/16".
- **To-scale drawings** on the Offset and Compound tabs, styled like the spreadsheet's drawing. Every value has its own dimension line, colored like the legend:
  - 🟨 **Yellow:** what you enter (A, B, H, I)
  - 🟩 **Green:** what you cut or make up (C, E)
  - 🟥 **Red:** elbow centerline numbers (D, G)
  - 🟦 **Cyan:** F
- **Mixed elbows.** Turn on *"2nd elbow is a different angle"* for a 60 to a 30, a 45 to a 90 and so on. The cut piece uses each elbow's own ℄ rise.
- **Overlap warning.** If the two elbows would overlap and leave no room for a cut piece, the app says so instead of giving a negative length.

## Formulas

Every formula is ported 1:1 from the spreadsheet, and the unit tests check them against its saved results.

```
(G) ℄ to ℄ at angle      = A ÷ sin(I)
(D) ℄ rise of elbow      = H × B × tan(I ÷ 2)
(C) Cut piece            = G − D₁ − D₂
(F) Offset length ℄s     = √(G² − A²)
(E) Offset length makeup = F + D₁ + D₂

Compound offset          C  = √(A² + B²)
O.W.L. miter             Miter = Offset × Width ÷ Length
Same-area resize         H2 = (W1 × H1) ÷ W2
```

The ℄ rise of a 90° elbow at a 1.5 multiplier is 1.5 × diameter, matching the note in the sheet.

**Worked example (from the sheet):** a 14"ø duct with a 21" offset and two 45° elbows at a 1.5 multiplier.
G = 29.6985, D = 8.6985, **cut piece C = 12.3015 (12 5/16")**.

## Installing on an Android phone

1. On the phone, open this repository's **[Releases](../../releases)** page in Chrome.
2. Tap the newest **`OffsetCalc-vX.X.apk`** under **Assets** to download it, then open it.
3. If Android blocks the install, tap **Settings** and turn on **Allow from this source**, then go back.
4. Tap **Install**. If Google Play Protect warns that it doesn't recognize the app, tap **More details → Install anyway**. This is normal for apps that aren't from the Play Store.

A newer version installs over the old one.

## Building it yourself

**You'll need:** Android Studio (any recent version) and an Android phone or emulator running Android 7.0 or later.

1. Clone the repository and open the folder in Android Studio (**File → Open**).
2. Let Gradle sync, then press **Run ▶**.
3. To run the math tests, right-click `OffsetMathTest` and choose **Run**, or run:
   ```
   ./gradlew testDebugUnitTest
   ```

## Publishing a new version

GitHub builds and signs the APK automatically. The script is `.github/workflows/release.yml`.

1. Commit and push your changes.
2. On GitHub, go to **Releases → Draft a new release** and create a new tag one step higher than the last, e.g. `v1.1`.
3. Click **Publish release**. The tests run first, then the signed APK is attached to the release in about 5 minutes. You can watch progress in the **Actions** tab.

The version name comes from the tag (`v1.1` → "1.1"), and the version code goes up with each build, so phones accept it as an update.

**Signing:** the release key is stored in four repository secrets: `KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD`. The key file (`release.jks`) is never committed; keep a backup of it and its password somewhere safe. Without that same key, future versions can't install over the current one.

## Project layout

```
app/src/main/java/com/ductcalc/offset/
├── OffsetMath.kt        All the formulas (no Android code, easy to test)
├── MainActivity.kt      The four calculator tabs and results
├── DuctDiagram.kt       To-scale side view of the offset
├── CompoundDiagram.kt   To-scale compound offset triangle
├── DrawingKit.kt        Shared drawing style: colors, dimension lines, labels, legend
└── ui/Theme.kt          App colors

app/src/test/.../OffsetMathTest.kt   Checks every formula against the spreadsheet's results
.github/workflows/release.yml        Builds and publishes the APK on each release
```

Built with Kotlin and Jetpack Compose.
