package com.example.accalendar.decorators;

import com.example.accalendar.decorators.DotSpanPadded;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

public class ResourceDecorator implements DayViewDecorator {
    private final int color;
    private final Map<String, Map<String, Long>> resources;

    public ResourceDecorator(int color, Map<String, Map<String, Long>> resources) {
        this.color = color;
        this.resources = new HashMap<>(resources);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        for (Map.Entry <String, Map<String, Long>> resource : resources.entrySet()) {
            if (isResourceEvent(resource, date)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isResourceEvent(Map.Entry <String, Map<String, Long>> resource, LocalDate day) {
        Map<String, Long> resourceInfo = resource.getValue();
        if (day.getMonth().getValue() == resourceInfo.get("start month")
                && day.getDayOfMonth() == resourceInfo.get("start day")) {
            return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpanPadded(7, color, 4));
    }
}
