package it.polito.maddroid.lab3.restaurateur;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.DishDiffUtilCallBack;
import it.polito.maddroid.lab3.common.Order;

public class OrderFragment extends Fragment {
    private RecyclerView rvOrder;

    private static final String TAG = "OrderFragment";

    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private OrderListAdapter adapter;

    private OrderRestaurant lastOrderClicked;

    public List<Order> orders;

    private ProgressBar pbLoading;


    public OrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        pbLoading = view.findViewById(R.id.pb_loading);

        dbRef = FirebaseDatabase.getInstance().getReference();

        rvOrder = view.findViewById(R.id.rv_orders);

        rvOrder.setLayoutManager(new LinearLayoutManager(getContext()));

        //downloadDishesInfo();

        adapter = new OrderListAdapter(new OrderRestaurantDiffUtilCallBack(), order -> {
            Intent i = new Intent(getContext(), OrderDetailsActivity.class);
            i.putExtra(OrderDetailsActivity.PAGE_TYPE_KEY, OrderDetailsActivity.MODE_SHOW);
            i.putExtra(OrderDetailsActivity.ORDER_KEY, order);
            lastOrderClicked = order;
            startActivityForResult(i, MainActivity.ORDER_DETAIL_CODE);
        });

        rvOrder.setAdapter(adapter);
        return view;
    }
}
