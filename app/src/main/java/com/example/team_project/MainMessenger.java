package com.example.team_project;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.team_project.models.Conversation;
import com.example.team_project.models.Message;
import com.example.team_project.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.example.team_project.SearchViewHolder.decodeFromFirebaseBase64;

public class MainMessenger extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> mAdapter;
    RecyclerView mRecyclerViewConversations;
    FloatingActionButton mComposeMessageButton;
    ArrayList<Map<String,Object>> mConversations;
    private DatabaseReference mDatabaseReference;
    //private MessageAdapter mMessageAdapter;
    String username;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_messenger);

        mRecyclerViewConversations = findViewById(R.id.rvMessages);
        mComposeMessageButton = findViewById(R.id.btnComposeMessage);

        mConversations = new ArrayList<>();
        //mMessageAdapter = new MessageAdapter(this, mConversations);
        //mRecyclerViewConversations.setAdapter(mMessageAdapter);

        //mAdapter = new MessageAdapter(this, mConversations);
        //mRecyclerViewConversations.setAdapter(mAdapter);
        mRecyclerViewConversations.setLayoutManager(new LinearLayoutManager(this));

        mComposeMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMessenger.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // populateConversations(currentUserId);

        Log.i("MainMessenger", getQuery(mDatabaseReference, currentUserId).toString());

        mAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(Conversation.class, R.layout.item_conversation,
                ConversationViewHolder.class, getQuery(mDatabaseReference, currentUserId)) {
            @Override
            protected void populateViewHolder(ConversationViewHolder viewHolder,final Conversation model, final int position) {
                final DatabaseReference postRef = getRef(position);
                Log.i("MainMessenger", "" + model.otherUser);

                // Set click listener for the whole post view
                //final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        findUser(model.otherUser);
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model);
            }
        };
        mRecyclerViewConversations.setAdapter(mAdapter);
    }

    public Query getQuery(DatabaseReference databaseReference, String currentUser) {
        // [START recent_messages_query]
        // due to sorting by push() keys
        Query recentConversationsQuery = databaseReference.child("user-conversations/" + currentUser);

        // [END recent_messages_query]

        return recentConversationsQuery;
    }

    public Query getQuery(DatabaseReference databaseReference, String currentUser, String receiverId) {
        // [START recent_messages_query]
        // due to sorting by push() keys
        Query recentMessagesQuery = databaseReference.child("user-conversations/" + currentUser );
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
                Map<String, Object> newConversation = (Map<String, Object>) snapshot.getValue();
                Log.i("MessageDetailsActivity", "Username: " + newConversation.get("username"));

                mConversations.add(newConversation);
                mAdapter.notifyItemInserted(mConversations.size()-1);

                System.out.println("Author: " + newConversation.get("username"));
                System.out.println("Title: " + newConversation.get("messageText"));
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

    // find the target users username and send intent
    public void findUser (final String userId) {
        Query query = mDatabaseReference.child("users").child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                username = newUser.get("username").toString();
                Intent intent = new Intent(MainMessenger.this, MessageDetailsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("uid", userId);
                startActivity(intent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }


}
