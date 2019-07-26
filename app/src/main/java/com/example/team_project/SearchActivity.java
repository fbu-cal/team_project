package com.example.team_project;

import android.content.Intent;
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
import android.widget.Toast;

import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;


    private SearchView mSearchView;
    private FirebaseRecyclerAdapter<User, SearchViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

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
        mRecycler = (RecyclerView) findViewById(R.id.post_recycler_view);
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchUser(query);
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUser(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void searchUser(String s) {
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(s);
        mAdapter = new FirebaseRecyclerAdapter<User, SearchViewHolder>(User.class, R.layout.item_search,
                SearchViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(SearchViewHolder viewHolder, final User model, int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (model.uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            Toast.makeText(SearchActivity.this, "Clicking on your own profile", Toast.LENGTH_SHORT);
                        }
                        else {
                            // Launch OtherUserProfileActivity
                            Intent intent = new Intent(SearchActivity.this, OtherUserProfileActivity.class);
                            intent.putExtra("uid", model.uid);
                            // show the activity
                            startActivity(intent);
                        }
                    }
                });
                // Bind Post to ViewHolder, setting OnClickListener for the star button
                try {
                    viewHolder.bindToPost(model);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mRecycler.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public Query getQuery(String s) {
        Query recentPostsQuery = FirebaseDatabase.getInstance().getReference("users").orderByChild("username")
                .startAt(s)
                .endAt(s + "\uf8ff");
        return recentPostsQuery;
    }
}
