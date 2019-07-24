package com.example.team_project;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.team_project.models.Message;
import com.example.team_project.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageDetailsActivity extends AppCompatActivity {

    private EditText mMessageTextInput;
    private Button mSendButton;
    private RecyclerView mRecyclerView;
    private ArrayList<Map<String, Object>> mMessages;
    private MessageAdapter mMessageAdapter;

    private LinearLayoutManager mLinearLayoutManager;


    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        mRecyclerView = findViewById(R.id.rvMessages);
        mMessages = new ArrayList<Map<String, Object>>();
        // construct the adapter from this data source
        mMessageAdapter = new MessageAdapter(this, mMessages);
        // RecyclerView setup (layout manager, adapter)
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        //set the adapter
        mRecyclerView.setAdapter(mMessageAdapter);



        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String messageText = mMessageTextInput.getText().toString();

                mDatabaseReference.child("users").child(userId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Get user value
                                User user = dataSnapshot.getValue(User.class);
                                // [START_EXCLUDE]
                                if (user == null) {
                                    // User is null, error out
                                    Log.e("MessageDetailsActivity", "User " + userId + " is unexpectedly null");
                                } else {
                                    // Write new post
                                    writeNewPost(userId, user.username, messageText);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("MessageDetailsActivity", "getUser:onCancelled", databaseError.toException());
                            }
                        }
                );
                final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            }
        });

        //displayChatMessages();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        populateMessages();
        //Log.i("MessageDetailsActivity", "" + mMessages.size());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void writeNewPost(String userId, String username, String messageText) {
        String key = mDatabaseReference.child("messages").push().getKey();
        Message message = new Message(userId,username, messageText);
        //set message delivery time
        Date date = new Date();
        message.setTimeSent(date);
        Map<String, Object> postValues = message.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/messages/" + key, postValues);
        childUpdates.put("/user-messages/" + username + "/" + key, postValues);

        mDatabaseReference.updateChildren(childUpdates);

        mMessageTextInput.setText("");
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("messages");
        // [END recent_posts_query]

        return recentPostsQuery;
    }

    public void populateMessages(){

        // Set up FirebaseRecyclerAdapter with the Query
        final Query postsQuery = getQuery(mDatabaseReference);
        Log.i("MessageDetailsActivity", postsQuery.toString());
        // Retrieve new posts as they are added to Firebase
        postsQuery.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to Firebase
            //@Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                Log.i("MessageDetailsActivity", "Username: " + newPost.get("username"));
//                        Log.i("PostsFragment", "Description: " + newPost.get("body"));
                mMessages.add(newPost);
                mMessageAdapter.notifyItemInserted(mMessages.size()-1);
                Log.i("MessageDetailsActivity", "" + mMessages.size());


                System.out.println("Author: " + newPost.get("username"));
                System.out.println("Title: " + newPost.get("messageText"));
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
