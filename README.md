<p align="center">
  <img src="windows/src/VPUNote/Assets/logo.png" width="220" alt="Victoria Pacific University">
</p>

<h1 align="center">VPUNote</h1>

<p align="center">
  Live bilingual subtitles, automatic meeting notes and real-time interpreting for online meetings.<br>
  For Windows 10/11 64-bit.
</p>

<p align="center"><a href="README.md">English</a> · <a href="README.vi.md">Tiếng Việt</a></p>

## What VPUNote does

VPUNote runs **on your own computer**, next to the meeting app you already use (Zoom, Microsoft Teams, Google Meet, Webex, Tencent Meeting…). It does not replace that app. It does two jobs:

| Job | How |
|---|---|
| **Understand what others say** | It listens to the sound your computer plays, shows translated subtitles on your screen, and writes the meeting notes (summary, topics, conclusions, action items). |
| **Be understood** | You hold one key and speak your language. VPUNote translates it and plays a voice in the meeting in the other side's language. |

Interface languages: Tiếng Việt (default), English, Русский, 简体中文, 日本語, 한국어. Listening and translation cover all six.

## How other participants take part

**They do not need to do anything.** VPUNote has no meeting room of its own, no meeting code and no accounts. The meeting stays in Zoom/Teams/Meet as usual; VPUNote only helps you.

```
        Partner (speaking English)
                │  through Zoom / Teams / Meet
                ▼
   ┌──────── Your computer ───────┐
   │ Speakers ──► VPUNote listens ──► Vietnamese subtitles and notes (only you see them)
   │
   │ You speak Vietnamese, holding Right Ctrl
   │   └─► VPUNote translates ─► English voice ─► virtual microphone (VB-Cable)
   └──────────────┬───────────────┘
                  ▼  Zoom/Teams/Meet sends the virtual microphone as yours
        The partner hears the English voice on their own device
```

1. **You hear the partner:** they speak English, the sound plays through your speakers, and VPUNote shows Vietnamese subtitles on your screen.
2. **The partner hears you:** you hold **Right Ctrl** and speak Vietnamese. VPUNote translates it to English and plays the voice into the **VB-Cable virtual microphone**. In Zoom/Teams you choose that virtual microphone. The meeting app sends the English voice, so the partner hears it **on their own device, through the meeting app**.

### Is there a login or authentication?

- **In VPUNote: no.** No account and no meeting code. The only thing you enter is an **API key** for an AI service (see below), stored on your computer and encrypted with your Windows account.
- **In the meeting:** invitations, passwords and waiting rooms are handled by Zoom/Teams/Meet as usual.
- **Does the partner need anything?** Nothing. They hear your voice through the meeting app.

### Can the partner see my subtitles?

Not automatically. Subtitles and notes appear only on your computer. To show them, you can **share your screen** (see "Notes window and screen sharing" below) or send the notes after the meeting. The Windows app does not have a link-sharing feature at the moment.

## Installing on Windows

**Requirements:** Windows 10 (version 2004 or later) or Windows 11, 64-bit; an internet connection.

