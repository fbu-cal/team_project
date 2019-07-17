package com.example.team_project.models;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String email;
    private String password;
    private String photoUrl;
    private String uid;
    private Calendar calendar;


    public User() {
        //Empty Constructor For Firebase
    }

    public User(String username, String email, String password, String photoUrl, String uid, Calendar calendar)
    {
        this.username = username; //Parameterized for Program-Inhouse objects.
        this.email = email;
        this.password = password;
        this.photoUrl = photoUrl;
        this.uid = uid;
        this.calendar = calendar;
    }

    //Getters and Setters
    public String getUsername()
    {
        return username;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
