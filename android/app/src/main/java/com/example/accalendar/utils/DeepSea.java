package com.example.accalendar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DeepSea extends ClassUtils.Catchable {
    public String swimPattern;
    public DeepSea(Map<String, Object> data, String name) {
        this.index = ((Long) data.get("index")).intValue();
        this.name = name;
        this.image = data.get("image").toString();
        this.location = data.get("location").toString();
        this.price = ((Long) data.get("price")).intValue();
        this.north = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("north"));
        this.south = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("south"));
        this.times = ParseTimeHash(((ArrayList<HashMap<String, Object>>) data.get("times")));
        this.swimPattern = data.get("swim pattern").toString();
    }
    @Override
    public void fillKeyValues(HashMap<String, Object> values, boolean isNorth) {
        super.fillKeyValues(values, isNorth);
        values.put("swim pattern", swimPattern);
    }
    @Override
    public String get(String value) {
        String s = super.get(value);
        if (s == null) {
            if ("Swim Patterns".equals(value)) {
                return swimPattern;
            }
        }
        return s;
    }
}
