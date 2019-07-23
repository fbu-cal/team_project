package com.example.team_project;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.team_project.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageDetailsActivity extends AppCompatActivity {

    private EditText mMessageTextInput;
    private Button mSendButton;
    //private FirebaseListAdapter<Message> adapter;
    //private RecyclerView mRecycelerView;
    private RecyclerView mRecyclerView;
    private ArrayList<Map<String, Object>> mMessages;
    private MessageAdapter mMessageAdapter;

    private LinearLayoutManager mLinearLayoutManager;


    DatabaseReference mDatabaseReference;

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
        // RecyclerView setup (layout manager, use adapter)
         //set the adapter
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String messageText = mMessageTextInput.getText().toString();
        final String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessage();
                writeNewPost(userId,username, messageText);
            }
        });

        //displayChatMessages();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        populateMessages();
        //Log.i("MessageDetailsActivity", "" + mMessages.size());

    }
    public void sendMessage(){
        // Read the input field and push a new instance
        // of ChatMessage to the Firebase database
//        FirebaseDatabase.getInstance()
//                .getReference()
//                .child("messages")
//                .push()
//                .setValue(new Message(mMessageTextInput.getText().toString(),
//                        FirebaseAuth.getInstance()
//                                .getCurrentUser()
//                                .getDisplayName())
//                );
//
//        // Clear the input
//        mMessageTextInput.setText("");
    }

    private void writeNewPost( String userId, String username, String messageText) {
        String key = mDatabaseReference.child("messages").push().getKey();
        Message message = new Message(userId,username, messageText);
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

    public void  displayChatMessages(){
//        Query query = FirebaseDatabase.getInstance().getReference().child("message");
//        FirebaseListOptions<Message> options =
//                new FirebaseListOptions.Builder<Message>()
//                        .setQuery(query, Message.class)
//                        .setLayout(R.layout.message_item)
//                        .build();
//        adapter = new FirebaseListAdapter<Message>(options){
//
//            //        adapter = new FirebaseListAdapter<Message>(this, Message.class,
////                R.layout.message_item, FirebaseDatabase.getInstance().getReference()) {
//            @Override
//            protected void populateView(View v, Message model, int position) {
//                // Get references to the views of message_item.xml
//                TextView messageText = v.findViewById(R.id.tvMessageText);
//                TextView messageUser = v.findViewById(R.id.tvUsername);
//                TextView messageTime = v.findViewById(R.id.tvDate);
//
//                // Set their text
//                messageText.setText(model.getMessageText());
//                messageUser.setText(model.getUsername());
//
//                // Format the date before showing it
//                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
//                        model.getMessageTimeStamp()));
//
//                // Set up FirebaseRecyclerAdapter with the Query
//                final Query postsQuery = getQuery(mDatabaseReference);
//                //Log.i("PostsFragment", postsQuery.toString());
//                // Retrieve new posts as they are added to Firebase
//                postsQuery.addChildEventListener(new ChildEventListener() {
//                    // Retrieve new posts as they are added to Firebase
//                    //@Override
//                    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
//                        Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
//                        //Log.i("MessageDetailsActivity", "Username: " + newPost.get("username"));
////                        Log.i("PostsFragment", "Description: " + newPost.get("body"));
//                        mMessages.add(newPost);
//                        mMessageAdapter.notifyItemInserted(0);
//                        adapter.notifyDataSetChanged();
//                        Log.i("MessageDetailsActivity", "" + mMessages.size());
//
//
//                        System.out.println("Author: " + newPost.get("username"));
//                        System.out.println("Title: " + newPost.get("messageText"));
//                    }
//
//                    @Override
//                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    }
//
//                    @Override
//                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                    }
//
//                    @Override
//                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//
//                });
//            }
//        };

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
