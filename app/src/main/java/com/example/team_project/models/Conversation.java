package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Conversation {
    public String currentUser;
    public String otherUser;
    public String timeStamp;
    public String latestMessageText;

    public Conversation () {

    }

    public Conversation (String currentUser, String otherUser){
        this.currentUser=currentUser;
        this.otherUser=otherUser;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(String otherUser) {
        this.otherUser = otherUser;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("currentUser", currentUser);
        result.put("otherUser", otherUser);
        return result;
    }
    // [END post_to_map]
}
