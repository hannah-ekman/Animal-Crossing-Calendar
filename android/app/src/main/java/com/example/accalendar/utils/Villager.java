package com.example.accalendar.utils;
import java.util.Map;

public class Villager extends ClassUtils.Trackable {
    public ClassUtils.Date birthday;
    public String catchphrase;
    public String personality;
    public String species;
    public String hobby;
    public String gender;
    public Villager(Map<String, Object> data, String name) {
        this.name = name;
        this.birthday.day = ((Long) data.get("day")).intValue();
        this.birthday.month = ((Long) data.get("month")).intValue();
        this.catchphrase = data.get("catchphrase").toString();
        this.personality = data.get("personality").toString();
        this.species = data.get("species").toString();
        this.hobby = data.get("hobby").toString();
        this.image = data.get("image").toString();
        this.gender = data.get("gender").toString();
    }
}
