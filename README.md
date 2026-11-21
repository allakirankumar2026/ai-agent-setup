
# J.A.R.V.I.S Android Starter

A beginner native Android starter for voice commands and user-approved accessibility actions.

## Included

- Speech recognition while the app is open
- Text-to-speech replies
- Launch common apps: WhatsApp, YouTube, Gmail, Chrome, Maps, Messages, Settings, Phone
- Home, Back, Recents, scroll up/down
- Accessibility-based tap by visible accessible text

## Build

1. Install Android Studio on a computer.
2. Extract this ZIP and open the `JARVIS_Android_Starter` folder in Android Studio.
3. Allow Gradle sync and install Android SDK 35 if prompted.
4. Connect your Android phone with USB debugging enabled, or pair it wirelessly from Android Studio.
5. Run the `app` configuration.

## Phone setup

1. Open the app and allow microphone permission.
2. Tap **Enable Accessibility Control**.
3. In Android Accessibility settings, enable **J.A.R.V.I.S Mobile**. Read the Android sensitive-permission warning and enable only if you trust this app.
4. Return to the app and tap **Start Listening**.
5. Say a command.

## Commands

- Open WhatsApp / YouTube / Gmail / Chrome / Maps / Settings / Phone / Messages
- Go home / Go back / Recent apps
- Scroll down / Scroll up
- Tap Settings (tries to click a visible accessible item matching the text)

## Limitations and safety

- This is not a true always-on wake-word assistant. Recognition starts while the activity is open; Android limits background microphone use.
- Always-on wake-word behavior needs a supported on-device hotword engine or a properly implemented selected Android assistant (`VoiceInteractionService`) and device-specific testing.
- Accessibility works only with UI elements exposed to accessibility. Secure screens and some apps may block it.
- `QUERY_ALL_PACKAGES` is sensitive and may be restricted on Google Play. For personal sideloading, review the permission and consider a smaller allowlist.
- Do not use accessibility to bypass security, consent, payment, or authentication controls.
- Never hardcode Gemini API keys in the app or public repository.