1. Open [Releases](https://github.com/TSQTT-Cyber-Lab/vpu-note/releases/latest) and download `VPUNote-<version>-Windows-x64.exe` (on Snapdragon / ARM64 machines, download `-Windows-ARM64.exe` if it is listed). Check the SHA-256 against `checksums.txt` on the same page:
   ```powershell
   Get-FileHash .\VPUNote-*-Windows-x64.exe -Algorithm SHA256
   ```
   Releases marked **Pre-release** are test builds; use the one with the highest number.
2. Copy the file to a local folder, for example the Desktop. **Do not run it directly from a network share.**
3. Double-click it. The app is not code-signed, so Windows SmartScreen may block it: click **More info**, then **Run anyway**.
4. The installer installs VPUNote **for your account only**, needs no administrator rights, and opens the app. After that, open VPUNote from the Start menu, or from the icon in the system tray (bottom-right of the taskbar, possibly behind the `^` arrow).

| Content | Location |
|---|---|
| Program | `%LOCALAPPDATA%\Programs\VPUNote\VPUNote.exe` |
| Data (settings, keys, meeting records) | `%APPDATA%\VPUNote` |

**Upgrading:** download the newer build and run it. If it is newer than what is installed, the installer asks whether to replace it. Records and settings are kept. If an overwrite error appears, quit VPUNote from the system tray and run the installer again.

**Uninstalling:** Settings › Apps › Installed apps › VPUNote › Uninstall. Meeting records are kept by default.

## Setting up an AI service (required, once)

VPUNote sends audio and text to an AI service for speech recognition, translation and notes. You need an API key.

| Service | How to get a key | Notes |
|---|---|---|
| **Google Gemini** | Create a key at [aistudio.google.com/apikey](https://aistudio.google.com/apikey) (a key starts with `AQ.` or `AIza`) | Has a free tier (limited requests). No AI voice; it uses the Windows voice. |
| **OpenRouter** | Create a key at [openrouter.ai/settings/keys](https://openrouter.ai/settings/keys) (starts with `sk-or-`) | Pay per use. Has natural AI voices. |
| **Other (OpenAI-compatible)** | Enter the address and key of your service | The service must accept audio input. A server on your local network may need no key. |

How to enter it: at the first step of the setup guide, or in **Settings › AI service**: choose the service, paste the key and click **Save and check**. When all four lines (connection, key, notes and translation, listening to the other side) show a ✓, it works. VPUNote picks the models your key can use.

Until an AI service works, text translation still runs on Google Translate (free). Speech recognition and notes need the AI service.

## Using it in a meeting

### Only to understand others (no virtual microphone)

1. Click **Start listening** in the tray icon, or in the main window.
2. Join the meeting as usual. Subtitles and notes appear in the **notes window**, floating above other windows. It never takes focus, so you can keep typing in other apps.
3. Choose the translation language in **Settings › Language › Translate to**. By default VPUNote detects what the other side speaks.
4. When finished, click **End**. The meeting is kept in the records list for review, search and export to Markdown.

If someone is in the same room, click **Turn on mic** to record them too.

### So the other side hears you in their language (Interpreting)

**One-time setup:**

1. In **Settings › Interpreting**, switch it on (top right).
2. Under **Send the translation into the meeting**, click **Install** to install **VB-Cable**, a free virtual microphone. Windows asks you to confirm the driver installation.
3. Choose **Language I speak** (for example Vietnamese) and **Language people online hear** (for example English, or "Language they speak" to follow the partner).
4. In Zoom/Teams/Meet: **Settings › Audio**, set **Microphone** to **CABLE Output (VB-Audio Virtual Cable)**.

**During the meeting:**

- Hold **Right Ctrl**, speak, release. VPUNote translates and plays the voice into the meeting. The talk key can be changed in **Settings › Interpreting** if your keyboard has no right Ctrl.
- Use **Preview** to hear the voice before the meeting.

**Notes:**

- **Delay:** about 1 to 3 seconds from the end of your sentence to the partner hearing it.
- **Voice:** with Gemini the voice is the Windows voice, so your computer needs a voice for the target language (Settings › Time & language › Speech). OpenRouter has AI voices.
- **Echo:** wear headphones, so the partner's voice from your speakers is not picked up by your microphone.
- **Only translations are spoken:** VPUNote never speaks a sentence that has not been translated. If a translation fails, that sentence is skipped.
- **The partner hears the synthetic voice, not your own voice.**

### Notes window and screen sharing

By default the notes window **does not appear when you share your screen**, so others do not see your subtitles. To let them see it, turn on **Settings › Privacy & startup › Show when sharing screen**. The window then shows a "Visible to others" label.

## Privacy

- Audio and text from the meeting are sent to **the AI service you chose** (Google Gemini, OpenRouter, or your own server). Tell participants if the meeting contains sensitive content.
- The fallback translation sends text to Google Translate, unless you use your own AI service and turn on **Send audio and text only to your service**.
- API keys are encrypted with your Windows account and stored in `%APPDATA%\VPUNote`. They are never written to logs or exports.
- Meeting records stay on your computer, in the data folder above.

## Troubleshooting

| Symptom | What to do |
|---|---|
| SmartScreen blocks the app | Click **More info › Run anyway**. The app is not code-signed. |
| The new file still shows the old interface | Use the build with the higher number than the one installed. If that doesn't help, uninstall VPUNote in Settings › Apps and run the file again. |
| "This key doesn't work" | The key was cut short, deleted, or belongs to another service. Create a new key and paste all of it. |
| "The model doesn't work" | Update to the newest build, then in **Settings › AI service** click **Check again**. |
| "Can't reach the service" | Check your network or proxy. In some places you need the system proxy on to reach Google or OpenRouter. |
| No subtitles during a meeting | Make sure the computer is not muted; the volume meter during the sound test should move when something plays. VPUNote listens to what the computer plays, not to the microphone. |
| The partner does not hear the translation | Check that VB-Cable is installed and the microphone in the meeting app is **CABLE Output**, not your real microphone. |
| The partner hears echo or repeats | Wear headphones, and turn off the real microphone in the meeting app. |

## For developers

The repository also contains the Android and iOS ports, which are in development and not released. The Windows app is written in .NET 10 and WPF with no NuGet packages. Folder layout, commands and verification steps are in [windows/README.md](windows/README.md) and [AGENTS.md](AGENTS.md). The Mac app and the Linux app were removed from this repository; their history stays in git.

Website: [tbd.edu.vn](https://tbd.edu.vn).
