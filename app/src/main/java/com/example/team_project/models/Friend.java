package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Friend {

    public String uid;
    public boolean outboundRequest;
    public boolean inboundRequest;
    public boolean friends;
    public String timestamp;

    public Friend() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Friend(String uid, Boolean outboundRequest) {
        this.uid = uid;
        this.outboundRequest = outboundRequest;
        this.inboundRequest = false;
        this.friends = false;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("outboundRequest", outboundRequest);
        result.put("inboundRequest", inboundRequest);
        result.put("friends", friends);
        result.put("timestamp", timestamp);
        return result;
    }
    // [END post_to_map]

}
