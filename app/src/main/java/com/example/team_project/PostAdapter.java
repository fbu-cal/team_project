package com.example.team_project;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Map<String, Object>> mPosts;
    Context context;

    // pass in the Posts array in the constructor
    public PostAdapter(List<Map<String, Object>> posts) {
        mPosts = posts;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get the data according to position
        final Map<String, Object> post = mPosts.get(position);
        final ViewHolder holder2 = holder;

//        final long uid = post.uid;

        // populate the views according to this data
        holder.tvUsername.setText(post.get("author").toString());
        holder.tvBody.setText(post.get("body").toString());
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    // create ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvBody;

        public ViewHolder(View itemView) {
            super(itemView);
            // perform findViewById lookups
            tvUsername = (TextView) itemView.findViewById(R.id.username_text_view);
            tvBody = (TextView) itemView.findViewById(R.id.body_text_view);
        }
    }


    // Clean all elements of the recycler
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }
}
