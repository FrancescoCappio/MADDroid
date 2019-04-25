package it.polito.maddroid.lab3.user;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

import it.polito.maddroid.lab3.common.Restaurant;


public class RestaurantAdapter extends BaseAdapter {
    private static final String TAG = "RestaurantAdapter";
    private List<Restaurant> restaurants;
    private Context context;
    private StorageReference mStorageRef;

    public RestaurantAdapter(List<Restaurant> restaurants, Context context) {
        this.restaurants = restaurants;
        this.context = context;
    }

    @Override
    public int getCount() {
        return restaurants.size();
    }

    @Override
    public Object getItem(int position) {
        return restaurants.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.restaurant_list_item, parent, false);

        TextView tvName = v.findViewById(R.id.tv_restaurant_name);
        TextView tvDescription = v.findViewById(R.id.tv_restaurant_description);
        ImageView ivRestaurantPhoto = v.findViewById(R.id.iv_restaurant_photo);


        Restaurant r = restaurants.get(position);
        downloadAvatar(r.getRestaurantID());

        Glide.with(context)
                .load(getAvatarTmpFile(r.getRestaurantID()))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(ivRestaurantPhoto);

        tvName.setText(r.getName());
        tvDescription.setText(r.getDescription());

        return v;
    }

    private File getAvatarTmpFile(String UID) {
        // Determine Uri of camera image to save.
        final File root = new File(context.getFilesDir() + File.separator + "images" + File.separator);
        root.mkdirs();
        final String fname = "CustomerAvatar_"+ UID +"_tmp.jpg";
        return new File(root, fname);
    }

    private void downloadAvatar(String UID) {
        File localFile = getAvatarTmpFile(UID);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = mStorageRef.child("avatar_" + UID +".jpg");

        riversRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Avatar downloaded successfully");
                }).addOnFailureListener(exception -> {
            Log.e(TAG, "Error while downloading avatar image: " + exception.getMessage());
                });
    }

}
