package com.example.admin.betty.Activity;


import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class ScheduleDTO {
    public String title;
    public String date;
    public String context;
    public String prepare;
    public long createdate;

    public ScheduleDTO(){
        // Default constructor required for calls to DataSnapshot.getValue(ScheduleDTO.class)
    }

    public ScheduleDTO(String title, String date, String context, String prepare, long createdate){
        this.title = title;
        this.date = date;
        this.context = context;
        this.prepare = prepare;
        this.createdate = createdate;
    }

    /*@Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("date", date);
        result.put("context", context);
        result.put("prepare", prepare);
        result.put("createdate", createdate);

        return result;
    }*/
}
