package app.still.focus;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BlockerService extends AccessibilityService {
    private static volatile BlockerService instance;
    private FocusStore store;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean querying, instagramVisible, overlayVisible, usageVerified;
    private long lastQuery, lastHome, foregroundVersion;
    private LinearLayout overlay;
    private TextView overlayDetail;

    public static boolean connected() { return instance != null; }
    public static boolean pauseForPayment() {
        BlockerService service = instance;
        if (service == null) return false;
        service.handler.post(service::disableSelf);
        return true;
    }
    public static void refresh() {
        BlockerService service = instance;
        if (service != null) service.handler.post(service::check);
    }
    @Override protected void onServiceConnected() {
        store = new FocusStore(this); instance = this;
        handler.post(tick);
    }
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            check();
            handler.postDelayed(this, 500);
        }
    };
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (store == null || event.getPackageName() == null) return;
        foregroundVersion++;
        String pkg = event.getPackageName().toString();
        if (UsageReader.isInstagram(pkg)) {
            instagramVisible = true;
            check();
        } else if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && !"com.android.systemui".equals(pkg) && !getPackageName().equals(pkg)) {
            instagramVisible = false;
        } else if (getPackageName().equals(pkg) && !overlayVisible) instagramVisible = false;
    }
    private void check() {
        if (store == null) return;
        PowerManager power = getSystemService(PowerManager.class);
        if (power != null && !power.isInteractive()) { instagramVisible = false; hideOverlay(); }
        int budget = store.budget();
        long elapsed = SystemClock.elapsedRealtime();
        if (budget > 0 && UsageReader.allowed(this) && !querying
                && elapsed - lastQuery >= (instagramVisible ? 1000 : 10000)) {
            querying = true; lastQuery = elapsed;
            long queriedForegroundVersion = foregroundVersion;
            long start = store.dayStart(), end = System.currentTimeMillis();
            worker.execute(() -> {
                UsageReader.Snapshot snapshot = UsageReader.snapshot(this, start, end);
                handler.post(() -> {
                    querying = false;
                    if (instance != this || store.dayStart() != start) return;
                    usageVerified = snapshot.used >= 0;
                    if (usageVerified) {
                        store.recordUsage(snapshot.used);
                        PowerManager currentPower = getSystemService(PowerManager.class);
                        if (foregroundVersion == queriedForegroundVersion && UsageReader.isInstagram(snapshot.foreground)
                                && currentPower != null && currentPower.isInteractive())
                            instagramVisible = true;
                    }
                    if (instagramVisible && shouldBlock()) block();
                });
            });
        }
        if (instagramVisible && shouldBlock()) block();
        if (overlayVisible) {
            if (!shouldBlock()) hideOverlay();
            else overlayDetail.setText(reason());
        }
    }
    private boolean shouldBlock() {
        int budget = store.budget();
        return Rules.blocked(store.lockRemaining(), budget > 0, UsageReader.allowed(this) && usageVerified,
                budget == 0 || store.clockStable(), store.used(), budget * 60000L);
    }
    private String reason() {
        long remaining = store.lockRemaining();
        if (remaining > 0) return MainActivity.formatTime(remaining) + " left in your lock";
        if (!UsageReader.allowed(this)) return "Restore usage access in Still. Your daily rule stays active.";
        if (!usageVerified) return "Checking today's usage. Your daily rule stays active.";
        if (!store.clockStable()) return "Clock changed. Your daily rule stays locked until reset.";
        return "You've used your 1 hour of Instagram today. Access returns at "
                + android.text.format.DateFormat.format("h:mm a", store.dayEnd()) + ".";
    }
    private void block() {
        if (!overlayVisible) showOverlay();
        long elapsed = SystemClock.elapsedRealtime();
        if (elapsed - lastHome > 800) {
            lastHome = elapsed;
            performGlobalAction(GLOBAL_ACTION_HOME);
        }
    }
    private void showOverlay() {
        overlay = new LinearLayout(this);
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28), Ui.dp(this, 28));
        overlay.setBackgroundColor(Ui.CREAM);
        TextView mark = Ui.text(this, "II", 56, Ui.GREEN);
        mark.setGravity(Gravity.CENTER); overlay.addView(mark);
        TextView title = Ui.text(this, "Instagram is locked.", 30, Ui.INK);
        title.setGravity(Gravity.CENTER); overlay.addView(title);
        overlayDetail = Ui.text(this, reason(), 18, Ui.MUTED);
        overlayDetail.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(-1, -2);
        detailParams.setMargins(0, Ui.dp(this, 22), 0, Ui.dp(this, 32));
        overlay.addView(overlayDetail, detailParams);
        overlay.addView(Ui.button(this, "Back to life", () -> {
            performGlobalAction(GLOBAL_ACTION_HOME); instagramVisible = false; hideOverlay();
        }, true), new LinearLayout.LayoutParams(-1, Ui.dp(this, 58)));
        TextView note = Ui.text(this, "You made this space for yourself.", 14, Ui.MUTED);
        note.setGravity(Gravity.CENTER); note.setPadding(0, Ui.dp(this, 20), 0, 0); overlay.addView(note);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(-1, -1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.OPAQUE);
        params.gravity = Gravity.TOP | Gravity.START;
        try {
            getSystemService(WindowManager.class).addView(overlay, params);
            overlayVisible = true;
        } catch (RuntimeException unavailable) {
            overlay = null;
            android.widget.Toast.makeText(this, "Instagram is locked. " + reason(), android.widget.Toast.LENGTH_SHORT).show();
        }
    }
    private void hideOverlay() {
        if (overlay != null) {
            try { getSystemService(WindowManager.class).removeView(overlay); } catch (RuntimeException ignored) { }
        }
        overlay = null; overlayVisible = false;
    }
    @Override public void onInterrupt() { }
    @Override public void onDestroy() {
        instance = null; handler.removeCallbacksAndMessages(null);
        hideOverlay(); worker.shutdownNow(); super.onDestroy();
    }
}
