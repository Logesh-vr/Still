# Security and reporting

Still is a voluntary personal blocker. Android Settings can disable permissions, clear its data, force-stop it, or uninstall it. Those user controls are part of the documented design, not a claim of tamper-proof enforcement.

The app's accessibility service requires Android's `BIND_ACCESSIBILITY_SERVICE` permission. Screen-content retrieval and gesture capabilities are not requested. The current manifest has no internet permission.

For a suspected security or privacy issue, use GitHub's private vulnerability reporting option if available on this repository. Otherwise, contact the repository owner through an existing private channel. Do not publish credentials, sensitive phone data, or working exploit details in a public issue.

Ordinary blocking failures can be reported through the issue tracker with the information requested in [CONTRIBUTING.md](CONTRIBUTING.md).

The signing key is retained privately by the owner. Only the signed APK, its public checksum, and build/validation information are distributed here. Signature and checksum checks verify package identity and integrity; device behavior must still be tested.
