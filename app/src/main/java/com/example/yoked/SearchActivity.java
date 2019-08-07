package com.example.yoked;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;

import com.example.yoked.R;
import com.example.yoked.models.Conversation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class SearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private ArrayList<Map<String, Object>> mSearches;
    private String[] usernameArray;
    private RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private LinearLayoutManager mLinearLayoutManager;

    public ImageButton mSearchButton;
    public SpinnerDialog mSpinnerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_actionbar, menu);
        // Set up Layout Manager, reverse layout
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);
        // find the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.post_recycler_view);
        // init the arraylist (data source)
        mSearches = new ArrayList<Map<String, Object>>();
        // construct the adapter from this data source
        mSearchAdapter = new SearchAdapter(this, mSearches);
        // RecyclerView setup (layout manager, use adapter)
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // set the adapter
        mRecyclerView.setAdapter(mSearchAdapter);

        mSearchButton = findViewById(R.id.back_image_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("searchButton","Click successful");
                goToSearch();
            }
        });

//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // perform query here
//                searchUser(query);
//                mSearchAdapter.notifyDataSetChanged();
//                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
//                // see https://code.google.com/p/android/issues/detail?id=24599
//                mSearchView.clearFocus();
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                searchUser(newText);
//                mSearchAdapter.notifyDataSetChanged();
//                return false;
//            }
//        });
        return super.onCreateOptionsMenu(menu);
    }

    private void searchUser(String s) {
        mSearches.clear();
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("users").orderByChild("username")
                .startAt(s)
                .endAt(s + "\uf8ff");
        query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
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
                mSpinnerDialog = new SpinnerDialog(SearchActivity.this, userList, "Search Users");
                mSpinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
                    @Override
                    public void onClick(String s, int i) {
                        String targetUserUid = userUidList.get(i);
                        findUser(targetUserUid);
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

    public void findUser (final String targetUserUid) {
        final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabaseReference.child("users").child(targetUserUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String username = newUser.get("username").toString();
                String targetUserUsername = username;
                Log.i("SearchAdapter", "Username" + targetUserUsername);
                Intent intent = new Intent(SearchActivity.this, MessageDetailsActivity.class);
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