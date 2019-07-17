package com.example.team_project;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.team_project.models.Message;

import java.util.ArrayList;

public class MainMessenger extends AppCompatActivity {

    private MessageAdapter mAdapter;
    RecyclerView mRecyclerViewMessages;
    FloatingActionButton mComposeButton;
    ArrayList<Message> mMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_messenger);

        mRecyclerViewMessages = findViewById(R.id.rvMessages);
        mComposeButton = findViewById(R.id.btnComposeMessage);

        mMessages = new ArrayList<>();

        mAdapter = new MessageAdapter(this, mMessages);
        mRecyclerViewMessages.setAdapter(mAdapter);
        mRecyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        mComposeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMessenger.this, ComposeMessageActivity.class);
                startActivity(intent);
            }
        });
    }
}
