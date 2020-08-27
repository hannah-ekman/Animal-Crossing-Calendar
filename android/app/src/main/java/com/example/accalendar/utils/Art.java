package com.example.accalendar.utils;

import java.util.Map;

// need to add selling price/cost to buy
public class Art extends ClassUtils.Discoverable {
    public int cost;
    public boolean hasFake;
    public String fake;
    public String tip;
    public Art(Map<String, Object> data, String name) {
        this.name = name;
        this.image = data.get("real").toString();
        this.price = ((Long) data.get("price")).intValue();
        this.cost = ((Long) data.get("cost")).intValue();
        this.tip = data.get("tip").toString();
        if (data.containsKey("fake")){
            this.fake = data.get("fake").toString();
            this.hasFake = true;
        } else {
            this.hasFake = false;
        }
    }

}
