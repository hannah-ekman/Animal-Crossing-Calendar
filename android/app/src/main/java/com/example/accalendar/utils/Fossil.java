package com.example.accalendar.utils;

import java.util.Map;

public class Fossil extends ClassUtils.Discoverable {
    public Fossil (Map<String, Object> data, String name) {
        this.index = ((Long) data.get("index")).intValue();
        this.name = name;
        this.image = data.get("image").toString();
        this.price = ((Long) data.get("price")).intValue();
    }
}
