package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private String userId;
    private String messageText;
    private String username;
    private Date timeSent;
    private long messageTimeStamp;

    public Message(String userId, String username, String messageText){
        this.userId = userId;
        this.messageText = messageText;
        this.username = username;
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
        return messageTimeStamp;
    }

    public void setMessageTimeStamp(long timeStamp){
        this.messageTimeStamp = timeStamp;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("messageText", messageText);
        result.put("username", username);
        result.put("timeSent", timeSent.toString());
        return result;
    }
    // [END post_to_map]
}
