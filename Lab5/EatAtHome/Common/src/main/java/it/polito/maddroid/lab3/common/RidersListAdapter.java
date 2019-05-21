package it.polito.maddroid.lab3.common;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


public class RidersListAdapter extends ListAdapter<Rider, RidersListAdapter.MyViewHolder> {
    
    private ItemClickListener clickListener;
    
    protected RidersListAdapter(@NonNull DiffUtil.ItemCallback<Rider> diffCallback, ItemClickListener clickListener) {
        super(diffCallback);
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.riders_list_item, parent, false);
    
        // create view holder and pass main view to it
        return new MyViewHolder(v);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setupRider(getItem(position), clickListener);
    }
    
    public interface ItemClickListener {
        void onItemClick(Rider rider);
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        
        private View itemView;
        private TextView tvRiderName;
        private TextView tvRiderDistance;
        
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvRiderName = itemView.findViewById(R.id.tv_rider_name);
            tvRiderDistance = itemView.findViewById(R.id.tv_rider_distance);
            this.itemView = itemView;
            
        }
        
        public void setupRider(Rider rider, ItemClickListener itemClickListener) {
            
            tvRiderName.setText(rider.getName());
            tvRiderDistance.setText(String.format(Locale.US, "%.02f",rider.getDistance()) + " Km");
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(rider);
                }
            });
        }
    }
}