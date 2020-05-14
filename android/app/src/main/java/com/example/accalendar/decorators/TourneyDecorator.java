package com.example.accalendar.decorators;

import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TourneyDecorator implements DayViewDecorator {
    private final Drawable d;
    private final Map<String, Map<ArrayList<Long>, Integer>> tourneys;

    public TourneyDecorator(Map<String, Map<ArrayList<Long>, Integer>> tourneys, Drawable d) {
        this.d = d;
        this.tourneys = new HashMap<>(tourneys);
    }

    @Override
    public boolean shouldDecorate(final CalendarDay day) {
        LocalDate date = day.getDate();
        for (Map.Entry <String, Map<ArrayList<Long>, Integer>> tourney : tourneys.entrySet()) {
            if(isTourney(tourney, date))
                return true;
        }
        return false;
    }

    public static boolean isTourney(Map.Entry <String, Map<ArrayList<Long>, Integer>> tourney,
                                    LocalDate date) {
        // tourney.getValue() = Map(EventMap, saturdayOfMonth)
        // each map only has one entry, might've been better to use tuples but oh well :P
        Map<ArrayList<Long>, Integer> tourneyInfo = tourney.getValue();
        // months = EventMap: months(int, monthValue)
        //      (aka an array of months the event occurs on but firebase stores arrays as maps)
        ArrayList<Long> months = tourneyInfo.keySet().iterator().next();
        // get the saturdayOfMonth using months as the key
        int saturday = tourneyInfo.get(months);
        Long curMonth = new Long(date.getMonth().getValue());
        // If the date's month is in the array of months for the event and it is a Saturday
        if(months.contains(curMonth) && date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            // By this point we have already determined that this date is the correct day of week,
            // and is in the correct month. So now we have to determine if it falls on the correct
            // week of the month

            // General idea of how it works: using saturday = 2
            // For the date to be the 2nd day of the month, it must have occurred once before
            // so there must have been 7 days that must have passed before reaching the 2nd day: daysBetween > 7*(saturday-1)
            // so the date must lie somewhere between the 7th day, and the next 7 days: daysBetween <= 7*saturday
            // 7 < daysBetween <= 14
            int daysBetween = date.getDayOfMonth();
            if (daysBetween <= 7*saturday && daysBetween > 7*(saturday-1))
                return true;
        }
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        DecoratorImageSpan span = new DecoratorImageSpan(d, 1);
        view.addSpan(span);
    }
}
