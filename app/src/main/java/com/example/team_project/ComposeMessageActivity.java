package com.example.team_project;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Map;

public class ComposeMessageActivity extends AppCompatActivity {

    EditText mMessageTextInput;
    //EditText mUserSearchTextInput;
    Button mSendButton;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView mRecyclerView;
    private ArrayList<Map<String, Object>> mSearches;
    //private String uid;
    //public String username;
    //public TextView mUsername;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        //mUserSearchTextInput = findViewById(R.id.etUserSearch);
//        uid = getIntent().getStringExtra("uid");

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        //findUser();
    }

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar_compose_message, menu);

//        ActionBar actionBar = getActionBar();
//        if (actionBar!=null) {
//            if (username != null) {
//                actionBar.setTitle(username);
//            } else {
//                actionBar.setDisplayShowTitleEnabled(false);
//                actionBar.setDisplayShowHomeEnabled(false);
//            }
//        }
        // Set up Layout Manager, reverse layout
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        // find the RecyclerView
        mRecyclerView = findViewById(R.id.rvUserList);
        // init the arraylist (data source)
        mSearches = new ArrayList<>();
        // construct the adapter from this data source
        mSearchAdapter = new SearchAdapter(this, mSearches);
        // RecyclerView setup (layout manager, use adapter)
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // set the adapter
        mRecyclerView.setAdapter(mSearchAdapter);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchUser(query);
                mSearchAdapter.notifyDataSetChanged();
                Log.i("MainActivity", "This is it.");
                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                mSearchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUser(newText);
                mSearchAdapter.notifyDataSetChanged();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void sendMessage(){
//        FirebaseMessaging fm = FirebaseMessaging.getInstance();
//        fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
//                .setMessageId(Integer.toString(messageId))
//                .addData("my_message", "Hello World")
//                .addData("my_action","SAY_HELLO")
//                .build());
    }

    private void searchUser(String s) {
        mSearches.clear();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("username")
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                Log.i("MainActivity", "User Id: " + newUser.get("uid"));
                Log.i("MainActivity", "Username: " + newUser.get("username"));
                mSearches.add(newUser);
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

//    public void findUser () {
//        Query query = FirebaseDatabase.getInstance().getReference("users")
//                .orderByChild("username");
//        query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
//            @Override
//            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
//                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
//                if (newUser.get("uid").toString().equals(uid)) {
//                    uid = newUser.get("uid").toString();
//                    username = newUser.get("username").toString();
//                    //mUsername.setText(username);
//                }
//            }
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            }
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//            }
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
//    }
}
