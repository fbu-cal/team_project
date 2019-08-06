package com.example.yoked;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yoked.models.Conversation;
import com.example.yoked.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private ArrayList<Map<String, Object>> mSearches;
    private Context context;

    // pass in the Posts array in the constructor
    public SearchAdapter(Context context, ArrayList<Map<String, Object>> searches) {
        this.context = context;
        this.mSearches = searches;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(view);
//        return viewHolder;
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Map<String, Object> search = mSearches.get(position);
        User user = new User (search.get("fullname").toString(), search.get("uid").toString(),
                search.get("username").toString(), search.get("email").toString(), search.get("profile_picture").toString());
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return mSearches.size();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvUsername;

        public ViewHolder(View itemView) {
            super(itemView);
            // perform findViewById lookups
            tvUsername = (TextView) itemView.findViewById(R.id.username_text_view);
            itemView.setOnClickListener(this);
        }

        // binds the view elements to the post
        public void bind(final User user) {
            tvUsername.setText(user.username);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the movie at the position, this won't work if the class is static
                final Map<String, Object> targetUser = mSearches.get(position);

                Log.i("SearchAdapter", "Username" + targetUser.get("username") );
                Intent intent = new Intent(context, MessageDetailsActivity.class);
                intent.putExtra("username", targetUser.get("username").toString());
                intent.putExtra("uid", targetUser.get("uid").toString());

                // TODO - move into a method
                final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
                final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                final String receiverId = targetUser.get("uid").toString();
                Query query = FirebaseDatabase.getInstance().getReference().child("user-conversations").child(currentUserId);
                Log.i("MessageDetails", "Q: " + query);
                query.addListenerForSingleValueEvent(new ValueEventListener() {// Retrieve new posts as they are added to Firebase
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean conversationExists = false;
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Map<String, Object> map = (HashMap<String, Object>) data.getValue();
                            if (receiverId.equals(map.get("otherUser"))) {
                                conversationExists = true;
                            }
                            if (conversationExists) {
                                break;
                            }
                        }
                        if (!conversationExists){
                            String conversationKey = mDatabaseReference.child("conversations").push().getKey();
                            Conversation conversation = new Conversation(currentUserId, receiverId);
                            Map<String,Object> conversationValues = conversation.toMap();

                            Map<String, Object> childUpdates = new HashMap<>();

                            childUpdates.put("user-conversations/" + currentUserId + "/" + conversationKey, conversationValues);
                            mDatabaseReference.updateChildren(childUpdates);
                            //Intent intent = new Intent(context, MessageDetailsActivity.class);
                            //intent.putExtra("conversation", (Parcelable) conversation);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

                // show the activity
                context.startActivity(intent);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mSearches.clear();
    }

}
