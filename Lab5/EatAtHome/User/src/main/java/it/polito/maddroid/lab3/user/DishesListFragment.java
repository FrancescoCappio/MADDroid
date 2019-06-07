package it.polito.maddroid.lab3.user;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Restaurant;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DishesListFragment extends Fragment {
    
    public static final String TAG = "DishesListFragment";
    
    private static final String RESTAURANT_KEY = "RESTAURANT_KEY";
    private static final String DISHES_KEY = "CHOSEN_DISHES_KEY";
    
    private List<Dish> dishes;
    private DishOrderListAdapter adapter;
    private List<Dish> chosenDishes;
    
    private ProgressBar pbLoading;
    private RecyclerView rvOrderDishes;
    
    private Restaurant currentRestaurant;
    private DatabaseReference dbRef;
    
    private int waitingCount;
    private TotalCostListener currentTotalCostListener;
    
    public interface TotalCostListener {
        public void updateTotalCost(List<Dish> chosenDishes);
    }
    
    public DishesListFragment() {
        // Required empty public constructor
    }
    
    public static DishesListFragment newInstance(Restaurant restaurant) {
        Bundle args = new Bundle();
        DishesListFragment fragment = new DishesListFragment();
        args.putSerializable(RESTAURANT_KEY, restaurant);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void setArguments(@Nullable Bundle args) {
        super.setArguments(args);
        currentRestaurant = (Restaurant) args.getSerializable(RESTAURANT_KEY);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dishes_list, container, false);
    
        pbLoading = view.findViewById(R.id.pb_loading);
        rvOrderDishes = view.findViewById(R.id.rv_order_dishes);
        
        rvOrderDishes.setLayoutManager(new LinearLayoutManager(getContext()));
    
        adapter = new DishOrderListAdapter(new DishDiffUtilCallBack(), currentRestaurant, this::chosenDishesUpdated);
    
        rvOrderDishes.setAdapter(adapter);
        
        dbRef = FirebaseDatabase.getInstance().getReference();
    
        if (getActivity() instanceof TotalCostListener) {
            currentTotalCostListener = (TotalCostListener) getActivity();
        }
        
        if (savedInstanceState != null) {
            dishes = (List<Dish>) savedInstanceState.getSerializable(DISHES_KEY);
            adapter.submitList(dishes);
            chosenDishes = adapter.getChosenDishes();
            chosenDishesUpdated();
        } else {
            downloadDishesInfo();
        }
        
        return view;
    }
    
    private void chosenDishesUpdated() {
        chosenDishes = adapter.getChosenDishes();
        
        if (currentTotalCostListener != null)
            currentTotalCostListener.updateTotalCost(chosenDishes);
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putSerializable(DISHES_KEY, (Serializable) dishes);
    }
    
    private void downloadDishesInfo() {
        setActivityLoading(true);
        Query queryRef = dbRef
                .child(EAHCONST.DISHES_SUB_TREE).child(currentRestaurant.getRestaurantID())
                .orderByKey();
        
        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                dishes = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    
                    String dishId = ds.getKey();
                    String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                    
                    
                    Float f = ds.child(EAHCONST.DISH_PRICE).getValue(Float.class);
                    float price = 0;
                    if (f != null) {
                        price = f;
                    }
                    String dishDescription = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();
                    
                    Dish dish = new Dish(Integer.parseInt(dishId),dishName,price,dishDescription);
                    
                    dishes.add(dish);
                    
                }
                adapter.submitList(dishes);
                setActivityLoading(false);
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }
    
    private synchronized void setActivityLoading(boolean loading) {
        // this method is necessary to show the user when the activity is doing a network operation
        // as downloading data or uploading data
        // how to use: call with loading = true to notify that a new transmission has been started
        // call with loading = false to notify end of transmission
        
        if (loading) {
            if (waitingCount == 0)
                pbLoading.setVisibility(View.VISIBLE);
            waitingCount++;
        } else {
            waitingCount--;
            if (waitingCount == 0)
                pbLoading.setVisibility(View.INVISIBLE);
        }
    }
    
    public List<Dish> getChosenDishes() {
        if (chosenDishes == null)
            return adapter.getChosenDishes();
        return chosenDishes;
    }
    
}
