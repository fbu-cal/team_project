package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Notification {

    public String type;
    public String icon;
    public String title;
    public String body;
    public String timestamp;
    public String toUid;
    public String fromUid;
    public String key = "";
    public Boolean seen = false;

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String type, String icon, String title, String body, String timestamp, String toUid, String fromUid) {
        this.type = type;
        this.icon = icon;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.toUid = toUid;
        this.fromUid = fromUid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("icon", icon);
        result.put("title", title);
        result.put("body", body);
        result.put("timestamp", timestamp);
        result.put("toUid", toUid);
        result.put("fromUid", fromUid);
        result.put("key", key);
        result.put("seen", seen);
        return result;
    }
}
