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


