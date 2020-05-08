package com.example.accalendar.decorators;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

public class ResourceDecorator implements DayViewDecorator {
    private final Map<String, Map<String, Object>> resources;
    private final Drawable d;

    public ResourceDecorator(Map<String, Map<String, Object>> resources, Drawable d) {
        this.resources = new HashMap<>(resources);
        this.d = d;
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        for (Map.Entry <String, Map<String, Object>> resource : resources.entrySet()) {
            if (isResourceEvent(resource, date)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isResourceEvent(Map.Entry <String, Map<String, Object>> resource, LocalDate day) {
        Map<String, Object> resourceInfo = resource.getValue();
        if (day.getMonth().getValue() == (Long) resourceInfo.get("start month")
                && day.getDayOfMonth() == (Long) resourceInfo.get("start day")) {
            return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        DecoratorImageSpan span = new DecoratorImageSpan(d, 4);
        view.addSpan(span);
    }
}
