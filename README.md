# Offset Calc

An Android app (Kotlin + Jetpack Compose) for sheet-metal duct offset calculations. It's a port of a shop spreadsheet (`offset_calc.xlsx`) with to-scale, dimensioned drawings of each fitting.

Prebuilt, signed APKs are on the **[Releases](../../releases)** page.

---

## Overview

- Single-activity Compose app: four calculator tabs (Offset, Compound, O.W.L., Resize).
- No network access, no permissions, no third-party dependencies beyond AndroidX/Compose.
- All math lives in one pure-Kotlin object (`OffsetMath`) with no Android imports, so it's trivial to unit test or reuse.
- Drawings are hand-rolled on a Compose `Canvas` from the computed geometry. No image assets.

| | |
|---|---|
| Language | Kotlin 2.1.21 |
| UI | Jetpack Compose (BOM 2025.05.00), Material 3 |
| Build | Gradle 8.14.3 (wrapper), Android Gradle Plugin 8.10.1, JDK 17 |
| SDK | minSdk 24 (Android 7.0), compile/targetSdk 35 |

## Getting started

```bash
git clone <this repo>
cd OffsetCalc
```

Open the folder in Android Studio and press **Run ▶**, or use the command line:

```bash
./gradlew assembleDebug        # build app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install on a connected device/emulator
./gradlew testDebugUnitTest    # run the math tests
```

## Project layout

```
app/src/main/java/com/ductcalc/offset/
├── OffsetMath.kt        Pure formulas + OffsetResult data class (no Android code)
├── MainActivity.kt      Scaffold, tabs, input fields, result rows, notes
├── DuctDiagram.kt       To-scale side view of the 2-elbow offset
├── CompoundDiagram.kt   To-scale compound-offset triangle
├── DrawingKit.kt        Shared drawing primitives: palette, dimension lines,
│                        arrowheads, rotated labels, legend
└── ui/Theme.kt          Material color schemes (light/dark)

app/src/test/java/com/ductcalc/offset/OffsetMathTest.kt
.github/workflows/release.yml
```

## The math

Every formula is a 1:1 port of the spreadsheet (Sheet1). Angles are in degrees at the API boundary.

| Value | Formula | Spreadsheet cell |
|---|---|---|
| (G) ℄ to ℄ at angle | `A / sin(I)` | `D21 = D16/F18` |
| (D) ℄ rise of elbow | `H · B · tan(I/2)` | `I21 = PRODUCT(J16,H16,TAN(F19))` |
| (C) Cut piece | `G − D₁ − D₂` | `F26 = D21-(I21*2)` |
| (F) Offset length ℄s | `√(G² − A²)` | `B26 = SQRT(D20-D18)` |
| (E) Offset length makeups | `F + D₁ + D₂` | `J26 = B26+I21+I21` |
| Compound offset | `√(A² + B²)` | `K6 = SQRT(K5)` |
| O.W.L. miter | `Offset · Width / Length` | `F37` |
| Same-area resize | `(W1 · H1) / W2` | `K37` |

**Inputs:** (A) offset, (I) elbow angle, (B) ℄ rise multiplier, (H) diameter or cheek.

**Mixed elbows:** `offsetCutPiece(..., elbow2Deg)` adds a separate second-elbow angle, an extension of the sheet's written note. Elbow 1 sets the travel angle of the cut piece; each elbow subtracts its own ℄ rise. With the default `elbow2Deg = angleDeg` the results match the sheet exactly.

**Geometry note:** B × H is the elbow's centerline bend radius *R*, and D = R · tan(θ/2) is the tangent length. The drawings rely on this: each elbow is drawn as an arc of radius *R* that meets the straight sections exactly at the D points.

## Drawings

Both diagrams follow the same pipeline:

1. Build the geometry in **world units (inches)**. P1/P2 are the run-centerline intersections; T-points are the arc tangent points.
2. Fit to the canvas with a uniform scale. In the side view, screen y grows downward, so the offset drops, matching the spreadsheet's drawing.
3. Draw the duct as a thick stroked centerline path (outline pass, then fill pass), then seams, construction lines, and dimensions.

`DrawingKit.dimension()` is a reusable drafting-style dimension: extension lines, arrowhead or slash ends, and a label rotated along the line that never renders upside-down. If the label doesn't fit between the ends, it moves outside the line.

The colors follow the spreadsheet's legend and are shared with the result rows (the color tags):

| Color | Meaning | Letters |
|---|---|---|
| Yellow | Inputs | A, B, H, I |
| Green | Cut / makeup lengths | C, E |
| Red | Elbow centerline values | D, G |
| Cyan | ℄ horizontal length | F |

The side-view canvas height comes from the geometry's aspect ratio (clamped to 260–480 dp). The compound triangle stretches any side shorter than ⅓ of the long side and labels the drawing "not to scale". The dimension values stay exact.

## Tests

`OffsetMathTest` checks every formula against the **cached results stored in the spreadsheet**, plus the sheet's worked example (14"ø, 21" offset, 2 × 45° → cut piece 12.3015) and mixed-elbow cases. A 90° elbow at a 1.5 multiplier must give a ℄ rise of 1.5 × ø.

If you change anything in `OffsetMath`, add or update a test. CI runs the tests before every release build, so a failing test blocks publishing.

## CI / releases

`.github/workflows/release.yml` runs on **published GitHub Releases** (and can be run by hand from the Actions tab):

1. Sets up JDK 17 and Gradle.
2. Decodes the signing keystore from secrets.
3. Runs `testDebugUnitTest assembleRelease`.
4. Attaches `OffsetCalc-<tag>.apk` to the release. Manual runs upload it as a workflow artifact instead.

Versioning is automatic, configured in `app/build.gradle.kts`:
- `versionCode` = `GITHUB_RUN_NUMBER`, so it always increases.
- `versionName` = release tag without the `v` (`v1.2` → `1.2`).

Signing uses four repository secrets: `KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`. Without them, release builds fall back to the debug key, which is also what happens for local `assembleRelease` builds. The keystore file is git-ignored (`*.jks`).

To cut a release: push to `main`, then **Releases → Draft a new release**, create a new `vX.Y` tag, and publish.

## Contributing

Issues and pull requests are welcome. A few guidelines:

- **Keep the math matching the spreadsheet.** Any new formula should cite the source (cell, note, or reference) in a comment and come with a test.
- **Keep `OffsetMath` free of Android/Compose imports.**
- **Reuse `DrawingKit`** for any new diagram so that colors, dimension style and labels stay consistent.
- Use standard Kotlin style (`kotlin.code.style=official`).
