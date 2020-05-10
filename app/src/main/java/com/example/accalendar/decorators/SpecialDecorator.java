package com.example.accalendar.decorators;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

public class SpecialDecorator implements DayViewDecorator {
    private final  Map<String, Map<String, Map<String, Long>>> specialDays;
    private final Drawable d;

    public SpecialDecorator(Map<String, Map<String, Map<String, Long>>> specialDays, Drawable d) {
        this.d = d;
        this.specialDays = new HashMap<>(specialDays);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        // Map contains: (special: Map of events, regular: Map of events)
        // Special events = events that occur on a certain day of week in a month (ie. 2nd sunday of June)
        // Regular events = events that occur on a specific date in a month (25th of December)
        for (Map.Entry <String, Map<String, Map<String, Long>>> special : specialDays.entrySet()) {
            // special.getValue() = event maps: (event: day, month)
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
            // If the date is equal to the event date
            if(date.getMonth().getValue() == info.get("month")
                    && date.getDayOfMonth() == info.get("day"))
                return true;
        } else { //If special
            if (date.getMonth().getValue() == info.get("month") && // If the date is in the month of the event
                    date.getDayOfWeek().getValue() == info.get("day of week")) { // If the date is on the correct day of week
                int daysBetween = date.getDayOfMonth();
                // By this point we have already determined that this date is the correct day of week,
                // and is in the correct month. So now we have to determine if it falls on the correct
                // week of the month

                // General idea of how it works: using week = 2
                // For the date to be the 2nd day of the month, it must have occurred once before
                // so there must have been 7 days that must have passed before reaching the 2nd day: daysBetween > 7*(info.get("week")-1
                // so the date must lie somewhere between the 7th day, and the next 7 days: daysBetween <= 7*info.get("week")
                // 7 < daysBetween <= 14
                if (daysBetween <= 7*info.get("week") && daysBetween > 7*(info.get("week")-1))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        DecoratorImageSpan span = new DecoratorImageSpan(d, 3);
        view.addSpan(span);
    }
}