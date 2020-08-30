package com.example.accalendar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bug extends ClassUtils.Catchable {
    public Bug (Map<String, Object> data, String name) {
        this.index = ((Long) data.get("index")).intValue();
        this.name = name;
        this.image = data.get("image").toString();
        this.location = data.get("location").toString();
        this.price = ((Long) data.get("price")).intValue();
        this.north = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("north"));
        this.south = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("south"));
        this.times = ParseTimeHash(((ArrayList<HashMap<String, Object>>) data.get("times")));
    }
}
