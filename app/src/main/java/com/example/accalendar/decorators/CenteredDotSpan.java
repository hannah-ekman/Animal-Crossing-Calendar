package com.example.accalendar.decorators;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

public class CenteredDotSpan implements LineBackgroundSpan {
    private final float radius;
    private final int color;

    public CenteredDotSpan(float radius, int color) {
        this.radius = radius;
        this.color = color;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        int oldColor = paint.getColor();
        if (color != 0) {
            paint.setColor(color);
        }
        float y = baseline;
        float x = (left + right) / 2;

        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(oldColor);
    }

}
