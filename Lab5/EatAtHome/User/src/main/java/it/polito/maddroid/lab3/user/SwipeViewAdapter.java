package it.polito.maddroid.lab3.user;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class SwipeViewAdapter extends FragmentStatePagerAdapter {
    private Fragment[] childFragments;

    public SwipeViewAdapter(FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        childFragments = new Fragment[] {
                new TopRestaurantFragment(), //0
                new FavoriteRestaurantFragment(), //1
                new RestaurantsFragment()//2
        };
    }

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
            title ="Top";
        else if (position == 1)
            title = "Your Favorites";
        else if (position == 2)
            title = "Search";
        return title;
    }
}
