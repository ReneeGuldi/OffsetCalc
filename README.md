# Offset Calc

A duct offset calculator for your Android phone. Enter your offset, angle and duct size, and it gives you the **cut piece** and every other measurement you need, plus a to-scale drawing showing where each one goes.

It does the same math as the shop's offset spreadsheet, so the numbers match.

**[⬇ Download the latest version](../../releases/latest)**

---

## Installing the app

You only need to do this once. Updates install the same way.

1. **On your Android phone**, open this page in Chrome and tap **[Download the latest version](../../releases/latest)**.
2. Under **Assets**, tap the file ending in **`.apk`** (for example `OffsetCalc-v1.0.apk`). It downloads in a few seconds.
3. Tap the downloaded file to open it.
4. If your phone says Chrome isn't allowed to install apps, tap **Settings**, turn on **Allow from this source**, then tap back.
5. Tap **Install**.
   - If **Google Play Protect** warns that it doesn't recognize the app, tap **More details → Install anyway**. You'll see this for any app that doesn't come from the Play Store.
6. Open **Offset Calc** from your app drawer.

> **iPhone?** This app is Android only.

### Updating

When a new version comes out, repeat the steps above. The new version installs right over the old one; there's no need to uninstall first.

---

## Using the app

The app has four tabs along the top. **Yellow boxes are where you type.** Results update instantly as you type, shown as a decimal with the nearest 1/16" underneath (for example `27.0883` → `≈ 27 1/16"`).

### Offset: find your cut piece

This is the main calculator, for a two-elbow offset.

| Box | What to enter |
|---|---|
| **(A) Offset** | How far the duct needs to move, centerline to centerline |
| **(I) Angle** | The elbow angle, e.g. `45` |
| **(B) ℄ rise multiplier** | Usually `1.5` |
| **(H) ø or cheek** | The duct diameter (or cheek size) |

The big number at the top of the results is **(C) Cut piece**: the length of straight duct to cut between the two elbows.

**Example:** 14"ø duct, 21" offset, two 45° elbows, multiplier 1.5 → **cut piece 12 5/16"** (12.3015).

#### Elbows at two different angles
Turn on **"2nd elbow is a different angle"** for combinations like a 60 with a 30, or a 45 with a 90. A second angle box appears, and the cut piece adjusts automatically.

#### If the elbows don't fit
If the offset is too small for the elbows you chose, the drawing turns red and says **"Elbows overlap"**. Try a smaller angle, a smaller multiplier, or a bigger offset.

### Reading the drawing

Under the results is a to-scale picture of your offset, with every measurement marked. The colors match the legend in the spreadsheet, and the small colored square next to each result:

| Color | Meaning | Letters |
|---|---|---|
| 🟨 Yellow | What you entered | **A** offset · **B** multiplier · **H** diameter · **I** angle |
| 🟩 Green | What you cut or make up | **C** cut piece · **E** offset length makeups |
| 🟥 Red | Elbow centerline measurements | **D** ℄ rise of elbow · **G** ℄ to ℄ at angle |
| 🟦 Cyan | Horizontal centerline length | **F** offset length ℄s |

The cut piece is shaded green in the drawing. Each measurement shows the fraction and the exact decimal in [brackets], just like the spreadsheet's drawing.

### Compound: an offset that moves two ways

Use this when the duct moves in two directions at once, for example **up 17" and left 12.25"**.

1. Enter **A** (one direction) and **B** (the other).
2. The app gives you **C**, the true offset (here **20.95"**).
3. Go to the **Offset** tab and use C as your **(A) Offset**. Rounding up is fine (21").

The triangle drawing shows which side is which.

### O.W.L.: square mitered offsets

Enter **Offset**, **Width** and **Length** to get the **Miter** (Offset × Width ÷ Length).

### Resize: same square inches

Changing duct size but need the same area? Enter the current **W1 × H1** and the new width **W2**, and the app gives you the new height **H2**. It works for either dimension.

---

## Questions

**Do I need internet?** No. The app works completely offline and asks for no permissions.

**Can I trust the numbers?** The formulas are copied directly from the shop spreadsheet, and every version is automatically tested against the spreadsheet's own results before it's published. If a number ever looks wrong, please [open an issue](../../issues) with what you entered and what you expected.

**What if my measurements use mm?** Any unit works, as long as you use the same one for every box. Only the 1/16" fractions assume inches.

---

## For developers: contributing an update

Want to fix something or add a feature? Contributions are welcome.

### Setup
1. **Fork** this repository (the button at the top right), then clone your fork.
2. Open the folder in **Android Studio** (any recent version) and let it sync.
3. Press **Run ▶** to try the app on a phone or emulator.

The app is Kotlin + Jetpack Compose. The key files are in `app/src/main/java/com/ductcalc/offset/`:

| File | What's in it |
|---|---|
| `OffsetMath.kt` | All the formulas. Start here for any change to the math. |
| `MainActivity.kt` | The four tabs, input boxes and results |
| `DuctDiagram.kt` / `CompoundDiagram.kt` | The drawings |
| `DrawingKit.kt` | Shared drawing style: colors, dimension lines, labels |

### Making a change
1. Create a branch for your change.
2. **If you touch the math**, keep it matching the spreadsheet and add a test in `app/src/test/.../OffsetMathTest.kt`. Run the tests with:
   ```
   ./gradlew testDebugUnitTest
   ```
3. Check the app still runs and the drawings still look right.
4. Push your branch and open a **pull request** that describes what you changed and why.

### How new versions get released
Once a pull request is merged, the repository owner publishes a new release, e.g. `v1.1`. GitHub then automatically runs the tests, builds and signs the app, and attaches the new `.apk` to the [Releases](../../releases) page, where everyone can download it. Contributors don't need a signing key. Only the owner's releases are signed, so updates always install cleanly over the previous version.# Offset Calc

A duct offset calculator for your Android phone. Enter your offset, angle and duct size, and it gives you the **cut piece** and every other measurement you need, plus a to-scale drawing showing where each one goes.

It does the same math as the shop's offset spreadsheet, so the numbers match.

**[⬇ Download the latest version](../../releases/latest)**

---

## Installing the app

You only need to do this once. Updates install the same way.

1. **On your Android phone**, open this page in Chrome and tap **[Download the latest version](../../releases/latest)**.
2. Under **Assets**, tap the file ending in **`.apk`** (for example `OffsetCalc-v1.0.apk`). It downloads in a few seconds.
3. Tap the downloaded file to open it.
4. If your phone says Chrome isn't allowed to install apps, tap **Settings**, turn on **Allow from this source**, then tap back.
5. Tap **Install**.
   - If **Google Play Protect** warns that it doesn't recognize the app, tap **More details → Install anyway**. You'll see this for any app that doesn't come from the Play Store.
6. Open **Offset Calc** from your app drawer.

> **iPhone?** This app is Android only.

### Updating

When a new version comes out, repeat the steps above. The new version installs right over the old one; there's no need to uninstall first.

---

## Using the app

The app has four tabs along the top. **Yellow boxes are where you type.** Results update instantly as you type, shown as a decimal with the nearest 1/16" underneath (for example `27.0883` → `≈ 27 1/16"`).

