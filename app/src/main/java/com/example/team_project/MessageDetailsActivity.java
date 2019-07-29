package com.example.team_project;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
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
    private String uid;
    public String username;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        mRecyclerView = findViewById(R.id.rvMessages);
        mMessages = new ArrayList<>();
        // construct the adapter from this data source
        mMessageAdapter = new MessageAdapter(this, mMessages);
        // RecyclerView setup (layout manager, adapter)
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        //set the adapter
        mRecyclerView.setAdapter(mMessageAdapter);

        uid = getIntent().getStringExtra("uid");
        username = getIntent().getStringExtra("username");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            if (username != null) {
                actionBar.setTitle(username);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayShowHomeEnabled(false);
            }
        }
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String messageText = mMessageTextInput.getText().toString();
//                final String uid = "M10nBkyMAfatCi4fwgr1KmB0UFz1";
                final String receiverId = getIntent().getStringExtra("uid");
                mDatabaseReference.child("users").child(senderId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @TargetApi(Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Get user value
                                User user = dataSnapshot.getValue(User.class);
                                // [START_EXCLUDE]
//                                if (user == null) {
//                                    // User is null, error out
//                                    Log.e("MessageDetailsActivity", "User " + senderId + " is unexpectedly null");
//                                } else {
                                    // Write new message

                                    sendMessage(senderId, receiverId ,user.username, messageText);
                                //}
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("MessageDetailsActivity", "getUser:onCancelled", databaseError.toException());
                            }
                        }
                );
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        populateMessages(currentUserId, uid);
        findUser();

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage(String senderId, String receiverId ,String username, String messageText) {
        String key = mDatabaseReference.child("messages").push().getKey();
        Message message = new Message(senderId,receiverId,username, messageText);
        //set message delivery time
        Date date = new Date();
        message.setTimeSent(date);
        Map<String, Object> messageValues = message.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        //childUpdates.put("/messages/" + key, messageValues);
        childUpdates.put("/user-messages/" + receiverId + "/" + senderId + "/" + key, messageValues);

        childUpdates.put("/user-messages/" + senderId + "/" + receiverId + "/" + key, messageValues);

        mDatabaseReference.updateChildren(childUpdates);

        mMessageTextInput.setText("");
    }

    public Query getQuery(DatabaseReference databaseReference, String currentUser, String receiverId) {
        // [START recent_messages_query]
        // due to sorting by push() keys
        Query recentMessagesQuery = databaseReference.child("user-messages/" + currentUser + "/" + receiverId);
        // [END recent_messages_query]

        return recentMessagesQuery;
    }

    public void populateMessages(String currentUser, String receiverId){

        // Set up FirebaseRecyclerAdapter with the Query
        final Query messagesQuery = getQuery(mDatabaseReference, currentUser, receiverId);
        Log.i("MessageDetailsActivity", messagesQuery.toString());
        // Retrieve new messages as they are added to Firebase
        messagesQuery.addChildEventListener(new ChildEventListener() {
            // Retrieve new messages as they are added to Firebase
            //@Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newMessage = (Map<String, Object>) snapshot.getValue();
                Log.i("MessageDetailsActivity", "Username: " + newMessage.get("username"));

                mMessages.add(newMessage);
                mMessageAdapter.notifyItemInserted(mMessages.size()-1);
                Log.i("MessageDetailsActivity", "" + mMessages.size());


                System.out.println("Author: " + newMessage.get("username"));
                System.out.println("Title: " + newMessage.get("messageText"));
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

    public String getReceiverId(){
        return null;
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
                    //mUsername.setText(username);
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
