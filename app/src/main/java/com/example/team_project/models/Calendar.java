package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Calendar {
    public ArrayList<String> mFreeTime = new ArrayList<String>();
    public String userId;


    public Calendar() {
        // Default constructor required
    }

    public Calendar(String userId, ArrayList mFreeTime) {
        this.userId = userId;
        this.mFreeTime = mFreeTime;
    }

    public List<String> getmFreeTime() {
        return mFreeTime;
    }

    //gets values from CalendarActivity


    public void setmFreeTime(ArrayList<String> mFreeTime) {
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