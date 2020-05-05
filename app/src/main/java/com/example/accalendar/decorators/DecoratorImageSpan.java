package com.example.accalendar.decorators;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.text.style.LineBackgroundSpan;

public class DecoratorImageSpan implements LineBackgroundSpan {
    private final Drawable drawable;
    private final int padding = 6;
    private final int location;
    public DecoratorImageSpan(Drawable drawable, int location) {
        this.drawable = drawable;
        this.location = location;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        int y;
        int x;
        drawable.setBounds(0, 0, 30, 30);
        if (location == 1 || location == 2)
            y = top-drawable.getBounds().bottom-padding;
        else
            y = baseline+padding;
        if (location == 1 || location == 3)
            x = left+drawable.getBounds().right-padding;
        else
            x = right-drawable.getBounds().right*2+padding;
        canvas.save();
        canvas.translate(x, y);
        drawable.draw(canvas);
        canvas.restore();
    }

}