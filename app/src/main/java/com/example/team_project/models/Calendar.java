package com.example.team_project.models;

import java.util.List;

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
}