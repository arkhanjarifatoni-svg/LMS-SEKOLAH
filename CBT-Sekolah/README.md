# Moodle Exam Guard 2.0

Free, ad-free Android examination browser companion for Moodle. Kotlin + Jetpack Compose + Android WebView.

## Features in this build
- Configure a Moodle HTTPS URL on-device.
- Optional comma-separated trusted host allowlist for institutional SSO.
- Internal WebView; no tab UI or general-purpose address bar.
- Blocks non-HTTPS navigation and hosts outside the allowlist.
- Blocks Android Back while the browser is open; use the in-app End button to leave.
- Blocks new windows/popups, file/content URL access, and mixed content.
- Enables Android `FLAG_SECURE` to block ordinary screenshots and screen recording.
- Counts app background/stop events locally on this device.
- Bilingual Indonesian/English labels.

## Important security limitations
This is an early 2.0 browser build, not a certified secure exam environment.
- On personally owned Android phones, a normal app cannot guarantee that Home, Recents, system settings, split-screen, a second device, or external AI use are blocked.
- Full device kiosk/Lock Task Mode requires the device to be provisioned/allowlisted by a Device Policy Controller (typically school-managed dedicated devices).
- Background-event counts are local diagnostics, not tamper-proof evidence. They are not sent to Moodle.
- `FLAG_SECURE` is best-effort Android screenshot/recording protection; it cannot stop a second camera/device.
- Moodle themes, plugins, SSO providers, and versions differ. Add each legitimate SSO hostname to the trusted-host list. Do not add broad or unknown domains.
- JavaScript is enabled because Moodle requires it. No JavaScript-to-native bridge is exposed.
- This build does not yet implement Moodle web-service token authentication, Moodle plugins, remote proctoring, or a teacher monitoring dashboard.

## Build
The repository workflow at `../.github/workflows/build-apk.yml` builds this project from the `CBT-Sekolah` directory. The project uses Java 17 and the existing Gradle 8.13 setup.

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Use
1. Install the APK.
2. Enter the school's Moodle base URL, including `https://`.
3. If the Moodle login redirects to a school SSO host, enter that exact hostname in the optional trusted-host field (comma-separated; hostname only, no scheme or path).
4. Tap Connect.
5. Test login, course pages, quiz attempts, file uploads, and any SSO flow before using in a real exam.

## Privacy
The app stores the configured URL and local background-event counter in private app preferences. It does not send these values to a developer server. Moodle credentials and exam answers are handled by the configured Moodle server through its own web pages.

## License
Apache-2.0. See `LICENSE`.
