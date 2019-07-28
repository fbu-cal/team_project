package com.example.team_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class UserSettingsActivity extends AppCompatActivity {

    private Button mLogoutButton, mUploadPictureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        mLogoutButton = findViewById(R.id.logout_button);
        mUploadPictureButton = findViewById(R.id.upload_picture_button);

        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent (UserSettingsActivity.this, FirstActivity.class);
                startActivity(i);
            }
        });

        mUploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toUpload = new Intent (UserSettingsActivity.this, ProfilePictureActivity.class);
                startActivity(toUpload);
            }
        });
    }
}
