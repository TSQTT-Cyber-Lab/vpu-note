# AGENTS.md (Android)

The Android port: listening, plus 我说 in 面对面 (hold, speak, the phone reads the translation aloud; no virtual mic, so never into another app's call). The root `AGENTS.md` applies too (the Gemini key, `VPUNoteClient`, no third-party dependencies, the Mac app as the reference, one `VERSION`). Commands, layout and how to verify are in `README.md`; the plan is `docs/android-plan.md`.

- Kotlin and the Android framework only: no AndroidX, Compose or other libraries; UI in code, no XML layouts. `core` is plain JVM and must build and test on the Mac.
- 说话人识别 runs Linux's `native/voiceprint.c` (the Mac's model) through the NDK (`app/src/main/jni`): never copy or fork that C, and keep thresholds, labels and the name prompt the Mac's.
- Keys: `AIKey` in `core/.../VPUNoteClient.kt`, one Keystore-sealed file per AI service in the app's private folder (`ai-key-<service>`, as on the Mac), kept out of backups. The services and their recommended models are `core/.../Providers.kt`; every AI call goes through `VPUNoteClient`, which reads the service from settings on each call.
- 我说 speaks only `ListenEngine.speakable` text (a translation, never my words), in the voice chosen for that language (`VoiceCatalog.chosen`: the service's speech voice when it has one, else the phone's), and mutes the segmenter while the phone speaks.
- The overlay is `FLAG_NOT_FOCUSABLE` and `FLAG_SECURE` unless 共享屏幕时让对方看到 is on.
- Shared UI text comes from `Resources/*.lproj/Localizable.strings`; Android-only text is in `app/src/main/strings/`. Run `python3 scripts/strings.py` after changing either.
- Verify with `scripts/test.sh`, then in the emulator with `scripts/vpunotectl.sh`. System consent dialogs, vendor background limits and a real microphone need a person.
- No release build ships until release signing exists; never sign a release with the debug key.
- Commits use the `(android)` scope: `feat(android):` / `fix(android):` are this platform's release notes.
- Brand (Victoria Pacific University, as the Windows port): navy `#2D397E` is `Palette.primary`, the logo gold is `Palette.accent` for "live" (never as text on white), `Palette.problem` red marks errors; the launcher icon is the gold bird of the logo on a navy ground (`drawable-nodpi/ic_launcher_*.png`, `ic_launcher_background.xml`), `ic_notification` its white silhouette.
