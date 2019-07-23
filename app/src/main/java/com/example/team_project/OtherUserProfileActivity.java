package com.example.team_project;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Map;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String uid;
    private String username;

    private TextView mUsername;

    String tempUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mUsername = findViewById(R.id.username_text_view);

        // unwrap the post passed in via intent, using its simple name as a key
        uid = getIntent().getStringExtra("uid");
        // set variables with content from post
        findUser();


        Log.i("OtherUser", "EEEP: "+tempUsername);
    }

    public void findUser () {
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
            query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                if (newUser.get("uid").toString().equals(uid)) {
                    uid = newUser.get("uid").toString();
                    username = newUser.get("username").toString();
                    mUsername.setText(username);
                    tempUsername = username;
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
