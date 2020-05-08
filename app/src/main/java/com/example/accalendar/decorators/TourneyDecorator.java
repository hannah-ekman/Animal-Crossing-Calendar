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
        Map<ArrayList<Long>, Integer> tourneyInfo = tourney.getValue();
        ArrayList<Long> months = tourneyInfo.keySet().iterator().next();
        int saturday = tourneyInfo.get(months);
        Long curMonth = new Long(date.getMonth().getValue());
        if(months.contains(curMonth) && date.getDayOfWeek() == DayOfWeek.SATURDAY) {
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
