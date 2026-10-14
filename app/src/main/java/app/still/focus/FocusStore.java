package app.still.focus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import java.util.Calendar;

public final class FocusStore {
    private final Context context;
    private final SharedPreferences prefs;
    public FocusStore(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("focus", Context.MODE_PRIVATE);
    }
    public synchronized long now() {
        long wall = System.currentTimeMillis(), elapsed = SystemClock.elapsedRealtime();
        int boot = Settings.Global.getInt(context.getContentResolver(), Settings.Global.BOOT_COUNT, -1);
        long trusted = Rules.trustedTime(prefs.getLong("wall", 0), prefs.getLong("elapsed", 0),
                prefs.getInt("boot", -2), wall, elapsed, boot);
        prefs.edit().putLong("wall", trusted).putLong("elapsed", elapsed).putInt("boot", boot).apply();
        return trusted;
    }
    public long lockRemaining() { return Rules.remaining(prefs.getLong("lockUntil", 0), now()); }
    public long lockDuration() { return prefs.getLong("lockDuration", 1); }
    public boolean startLock(int minutes) {
        if (minutes < 1 || minutes > 10080 || lockRemaining() > 0) return false;
        long duration = minutes * 60000L;
        return prefs.edit().putLong("lockUntil", now() + duration).putLong("lockDuration", duration).commit();
    }
    public void rollDay() {
        long time = now();
        long end = prefs.getLong("dayEnd", 0);
        if (end != 0 && time < end) return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis(); cal.add(Calendar.DAY_OF_MONTH, 1);
        prefs.edit().putLong("dayStart", start).putLong("dayEnd", cal.getTimeInMillis())
                .putLong("used", 0).putBoolean("clockChanged", false).apply();
    }
    // This rule is always active, including upgrades from the optional-allowance version.
    // Old budget/nextBudget preferences cannot disable or relax the one-hour cap.
    public int budget() { rollDay(); return Rules.DAILY_INSTAGRAM_MINUTES; }
    public long dayStart() { rollDay(); return prefs.getLong("dayStart", 0); }
    public long dayEnd() { rollDay(); return prefs.getLong("dayEnd", 0); }
    public boolean clockStable() {
        rollDay();
        if (Math.abs(System.currentTimeMillis() - now()) > 120000)
            prefs.edit().putBoolean("clockChanged", true).apply();
        return !prefs.getBoolean("clockChanged", false);
    }
    public long used() { rollDay(); return prefs.getLong("used", 0); }
    public void recordUsage(long usage) {
        rollDay();
        if (usage > used()) prefs.edit().putLong("used", usage).apply();
    }
}
