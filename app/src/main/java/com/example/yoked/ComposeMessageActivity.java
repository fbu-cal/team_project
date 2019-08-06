package com.example.yoked;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ComposeMessageActivity extends AppCompatActivity {

    EditText mMessageTextInput;
    EditText mUserSearchTextInput;
    Button mSendButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_compose);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        mUserSearchTextInput = findViewById(R.id.etUserSearch);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    public void sendMessage(){
//        FirebaseMessaging fm = FirebaseMessaging.getInstance();
//        fm.send(new RemoteMessage.Builder(SENDER_ID + "@fcm.googleapis.com")
//                .setMessageId(Integer.toString(messageId))
//                .addData("my_message", "Hello World")
//                .addData("my_action","SAY_HELLO")
//                .build());
    }
}
