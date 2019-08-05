package com.example.team_project.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String uid;
    public String author;
    public String body;
    public String postImageUrl;
    public int likeCount = 0;
    public Map<String, Boolean> likes = new HashMap<>();
    public Map<String, Boolean> comments = new HashMap<>();
    public String timestamp;
    public String taggedFriend;
    public String taggedFriendUid;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String author, String body, String postImageUrl, String timestamp, String taggedFriend, String taggedFriendUid) {
        this.uid = uid;
        this.author = author;
        this.body = body;
        this.postImageUrl = postImageUrl;
        this.timestamp = timestamp;
        this.taggedFriend = taggedFriend;
        this.taggedFriendUid = taggedFriendUid;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("body", body);
        result.put("postImageUrl", postImageUrl);
        result.put("likeCount", likeCount);
        result.put("likes", likes);
        result.put("comments", comments);
        result.put("timestamp", timestamp);
        result.put("taggedFriend", taggedFriend);
        result.put("taggedFriendUid", taggedFriendUid);
        return result;
    }
}
