package com.example.accalendar.decorators;

import com.example.accalendar.decorators.DotSpanPadded;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

public class SpecialDecorator implements DayViewDecorator {
    private final int color;
    private final  Map<String, Map<String, Map<String, Long>>> specialDays;

    public SpecialDecorator(int color,  Map<String, Map<String, Map<String, Long>>> specialDays) {
        this.color = color;
        this.specialDays = new HashMap<>(specialDays);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        for (Map.Entry <String, Map<String, Map<String, Long>>> special : specialDays.entrySet()) {
            Map<String, Map<String, Long>> specialCategories = special.getValue();
            for(Map.Entry <String, Map<String, Long>> category : specialCategories.entrySet()) {
                if (isSpecialEvent(category, special.getKey(), date))
                    return true;
            }
        }
        return false;
    }

    public static boolean isSpecialEvent(Map.Entry <String, Map<String, Long>> category, String type,
                                         LocalDate date) {
        Map<String, Long> info = category.getValue();
        if(type.equals("regular")) {
            if(date.getMonth().getValue() == info.get("month")
                    && date.getDayOfMonth() == info.get("day"))
                return true;
        } else {
            if (date.getMonth().getValue() == info.get("month") &&
                    date.getDayOfWeek().getValue() == info.get("day of week")) {
                int daysBetween = date.getDayOfMonth();
                if (daysBetween <= 7*info.get("week") && daysBetween > 7*(info.get("week")-1))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpanPadded(7, color, 3));
    }
}