# Agent Notes

This repository is primarily an Android library workspace, but it can also host short-lived standalone application modules when fast APK delivery is more important than starting a separate repository.

Read these docs before changing project structure:

- `docs/analoge-clock-module.md`: explains the standalone `analoge_clock` app module that was added on the `analoge_clock` branch.
- `docs/standalone-app-modules.md`: checklist for adding another independent app module in this codebase.
- `docs/analog_clock_b4a_development.doc`: simple Farsi client-facing document that describes the clock app as if it was developed with B4A.

Important rules for future agents:

- Keep library modules and standalone app modules separate.
- Do not add signing keys, generated APKs, or module build outputs to Git.
- Prefer creating a new app module for urgent/demo APKs instead of modifying the existing `app` demo module.
- Keep each standalone app's `applicationId`, namespace, label, resources, and signing config independent from the libraries.
- Before committing, run the target module build command and verify `git status --short --ignored`.
