package app.still.focus;

/** No Android dependencies; throws on any policy regression. */
public final class RulesTest {
    private static int assertions;
    private static void equal(long expected, long actual, String message) {
        assertions++;
        if (expected != actual) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }
    private static void check(boolean expected, boolean actual, String message) { equal(expected ? 1 : 0, actual ? 1 : 0, message); }
    public static void main(String[] args) {
        equal(3600000L, Rules.DAILY_INSTAGRAM_MILLIS, "Automatic daily Instagram cap is one hour");
        check(false, Rules.blocked(0, true, true, true, 3599999L, Rules.DAILY_INSTAGRAM_MILLIS), "Instagram allowed just before one hour");
        check(true, Rules.blocked(0, true, true, true, 3600000L, Rules.DAILY_INSTAGRAM_MILLIS), "Instagram blocks at exactly one hour");
        check(true, Rules.blocked(0, true, true, true, 3601000L, Rules.DAILY_INSTAGRAM_MILLIS), "Instagram stays blocked beyond one hour");
        Rules.UsageCounter otherApps = new Rules.UsageCounter(0, 7200000L);
        otherApps.resume("browser", "Main", 0, false);
        otherApps.resume("camera", "Main", 3600000L, false);
        equal(0, otherApps.result(), "Two hours in other apps do not consume Instagram allowance");
        check(true, Rules.blocked(1, true, true, true, 0, Rules.DAILY_INSTAGRAM_MILLIS), "Manual timed lock still blocks before daily cap");
        equal(1000, Rules.trustedTime(0, 0, -2, 1000, 200, 1), "First start");
        equal(1500, Rules.trustedTime(1000, 200, 1, 999999, 700, 1), "Forward clock jump ignored within boot");
        equal(1500, Rules.trustedTime(1000, 200, 1, 1, 700, 1), "Backward clock jump ignored within boot");
        equal(1000, Rules.trustedTime(1000, 200, 1, 1, 100, 2), "Reboot with backward clock keeps floor");
        equal(2000, Rules.trustedTime(1000, 200, 1, 2000, 100, 2), "Normal reboot uses wall elapsed");
        equal(0, Rules.remaining(1000, 1000), "Exact expiry");
        equal(0, Rules.remaining(1000, 1500), "Expired deadline");
        equal(300, Rules.remaining(1000, 700), "Active deadline");
        check(true, Rules.blocked(1, false, false, false, 0, 0), "Timed lock independent of daily permission");
        check(false, Rules.blocked(0, false, false, false, 0, 0), "No rules");
        check(false, Rules.blocked(0, true, true, true, 59999, 60000), "Budget available");
        check(true, Rules.blocked(0, true, true, true, 60000, 60000), "Exact budget exhaustion");
        check(true, Rules.blocked(0, true, false, true, 0, 60000), "Permission loss fails closed");
        check(true, Rules.blocked(0, true, true, false, 0, 60000), "Clock shift fails closed");
        check(true, Rules.blocked(1000, true, true, true, 0, 60000), "Timed lock takes priority");
        Rules.UsageCounter c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 500, true); c.pause("instagram", "Feed", 2000);
        equal(1000, c.result(), "Midnight spanning session clipped to day");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 2000, true); c.resume("instagram", "Feed", 2200, true);
        equal(3000, c.result(), "Duplicate resume does not double count");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 2000, true); c.resume("browser", "Main", 3000, false);
        c.pause("instagram", "Feed", 3500);
        equal(1000, c.result(), "Switching away closes usage");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 1500, true); c.screenOff(2500);
        equal(1000, c.result(), "Screen off closes usage");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 2000, true); c.pause("browser", "Main", 2500);
        equal(3000, c.result(), "Unrelated pause does not end Instagram");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 1500, true); c.resume("instagram", "Reels", 2500, true);
        c.pause("instagram", "Feed", 2600); c.pause("instagram", "Reels", 4000);
        equal(2500, c.result(), "Outgoing activity pause does not close newly resumed activity");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 1500, true); c.pause("instagram", "Feed", 2000);
        c.resume("lite", "Feed", 3000, true); c.pause("lite", "Feed", 4500);
        equal(2000, c.result(), "Instagram and Lite share allowance");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 4000, true); c.pause("instagram", "Feed", 7000);
        equal(1000, c.result(), "End boundary clips usage");
        c = new Rules.UsageCounter(1000, 5000);
        c.resume("instagram", "Feed", 100, true); c.pause("instagram", "Feed", 200);
        equal(0, c.result(), "Yesterday use excluded");
        System.out.println("PASS: " + assertions + " timer, strict policy, clock and usage assertions");
    }
}
