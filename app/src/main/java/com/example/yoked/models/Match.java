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
    public Boolean finished;

    public Match() {

    }

    public Match(String userId, String otherUserId, String freeTime, Boolean currentUserStatus, Boolean otherUserStatus, Boolean finished) {
        this.userId = userId;
        this.otherUserId = otherUserId;
        this.freeTime = freeTime;
        this.currentUserStatus = currentUserStatus;
        this.otherUserStatus = otherUserStatus;
        this.finished = finished;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public String getFreeTime() {
        return freeTime;
    }

    public Boolean getCurrentUserStatus() {
        return currentUserStatus;
    }

    public Boolean getOtherUserStatus() {
        return otherUserStatus;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public void setFreeTime(String freeTime) {
        this.freeTime = freeTime;
    }

    public void setCurrentUserStatus(Boolean currentUserStatus) {
        this.currentUserStatus = currentUserStatus;
    }

    public void setOtherUserStatus(Boolean otherUserStatus) {
        this.otherUserStatus = otherUserStatus;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("otherUserId", otherUserId);
        result.put("freeTime", freeTime);
        result.put("currentUserStatus", currentUserStatus);
        result.put("otherUserStatus", otherUserStatus);
        result.put("finished", finished);
        return result;
    }
}
