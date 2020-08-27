package com.example.accalendar.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Fish extends ClassUtils.Catchable {
    public int shadow;
    public Fish (Map<String, Object> data, String name) {
        this.name = name;
        this.image = data.get("image").toString();
        this.location = data.get("location").toString();
        this.price = ((Long) data.get("price")).intValue();
        this.north = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("north"));
        this.south = ParseTimeHash((ArrayList<HashMap<String, Object>>) data.get("south"));
        this.times = ParseTimeHash(((ArrayList<HashMap<String, Object>>) data.get("times")));
        this.shadow = ((Long) data.get("shadow size")).intValue();
    }
}
