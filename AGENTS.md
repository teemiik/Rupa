# AGENTS.md

Compact instructions for OpenCode sessions working on the Rupa libGDX game. Collects the high‑signal facts an agent is most likely to miss.

## What this is

**Rupa** is a [libGDX](https://libgdx.com/) game (codenamed "circles and holes" — Java package `com.circlesandholes.game`). Gameplay: tilt a board left/right to roll a ball up the screen past holes to the top. Falling into a hole or off the bottom = fail; reaching the top = win. Levels are data‑driven (JSON); art is generated at runtime. Pure Java (Java 8), no Kotlin, no tests.

## Build & Run

- Gradle wrapper (`./gradlew`, Gradle 8.10.2). Java 8 source compatibility.
- **Desktop dev loop:** `./gradlew lwjgl3:run` – window is 480×640 portrait. Dev run sets `-Ddebug=true` so `Debug.enabled` is on.
- **Build desktop jar:** `./gradlew lwjgl3:jar` → `lwjgl3/build/lib`. The packaged jar has `-Ddebug=true` removed. (`lwjgl3:dist` is an alias for `jar`.)
- **Native installers (.app/.dmg/.exe/.tar):** the `construo` plugin generates `lwjgl3:package{MacM1,MacX64,LinuxX64,WinX64}`. Each target downloads its own JDK 17 (no system JDK needed) and produces a self-contained image; the plain `jar` is just a fat JAR, not a native package.
- **Android release:** `./gradlew android:assembleRelease` / `android:bundleRelease` (requires `local.properties` with `sdk.dir`).
- **Web build:** `./gradlew html:dist` (output in `html/build/dist`); `./gradlew html:superDev` for live dev at `localhost:8080/html`.
- **Clean:** `./gradlew clean` (or per‑module `core:clean`, `lwjgl3:clean`, etc.).
- **Test task exists but there are no test sources.** No lint, checkstyle, or typecheck tasks. Don't add them ad‑hoc.
- All platform subprojects (`lwjgl3`, `core`, `android`, `ios`, `html`) are listed in `settings.gradle`; scope any task with the module prefix (`core:clean`, `lwjgl3:run`, …).

## Project Structure

- `core` – all game logic (`com.circlesandholes.game`). Pure Java – the large block of KTX/Kotlin/Artemis/Ashley/Vis‑UI dependencies in `core/build.gradle` are unused liftoff‑template leftovers; **there are no Kotlin sources in the project**. Do not add Kotlin or wire those libs up.
- `lwjgl3`, `android`, `ios`, `html` – thin platform launchers (`*Launcher.java`) that each instantiate `Intro` and hand it to the backend. Android is portrait‑locked; LWJGL3 adds `-XstartOnFirstThread` on macOS.
- Entry point: `Intro extends Game` (`core/src/main/java/com/circlesandholes/game/Intro.java`). Most shared state is exposed as `public static` fields (`circle`, `board`, `w_world`, `h_world`, `faild`, `win`, `text`, `box_din`, `paused`, `currentBackground`, …). Screens/screens never DI; they mutate `Intro` statics.
- **Route every level start through `Intro.goToLevel(int)`** – it resets the per‑run flags (`faild=true`, `win=false`, `box_din=0`, `paused=false`, `consumeTouch=true`), cancels the oscillating‑hole timer, rebuilds the board/background theme, and shows `LevelScreen`. Bypassing it leaves stale state.

## Architecture

A small set of libGDX `Screen`s coordinated through one global state holder (`Intro`), with **data‑driven levels** and **procedurally generated art**.

- **`Intro extends Game`** — entry point (set by every launcher) and central state container. `create()` generates procedural textures, builds the start‑menu stage, starts timers, computes world size. `goToLevel(int)` starts a level (see Project Structure); `showMenu()` returns to the start screen.
- **`LevelScreen`** — one generic, data‑driven screen that **replaced the old `Level1`–`Level6` classes; there is no per‑level Java class anymore.** It takes a `LevelData`, builds static holes / oscillating holes / barriers / rotating platforms from it, then runs the shared tilt‑physics / win‑fail loop, the pause overlay and a debug "WIN" button.
- **`LevelData` / `LevelLoader`** — a level is `assets/levels/levelN.json`, parsed with `JsonReader`/`JsonValue` (no reflection → GWT‑safe). `LevelLoader.count()` auto‑detects contiguous level files.
- **`LevelSelectScreen`** — procedural grid of level tiles, auto‑laid‑out from `LevelLoader.count()`; takes a `returnTo` screen so Back returns to wherever it was opened from.
- **`TryContinueEnd`** — win / lose / "the end" result screen; records the best time, plays a win particle burst, routes retry / next / choose‑level through `Intro.goToLevel`.
- **`ProceduralAssets`** — generates all primitive art at runtime via `Pixmap` (ball, hole, gradient backgrounds, back arrow, check, pause icon). `Intro.buildLevelTheme(level)` derives a per‑level hue (HSV from the level number) for the background gradient **and** the board colour.
- **`Progress`** — persistence over libGDX `Preferences`: best time per level, completion flag, `formatTime`.
- **`PhysicalBodies/`** — Box2D body factories (`BoxBoard`, `BoxCircle`, `BoxHole`, `BoxRectangleBarrier`). Bodies are tagged via `setUserData("circle"/"hole"/"board"/"barrier")`; `beginContact` compares tags and fails the run on circle↔hole contact.

## Code Conventions

- **Global mutable static state is the norm.** Screens communicate via `Intro` static fields, not DI.
- **Resolution independence is formula‑based**, not asset buckets. Sizes/tuning (`size_text`, `speed_hole`, ball/hole diameters, `uiScale`) are computed from `w_world` in `Intro`, calibrated so a ~1080px‑wide screen matches the original art. PNG buttons live under `assets/Menu/`; use the `uiScale` field, not pixel sizes.
- **Input handling differs per screen.** `LevelScreen` polls input in `render()` and sets the input processor to `null` in `show()`; menu/select/result screens use scene2d `Stage`s and (re)claim the processor in `show()`. `Intro.consumeTouch` makes a starting level ignore the in‑progress touch until release, so a menu tap doesn't also tilt the board.
- **Physics loop:** Box2D `World` stepped with `world.step(sub, 8, 8)` where `sub = time_step * (h_world / 640f)` split into sub-steps for stability (see `LevelScreen`). Constants `VELOCITY_ITERATIONS`/`POSITION_ITERATIONS` live in `Intro`. Tilt = touching the left/right screen half, clamped ±28°. Accelerometer control was tried and removed.
- **Timer threads** (`java.util.Timer` / `TimerTask`) drive the clock and the oscillating‑hole counter (`box_din`); both early‑return while `Intro.paused`. `Intro.dispose()` cancels them.
- **Note field name typos that are public API:** `faild` (not `failed`), `box_din` (oscillating‑hole counter), `box_hole_din_sign`. Renaming them is a breaking change.
- **Don't add comments** unless asked; the file is mostly self‑documented with terse `//` lines where context is needed.

## Assets

- Top‑level `assets/` shared by all modules (the Android module maps `assets.setSrcDirs(['../assets'])`; LWJGL3 sets `run.workingDir = rootProject.file('assets').path` so relative asset paths resolve).
- Most art is generated at runtime by `ProceduralAssets` (ball, hole, gradient backgrounds, back arrow, check, pause icon, etc.). Hand‑authored assets that ship:
  - `10771.ttf` – the FreeType font, rendered with the `Intro.lang` charset (Cyrillic + Latin + digits + symbols). Add new glyphs to that string.
  - `Menu/{play,try,continue,menu_button_up}.png` – menu buttons.
  - `Gestures/training_{left,right}_{64,74}.png` – tilt‑hint sprites used on the tutorial level.
  - `levels/level*.json` – level data.
  - `strings.properties`, `strings_en.properties` – i18n bundles (loaded via `I18NBundle`).
- `assets/assets.txt` is **auto‑generated** by the root `build.gradle`’s `compileJava.doLast` (lists every file under `assets/`) – **do not hand‑edit** and don't commit it (it is gitignored). The same task deletes the file first to avoid stale entries.

## Debugging

- `Debug.enabled` is set by each launcher from its build type:
  - Desktop: `Boolean.getBoolean("debug")` from `-Ddebug=true` passed by `lwjgl3/build.gradle`'s `run` task.
  - Android: `BuildConfig.DEBUG`.
  - Off in release/packaged builds. Gates dev‑only affordances (the instant‑win button). Do not replace this with platform string checks.

## Adding a Level

1. Drop a new `levelN.json` into `assets/levels/` (must be contiguous: `level1`, `level2`, …, `levelN`).
2. No code changes needed – `LevelLoader.count()` walks `levels/level{n}.json` until one is missing, and `LevelSelectScreen` lays itself out from that count.
3. Positions/sizes are fractions of `w_world`/`h_world` (x of w_world, y of h_world). The schema (`LevelData.java` / `LevelLoader.java`):
   - `board` / `background` – theme names, `"board"`+`"background"` or `"board_2"`+`"background_2"`.
   - `showHints` (bool) – draw the tilt‑gesture sprites (used by the tutorial level).
   - `holes` – array of `{x, y}` static holes.
   - `dynamicHoles` + `dynamicControl` – oscillating holes driven by `box_din`. Per‑hole `{x, y, xDin, yDin}` (each din ∈ {-1, 0, +1}); the per‑level `dynamicControl` `{lowerX, upperX, inverted}` flips the oscillation direction on threshold.
   - `barriers` – static rotating rectangles `{x, y, w, h, rotation}`.
   - `rotatingPlatforms` – continuously rotating kinematic platforms `{x, y, w, h, rotationSpeed, color}`; see `level10.json` for an example.
4. JSON is parsed with `JsonReader`/`JsonValue` (no reflection) so it remains GWT‑safe. Don't add fields that require reflection.
5. Persistence (`Progress`) records best time per level in libGDX `Preferences`.

## Android Notes

- `local.properties` must contain `sdk.dir` pointing to Android SDK.
- `minSdk 21`, `targetSdk 35`, `applicationId com.circlesandholes.game`.
- Build variants: `assembleRelease`, `bundleRelease`.

## iOS & Web

- iOS uses RoboVM (`ios` module).
- Web uses GWT/WebGL (`html` module); supports only Java sources (no Kotlin, no reflection).