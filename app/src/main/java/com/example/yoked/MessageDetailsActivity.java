package com.example.yoked;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yoked.models.Message;
import com.example.yoked.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageDetailsActivity extends AppCompatActivity {

    private EditText mMessageTextInput;
    private Button mSendButton;
    private RecyclerView mRecyclerView;
    private ArrayList<Map<String, Object>> mMessages;
    private MessageAdapter mMessageAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private String uid;
    public String username;
    private Parcelable conversation;
    String currentUserId;
    String mConversationKey;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        mMessageTextInput = findViewById(R.id.etMessageText);
        mSendButton = findViewById(R.id.btnSend);
        mRecyclerView = findViewById(R.id.rvMessages);
        mMessages = new ArrayList<>();
        // construct the adapter from this data source
        mMessageAdapter = new MessageAdapter(this, mMessages);
        // RecyclerView setup (layout manager, adapter)
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        //set the adapter
        mRecyclerView.setAdapter(mMessageAdapter);

        uid = getIntent().getStringExtra("uid");
        username = getIntent().getStringExtra("username");
        //conversation = getIntent().getBundleExtra("conversation");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null) {
            if (username != null) {
                actionBar.setTitle(username);
            } else {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String messageText = mMessageTextInput.getText().toString();
                final String receiverId = getIntent().getStringExtra("uid");
                mDatabaseReference.child("users").child(senderId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @TargetApi(Build.VERSION_CODES.O)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Get user value
                                User user = dataSnapshot.getValue(User.class);
                                // [START_EXCLUDE]
//                                if (user == null) {
//                                    // User is null, error out
//                                    Log.e("MessageDetailsActivity", "User " + senderId + " is unexpectedly null");
//                                } else {
                                    // Write new message

                                    sendMessage(senderId, receiverId ,user.username, messageText);
                                //}
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w("MessageDetailsActivity", "getUser:onCancelled", databaseError.toException());
                            }
                        }
                );
            }
        });
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        populateMessages(currentUserId, uid);
        findUser();

    }

    public Query getQuery(DatabaseReference databaseReference, String currentUser, String otherUser) {
        // [START recent_messages_query]
        // due to sorting by push() keys
        //String conversationKey = getConversationKey(databaseReference, getOtherUser(currentUser, otherUser));
        Log.i("getQueryyy","" + mConversationKey);
        Query recentMessagesQuery = databaseReference.child("conversation-messages/" + currentUserId + "/" + mConversationKey);
        Log.i("MDA", "" + recentMessagesQuery);
        // [END recent_messages_query]

        return recentMessagesQuery;

    }

    public void populateMessages(final String currentUser, final String receiverId){

        //
        mMessageAdapter.findProfilePicture(getOtherUser(currentUser,receiverId));
        //set ConversationKey (it cannot be null)
        getConversationKey(mDatabaseReference,  currentUser, receiverId);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Query messagesQuery = getQuery( mDatabaseReference, currentUser, receiverId);//Do something after 100ms


        Log.i("MessageDetailsActivity", messagesQuery.toString());

        messagesQuery.addChildEventListener(new ChildEventListener() {
            // Retrieve new messages as they are added to Firebase
            //@Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newMessage = (Map<String, Object>) snapshot.getValue();
                Log.i("MessageDetailsActivity", "Username: " + newMessage.get("username"));

                mMessages.add(newMessage);
                mMessageAdapter.notifyItemInserted(mMessages.size()-1);
                Log.i("MessageDetailsActivity", "" + mMessages.size());


                System.out.println("Author: " + newMessage.get("username"));
                System.out.println("Title: " + newMessage.get("messageText"));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
            }
        }, 100);
    }

    public void findUser () {
        Query query = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("username");
        query.addChildEventListener(new ChildEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                Map<String, Object> newUser = (Map<String, Object>) snapshot.getValue();
                if (newUser.get("uid").toString().equals(uid)) {
                    uid = newUser.get("uid").toString();
                    username = newUser.get("username").toString();
                    //mUsername.setText(username);
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public String getOtherUser(String senderId, String receiverId){
        if (receiverId==currentUserId){
            return senderId;
        }else{
            return receiverId;
        }
    }

    private void sendMessage(final String senderId, final String receiverId , final String username, final String messageText){
        Query query = FirebaseDatabase.getInstance().getReference().child("user-conversations").child(currentUserId);
        Log.i("MessageDetails", "Q: " + query);
        query.addListenerForSingleValueEvent(new ValueEventListener() {// Retrieve new posts as they are added to Firebase
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean conversationExists = false;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Map<String, Object> map = (HashMap<String, Object>) data.getValue();

                    //determine if new conversation exists
                    if (receiverId.equals(map.get("otherUser"))) {
                        conversationExists = true;
                    }
                    if (conversationExists) {
                        //if conversation exists then
                        //grab old conversation key
                        String conversationKey = data.getKey();
                        //make new message
                        String key = mDatabaseReference.child("messages").push().getKey();
                        Message message = new Message(senderId, receiverId, username, messageText);


                        //conversation.latestMessageText = messageText;
                        //set message delivery time
                        Date date = new Date();
                        message.setTimeStamp(date);
                        Map<String, Object> messageValues = message.toMap();

                        Map<String, Object> childUpdates = new HashMap<>();
                        //childUpdates.put("/messages/" + key, messageValues);


                        //Log.i("MessageDetailsActivity", mDatabaseReference.child("conversations/" + senderId + "/" + conversationKey).toString());
                        //childUpdates.put("/user-messages/" + receiverId + "/" + senderId + "/" + key, messageValues);

                        //childUpdates.put("/user-messages/" + senderId + "/" + receiverId + "/" + key, messageValues);
                        //put message in conv-messages
                        childUpdates.put("/conversation-messages/"  + currentUserId + "/"+ conversationKey + "/" + key, messageValues);
                        childUpdates.put("/conversation-messages/"  + receiverId + "/"+ conversationKey + "/" + key, messageValues);

                        DatabaseReference ref = FirebaseDatabase.getInstance()
                                .getReference("user-conversations")
                                .child(currentUserId)
                                .child(conversationKey)
                                .child("latestMessageText");
                        ref.setValue(messageText);

                        DatabaseReference timeStamp = FirebaseDatabase.getInstance()
                                .getReference("user-conversations")
                                .child(currentUserId)
                                .child(conversationKey)
                                .child("timeStamp");
                        timeStamp.setValue(date);

                        mDatabaseReference.updateChildren(childUpdates);
                        mMessageTextInput.setText("");
                        break;
                    }
                }
                if (!conversationExists) {
                    String key = mDatabaseReference.child("messages").push().getKey();
                    Message message = new Message(senderId, receiverId, username, messageText);

                    String conversationKey = mDatabaseReference.child("conversations").push().getKey();
                    //Conversation conversation = new Conversation(currentUserId, getOtherUser(senderId, receiverId));
                    //Map<String, Object> conversationValues = conversation.toMap();

                    //set message delivery time
                    Date date = new Date();
                    message.setTimeStamp(date);

                    Map<String, Object> messageValues = message.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();

                    //childUpdates.put("user-conversations/" + currentUserId + "/" + conversationKey, conversationValues);
                    childUpdates.put("/conversation-messages/" + currentUserId + "/" + conversationKey + "/" + key, messageValues);

                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("user-conversations")
                            .child(currentUserId)
                            .child(conversationKey)
                            .child("latestMessageText");
                    ref.setValue(messageText);

                    DatabaseReference timeStamp = FirebaseDatabase.getInstance()
                            .getReference("user-conversations")
                            .child(currentUserId)
                            .child(conversationKey)
                            .child("timeStamp");
                    timeStamp.setValue(date);

                    mDatabaseReference.updateChildren(childUpdates);
                    mMessageTextInput.setText("");
                    //mDatabaseReference.child("user-conversations").child(currentUserId).
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void getConversationKey(final DatabaseReference databaseReference, final String currentUser, final String otherUser){

        Query query = FirebaseDatabase.getInstance().getReference().child("user-conversations").child(currentUserId);
        Log.i("MessageDetails", "Q: " + query);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            // Retrieve new messages as they are added to Firebase
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean isCorrectConversation = false;
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Map<String, Object> map = (HashMap<String, Object>) data.getValue();
                    if (map.get("otherUser").equals(otherUser)){
                        isCorrectConversation = true;
                    }
                    if (isCorrectConversation){
                        setConvoKey(data.getKey());
                        Log.i("conversationKey", data.getKey());
                        break;
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void setConvoKey(String conversationKey){
        mConversationKey = conversationKey;
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public void findProfilePicture (String userId) {
        Query query = mDatabaseReference.child("users").child(userId);
        Log.i("ConversationView", userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                String imageUrl = newUser.get("profile_picture").toString();
                // if profile pic is already set
                if (!imageUrl.equals("")) {
                    Log.i("PostViewHolder", "imageUrl: " + imageUrl);
                    try {
                        // set profile picture
                        Bitmap realImage = getCircleBitmap(decodeFromFirebaseBase64(imageUrl));

                        Log.i("PostViewHolder", "realImage: " + realImage);
                        //.setImageBitmap(realImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("PostViewHolder", "Profile pic issue", e);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OtherUser", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

}
