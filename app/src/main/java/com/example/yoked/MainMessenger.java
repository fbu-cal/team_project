package com.example.yoked;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.yoked.models.Conversation;
import com.example.yoked.models.Message;
import com.example.yoked.models.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class MainMessenger extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> mAdapter;
    RecyclerView mRecyclerViewConversations;
    FloatingActionButton mComposeMessageButton;
    ArrayList<Map<String,Object>> mConversations;
    private DatabaseReference mDatabaseReference;
    String username;
    String currentUserId;

    public SpinnerDialog mSpinnerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_messenger);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

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
//                Intent intent = new Intent(MainMessenger.this, SearchActivity.class);
//                startActivity(intent);
                Log.i("searchButton","Click successful");
                goToSearch();
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

    public void populateMessages(String currentUser){

        // Set up FirebaseRecyclerAdapter with the Query
        final Query messagesQuery = getQuery(mDatabaseReference, currentUser);
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


    private void goToSearch() {
        addSpinner();
    }

    private void addSpinner() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> userList = new ArrayList<String>();
                final ArrayList<String> userUidList = new ArrayList<String>();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> newUser = (Map<String, Object>) data.getValue();
                    userList.add(newUser.get("username").toString());
                    userUidList.add(newUser.get("uid").toString());
                }
                // spinner
                mSpinnerDialog = new SpinnerDialog(MainMessenger.this, userList, "Search Users");
                mSpinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String s, int i) {
                        String targetUserUid = userUidList.get(i);
                        sendComposeMessageIntent(targetUserUid);
                    }
                });
                mSpinnerDialog.showSpinerDialog();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    public void sendComposeMessageIntent (final String targetUserUid) {
        final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child("users").child(targetUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String username = newUser.get("username").toString();
                String targetUserUsername = username;
                Log.i("SearchAdapter", "Username" + targetUserUsername);
                Intent intent = new Intent(MainMessenger.this, MessageDetailsActivity.class);
                intent.putExtra("username", targetUserUsername);
                intent.putExtra("uid", targetUserUid);
                // TODO - move into a method
                final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String receiverId = targetUserUid;
                Query query = FirebaseDatabase.getInstance().getReference().child("user-conversations").child(currentUserId);
                Log.i("MessageDetails", "Q: " + query);
                query.addListenerForSingleValueEvent(new ValueEventListener() {// Retrieve new posts as they are added to Firebase
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean conversationExists = false;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Map<String, Object> map = (HashMap<String, Object>) data.getValue();
                            if (receiverId.equals(map.get("otherUser"))) {
                                conversationExists = true;
                            }
                            if (conversationExists) {
                                break;
                            }
                        }
                        if (!conversationExists){
                            String conversationKey = mDatabaseReference.child("conversations").push().getKey();
                            Conversation conversation = new Conversation(currentUserId, receiverId);
                            Map<String,Object> conversationValues = conversation.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put("user-conversations/" + currentUserId + "/" + conversationKey, conversationValues);
                            childUpdates.put("user-conversations/" + receiverId + "/" + conversationKey, conversationValues);
                            mDatabaseReference.updateChildren(childUpdates);
                            //Intent intent = new Intent(context, MessageDetailsActivity.class);
                            //intent.putExtra("conversation", (Parcelable) conversation);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                // show the activity
                startActivity(intent);


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }
}
