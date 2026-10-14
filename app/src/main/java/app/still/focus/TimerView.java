package app.still.focus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public final class TimerView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private String title = "Make room.", caption = "for life beyond the feed";
    private float fraction = 1;
    public TimerView(Context context) { super(context); setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES); }
    public void update(String title, String caption, float fraction) {
        this.title = title; this.caption = caption; this.fraction = Math.max(0, Math.min(1, fraction));
        setContentDescription(title + ". " + caption); invalidate();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f, cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f - Ui.dp(getContext(), 15);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(Ui.dp(getContext(), 7)); paint.setColor(Ui.LINE);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setColor(Ui.GREEN); paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(new RectF(cx-radius, cy-radius, cx+radius, cy+radius), -90, 360 * fraction, false, paint);
        paint.setStrokeCap(Paint.Cap.BUTT); paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER); paint.setColor(Ui.INK);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        float scale = getResources().getDisplayMetrics().scaledDensity;
        paint.setTextSize(34 * scale);
        while (paint.measureText(title) > radius * 1.7f && paint.getTextSize() > 18 * scale)
            paint.setTextSize(paint.getTextSize() - scale);
        canvas.drawText(title, cx, cy, paint);
        paint.setTextSize(13 * scale); paint.setColor(Ui.MUTED); paint.setTypeface(Typeface.DEFAULT);
        while (paint.measureText(caption) > radius * 1.75f && paint.getTextSize() > 10 * scale)
            paint.setTextSize(paint.getTextSize() - scale);
        canvas.drawText(caption, cx, cy + Ui.dp(getContext(), 29), paint);
    }
}