### Offset: find your cut piece

This is the main calculator, for a two-elbow offset.

| Box | What to enter |
|---|---|
| **(A) Offset** | How far the duct needs to move, centerline to centerline |
| **(I) Angle** | The elbow angle, e.g. `45` |
| **(B) ℄ rise multiplier** | Usually `1.5` |
| **(H) ø or cheek** | The duct diameter (or cheek size) |

The big number at the top of the results is **(C) Cut piece**: the length of straight duct to cut between the two elbows.

**Example:** 14"ø duct, 21" offset, two 45° elbows, multiplier 1.5 → **cut piece 12 5/16"** (12.3015).

#### Elbows at two different angles
Turn on **"2nd elbow is a different angle"** for combinations like a 60 with a 30, or a 45 with a 90. A second angle box appears, and the cut piece adjusts automatically.

#### If the elbows don't fit
If the offset is too small for the elbows you chose, the drawing turns red and says **"Elbows overlap"**. Try a smaller angle, a smaller multiplier, or a bigger offset.

### Reading the drawing

Under the results is a to-scale picture of your offset, with every measurement marked. The colors match the legend in the spreadsheet, and the small colored square next to each result:

| Color | Meaning | Letters |
|---|---|---|
| 🟨 Yellow | What you entered | **A** offset · **B** multiplier · **H** diameter · **I** angle |
| 🟩 Green | What you cut or make up | **C** cut piece · **E** offset length makeups |
| 🟥 Red | Elbow centerline measurements | **D** ℄ rise of elbow · **G** ℄ to ℄ at angle |
| 🟦 Cyan | Horizontal centerline length | **F** offset length ℄s |

