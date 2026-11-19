# Install and enable Still

Download [Still 1.2.0](../releases/Still-1.2.0.apk). Android 8.0 or newer is required. This is a signed test build; verify blocking on your phone before relying on it.

1. Open the APK on your phone and allow installation from the browser or file manager when Android asks.
2. Open Still and tap **Enable Instagram blocking**. Read the disclosure, then enable **Still · Instagram blocker** under Accessibility → Installed/Downloaded apps.
3. Tap **Allow daily usage counting** and enable Still under Usage access.
4. Return to Still. When both permissions are ready and the daily hour is not exhausted, the status reads **AUTOMATIC 1-HOUR LIMIT ACTIVE**.

The one-hour rule starts automatically after setup. You do not need to start a timed lock to activate it. Use the separate **Lock Instagram now** control for an immediate lock of 1 minute to 7 days.

Instagram use earlier today already counts. If it has reached one hour, Instagram will block as soon as Still reads that usage. Removing Usage access causes blocking while Accessibility remains enabled.

## Restricted settings after installing an APK

Some Android versions restrict accessibility permissions for sideloaded apps. If enabling Still is refused, check Settings → Apps → Still → the menu → **Allow restricted settings**, then return to Accessibility. Options and menu names vary by phone. See [Android's restricted-settings guidance](https://support.google.com/android/answer/12623953?hl=en).

## Background operation

If the phone stops Still's accessibility service, allow background activity for Still in battery settings. Return to Still and check the connection status. If the permission is enabled but the service stays disconnected, toggle Still off and on in Accessibility.

Confirm service connection after a reboot. Phone manufacturers differ in how they manage background apps.

## Opening payment apps

Some payment apps reject an active Accessibility service. Before paying, open Still and tap **Pause Still for a payment**, then confirm. Still disables its own service so the payment app can open. After payment, return to Still, tap **Re-enable Still after payment**, and turn Still back on in Android Accessibility settings. The switch back on must be manual: Android only allows the user or system to enable an Accessibility service. A normal app cannot restore it automatically.

## Updates

The checked-in 1.2.0 APK uses the same signing certificate as the previously distributed build. Installing it as an update preserves saved timed-lock state. The current rule is always 60 minutes per day; older configurable allowance settings are no longer used.

A debug APK built by CI or your local Gradle installation uses a different signing key. It cannot install directly over the signed distribution. Build with the owner's original signing key for an in-place update.

## Checksums

Compare the downloaded APK's SHA-256 against [SHA256SUMS](../releases/SHA256SUMS). The checksum verifies which bytes you downloaded; it does not prove correct blocking behavior.

On Windows, use `Get-FileHash` with `-Algorithm SHA256`. On Linux, run `sha256sum -c releases/SHA256SUMS` from the repository root. On macOS, use `shasum -a 256` on the APK and compare the result.
