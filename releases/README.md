# Current APK

This directory contains only the latest distributed build, **Still 1.1.0**, package `app.still.focus`, version code 2. It is a signed test APK for Android 8.0+.

Download [Still-1.1.0.apk](Still-1.1.0.apk), then follow [installation](../docs/INSTALLATION.md). Compare its SHA-256 against [SHA256SUMS](SHA256SUMS).

The APK uses the original private signing key, verified against the previously distributed build's signing certificate. The key and its passwords are not in this repository.

This APK was assembled directly with installed Android build tools because SDK downloads were incomplete on the build machine. [VALIDATION.md](../docs/VALIDATION.md) records the build method and checks. The repository provides a standard Gradle project for subsequent builds. CI produces debug test artifacts separately and does not replace this signed APK.

Phone installation, permission setup, real Instagram blocking, and manufacturer background behavior remain unverified. Follow the [device checklist](../docs/DEVICE_TESTING.md).