The cut piece is shaded green in the drawing. Each measurement shows the fraction and the exact decimal in [brackets], just like the spreadsheet's drawing.

### Compound: an offset that moves two ways

Use this when the duct moves in two directions at once, for example **up 17" and left 12.25"**.

1. Enter **A** (one direction) and **B** (the other).
2. The app gives you **C**, the true offset (here **20.95"**).
3. Go to the **Offset** tab and use C as your **(A) Offset**. Rounding up is fine (21").

The triangle drawing shows which side is which.

### O.W.L.: square mitered offsets

Enter **Offset**, **Width** and **Length** to get the **Miter** (Offset × Width ÷ Length).

### Resize: same square inches

Changing duct size but need the same area? Enter the current **W1 × H1** and the new width **W2**, and the app gives you the new height **H2**. It works for either dimension.

---

## Questions

**Do I need internet?** No. The app works completely offline and asks for no permissions.

**Can I trust the numbers?** The formulas are copied directly from the shop spreadsheet, and every version is automatically tested against the spreadsheet's own results before it's published. If a number ever looks wrong, please [open an issue](../../issues) with what you entered and what you expected.

**What if my measurements use mm?** Any unit works, as long as you use the same one for every box. Only the 1/16" fractions assume inches.

---

## For developers: contributing an update

Want to fix something or add a feature? Contributions are welcome.

### Setup
1. **Fork** this repository (the button at the top right), then clone your fork.
2. Open the folder in **Android Studio** (any recent version) and let it sync.
3. Press **Run ▶** to try the app on a phone or emulator.

The app is Kotlin + Jetpack Compose. The key files are in `app/src/main/java/com/ductcalc/offset/`:

| File | What's in it |
|---|---|
| `OffsetMath.kt` | All the formulas. Start here for any change to the math. |
| `MainActivity.kt` | The four tabs, input boxes and results |
| `DuctDiagram.kt` / `CompoundDiagram.kt` | The drawings |
| `DrawingKit.kt` | Shared drawing style: colors, dimension lines, labels |

### Making a change
1. Create a branch for your change.
2. **If you touch the math**, keep it matching the spreadsheet and add a test in `app/src/test/.../OffsetMathTest.kt`. Run the tests with:
   ```
   ./gradlew testDebugUnitTest
   ```
3. Check the app still runs and the drawings still look right.
4. Push your branch and open a **pull request** that describes what you changed and why.

### How new versions get released
Once a pull request is merged, the repository owner publishes a new release, e.g. `v1.1`. GitHub then automatically runs the tests, builds and signs the app, and attaches the new `.apk` to the [Releases](../../releases) page, where everyone can download it. Contributors don't need a signing key. Only the owner's releases are signed, so updates always install cleanly over the previous version.
