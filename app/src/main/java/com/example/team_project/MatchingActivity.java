package com.example.team_project;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.example.team_project.MainActivity;
import com.example.team_project.R;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import java.util.ArrayList;


public class MatchingActivity extends Activity {

    private ArrayList<String> al;
    private ArrayAdapter<String> arrayAdapter;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        /**
         * Adding to al is the name card,
         * i should keep the userId here and
         * add a new box with info about the user
         * maybe profile image
         */
        al = new ArrayList<>();
        al.add("php");
        al.add("c");
        al.add("python");
        al.add("java");
        al.add("html");
        al.add("c++");
        al.add("css");
        al.add("javascript");

        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_choice, R.id.helloText, al );

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
                al.remove(0);
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
                al.add("XML ".concat(String.valueOf(i)));
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

}

