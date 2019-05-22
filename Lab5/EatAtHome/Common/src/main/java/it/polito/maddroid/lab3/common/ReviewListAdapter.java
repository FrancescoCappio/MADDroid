package it.polito.maddroid.lab3.common;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


public class ReviewListAdapter extends ListAdapter<EAHCONST.Review, ReviewListAdapter.MyViewHolder> {
    
    protected ReviewListAdapter(@NonNull DiffUtil.ItemCallback<EAHCONST.Review> diffCallback) {
        super(diffCallback);
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_list_item, parent, false);
    
        // create view holder and pass main view to it
        return new MyViewHolder(v);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupReview(getItem(position));
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private RatingBar ratingBar;
        private TextView tvAuthorName;
        private TextView tvComment;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvAuthorName = itemView.findViewById(R.id.tv_name);
            tvComment = itemView.findViewById(R.id.tv_content);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            
        }
        
        public void setupReview(EAHCONST.Review review) {
            
            tvAuthorName.setText(review.getAuthorName());
            tvComment.setText(review.getComment());
            
            ratingBar.setRating(review.getRate());
        }
    }
}
