package com.example.team_project.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class NotifMatch {
    public String userId;
    public String otherUserId;
    public Boolean check;

    NotifMatch() {

    }
    public NotifMatch(String userId, String otherUserId, Boolean check) {
        this.check = check;
    }

    public Boolean getCheck() {
        return check;
    }

    public String getUserId() {
        return userId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("check", check);
        return result;
    }
}

