package it.polito.maddroid.lab2;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class DailyOffersFragment extends Fragment {

    private ListView lvDailyOffers;
    
    private static final String TAG = "DailyOffersFragment";
    
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
            startActivityForResult(i, MainActivity.DAILY_OFFER_DETAIL_CODE);
        });
        
        return view;
    }
    
    public void notifyUpdate() {
        List<DailyOffer> dailyOffers = DataManager.getInstance(getContext()).getDailyOffers();
        adapter.updateList(dailyOffers);
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
            case MainActivity.DAILY_OFFER_DETAIL_CODE:
                notifyUpdate();
        }
    }
}
