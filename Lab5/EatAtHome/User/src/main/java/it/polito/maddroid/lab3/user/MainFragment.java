package it.polito.maddroid.lab3.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;


public class MainFragment extends Fragment {
    
    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ViewPager viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(new SwipeViewAdapter(getChildFragmentManager()));
        viewPager.setSaveFromParentEnabled(false);
        
        viewPager.setOffscreenPageLimit(6);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    
        Log.d(TAG, "onCreateView");
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDestroy");
    }
    
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }
}
