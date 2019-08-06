package com.example.yoked.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Message {

    private String userId;
    private String messageText;
    private String username;
    private long messageTimeStamp;

    public Message(String userId, String username, String messageText){
        this.userId = userId;
        this.messageText = messageText;
        this.username = username;
    }

    public String getUsername(){
        return userId;
    }

    public void setUsername(String username){
        this.userId =username;
    }

    public String getMessageText(){
        System.out.println("Yeah"+messageText);
        return messageText;
    }

    public void setMessageText(String messageText){
        this.messageText =messageText;
    }

    public long getMessageTimeStamp(){
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(long timeStamp){
        this.messageTimeStamp = timeStamp;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("messageText", messageText);
        return result;
    }
    // [END post_to_map]
}
