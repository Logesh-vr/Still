package app.still.focus;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

public final class Ui {
    public static final int CREAM = Color.rgb(245, 244, 238), WHITE = Color.rgb(255, 254, 250);
    public static final int GREEN = Color.rgb(36, 78, 60), INK = Color.rgb(31, 44, 36);
    public static final int MUTED = Color.rgb(94, 105, 95), LINE = Color.rgb(221, 225, 215);
    private Ui() {}
    public static int dp(Context c, float value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    public static GradientDrawable background(Context c, int color, int radius, boolean stroke) {
        GradientDrawable drawable = new GradientDrawable(); drawable.setColor(color);
        drawable.setCornerRadius(dp(c, radius)); if (stroke) drawable.setStroke(dp(c, 1), LINE);
        return drawable;
    }
    public static TextView text(Context c, String value, float size, int color) {
        TextView view = new TextView(c); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setFontFeatureSettings("kern"); view.setLineSpacing(dp(c, 3), 1.05f); return view;
    }
    public static Button button(Context c, String label, Runnable action, boolean primary) {
        Button button = new Button(c); button.setText(label); button.setTextSize(16); button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setTextColor(primary ? WHITE : GREEN); button.setGravity(Gravity.CENTER);
        button.setBackground(background(c, primary ? GREEN : WHITE, 16, !primary));
        button.setOnClickListener(v -> action.run()); button.setMinHeight(dp(c, 52));
        button.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10)); return button;
    }
}
