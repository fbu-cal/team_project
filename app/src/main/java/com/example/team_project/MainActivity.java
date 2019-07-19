package com.example.team_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.team_project.fragments.ComposeFragment;
import com.example.team_project.fragments.NotificationFragment;
import com.example.team_project.fragments.PostsFragment;
import com.example.team_project.fragments.ProfileFragment;


public class MainActivity extends AppCompatActivity {

    private static final int MESSENGER_REQUEST_CODE = 100;
    private BottomNavigationView mBottomNavigationView;

    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final FragmentManager fragmentManager = getSupportFragmentManager();
        // handle bottom navigation selection
        mBottomNavigationView = findViewById(R.id.bottom_navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_home:
                        fragment = new PostsFragment();
                        break;
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_notification:
                        fragment = new NotificationFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.container_flowlayout, fragment).commit();
                return true;
            }
        });
        // Set default selection
        mBottomNavigationView.setSelectedItemId(R.id.action_home);
//        mSearches = new ArrayList<Map<String, Object>>();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.miMessenger:
                goToMessenger();
                return true;
            case R.id.miSearch:
                goToSearch();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToSearch() {
        Intent toSearch = new Intent(this, SearchActivity.class);
        startActivity(toSearch);
    }

    // open ComposeActivity to create a new tweet
    private void goToMessenger() {
        Intent toMessenger = new Intent(this, MainMessenger.class);
        startActivityForResult(toMessenger, MESSENGER_REQUEST_CODE);
    }
}

