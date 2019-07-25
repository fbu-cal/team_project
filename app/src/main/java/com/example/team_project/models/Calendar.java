package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Calendar {
    public HashMap<String, Boolean> mFreeTime = new HashMap<>();
    public String userId;
    //public String calendarKey;


    public Calendar() {
        // Default constructor required
    }

    public Calendar(String userId, HashMap<String, Boolean> mFreeTime) {
        this.userId = userId;
        this.mFreeTime = mFreeTime;
        //this.calendarKey = calendarKey;
    }


    public Map<String, Boolean> getmFreeTime() {
        return mFreeTime;
    }

    //gets values from CalendarActivity


    public void setmFreeTime(HashMap<String, Boolean> mFreeTime) {
        this.mFreeTime = mFreeTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("mFreeTime", mFreeTime);
        result.put("userId", userId);
        return result;
    }
}