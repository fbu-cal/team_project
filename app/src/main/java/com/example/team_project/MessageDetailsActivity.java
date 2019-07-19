package com.example.team_project;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_project.models.Message;
import com.example.team_project.models.Post;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
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
    private FirebaseListAdapter<Message> adapter;
    private RecyclerView mRecycelerView;
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
        mRecycelerView = findViewById(R.id.rvMessages);
        mMessages = new ArrayList<Map<String, Object>>();
        mLinearLayoutManager = new LinearLayoutManager(this);
        // construct the adapter from this data source
        mMessageAdapter = new MessageAdapter(this, mMessages);
        // RecyclerView setup (layout manager, use adapter)
        mRecycelerView.setLayoutManager(mLinearLayoutManager);
        // set the adapter
        mRecycelerView.setAdapter(mMessageAdapter);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sendMessage();
                writeNewPost(FirebaseAuth.getInstance().getCurrentUser().getUid(), mMessageTextInput.getText().toString());
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference().child("chats");
        FirebaseListOptions<Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(android.R.layout.simple_list_item_1)
                        .build();
        adapter = new FirebaseListAdapter<Message>(options){

//        adapter = new FirebaseListAdapter<Message>(this, Message.class,
//                R.layout.message_item, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, Message model, int position) {
                // Get references to the views of message_item.xml
                TextView messageText = v.findViewById(R.id.tvMessageText);
                TextView messageUser = v.findViewById(R.id.tvUsername);
                TextView messageTime = v.findViewById(R.id.tvDate);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getUsername());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTimeStamp()));

                // Set up FirebaseRecyclerAdapter with the Query
                final Query postsQuery = getQuery(mDatabaseReference);
                //Log.i("PostsFragment", postsQuery.toString());
                // Retrieve new posts as they are added to Firebase
                postsQuery.addChildEventListener(new ChildEventListener() {
                    // Retrieve new posts as they are added to Firebase
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                        Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                        Log.i("MessageDetailsActivity", "Username: " + newPost.get("username"));
//                        Log.i("PostsFragment", "Description: " + newPost.get("body"));
                        mMessages.add(newPost);
                        mMessageAdapter.notifyItemInserted(mMessages.size() - 1);
//                System.out.println("Author: " + newPost.get("author"));
//                System.out.println("Title: " + newPost.get("body"));
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
        };

        mRecycelerView.setAdapter(mMessageAdapter);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
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

    private void writeNewPost( String username, String messageText) {
        String key = mDatabaseReference.child("messages").push().getKey();
        Message message = new Message(username,messageText);
        Map<String, Object> postValues = message.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/messages/" + key, postValues);
        childUpdates.put("/user-messages/" + username + "/" + key, postValues);

        mDatabaseReference.updateChildren(childUpdates);

        Toast.makeText(this, "Post Successful!", Toast.LENGTH_LONG).show();
        mMessageTextInput.setText("");

        Intent launchPosts = new Intent(this, MainActivity.class);
        startActivity(launchPosts);
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("posts")
                .limitToFirst(20);
        // [END recent_posts_query]

        return recentPostsQuery;
    }

}
