package com.example.accalendar.decorators;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spanned;
import android.text.style.LineBackgroundSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashMap;
import java.util.Map;

public class BirthdayDecorator implements DayViewDecorator {
    private final Map<String, HashMap<String, Object>> dates;
    private final Drawable d;

    public BirthdayDecorator(Map<String, HashMap<String, Object>> dates, Drawable d) {
        this.dates = new HashMap<>(dates);
        this.d = d;
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        Map<String, Object> monthDay = new HashMap<String, Object>() {{
            put("day", day.getDay());
            put("month", day.getMonth());
        }};
        return dates.containsValue(monthDay);
    }

    @Override
    public void decorate(DayViewFacade view) {
        DecoratorImageSpan decorator = new DecoratorImageSpan(d, 2);
        view.addSpan(decorator);
    }
}


class DotSpanPadded implements LineBackgroundSpan {

    private final float radius;
    private final int color;
    private final int padding = 25;
    private final int location;

    public DotSpanPadded(float radius, int color, int location) {
        this.radius = radius;
        this.color = color;
        this.location = location;
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

        float y;
        float x;
        if (location == 1 || location == 2)
            y = top-bottom+radius+padding;
        else
            y = bottom+radius+padding;
        if (location == 1 || location == 3)
            x = left+radius*2+padding;
        else
            x = right-radius*2-padding;


        canvas.drawCircle(x, y, radius, paint);
        paint.setColor(oldColor);
    }
}