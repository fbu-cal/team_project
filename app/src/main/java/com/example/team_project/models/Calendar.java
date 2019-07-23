package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Calendar {
    public Map<String, Boolean> mFreeTime = new HashMap<>();
    public String userId;


    public Calendar() {
        // Default constructor required
    }

    public Calendar(String userId, Map mFreeTime) {
        this.userId = userId;
        this.mFreeTime = mFreeTime;
    }

    public Map<String, Boolean> getmFreeTime() {
        return mFreeTime;
    }

    //gets values from CalendarActivity


    public void setmFreeTime(Map<String, Boolean> mFreeTime) {
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