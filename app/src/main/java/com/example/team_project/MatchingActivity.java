package com.example.team_project;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.ArrayList;
import java.util.HashMap;


public class MatchingActivity extends Activity {

    private ArrayList<String> mMatches;
    private ArrayAdapter<String> arrayAdapter;
    private int i;
    public HashMap<String, Object> mNewCalendar;
    DatabaseReference mReference;
    private HashMap<String, Boolean> mPosts;
    private String mUserId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        mReference = FirebaseDatabase.getInstance().getReference();
        mPosts = new HashMap<String, Boolean>();
        mAuth = FirebaseAuth.getInstance();
        mUserId = mAuth.getCurrentUser().getUid();
        getUserCalendar();
        /**
         * Adding to mMatches is the name card,
         * i should keep the userId here and
         * add a new box with info about the user
         * maybe profile image
         */
        mMatches = new ArrayList<>();
        mMatches.add("php");
        mMatches.add("c");
        mMatches.add("python");
        mMatches.add("java");
        mMatches.add("html");
        mMatches.add("c++");
        mMatches.add("css");
        mMatches.add("javascript");

        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_choice, R.id.helloText, mMatches);

        /**
         * Removes the cards from the array here
         */
        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                mMatches.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            /**
             *If it comes out left then the user does not want to
             * hangout with them so nothing needs to be done
             */

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                //makeToast, "Left!");
                Toast.makeText(MatchingActivity.this, "swiped left", Toast.LENGTH_SHORT).show();

            }

            /**
             * If swiped right then add it to somewhere on database
             * and check if other user also swiped right
             * if they do hit them both with a notification
             * if not let them know later
             */
            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(MatchingActivity.this, "swiped right", Toast.LENGTH_SHORT).show();
                //makeToast(MyActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                mMatches.add("XML ".concat(String.valueOf(i)));
                arrayAdapter.notifyDataSetChanged();
                Log.d("LIST", "notified");
                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        /**
         * Can implement a closer look if wanted here
         */
        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

            }
        });
    }

    private void getUserCalendar() {
        final Query query = mReference.child("user-calendar/" + mUserId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //for (DataSnapshot data : dataSnapshot.getChildren()) {
                mNewCalendar = (HashMap<String, Object>) dataSnapshot.getValue();
                Log.i("CalendarActivity", "Free Time: " + mNewCalendar.get("mFreeTime"));
                Log.i("CalendarActivity", "UserId: " + mNewCalendar.get("userId"));
                Log.i("CalendarActivity", "!!!Map: " + mNewCalendar);
                //}
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
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mNewCalendar != null) {
                    mPosts = (HashMap<String, Boolean>) mNewCalendar.get("mFreeTime");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}

