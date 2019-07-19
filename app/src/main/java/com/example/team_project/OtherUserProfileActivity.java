package com.example.team_project;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.team_project.models.Post;
import com.example.team_project.models.User;

public class OtherUserProfileActivity extends AppCompatActivity {

    private String mUid;

    private TextView mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        mUsername = findViewById(R.id.username_text_view);

        // unwrap the post passed in via intent, using its simple name as a key
        mUid = getIntent().getStringExtra("uid");
        // set variables with content from post
        mUsername.setText(mUid);

    }
}
