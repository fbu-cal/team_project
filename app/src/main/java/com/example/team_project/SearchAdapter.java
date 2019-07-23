package com.example.team_project;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.team_project.models.User;

import java.util.ArrayList;
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
                search.get("username").toString(), search.get("email").toString());
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
                Map<String, Object> tempUser = mSearches.get(position);
                Intent intent = new Intent(context, OtherUserProfileActivity.class);
                intent.putExtra("uid", tempUser.get("uid").toString());
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
