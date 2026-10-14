# Contributing

Keep changes focused on the automatic one-hour Instagram rule, the independent timed lock, permission setup, or their reliability. The app intentionally has no account, backend, ads, or analytics.

## Development

1. Follow [the build guide](docs/BUILDING.md).
2. Run the host policy suite and `assembleDebug lintDebug`.
3. For changes to blocking or usage accounting, perform the relevant [device checks](docs/DEVICE_TESTING.md).
4. Describe the changed behavior and the checks actually run in your pull request.

Use clear commit messages and keep documentation consistent with the final code. Java uses four-space indentation. The repository's EditorConfig and Git attributes define whitespace and line-ending conventions.

## Bug reports

Include the APK version, Android version, phone model, permission status, steps to reproduce, expected behavior, and observed behavior. For a daily-limit bug, include the approximate displayed Instagram usage and when the issue occurred.

Do not post signing keys, passwords, private messages, or unfiltered phone logs. Explain device testing accurately; tests that have not been run should remain marked as unverified.

## Distribution files

Only the reviewed current APK is checked into `releases/`. Generated debug APKs stay in ignored build folders or CI artifacts. If a new signed version replaces the current distribution, update its filename, version settings, checksum, validation record, download link, and explicit APK allowlist in `.gitignore` together.

The Gradle wrapper JAR is a build prerequisite and is tracked. Private signing material and machine-specific SDK configuration are never part of a contribution.
