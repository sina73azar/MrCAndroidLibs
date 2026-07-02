# Memory Game Module

## Purpose

`memory_game` is a standalone Android application module for a simple Persian word memory game.

It is independent from the Network Logger, Network Proxy, existing demo `app`, and `analoge_clock` modules. It only shares this repository's Gradle/plugin/dependency setup so development can move quickly.

## Current Product Assumption

The requester described a simple memory game with buttons and words. The implemented interpretation is the classic matching-pairs memory game:

1. The screen shows a 4x4 grid of hidden cards.
2. Each card hides a Persian word.
3. The user taps two cards.
4. If both words match, the pair stays visible.
5. If they do not match, both cards hide again after a short delay.
6. The player wins after finding all pairs.

This is the safest default because it matches the common meaning of "memory game" and the requested "button with word" behavior.

## Module Identity

- Module path: `:memory_game`
- Directory: `memory_game/`
- Namespace: `com.mrc.memorygame`
- Application ID: `com.mrc.memorygame`
- App label: `بازی حافظه`
- Version code: `1`
- Version name: `1.0`

## Current UI And Behavior

The app is a Compose application with RTL layout and Persian text.

Current screen elements:

- Title: `بازی حافظه`
- Short instruction text.
- Move counter.
- Matched-pairs counter.
- 4x4 card grid.
- Reset button: `شروع دوباره`
- Win message after all pairs are found.

Current Persian word pairs:

```text
خورشید
ماه
کتاب
خانه
باران
گل
دریا
دوست
```

## Important Files

- `settings.gradle.kts`: includes `:memory_game`.
- `memory_game/build.gradle.kts`: standalone Android application Gradle config.
- `memory_game/src/main/AndroidManifest.xml`: app manifest and launcher activity.
- `memory_game/src/main/java/com/mrc/memorygame/MainActivity.kt`: full Compose UI and game logic.
- `memory_game/src/main/res/values/strings.xml`: app label.
- `memory_game/src/main/res/values/themes.xml`: no-action-bar Android theme.
- `memory_game/.gitignore`: prevents generated APKs/build outputs/signing keys from being committed.

## Build Commands

Debug build:

```bash
./gradlew :memory_game:assembleDebug
```

Release build:

```bash
./gradlew :memory_game:assembleRelease
```

Signing should remain local unless a secure signing setup is explicitly added.

## Git Hygiene

The module-local `.gitignore` ignores:

```text
/build/
/release/
*.jks
*.keystore
```

Before committing future changes, check:

```bash
git status --short --ignored memory_game
```

Do not commit generated APKs, signing keys, or local release folders.

## Suggested Next Steps

If another agent continues this module, the highest-value next checks are:

1. Run `./gradlew :memory_game:assembleDebug`.
2. Install the APK on a device or emulator and verify the RTL layout.
3. Check that wrong pairs close after the delay.
4. Check that matching pairs stay visible.
5. Confirm the requested word list with the requester.
6. If needed, add difficulty levels such as 2x2, 4x4, or 6x4.
7. If needed, add sound/vibration feedback behind a simple settings toggle.

Keep the game self-contained unless there is a clear reason to reuse project libraries.
