# Device verification checklist

The policy tests run on a computer. Accessibility, usage-event timing, permission setup, and manufacturer battery behavior need testing on a real phone or emulator. None of those device flows has been verified for the checked-in APK.

Record the phone model, Android version, APK version, and service/permission status when testing.

| Check | Expected result |
| --- | --- |
| Grant Accessibility and Usage access | Still shows an active one-hour rule or that today's hour is already exhausted |
| Spend time in unrelated apps | Their time does not consume the Instagram hour |
| Use Instagram across several sessions | Today’s Instagram usage accumulates across sessions |
| Switch between Instagram and Lite | Both consume the same daily hour |
| Reach 60 minutes of Instagram usage | Instagram blocks until the daily reset, allowing for system-event/check delay |
| Open blocked Instagram from its icon, a notification, a link, or Recents | Overlay or Home action prevents continued use |
| Start a short timed lock before exhausting the daily hour | Instagram blocks immediately |
| Let that timed lock expire | Instagram opens only if the daily hour is not exhausted |
| Close Still during a lock | The stored lock remains active while the service is connected |
| Reboot during a longer lock | Saved state remains; confirm the accessibility service reconnects and blocking resumes |
| Lock the screen with Instagram open | Screen-off time is excluded from usage counting |
| Reach midnight during an Instagram session | The new day counts only the part of the session after the reset |
| Revoke Usage access with Accessibility still enabled | Instagram blocks until usage access returns |
| Change the clock within a boot | The manual lock does not shorten; a large wall-clock shift locks the daily rule |
| Open calls, other apps, and Settings | They remain accessible; dismiss Still's block overlay using its home button if shown |
| Start the 24-hour lock | Instagram remains blocked for 24 hours from confirmation, unless Still or its permissions are removed |
| Uninstall Still or clear its data during a lock | The lock no longer persists; verify documentation describes this Android limitation |

Do not count a compilation result, a screenshot, or the host-side policy suite as evidence that these device checks passed. Report failures with [the contribution guide](../CONTRIBUTING.md).
