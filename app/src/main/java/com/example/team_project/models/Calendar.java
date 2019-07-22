package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Calendar {
    public String userId;
    public String mFridayMorning;
    public String mSaturdayMorning;
    public String mSundayMorning;
    public String mFridayAfternoon;
    public String mSaturdayAfternoon;
    public String mSundayAfternoon;
    public String mFridayEvening;
    public String mSaturdayEvening;
    public String mSundayEvening;

    public Calendar() {
        // Default constructor required
    }

    public Calendar(String userId, String mFridayMorning, String mFridayAfternoon,
                    String mFridayEvening, String mSaturdayMorning,
                    String mSaturdayAfternoon, String mSaturdayEvening, String mSundayMorning,
                    String mSundayAfternoon, String mSundayEvening) {
        this.userId = userId;
        this.mFridayMorning = mFridayMorning;
        this.mFridayAfternoon = mFridayAfternoon;
        this.mFridayEvening = mFridayEvening;
        this.mSaturdayMorning = mSaturdayMorning;
        this.mSaturdayAfternoon = mSaturdayAfternoon;
        this.mSaturdayEvening =  mSaturdayEvening;
        this.mSundayMorning = mSundayMorning;
        this.mSundayAfternoon = mSundayAfternoon;
        this.mSundayEvening = mSundayEvening;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getmFridayMorning() {
        return mFridayMorning;
    }

    public String getmSaturdayMorning() {
        return mSaturdayMorning;
    }

    public String getmSundayMorning() {
        return mSundayMorning;
    }

    public String getmFridayAfternoon() {
        return mFridayAfternoon;
    }

    public String getmSaturdayAfternoon() {
        return mSaturdayAfternoon;
    }

    public String getmSundayAfternoon() {
        return mSundayAfternoon;
    }

    public String getmFridayEvening() {
        return mFridayEvening;
    }

    public String getmSaturdayEvening() {
        return mSaturdayEvening;
    }

    public String getmSundayEvening() {
        return mSundayEvening;
    }

    public void setmFridayMorning(String mFridayMorning) {
        this.mFridayMorning = mFridayMorning;
    }

    public void setmSaturdayMorning(String mSaturdayMorning) {
        this.mSaturdayMorning = mSaturdayMorning;
    }

    public void setmSundayMorning(String mSundayMorning) {
        this.mSundayMorning = mSundayMorning;
    }

    public void setmFridayAfternoon(String mFridayAfternoon) {
        this.mFridayAfternoon = mFridayAfternoon;
    }

    public void setmSaturdayAfternoon(String mSaturdayAfternoon) {
        this.mSaturdayAfternoon = mSaturdayAfternoon;
    }

    public void setmSundayAfternoon(String mSundayAfternoon) {
        this.mSundayAfternoon = mSundayAfternoon;
    }

    public void setmFridayEvening(String mFridayEvening) {
        this.mFridayEvening = mFridayEvening;
    }

    public void setmSaturdayEvening(String mSaturdayEvening) {
        this.mSaturdayEvening = mSaturdayEvening;
    }

    public void setmSundayEvening(String mSundayEvening) {
        this.mSundayEvening = mSundayEvening;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        //result.put("mFreeTime", mFreeTime);
        result.put("userId", userId);
        result.put("mFridayMorning", mFridayMorning);
        result.put("mFridayAfternoon", mFridayAfternoon);
        result.put("mFridayEvening", mFridayEvening);
        result.put("mSaturdayMorning", mSaturdayMorning);
        result.put("mSaturdayAfternoon", mSaturdayAfternoon);
        result.put("mSaturdayEvening", mSaturdayEvening);
        result.put("mSundayMorning", mSundayMorning);
        result.put("mSundayAfternoon", mSundayAfternoon);
        result.put("mSundayEvening", mSundayEvening);
        return result;
    }
}