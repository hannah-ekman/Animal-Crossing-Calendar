package com.example.accalendar.decorators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CurrentDayDecorator implements DayViewDecorator {
    private final int color;
    private final LocalDate current;
    private GradientDrawable highlightDrawable;

    public CurrentDayDecorator(int color, LocalDate current) {
        this.color = color;
        this.current = current;
        highlightDrawable = new GradientDrawable();
        highlightDrawable.setCornerRadius(20);
        highlightDrawable.setColor(color);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        long between = ChronoUnit.DAYS.between(current, day.getDate());
        return between == 0;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new StyleSpan(Typeface.BOLD));
        view.setBackgroundDrawable(highlightDrawable);
        view.addSpan(new ForegroundColorSpan(Color.WHITE));
    }
}
