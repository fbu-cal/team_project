package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    public String uid;
    public String username;
    public String email;
    public String fullname;
    public String profilePicture;
    public Map<String, String> friendStatuses = new HashMap<String, String>();
    public Map<String, String> friendList = new HashMap<String, String>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullname, String uid, String username, String email, String profilePicture) {
        this.fullname = fullname;
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("email", email);
        result.put("fullname", fullname);
        result.put("profile_picture", profilePicture);
        result.put("friendStatuses", friendStatuses);
        result.put("friendList", friendList);
        return result;
    }

}