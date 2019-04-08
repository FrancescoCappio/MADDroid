package it.polito.maddroid.lab2;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


public class DailyOffersFragment extends Fragment {

    private ListView lvDailyOffers;
    
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
        
        
        return view;
    }
    
}
