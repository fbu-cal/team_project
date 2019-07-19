package com.example.team_project.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private String username;
    private String messageText;
    private long mMessageTimeStamp;

    public Message(String username, String messageText){
        this.username = username;
        this.messageText = messageText;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username =username;
    }

    public String getMessageText(){
        return messageText;
    }

    public void setMessageText(String messageText){
        this.messageText =messageText;
    }

    public long getMessageTimeStamp(){
        return mMessageTimeStamp;
    }

    public void setMessageTimeStamp(long timeStamp){
        this.mMessageTimeStamp = timeStamp;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("messageText", messageText);
//        result.put("starCount", starCount);
//        result.put("stars", stars);
        return result;
    }
    // [END post_to_map]
}
