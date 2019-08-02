package com.example.team_project;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private ArrayList<Map<String, Object>> mSearches;
    private String[] usernameArray;
    private RecyclerView mRecyclerView;
    private SearchAdapter mSearchAdapter;
    private SearchView mSearchView;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchUser(query);
                mSearchAdapter.notifyDataSetChanged();
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
}