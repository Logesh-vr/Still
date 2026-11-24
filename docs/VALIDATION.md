# Validation of Still

## Still 1.3.0 update

Built on 2026-11-24. Package `app.still.focus`, version code 4, minimum Android API 26, target API 36.

- Java sources compiled for Java 11 and D8 produced Android DEX using the locally cached Android toolchain.
- The signed APK reports version 1.3.0 and passes Android APK signature verification for v2 and v3.
- The update APK was verified against Still 1.2.0; both signatures use the same certificate.
- SHA-256: `B4FAA7F3A38EA18E34E1D1C9DA95EF0F1DA3C7BB11B7E2EB74B705B608C12D6E`.
- The existing 30-assertion policy suite was not rerun for this update.
- No phone or emulator was available to verify the 24-hour lock on-device. The feature uses the existing timed-lock storage and enforcement. Removing Still or clearing its data still removes the lock.

Android's [uninstall-blocking policy](https://developer.android.com/reference/android/app/admin/DevicePolicyManager) requires device/profile-owner privileges. The standard Still APK does not enroll as a device owner, so it cannot block the user from uninstalling Still.

## Still 1.2.0 update

Built on 2026-11-19. Package `app.still.focus`, version code 3, minimum Android API 26, target API 36.

- Java sources compiled for Java 11 and D8 produced Android DEX using the locally cached Android toolchain.
- The signed APK reports version 1.2.0 and passes Android APK signature verification for v2 and v3.
- The update APK was verified against Still 1.1.0; both signatures use the same certificate.
- SHA-256: `58C55AB46E7EA900DBF5CA7F445F5BD60F3CD92DEB23417CA4B9D637E7696C11`.
- No phone or emulator was available to verify payment-app launch, the Accessibility pause in Settings, or restoring the service after payment. Follow the updated [device checklist](DEVICE_TESTING.md).

Android provides `AccessibilityService.disableSelf()` for a service to stop itself. A regular third-party app cannot re-enable that service; Android's secure settings are user/system-controlled. The payment flow is therefore an explicit pause followed by a manual re-enable, not an automatic switch based on payment-app launch or transaction completion. See [Android's service lifecycle](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService#disableSelf()) and [secure settings](https://developer.android.com/reference/android/provider/Settings.Secure).

## Still 1.1.0 validation

Built on 2026-10-14. Package `app.still.focus`, version code 2, minimum Android API 26 (Android 8.0), target API 36.

## Passed

- All application Java sources compiled with Java 11 language/API compatibility against the locally cached Android API signatures.
- D8 compiled the application to Android DEX with minimum API 26. The packaged DEX contains the application classes and desugared callbacks.
- AAPT2 compiled the resources and linked the manifest against Android 36 framework resource data.
- Every stored APK entry, including `resources.arsc`, was checked for 4-byte alignment.
- APK signing verification passed for Android signature schemes v2 and v3, with the verifier checking from minimum platform API 26.
- The packaged manifest was decoded and inspected: correct launcher activity, exported accessibility service protected by `BIND_ACCESSIBILITY_SERVICE`, usage access permission, backup disabled, no internet permission.
- The packaged accessibility configuration was decoded and inspected: app window events only, no window-content retrieval capability, no gesture capability.
- The original and updated APKs were verified to use the same signing certificate, allowing an in-place update.
- 30 host-side assertions passed for the exact one-hour Instagram limit, excluding time in other apps, retaining independent timed locks, timer expiry, forward/backward clock changes, normal reboot clock recovery, timer/daily rule precedence, usage-permission loss, clock-change blocking, usage across midnight, duplicate events, app switching, screen off, outgoing activity events, and shared Instagram/Lite allowance.

APK SHA-256:

```text
2E1F4E91547845DD26D83E3A1E20E14131FEB1F3AB8962B004C18A24EB89EB61
```

## Build method

The machine had Android Studio's Java runtime, cached Android API signatures, AAPT2, D8, and APK signing libraries, but no usable installed Android SDK. Full SDK downloads repeatedly stalled. This APK was therefore packaged directly with those installed tools and Android 36 framework resource data extracted from the SDK jar mirror, rather than running Gradle's `assembleDebug` task. The provided project is a conventional Gradle Android project for subsequent Android Studio builds.

Tools: JetBrains OpenJDK 21.0.8, AAPT2 2.20-14304508, D8 from Android Studio's bundled R8, and Android apksig 8.13.2. A dedicated local RSA signing key is backed up separately from the source and APK.

## Not verified here

No running Android emulator or connected phone was available. Installation, UI rendering on a phone, permission setup, Instagram interception, real UsageStats event timing, overnight reset, actual reboot behavior, and manufacturer battery handling were not tested on a device. The initial build was checked without Android lint. Publication adds a GitHub Actions Gradle build and lint workflow; its first run compiled successfully and found a navigation-bar style setting requiring API 27. The current source and signed APK scope that setting to Android 8.1+ and use a contrasting navigation bar on Android 8.0. Check the current workflow run for the final lint result. The policy suite tests the Android-independent math and event reducer; it does not substitute for device tests of accessibility or persistent storage.

The README includes device checks to perform before relying on enforcement. Treat this as a signed first-version test APK, not a device-validated production release.
