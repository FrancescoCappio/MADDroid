package it.polito.maddroid.lab3.common;


import androidx.annotation.NonNull;


public class ReviewDiffUtil extends androidx.recyclerview.widget.DiffUtil.ItemCallback<EAHCONST.Review>{
    @Override
    public boolean areItemsTheSame(@NonNull EAHCONST.Review oldItem, @NonNull EAHCONST.Review newItem) {
        return oldItem.getRate() == newItem.getRate() &&
                oldItem.getAuthorUID().equals(newItem.getAuthorUID()) &&
                oldItem.getComment().equals(newItem.getComment()) &&
                oldItem.getDate().equals(newItem.getDate());
    }
    
    @Override
    public boolean areContentsTheSame(@NonNull EAHCONST.Review oldItem, @NonNull EAHCONST.Review newItem) {
        return oldItem.getRate() == newItem.getRate() &&
                oldItem.getAuthorUID().equals(newItem.getAuthorUID()) &&
                oldItem.getComment().equals(newItem.getComment()) &&
                oldItem.getDate().equals(newItem.getDate());
    }
}
