package it.polito.maddroid.lab3.user;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.RestaurantCategory;


public class RestaurantsFragment extends Fragment {

    private RecyclerView rvCategories;

    private static final String TAG = "RestaurantsFragment";

    private DatabaseReference dbRef;

    public List<RestaurantCategory> categories;


    public RestaurantsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_restaurants, container, false);

        rvCategories = view.findViewById(R.id.rv_categories);

        categories = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference();

        rvCategories.setHasFixedSize(true);
        
        rvCategories.setLayoutManager(new GridLayoutManager(getContext(),2));
        
        downloadCategoriesInfo();
    
        CardView cvSearch = view.findViewById(R.id.cv_search);
        
        cvSearch.setOnClickListener(v -> {
            Intent i = new Intent(getContext(),RestaurantSearchActivity.class);
            startActivity(i);
        });

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On resume");
    }
    
    private void downloadCategoriesInfo() {
        Query queryRef = dbRef
                .child(EAHCONST.CATEGORIES_SUB_TREE)
                .orderByChild(EAHCONST.CATEGORIES_NAME);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String catId = ds.getKey();
                    String catName = (String) ds.child(EAHCONST.CATEGORIES_NAME).getValue();
                    
                    RestaurantCategory rc = new RestaurantCategory(catId, catName);

                    categories.add(rc);
                }
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
            }
        });
    }

    private void setupAdapter() {
        CategoryGridAdapter adapter = new CategoryGridAdapter(categories, category -> {
            Intent i = new Intent(getContext(),RestaurantSearchActivity.class);
            i.putExtra(EAHCONST.RESTAURANT_CATEGORY_EXTRA, category);
            
            startActivity(i);
        });
        rvCategories.setAdapter(adapter);
    }
    

}
