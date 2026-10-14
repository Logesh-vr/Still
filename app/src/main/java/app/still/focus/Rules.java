package app.still.focus;

/** Android-free policy math, exercised by the host-side regression suite. */
public final class Rules {
    public static final int DAILY_INSTAGRAM_MINUTES = 60;
    public static final long DAILY_INSTAGRAM_MILLIS = DAILY_INSTAGRAM_MINUTES * 60000L;
    private Rules() {}
    public static long trustedTime(long savedWall, long savedElapsed, int savedBoot,
                                   long wall, long elapsed, int boot) {
        if (savedWall == 0) return wall;
        if (boot == savedBoot && elapsed >= savedElapsed) return savedWall + elapsed - savedElapsed;
        return Math.max(savedWall, wall);
    }
    public static long remaining(long deadline, long now) { return Math.max(0, deadline - now); }
    public static boolean blocked(long lockRemaining, boolean daily, boolean usageAllowed,
                                  boolean clockStable, long used, long allowance) {
        return lockRemaining > 0 || (daily && (!usageAllowed || !clockStable || used >= allowance));
    }
    public static final class UsageCounter {
        private final long start, end;
        private long opened = -1, total;
        private String currentPackage, currentActivity;
        public UsageCounter(long start, long end) { this.start = start; this.end = end; }
        public void resume(String pkg, String activity, long time, boolean target) {
            if (target && pkg != null && pkg.equals(currentPackage) && equal(activity, currentActivity)) return;
            close(time);
            if (target) { opened = time; currentPackage = pkg; currentActivity = activity; }
        }
        public void pause(String pkg, String activity, long time) {
            if (pkg != null && pkg.equals(currentPackage) && equal(activity, currentActivity)) close(time);
        }
        public void screenOff(long time) { close(time); }
        private void close(long time) {
            if (opened >= 0) total += Math.max(0, Math.min(time, end) - Math.max(opened, start));
            opened = -1; currentPackage = null; currentActivity = null;
        }
        private static boolean equal(String a, String b) { return a == null ? b == null : a.equals(b); }
        public long result() { close(end); return total; }
    }
}
