package app.still.focus;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Process;

public final class UsageReader {
    private UsageReader() {}
    public static boolean isInstagram(String pkg) {
        return "com.instagram.android".equals(pkg) || "com.instagram.lite".equals(pkg);
    }
    public static boolean allowed(Context context) {
        AppOpsManager ops = context.getSystemService(AppOpsManager.class);
        return ops != null && ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }
    public static long read(Context context, long start, long end) {
        return snapshot(context, start, end).used;
    }
    public static final class Snapshot {
        public final long used;
        public final String foreground;
        Snapshot(long used, String foreground) { this.used = used; this.foreground = foreground; }
    }
    public static Snapshot snapshot(Context context, long start, long end) {
        UsageStatsManager manager = context.getSystemService(UsageStatsManager.class);
        if (manager == null || !allowed(context)) return new Snapshot(-1, null);
        try {
            // Look back to capture an Instagram session spanning midnight.
            UsageEvents events = manager.queryEvents(start - 86400000L, end);
            if (events == null) return new Snapshot(-1, null);
            Rules.UsageCounter counter = new Rules.UsageCounter(start, end);
            UsageEvents.Event event = new UsageEvents.Event();
            String foreground = null, foregroundActivity = null;
            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                int type = event.getEventType();
                String pkg = event.getPackageName(); long time = event.getTimeStamp();
                if (type == UsageEvents.Event.ACTIVITY_RESUMED) {
                    counter.resume(pkg, event.getClassName(), time, isInstagram(pkg));
                    foreground = pkg; foregroundActivity = event.getClassName();
                } else if (type == UsageEvents.Event.ACTIVITY_PAUSED || type == UsageEvents.Event.ACTIVITY_STOPPED) {
                    counter.pause(pkg, event.getClassName(), time);
                    if (same(pkg, foreground) && same(event.getClassName(), foregroundActivity)) {
                        foreground = null; foregroundActivity = null;
                    }
                } else if (type == UsageEvents.Event.SCREEN_NON_INTERACTIVE || type == UsageEvents.Event.DEVICE_SHUTDOWN) {
                    counter.screenOff(time);
                    foreground = null; foregroundActivity = null;
                }
            }
            return new Snapshot(counter.result(), foreground);
        } catch (RuntimeException denied) { return new Snapshot(-1, null); }
    }
    private static boolean same(String a, String b) { return a == null ? b == null : a.equals(b); }
}
