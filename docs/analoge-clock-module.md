# Analoge Clock Module

## Purpose

`analoge_clock` is a standalone Android application module created on the `analoge_clock` branch.

It is not part of the Network Logger or Network Proxy libraries. It only shares this repository's Gradle setup, dependency catalog, Android plugin versions, and existing project infrastructure so an APK can be produced quickly without starting a new project from scratch.

## Why It Exists Here

The app was needed urgently, so creating a separate module inside the existing Android workspace was faster than creating and configuring a new repository.

This should be treated as a product/application module, not a library module.

## Module Identity

- Module path: `:analoge_clock`
- Directory: `analoge_clock/`
- Namespace: `com.mrc.analogeclock`
- Application ID: `com.mrc.analogeclock`
- App label: `Analoge Clock`
- Version code: `1`
- Version name: `1.0`

The module name intentionally matches the branch spelling: `analoge_clock`.

## Current Behavior

The app opens directly to a Compose screen that displays:

- Digital device time.
- Current date.
- Analog clock face.
- Hour, minute, and second hands.

The clock reads time from the device using `System.currentTimeMillis()` and updates every second.

## Important Files

- `settings.gradle.kts`: includes `:analoge_clock`.
- `analoge_clock/build.gradle.kts`: standalone Android application Gradle config.
- `analoge_clock/src/main/AndroidManifest.xml`: app manifest and launcher activity.
- `analoge_clock/src/main/java/com/mrc/analogeclock/MainActivity.kt`: full Compose UI and clock drawing logic.
- `analoge_clock/src/main/res/values/strings.xml`: app label.
- `analoge_clock/src/main/res/values/themes.xml`: no-action-bar Android theme.
- `analoge_clock/.gitignore`: prevents generated APKs/build outputs/signing keys from being committed.

## Build Commands

Debug build:

```bash
./gradlew :analoge_clock:assembleDebug
```

Release build:

```bash
./gradlew :analoge_clock:assembleRelease
```

Signing is intentionally left local. Do not commit `.jks`, `.keystore`, generated APKs, or `release/` output folders.

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
git status --short --ignored analoge_clock
```

Expected ignored local artifacts can include:

```text
analoge_clock/build/
analoge_clock/release/
analoge_clock/*.jks
analoge_clock/*.keystore
```

## Relationship To Other Modules

`analoge_clock` currently does not depend on:

- `network-logger-core`
- `network-logger-ui`
- `network-proxy-core`
- existing `app`

If future work adds those dependencies, keep them explicit in `analoge_clock/build.gradle.kts` and document why.
