package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.EAHCONST;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class MenuFragment extends Fragment {

    private RecyclerView rvMenu;

    private static final String TAG = "MenuFragment";

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MenuListAdapter adapter;
    
    private Dish lastDishClicked;

    private int waitingCount = 0;

    public List<Dish> dishes;

    private ProgressBar pbLoading;


    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        pbLoading = view.findViewById(R.id.pb_loading);

        dbRef = FirebaseDatabase.getInstance().getReference();

        rvMenu = view.findViewById(R.id.rv_menu);

        rvMenu.setLayoutManager(new LinearLayoutManager(getContext()));

        downloadDishesInfo();

        adapter = new MenuListAdapter(new DishDiffUtilCallBack(), dish -> {
            Intent i = new Intent(getContext(), DishDetailsActivity.class);
            i.putExtra(DishDetailsActivity.PAGE_TYPE_KEY, DishDetailsActivity.MODE_SHOW);
            i.putExtra(DishDetailsActivity.DISH_KEY, dish);
            lastDishClicked = dish;
            startActivityForResult(i, MainActivity.DISH_DETAIL_CODE);
        },currentUser.getUid(), MenuListAdapter.MODE_RESTAURANT_MENU);

        rvMenu.setAdapter(adapter);
        return view;
    }

    public  void downloadDishesInfo() {
        setActivityLoading(true);
        dishes = new ArrayList<>();
        Query queryRef = dbRef
                .child(EAHCONST.DISHES_SUB_TREE).child(currentUser.getUid())
                .orderByKey();

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange Called");
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    String dishId = ds.getKey();
                    String dishName = (String) ds.child(EAHCONST.DISH_NAME).getValue();
                    float dishPrice = Float.parseFloat( ds.child(EAHCONST.DISH_PRICE).getValue().toString());
                    String dishDescription = (String) ds.child(EAHCONST.DISH_DESCRIPTION).getValue();

                    Dish dish = new Dish(Integer.parseInt(dishId),dishName,dishPrice,dishDescription);
                    
                    if (lastDishClicked != null && lastDishClicked.getDishID() == dish.getDishID()) {
                        dish.markUpdated();
                        lastDishClicked = null;
                    }
                    dishes.add(dish);
                }
                setupAdapter();
                setActivityLoading(false);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Result not ok");
            return;
        }

        if (data == null) {
            Log.e(TAG, "Result data null");
            return;
        }

        switch (requestCode) {
            case MainActivity.DISH_DETAIL_CODE:
                downloadDishesInfo();

        }

    }

    private synchronized void setActivityLoading(boolean loading) {
        if(pbLoading == null)
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
}
