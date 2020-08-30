package com.example.accalendar.utils;

import androidx.core.content.res.ResourcesCompat;

import com.example.accalendar.R;
import com.example.accalendar.decorators.BirthdayDecorator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DocSnapToData {
    public static Map<String, HashMap<String, Object>> mapBirthdays(Map<String, Object> map) {
        Map<String, HashMap<String, Object>> birthdays = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<String, Object> eventSet = (Map<String, Object>) entry.getValue();
            final int month = ((Long) eventSet.get("month")).intValue();
            ;
            final int day = ((Long) eventSet.get("day")).intValue();

            birthdays.put(entry.getKey(), new HashMap<String, Object>() {{
                put("day", day);
                put("month", month);
            }});
        }
        return birthdays;
    }
    public static Map<String, Map<String, Map<String, Long>>> mapSpecialDays(Map<String, Object> map) {
        Map<String, Map<String, Map<String, Long>>> specialDays = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<String, Map<String, Long>> specialDay =
                    (Map<String, Map<String, Long>>) entry.getValue();
            specialDays.put(entry.getKey(), specialDay);
        }
        return specialDays;
    }

    public static Map<String, Map<ArrayList<Long>, Integer>> mapTourneys(Map<String, Object> map) {
        Map<String, Map<ArrayList<Long>, Integer>> tourneys = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<ArrayList<Long>, Integer> tourney = new HashMap<>();
            ArrayList<Long> months = (ArrayList<Long>) ((Map)entry.getValue()).get("months");
            tourney.put(months, ((Long)((Map)entry.getValue()).get("saturday")).intValue());
            tourneys.put(entry.getKey(), tourney);
        }
        return tourneys;
    }

    public static Map<String, Map<String, Object>> mapResources(Map<String, Object> map) {
        Map<String, Map<String, Object>> resources = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<String, Object> dateInfo = (Map<String, Object>) entry.getValue();
            resources.put(entry.getKey(), dateInfo);
        }
        return resources;
    }

    public static Map<String, Map<String, Long>> mapEvents(Map<String, Object> map) {
        Map<String, Map<String, Long>> events = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Map<String, Long> dateInfo = (Map<String, Long>) entry.getValue();
            events.put(entry.getKey(), dateInfo);
        }
        return events;
    }

    public static void sortByAttribute(ArrayList<Object> toBeSorted, final String key) {
        Collections.sort(toBeSorted, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if (key == "Name"){
                    ClassUtils.Trackable t1 = (ClassUtils.Trackable) o1;
                    ClassUtils.Trackable t2 = (ClassUtils.Trackable) o2;
                    return (t1.name.toLowerCase().compareTo(t2.name.toLowerCase()));
                } else if (key == "Catchphrase") {
                    Villager v1 = (Villager) o1;
                    Villager v2 = (Villager) o2;
                    return (v1.catchphrase.toLowerCase()).compareTo((v2.catchphrase.toLowerCase()));
                } else if (key == "Price"){
                    ClassUtils.Discoverable d1 = (ClassUtils.Discoverable) o1;
                    ClassUtils.Discoverable d2 = (ClassUtils.Discoverable) o2;
                    return Integer.valueOf(d1.price).compareTo(Integer.valueOf(d2.price));
                } else { // default
                    ClassUtils.Trackable t1 = (ClassUtils.Trackable) o1;
                    ClassUtils.Trackable t2 = (ClassUtils.Trackable) o2;
                    return (Integer.valueOf(t1.index).compareTo(Integer.valueOf((t2.index))));
                }
            }
        });
    }
}
