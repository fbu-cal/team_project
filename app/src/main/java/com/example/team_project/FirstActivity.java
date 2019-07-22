package com.example.team_project;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class FirstActivity extends AppCompatActivity {

    TextView mSignupButton, mLoginButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        mSignupButton = findViewById(R.id.signup_button);
        mLoginButton = findViewById(R.id.login_button);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(FirstActivity.this, MainActivity.class));
            finish();
        }

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toSignupPage = new Intent(FirstActivity.this, SignupActivity.class);
                startActivity(toSignupPage);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toLoginPage = new Intent(FirstActivity.this, LoginActivity.class);
                startActivity(toLoginPage);
            }
        });
    }
}
