package com.example.accalendar.decorators;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.NonNull;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class EventDecorator implements DayViewDecorator {
    private final int color;
    private final Map<String, Map<String, Long>> dates;

    public EventDecorator(int color, Map<String, Map<String, Long>> dates) {
        this.color = color;
        this.dates = new HashMap<>(dates);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        for (Map.Entry <String, Map<String, Long>> event : dates.entrySet()) {
            if(isEvent(event, date))
                return true;
        }
        return false;
    }

    public static boolean isEvent(Map.Entry<String, Map<String, Long>> event, LocalDate date) {
        Map<String, Long> resourceInfo = event.getValue();
        LocalDate start = LocalDate.of(date.getYear(), resourceInfo.get("start month").intValue(),
                resourceInfo.get("start day").intValue());
        LocalDate end = LocalDate.of(date.getYear(), resourceInfo.get("end month").intValue(),
                resourceInfo.get("end day").intValue());
        if(date.isEqual(start) || date.isEqual(end) || (date.isAfter(start) && date.isBefore(end)))
            return true;
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new LineBackgroundSpan() {
            @Override
            public void drawBackground(@NonNull Canvas canvas, @NonNull Paint paint,
                                       int left, int right, int top, int baseline, int bottom,
                                       @NonNull CharSequence text, int start, int end, int lineNumber) {
                Paint localPaint = new Paint();
                RectF rect = new RectF(left, top, right, bottom);
                localPaint.setColor(color);
                canvas.drawRect(rect, localPaint);
            }
        });
    }
}
