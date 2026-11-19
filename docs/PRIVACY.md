# Privacy

Still runs on the phone and has no internet permission, account system, analytics, advertising, or backend.

## What it reads

- App-window package names from the accessibility event stream, to detect Instagram.
- Android usage events after Usage access is granted, to calculate Instagram foreground time.
- The local clock, elapsed time, and boot counter to maintain deadlines.

Android usage-event queries can return events for other apps. Still uses those transitions to determine when Instagram is no longer in the foreground; only Instagram and Instagram Lite add to the daily total. Those event records are processed in memory and are not saved or uploaded.

## What it saves

Private SharedPreferences store timed-lock deadlines, the current daily window, aggregate daily Instagram usage, clock-protection state, and whether the user paused Still for payment access. Backup is disabled.

The accessibility configuration does not request window-content retrieval or gesture capability. Still does not read messages, passwords, screenshots, screen text, or notification contents.

## User control

Android Settings can revoke either permission. Clearing app storage or uninstalling Still removes its saved rules and usage totals. Disabling these permissions affects enforcement as described in [how the app works](HOW_IT_WORKS.md).

The GitHub Actions workflow builds the repository and produces test artifacts. It receives source files and build output, not usage data from phones.
