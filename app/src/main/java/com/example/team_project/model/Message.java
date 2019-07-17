package com.example.team_project.model;

public class Message {

    private static final String USERNAME = "username";
    private static final String MESSAGE_TEXT = "message_text";
    private static final String MESSAGE_TIME_AGO = "message_time_ago";

    public String getUsername(){
        return USERNAME;
    }

    public String getMessageText(){
        return MESSAGE_TEXT;
    }

    public String getMessageTimeStamp(){
        return MESSAGE_TIME_AGO;
    }
}
