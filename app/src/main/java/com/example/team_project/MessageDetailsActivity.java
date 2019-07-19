package com.example.team_project;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.team_project.models.Message;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MessageDetailsActivity extends AppCompatActivity {

    EditText mMessageTextInput;
    Button mSendButton;
    FirebaseListAdapter<Message> adapter;
    ListView mListViewMessages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        mListViewMessages = findViewById(R.id.lvMessageDetails);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        Query query = FirebaseDatabase.getInstance().getReference().child("chats");
        FirebaseListOptions<Message> options =
                new FirebaseListOptions.Builder<Message>()
                        .setQuery(query, Message.class)
                        .setLayout(android.R.layout.simple_list_item_1)
                        .build();
        adapter = new FirebaseListAdapter<Message>(options){

//        adapter = new FirebaseListAdapter<Message>(this, Message.class,
//                R.layout.message_item, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, Message model, int position) {
                // Get references to the views of message_item.xml
                TextView messageText = v.findViewById(R.id.tvMessageText);
                TextView messageUser = v.findViewById(R.id.tvUsername);
                TextView messageTime = v.findViewById(R.id.tvDate);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getUsername());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTimeStamp()));
            }
        };

        mListViewMessages.setAdapter(adapter);
    }
    public void sendMessage(){
        // Read the input field and push a new instance
        // of ChatMessage to the Firebase database
        FirebaseDatabase.getInstance()
                .getReference()
                .push()
                .setValue(new Message(mMessageTextInput.getText().toString(),
                        FirebaseAuth.getInstance()
                                .getCurrentUser()
                                .getDisplayName())
                );

        // Clear the input
        mMessageTextInput.setText("");
    }
}
