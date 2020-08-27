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

    public static void sortHashMapByIndex(Map<String, Object> map, String key) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Object>> list =
                new ArrayList<>(map.entrySet());

        String sortBy = "index";
        if(key == "Price")
            sortBy = "price";
        else if (key == "Name")
            sortBy = "name";
        else if (key == "Catchphrase")
            sortBy = "catchphrase";

        // Sort the list
        final String finalSortBy = sortBy;
        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                if (finalSortBy == "name"){
                    String fish1 = o1.getKey();
                    String fish2 = o2.getKey();
                    return fish1.compareTo(fish2);
                } else if (finalSortBy == "catchphrase") {
                    Map<String, Object> fish1 = (Map<String, Object>) o1.getValue();
                    Map<String, Object> fish2 = (Map<String, Object>) o2.getValue();
                    String i1 = (String) fish1.get(finalSortBy);
                    String i2 = (String) fish2.get(finalSortBy);
                    return (i1.toLowerCase()).compareTo((i2.toLowerCase()));
                } else {
                    Map<String, Object> fish1 = (Map<String, Object>) o1.getValue();
                    Map<String, Object> fish2 = (Map<String, Object>) o2.getValue();
                    Long i1 = (Long) fish1.get(finalSortBy);
                    Long i2 = (Long) fish2.get(finalSortBy);
                    return i1.compareTo(i2);
                }
            }
        });

        // put data from sorted list to hashmap (has to be a linked hashmap otherwise it will not be ordered)
        map.clear();
        for (Map.Entry<String, Object> aa : list) {
            map.put(aa.getKey(), aa.getValue());
        }
    }
}
