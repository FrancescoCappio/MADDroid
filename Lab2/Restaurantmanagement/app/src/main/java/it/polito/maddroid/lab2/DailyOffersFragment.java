package it.polito.maddroid.lab2;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class DailyOffersFragment extends Fragment {

    private ListView lvDailyOffers;
    
    DailyOfferAdapter adapter;
    
    public DailyOffersFragment() {
        // Required empty public constructor
    }
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_daily_offers, container, false);
        
        lvDailyOffers = view.findViewById(R.id.lv_daily_offer);
    
        List<DailyOffer> dailyOffers = DataManager.getInstance(getContext()).getDailyOffers();
        
        adapter = new DailyOfferAdapter(new ArrayList<>(dailyOffers), getContext());
        
        lvDailyOffers.setAdapter(adapter);
        
        lvDailyOffers.setOnItemClickListener((parent, view1, position, id) -> {
            Intent i = new Intent(getContext(), DailyOfferDetailActivity.class);
            i.putExtra(DailyOfferDetailActivity.PAGE_TYPE_KEY, DailyOfferDetailActivity.MODE_SHOW);
            i.putExtra(DailyOfferDetailActivity.OFFER_ID_KEY, ((DailyOffer) adapter.getItem(position)).getId());
            startActivity(i);
        });
        
        return view;
    }
    
}
