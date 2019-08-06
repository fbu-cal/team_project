package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message {

    private String senderId;
    private String receiverId;
    private String messageText;
    private String username;
    private Date timeStamp;
    private long messageTimeStamp;

    public Message(String senderId, String receiverId, String username, String messageText){
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username =username;
    }
//
//    public String getMessageText(){
//        return messageText;
//    }
//
//    public void setMessageText(String messageText){
//        this.messageText =messageText;
//    }
//
//    public long getMessageTimeStamp(){
//        return messageTimeStamp;
//    }
//
//    public void setMessageTimeStamp(long timeStamp){
//        this.messageTimeStamp = timeStamp;
//    }
//
//    public Date getTimeStamp() {
//        return timeStamp;
//    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("senderId", senderId);
        result.put("receiverId", receiverId);
        result.put("messageText", messageText);
        result.put("username", username);
        result.put("timeStamp", timeStamp.toString());
        return result;
    }
    // [END post_to_map]
}
