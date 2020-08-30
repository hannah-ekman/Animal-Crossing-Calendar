package com.example.accalendar.utils;
import java.util.HashMap;
import java.util.Map;

public class Villager extends ClassUtils.Trackable {
    public ClassUtils.Date birthday = new ClassUtils.Date();
    public String catchphrase;
    public String personality;
    public String species;
    public String hobby;
    public String gender;
    public Villager(Map<String, Object> data, String name) {
        this.index = ((Long) data.get("index")).intValue();
        this.name = name;
        this.birthday.day = ((Long) data.get("day")).intValue();
        this.birthday.month = ((Long) data.get("month")).intValue();
        this.catchphrase = data.get("catchphrase").toString();
        this.personality = data.get("personality").toString();
        this.species = data.get("species").toString();
        this.hobby = data.get("hobby").toString();
        this.image = data.get("image").toString();
        // get the updated web scraping from Charles, gender is missing
        //this.gender = data.get("gender").toString();
    }
    @Override
    public void fillKeyValues(HashMap<String, Object> values) {
        super.fillKeyValues(values);
        values.put("birthday", birthday.month + "/" + birthday.day);
        values.put("catchphrase", catchphrase);
        values.put("personality", personality);
        values.put("species", species);
        values.put("hobby", hobby);
        values.put("gender", gender);
    }

    public String get(String value) {
        switch (value) {
            case "Personalities":
                return personality;
            case "Hobbies":
                return hobby;
            case "Species":
                return species;
            case "Genders":
                return gender;
            default:
                return null;
        }
    }
}
