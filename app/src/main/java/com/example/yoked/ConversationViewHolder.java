package com.example.yoked;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yoked.models.Conversation;
import com.example.yoked.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ConversationViewHolder extends RecyclerView.ViewHolder {

    public TextView mUsernameTextView;
    public TextView mLastMessage;
    public ImageView mProfilePicture;
    public TextView mTimeStamp;
    private DatabaseReference mDatabaseReference;
    String mUsername;
    String mCurrentUserId;


    public ConversationViewHolder(View itemView) {
        super(itemView);

        mUsernameTextView = itemView.findViewById(R.id.tvUsername);
        mTimeStamp = itemView.findViewById(R.id.tvDate);
        mProfilePicture = itemView.findViewById(R.id.ivProfileImage);
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mLastMessage = itemView.findViewById(R.id.tvConversationText);
    }

    public void bindToPost(final Conversation conversation) {


        findUser(conversation.otherUser);
        mLastMessage.setText(conversation.latestMessageText);

        if (conversation.timeStamp!=null) {
            String stringDate = conversation.getTimeStamp().toString();

            Date date = new Date();
            try {
                date = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy").parse(stringDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            mTimeStamp.setText(getRelativeTimeAgo(date));
        }
    }

    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    public void findUser (String userId) {
        Query query = mDatabaseReference.child("users").child(userId);
        Log.i("ConversationView", userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> newUser = (Map<String, Object>) dataSnapshot.getValue();
                Log.i("find user", "" + newUser);
                mUsername = newUser.get("username").toString();
                mUsernameTextView.setText(mUsername);
                Log.i("finddd user", "" + newUser);
                String imageUrl = newUser.get("profile_picture").toString();
                // if profile pic is already set
                if (!imageUrl.equals("")) {
                    Log.i("PostViewHolder", "imageUrl: " + imageUrl);
                    try {
                        // set profile picture
                        Bitmap realImage = getCircleBitmap(decodeFromFirebaseBase64(imageUrl));

                        Log.i("PostViewHolder", "realImage: " + realImage);
                        mProfilePicture.setImageBitmap(realImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("PostViewHolder", "Profile pic issue", e);
                    }
                }
                //Intent intent = new Intent(MainMessenger.this, ConversationViewHolder.class);
                //intent.putExtra("username", username);
//                Conversation conversation = new Conversation(currentUserId, username);
////                conversation.setOtherUser(username);
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

    public static String getRelativeTimeAgo(Date date) {
        String relativeDate;
        long dateMillis = date.getTime();
        relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        return relativeDate;
    }
}