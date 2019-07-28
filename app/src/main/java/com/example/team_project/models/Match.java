package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Match {
    public String userId;
    //status: noOverlap, overlap, oneUser, denied, match
    public HashMap<String, String> status;
    //split into this satMor, friMor... linked to users that are free that time
//    public HashMap<String, String> userFreeMatchBefore;
//    //user to the time they free
//    public HashMap<String, String> userFreeMatchFinal;

    public Match() {

    }

    public Match(String userId, HashMap<String, String> status) {
        this.userId = userId;
        this.status = status;
//        this.userFreeMatchBefore = userFreeMatchBefore;
//        this.userFreeMatchFinal = userFreeMatchFinal;
    }

    public String getUserId() {
        return userId;
    }

    public HashMap<String, String> getStatus() {
        return status;
    }

//    public HashMap<String, String> getUserFreeMatchBefore() {
//        return userFreeMatchBefore;
//    }
//
//    public HashMap<String, String> getUserFreeMatchFinal() {
//        return userFreeMatchFinal;
//    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setStatus(HashMap<String, String> status) {
        this.status = status;
    }

//    public void setUserFreeMatchBefore(HashMap<String, String> userFreeMatchBefore) {
//        this.userFreeMatchBefore = userFreeMatchBefore;
//    }
//
//    public void setUserFreeMatchFinal(HashMap<String, String> userFreeMatchFinal) {
//        this.userFreeMatchFinal = userFreeMatchFinal;
//    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("status", status);
//        result.put("userFreeMatchBefore", userFreeMatchBefore);
//        result.put("userFreeMatchFinal", userFreeMatchFinal);
        return result;
    }
}
