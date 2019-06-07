package it.polito.maddroid.lab3.restaurateur;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;
import it.polito.maddroid.lab3.common.Utility;


public class MostPopularDishesFragment extends Fragment {

    private RecyclerView rvMostPopularView;
    private TextView tvNoDishesPlaceHolder;

    private static final String TAG = "MostPopularDishFragment";

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MenuListAdapter adapter;

    public List<Dish> dishes;

    private int waitingCount = 0;

    private ProgressBar pbLoading;


    public MostPopularDishesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_most_popular_dishes, container, false);
        pbLoading = view.findViewById(R.id.pb_loading);

        dbRef = FirebaseDatabase.getInstance().getReference();

        rvMostPopularView = view.findViewById(R.id.rv_most_popular_dishes);
        tvNoDishesPlaceHolder = view.findViewById(R.id.tv_no_dish);
        rvMostPopularView.setLayoutManager(new LinearLayoutManager(getContext()));

        downloadDishesInfo();

        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> {
            Intent i = new Intent(getContext(), DishDetailsActivity.class);
            i.putExtra(DishDetailsActivity.PAGE_TYPE_KEY, DishDetailsActivity.MODE_SHOW);
            i.putExtra(DishDetailsActivity.DISH_KEY, dish);
            startActivityForResult(i, MainActivity.DISH_DETAIL_CODE);
        }, currentUser.getUid(), MenuListAdapter.MODE_MOST_POPULAR_DISHES_LIST);

        rvMostPopularView.setAdapter(adapter);

        return view;
    }

    public void downloadDishesInfo() {
        setActivityLoading(true);
        dishes = new ArrayList<>();
        Query queryRef = dbRef
                .child(EAHCONST.DISHES_SUB_TREE).child(currentUser.getUid())
                .orderByChild(EAHCONST.DISH_COUNT);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child(EAHCONST.DISH_COUNT).getValue() != null) {
                        String dishId = ds.getKey();

                        String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                        float dishPrice = Float.parseFloat(ds.child(EAHCONST.DISH_PRICE).getValue().toString());
                        String dishDescription = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();
                        int dishCount = Integer.parseInt(ds.child(EAHCONST.DISH_COUNT).getValue().toString());

                        Dish dish = new Dish(Integer.parseInt(dishId), dishName, dishPrice, dishDescription);
                        dish.setQuantity(dishCount);
                        if(dishCount != 0)
                            dishes.add(dish);
                        Collections.reverse(dishes);
                        manageVisibility();
                    } else {
                        Log.d(TAG, "There are no popular dishes for this restaurateur");
                        manageVisibility();
                    }


                }

                setupAdapter();
                return;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "onCancelled called");
                setActivityLoading(false);
            }
        });
    }

    private void setupAdapter() {
        adapter.submitList(dishes);
    }

    private synchronized void setActivityLoading(boolean loading) {
        if (pbLoading == null)
            return;
        //pbLoading = view.findViewById(R.id.pb_loading1);
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

    private void manageVisibility() {

        if (dishes.size() == 0) {
            tvNoDishesPlaceHolder.setVisibility(View.VISIBLE);

        } else {
            tvNoDishesPlaceHolder.setVisibility(View.GONE);
        }
        setActivityLoading(false);
    }

}
