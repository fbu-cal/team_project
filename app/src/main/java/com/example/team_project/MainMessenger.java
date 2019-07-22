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
import java.util.Map;
//this is an outrageous comment
public class MainMessenger extends AppCompatActivity {

    private MessageAdapter mAdapter;
    RecyclerView mRecyclerViewMessages;
    FloatingActionButton mComposeMessageButton;
    FloatingActionButton mStopGapMessageDetailsButton;
    ArrayList<Map<String,Object>> mMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_messenger);

        mRecyclerViewMessages = findViewById(R.id.rvMessages);
        mComposeMessageButton = findViewById(R.id.btnComposeMessage);
        mStopGapMessageDetailsButton = findViewById(R.id.btnStopGap);

        mMessages = new ArrayList<>();

        mAdapter = new MessageAdapter(this, mMessages);
        mRecyclerViewMessages.setAdapter(mAdapter);
        mRecyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        mComposeMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMessenger.this, ComposeMessageActivity.class);
                startActivity(intent);
            }
        });

        mStopGapMessageDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMessenger.this, MessageDetailsActivity.class);
                startActivity(intent);
            }
        });
    }
}
