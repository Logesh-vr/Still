package app.still.focus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private FocusStore store;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private LinearLayout root, durationRow;
    private TextView status, setupDetail, dailyDetail, dailyMeta, lockDetail, selectedLabel;
    private Button accessibilityButton, usageButton, startButton;
    private TimerView timer;
    private int selectedMinutes = 60;
    private boolean querying, resumed;
    private long lastQuery;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); store = new FocusStore(this);
        if (state != null) selectedMinutes = state.getInt("minutes", 60);
        build(); render();
    }
    @Override public void onSaveInstanceState(Bundle state) { state.putInt("minutes", selectedMinutes); super.onSaveInstanceState(state); }
    private void build() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(Ui.CREAM);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(24), dp(24), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2)); setContentView(scroll);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        TextView wordmark = Ui.text(this, "II   still", 22, Ui.GREEN);
        wordmark.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); add(root, wordmark, 0);
        status = Ui.text(this, "", 13, Ui.MUTED); add(root, status, 12);
        timer = new TimerView(this);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(-1, dp(245));
        timerParams.setMargins(0, dp(20), 0, dp(12)); root.addView(timer, timerParams);

        LinearLayout lockCard = card();
        add(lockCard, heading("Lock Instagram now"), 0);
        lockDetail = Ui.text(this, "Choose a duration. No early release.", 14, Ui.MUTED); add(lockCard, lockDetail, 8);
        durationRow = new LinearLayout(this); durationRow.setOrientation(LinearLayout.HORIZONTAL);
        add(durationRow, choice("30m", 30), 0); add(durationRow, choice("1h", 60), 0); add(durationRow, choice("3h", 180), 0);
        Button custom = Ui.button(this, "Custom", () -> numberDialog("Lock duration", "Minutes · 1 to 10,080 (7 days)", selectedMinutes,
                1, 10080, value -> { selectedMinutes = value; render(); }), false);
        custom.setTextSize(12); custom.setPadding(dp(4), dp(8), dp(4), dp(8));
        durationRow.addView(custom, weighted()); add(lockCard, durationRow, 18);
        selectedLabel = Ui.text(this, "", 13, Ui.GREEN); add(lockCard, selectedLabel, 10);
        startButton = Ui.button(this, "Start a 1-hour lock", this::confirmLock, true); add(lockCard, startButton, 16);

        LinearLayout dailyCard = card();
        add(dailyCard, heading("Instagram · 1 hour per day"), 0);
        dailyDetail = Ui.text(this, "", 20, Ui.INK); add(dailyCard, dailyDetail, 12);
        dailyMeta = Ui.text(this, "", 14, Ui.MUTED); add(dailyCard, dailyMeta, 8);
        add(dailyCard, Ui.text(this, "Automatic. No need to start a timer.", 14, Ui.GREEN), 12);

        LinearLayout setupCard = card();
        add(setupCard, heading("Make the blocker ready"), 0);
        setupDetail = Ui.text(this, "", 14, Ui.MUTED); add(setupCard, setupDetail, 8);
        accessibilityButton = Ui.button(this, "Enable Instagram blocking", this::showDisclosure, false); add(setupCard, accessibilityButton, 14);
        usageButton = Ui.button(this, "Allow daily usage counting", () -> openSettings(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS,
                Uri.parse("package:" + getPackageName()))), false); add(setupCard, usageButton, 10);
        Button help = Ui.button(this, "Setup help & limits", this::showHelp, false); add(setupCard, help, 10);
        TextView footer = Ui.text(this, "Instagram + Instagram Lite · offline · no account", 12, Ui.MUTED);
        footer.setGravity(Gravity.CENTER); add(root, footer, 24);
    }
    private LinearLayout.LayoutParams weighted() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(52), 1);
        params.setMargins(dp(3), 0, dp(3), 0); return params;
    }
    private Button choice(String label, int minutes) {
        Button button = Ui.button(this, label, () -> { selectedMinutes = minutes; render(); }, false);
        button.setTextSize(14); button.setTag(minutes); button.setLayoutParams(weighted()); return button;
    }
    private int dp(int value) { return Ui.dp(this, value); }
    private TextView heading(String value) {
        TextView heading = Ui.text(this, value, 18, Ui.INK);
        heading.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL)); return heading;
    }
    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(20), dp(20), dp(20)); layout.setBackground(Ui.background(this, Ui.WHITE, 24, true));
        add(root, layout, 14); return layout;
    }
    private void add(LinearLayout parent, View child, int top) {
        LinearLayout.LayoutParams params = child.getLayoutParams() instanceof LinearLayout.LayoutParams
                ? (LinearLayout.LayoutParams) child.getLayoutParams() : new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(top); parent.addView(child, params);
    }
    private boolean accessibilityEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        String component = new ComponentName(this, BlockerService.class).flattenToString();
        if (enabled == null) return false;
        for (String item : enabled.split(":")) if (component.equalsIgnoreCase(item)) return true;
        return false;
    }
    private boolean ready() { return accessibilityEnabled() && BlockerService.connected(); }
    private void render() {
        long remaining = store.lockRemaining(); int budget = store.budget();
        boolean ready = ready(), usage = UsageReader.allowed(this);
        boolean blocked = Rules.blocked(remaining, budget > 0, usage, budget == 0 || store.clockStable(), store.used(), budget * 60000L);
        status.setText(!ready ? "SETUP NEEDED · blocking is inactive" : !usage ? "SETUP NEEDED · allow Instagram usage access" : blocked ? "INSTAGRAM LOCKED" : "AUTOMATIC 1-HOUR LIMIT ACTIVE");
        status.setTextColor(ready ? Ui.GREEN : Ui.MUTED);
        if (remaining > 0) timer.update(formatTime(remaining), ready ? "left in your Instagram lock" : "lock saved · enable blocker", remaining / (float)store.lockDuration());
        else if (!ready || !usage) timer.update("Finish setup", "enable blocking + usage access", 1);
        else if (blocked) timer.update("Locked for today", "Instagram resets at midnight", 1);
        else timer.update(formatTime(Math.max(0, Rules.DAILY_INSTAGRAM_MILLIS - store.used())),
                "Instagram time left today", Math.max(0, Rules.DAILY_INSTAGRAM_MILLIS - store.used()) / (float) Rules.DAILY_INSTAGRAM_MILLIS);
        lockDetail.setText(remaining > 0 ? "Your lock is active. It ends automatically." : "Choose a duration. No early release.");
        durationRow.setVisibility(remaining > 0 ? View.GONE : View.VISIBLE);
        selectedLabel.setVisibility(remaining > 0 ? View.GONE : View.VISIBLE);
        selectedLabel.setText("Selected: " + humanDuration(selectedMinutes));
        for (int i = 0; i < durationRow.getChildCount(); i++) {
            View child = durationRow.getChildAt(i); Object tag = child.getTag();
            boolean chosen = tag instanceof Integer && ((Integer)tag) == selectedMinutes;
            child.setBackground(Ui.background(this, chosen ? Ui.GREEN : Ui.WHITE, 14, !chosen));
            ((Button)child).setTextColor(chosen ? Ui.WHITE : Ui.GREEN);
        }
        startButton.setEnabled(remaining == 0 && ready);
        startButton.setAlpha(startButton.isEnabled() ? 1 : .55f);
        startButton.setText(remaining > 0 ? "Lock active · no early release" : !ready ? "Enable blocking first" : "Lock for " + humanDuration(selectedMinutes));
        dailyDetail.setText(!usage ? "Usage access needed" : formatTime(store.used()) + " used / 1 hour");
        if (!usage) dailyMeta.setText("Allow usage access to read Instagram's screen time. With blocking enabled, Instagram stays locked while usage access is missing.");
        else if (!store.clockStable()) dailyMeta.setText("Clock changed. The daily rule stays locked until its original reset.");
        else dailyMeta.setText("At 60 minutes of Instagram use, it blocks automatically until "
                + android.text.format.DateFormat.format("EEE h:mm a", store.dayEnd())
                + ".\nOnly Instagram + Instagram Lite count. Other apps do not.\nIncludes Instagram use earlier today.");
        setupDetail.setText(ready ? "Instagram blocking is connected. " + (usage ? "Your automatic one-hour daily limit is active." : "Enable usage access for the automatic one-hour Instagram limit.")
                : accessibilityEnabled() ? "Permission is on, but the blocker is reconnecting. If this persists, toggle Still off and on in Accessibility."
                : "Enable Still in Accessibility. It only checks app names and sends you home when Instagram is locked.");
        accessibilityButton.setText(ready ? "Blocking enabled · open settings" : "Enable Instagram blocking");
        usageButton.setText(usage ? "Usage access enabled · open settings" : "Allow daily usage counting");
        if (resumed && budget > 0 && usage && !querying && android.os.SystemClock.elapsedRealtime() - lastQuery > 2000) {
            querying = true; lastQuery = android.os.SystemClock.elapsedRealtime();
            long start = store.dayStart();
            worker.execute(() -> {
                long used = UsageReader.read(this, start, System.currentTimeMillis());
                handler.post(() -> {
                    querying = false;
                    if (!isDestroyed() && store.dayStart() == start && used >= 0) { store.recordUsage(used); if (resumed) render(); }
                });
            });
        }
    }
    private void confirmLock() {
        new AlertDialog.Builder(this).setTitle("Lock Instagram for " + humanDuration(selectedMinutes) + "?")
                .setMessage("The lock starts immediately. There is no pause or cancel button. Closing Still will not clear it.\n\nAndroid Settings can still disable the blocker.")
                .setNegativeButton("Not yet", null).setPositiveButton("Start lock", (dialog, which) -> {
                    if (!ready()) { render(); return; }
                    if (!store.startLock(selectedMinutes)) error("The lock could not be saved. Try again.");
                    BlockerService.refresh(); render();
                }).show();
    }
    private interface NumberAction { void run(int value); }
    private void numberDialog(String title, String description, int initial, int min, int max, NumberAction action) {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(24), dp(8), dp(24), 0);
        add(form, Ui.text(this, description, 14, Ui.MUTED), 0);
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setSingleLine(true);
        input.setText(String.valueOf(initial)); input.setSelectAllOnFocus(true); input.setContentDescription("Minutes"); add(form, input, 12);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(title).setView(form).setNegativeButton("Cancel", null).setPositiveButton("Continue", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                int value = Integer.parseInt(input.getText().toString().trim());
                if (value < min || value > max) { input.setError("Enter " + min + "–" + max + " minutes"); return; }
                dialog.dismiss(); action.run(value);
            } catch (NumberFormatException invalid) { input.setError("Enter a number of minutes"); }
        })); dialog.show();
    }
    private void showDisclosure() {
        new AlertDialog.Builder(this).setTitle("How Still blocks Instagram")
                .setMessage("Still uses Android's Accessibility service to detect app window changes, cover Instagram, and send you to the home screen when a rule is active.\n\nIt does not retrieve screen content, read messages, type, or perform taps. It has no internet permission and uploads nothing.\n\nOn the next screen, find Still and enable it. You can revoke the permission in Android Settings.")
                .setNegativeButton("Cancel", null).setPositiveButton("Open Accessibility", (d, w) -> openSettings(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))).show();
    }
    private void showHelp() {
        new AlertDialog.Builder(this).setTitle("Setup & honest limits")
                .setMessage("1. Enable Still in Settings → Accessibility → Installed / Downloaded apps.\n\n2. For daily allowances, allow Still under Usage access.\n\nIf Accessibility is restricted after installing the APK: Settings → Apps → Still → menu → Allow restricted settings, then try again. Menu names vary by phone.\n\nIf your phone stops the service, allow background activity for Still in its battery settings.\n\nStill blocks the official Instagram and Instagram Lite apps in the current Android profile. Browser Instagram, cloned apps, and other profiles are outside this version.\n\nAndroid lets you disable permissions, force-stop, clear app data, or uninstall Still. It cannot be tamper-proof on a normal phone. Same-boot clock changes do not shorten timed locks; daily access locks if the clock shifts. Changing time before reboot can bypass rules.\n\nNo screen content is retrieved. No analytics, ads, account, or network connection.")
                .setNeutralButton("App settings", (d, w) -> openSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()))))
                .setPositiveButton("Got it", null).show();
    }
    private void openSettings(Intent intent) {
        try { startActivity(intent); } catch (android.content.ActivityNotFoundException missing) {
            try { startActivity(new Intent(Settings.ACTION_SETTINGS)); } catch (android.content.ActivityNotFoundException unavailable) { error("Open Android Settings manually."); }
        }
    }
    private void error(String message) { new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", null).show(); }
    public static String formatTime(long millis) {
        long seconds = (millis + 999) / 1000;
        if (seconds >= 3600) return String.format(Locale.getDefault(), "%d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60);
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }
    private static String humanDuration(int minutes) {
        if (minutes % 1440 == 0) return (minutes / 1440) + (minutes == 1440 ? " day" : " days");
        if (minutes % 60 == 0) return (minutes / 60) + (minutes == 60 ? " hour" : " hours");
        return minutes + " minutes";
    }
    private final Runnable tick = new Runnable() { @Override public void run() { if (resumed) { render(); handler.postDelayed(this, 1000); } } };
    @Override public void onResume() { super.onResume(); resumed = true; handler.post(tick); }
    @Override public void onPause() { resumed = false; handler.removeCallbacks(tick); super.onPause(); }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null); worker.shutdownNow(); super.onDestroy(); }
}
