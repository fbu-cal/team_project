package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Calendar {
    public List<String> mFreeTime;
    public String userId;

    //sends to data base

    public Calendar() {
        // Default constructor required
    }

    public List<String> getmFreeTime() {
        return mFreeTime;
    }

    //gets values from CalendarActivity
    public void setmFreeTime(List<String> mFreeTime) {
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