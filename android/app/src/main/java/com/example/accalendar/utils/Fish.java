package com.example.accalendar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Fish extends ClassUtils.Catchable {
    public String shadow;
    public Fish (Map<String, Object> data, String name) {
        this.name = name;
        this.image = data.get("image").toString();
        this.location = data.get("location").toString();
        this.price = ((Long) data.get("price")).intValue();
        this.north = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("north"));
        this.south = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("south"));
        this.times = ParseTimeHash(((ArrayList<HashMap<String, Object>>) data.get("times")));
        this.shadow = data.get("shadow size").toString();
    }

    @Override
    public void fillKeyValues(HashMap<String, Object> values, boolean isNorth) {
        super.fillKeyValues(values, isNorth);
        values.put("shadow size", shadow);
    }

    @Override
    public String get(String value) {
        String s = super.get(value);
        if (s == null) {
            if ("Shadow Sizes".equals(value)) {
                return shadow;
            }
        }
        return s;
    }
}
