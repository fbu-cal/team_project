package com.example.team_project.models;

public class Message {

    private String mUsername;
    private String mMessageText;
    private long mMessageTimeStamp;

    public Message(String username, String messageText){
        this.mUsername= username;
        this.mMessageText= messageText;
    }

    public String getUsername(){
        return mUsername;
    }

    public void setUsername(String username){
        this.mUsername=username;
    }

    public String getMessageText(){
        return mMessageText;
    }

    public void setMessageText(String messageText){
        this.mMessageText=messageText;
    }

    public long getMessageTimeStamp(){
        return mMessageTimeStamp;
    }

    public void setMessageTimeStamp(long timeStamp){
        this.mMessageTimeStamp = timeStamp;
    }
}
