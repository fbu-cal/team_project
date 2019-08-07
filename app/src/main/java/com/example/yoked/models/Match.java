package com.example.yoked.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Match {
    public String userId;
    public String otherUserId;
    public String freeTime;
    public Boolean currentUserStatus;
    public Boolean otherUserStatus;

    public Match() {

    }

    public Match(String userId, String otherUserId, String freeTime, Boolean currentUserStatus, Boolean otherUserStatus) {
        this.userId = userId;
        this.otherUserId = otherUserId;
        this.freeTime = freeTime;
        this.currentUserStatus = currentUserStatus;
        this.otherUserStatus = otherUserStatus;
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
        result.put("userId", userId);
        result.put("otherUserId", otherUserId);
        result.put("freeTime", freeTime);
        result.put("currentUserStatus", currentUserStatus);
        result.put("otherUserStatus", otherUserStatus);
        return result;
    }
}
