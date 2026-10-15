# Still(for my personal use)

An offline Android app that blocks Instagram after **one hour of Instagram use each day**. It also lets you start a separate timed lock whenever you want a break from the app.

Time in other apps does not count toward the Instagram limit. Instagram and Instagram Lite share the daily hour. The daily rule resets at local midnight and starts automatically after permission setup.

[Download Still 1.1.0 APK](https://github.com/Logesh-vr/Still/raw/refs/heads/main/releases/Still-1.1.0.apk) · [Install and enable](docs/INSTALLATION.md) · [Build from source](docs/BUILDING.md)

**Status:** signed test build for Android 8.0+. Compilation, 30 policy assertions, APK packaging, and signatures have been checked. Phone installation and blocking behavior have not been verified. See [validation](docs/VALIDATION.md) and the [device checklist](docs/DEVICE_TESTING.md).

## Behavior

| Rule | When it blocks | When access returns |
| --- | --- | --- |
| Automatic daily limit | Instagram foreground use reaches 60 minutes today | At the next daily reset, if no timed lock remains |
| Manual timed lock | Immediately after starting a lock of 1 minute to 7 days | When the duration ends, if the daily hour remains |

There is no in-app cancellation for an active timed lock, or an off switch/increase for the daily hour. Instagram usage earlier today already counts. Daily usage reads normally refresh about once a second while Instagram is visible; Android event delivery can introduce a delay.

## Setup

Install the APK, open Still, and grant:

- **Accessibility:** detects Instagram's app window and enforces blocking with an overlay and Home action.
- **Usage access:** reads Android usage events to count Instagram foreground time.

Once both are ready, the daily rule runs without starting a timer. Use **Lock Instagram now** for the separate immediate lock. Missing Usage access causes Instagram to block while the accessibility service is enabled. Follow [the setup guide](docs/INSTALLATION.md) for restricted-settings and battery troubleshooting.

## How it works

`UsageReader` processes Android's activity events. `Rules` counts only Instagram sessions and defines the one-hour threshold. `FocusStore` saves deadlines and daily totals privately. `BlockerService` watches app-window changes and blocks Instagram when either rule requires it. `MainActivity` shows usage and controls timed locks.

Read [the architecture and clock behavior](docs/HOW_IT_WORKS.md) for the complete flow, or [the build guide](docs/BUILDING.md#how-java-becomes-an-apk) to see how Java source becomes an APK.

## Project layout

```text
app/
  src/main/
    java/app/still/focus/   App UI, policy, storage, usage reader and blocker
    res/                   Icon, theme, service configuration and text
    AndroidManifest.xml    App identity, permissions and components
docs/                      Build, setup, architecture, privacy and validation
gradle/wrapper/            Pinned Gradle wrapper and distribution checksum
scripts/                   Windows and Unix policy-test runners
tests/                     Android-independent rule and usage tests
releases/                  Current signed APK and SHA-256 checksum
.github/workflows/         Compile, lint, policy and APK integrity checks
```

## Build and test

Use JDK 17+ with Android SDK Platform 36 and Build Tools 36.0.0. The wrapper uses Gradle 8.13 and Android Gradle Plugin 8.13.2. The Java source level is 11. No application library dependencies are required.

Linux/macOS:

```bash
bash scripts/test.sh
./gradlew --no-daemon assembleDebug lintDebug
```

Windows/PowerShell, with `JAVA_HOME` pointing to JDK 17+:

```powershell
.\scripts\test.ps1
.\gradlew.bat --no-daemon assembleDebug lintDebug
```

CI runs these checks on Ubuntu and uploads the generated **debug** APK and lint report. That APK has a different signing identity from the checked-in distribution. For an update over the distributed APK, use the owner's original private signing key. See [signed builds](docs/BUILDING.md#signed-apks-and-updates).

## Privacy and limits

Still has no internet permission, account, backend, ads, or analytics. It does not request screen-content retrieval or gesture capability. Only deadlines, settings, aggregate Instagram time, and clock-protection state are stored privately. [Privacy details](docs/PRIVACY.md)

Android Settings can disable permissions, force-stop Still, clear its data, or uninstall it. This APK is not tamper-proof. Browser Instagram, cloned apps, and other Android profiles are outside this version. Battery restrictions can interrupt the service. A deliberate clock change before reboot can bypass local clock safeguards.

The app targets the official Instagram package names and does not intentionally block calls, Settings, or other apps. Verify behavior on your phone before relying on it.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development checks and bug-report details, and [SECURITY.md](SECURITY.md) for security reporting.
