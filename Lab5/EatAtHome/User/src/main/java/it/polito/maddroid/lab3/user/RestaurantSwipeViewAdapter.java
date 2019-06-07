package it.polito.maddroid.lab3.user;


import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import it.polito.maddroid.lab3.common.Dish;
import it.polito.maddroid.lab3.common.Restaurant;


public class RestaurantSwipeViewAdapter extends FragmentPagerAdapter {
    private Fragment[] childFragments;
    
    public RestaurantSwipeViewAdapter(@NonNull FragmentManager fm, Restaurant currentRestaurant) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        
        childFragments = new Fragment[] {
                FragmentRestaurantDetail.newInstance(currentRestaurant), //0
                DishesListFragment.newInstance(currentRestaurant)//1
        };
    }
    
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return childFragments[position];
    }
    
    @Override
    public int getCount() {
        return childFragments.length;
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";
        if (position == 0)
            title = "Restaurant detail";
        else if (position == 1)
            title = "Dishes";
        return title;
    }
    
    public List<Dish> getChosenDishes() {
        if (childFragments[1] instanceof DishesListFragment) {
            return ((DishesListFragment) childFragments[1]).getChosenDishes();
        }
        return new ArrayList<>();
    }
}
