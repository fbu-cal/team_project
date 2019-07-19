package com.example.team_project.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    //Calendar calendar;
    List<String> mFreeTime = new ArrayList<String>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        //this.calendar = null;
    }

    public List<String> getmFreeTime() {
        return mFreeTime;
    }

    public void setmFreeTime(List<String> mFreeTime) {
        this.mFreeTime = mFreeTime;
    }
}