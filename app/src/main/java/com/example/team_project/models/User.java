package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;
=======
import java.util.HashMap;
import java.util.Map;
>>>>>>> 4fe91584bf3db215247efc6d4ba57f1854255ee2

@IgnoreExtraProperties
public class User {

    public String uid;
    public String username;
    public String email;
    //Calendar calendar;
    List<String> mFreeTime = new ArrayList<String>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        //this.calendar = null;
    }

    public List<String> getmFreeTime() {
        return mFreeTime;
    }

<<<<<<< HEAD
    public void setmFreeTime(List<String> mFreeTime) {
        this.mFreeTime = mFreeTime;
    }
=======
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("email", email);
        return result;
    }

>>>>>>> 4fe91584bf3db215247efc6d4ba57f1854255ee2
}