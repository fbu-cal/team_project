package com.example.team_project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.team_project.models.Post;
import com.example.team_project.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText mUsername, mEmail, mPassword, mFullname;
    private Button mSignupButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        initializeUI();

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    private void registerNewUser() {
        // show progress bar

        final String fullname, username, email, password;
        fullname = mFullname.getText().toString();
        username = mUsername.getText().toString();
        email = mEmail.getText().toString();
        password = mPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("SignupActivity", "createUser:onComplete:" + task.isSuccessful());
//                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(SignupActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = mUsername.getText().toString();
        String fullname = mFullname.getText().toString();
        // Write new user
        writeNewUser(fullname, user.getUid(), username, user.getEmail());
        // Go to MainActivity
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish();
    }

    private void writeNewUser(String fullname, String userId, String name, String email) {
        Map<String, Object> user = (new User(fullname, userId, name, email)).toMap();
        mDatabase.child("users").child(userId).setValue(user);
    }


    private void initializeUI() {
        mFullname = findViewById(R.id.fullname_edit_text);
        mUsername = findViewById(R.id.username_edit_text);
        mEmail = findViewById(R.id.email_edit_text);
        mPassword = findViewById(R.id.password_edit_text);
        mSignupButton = findViewById(R.id.signup_button);
        // progress bar
    }
}
